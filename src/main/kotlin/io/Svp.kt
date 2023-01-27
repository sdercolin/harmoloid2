package io

import com.sdercolin.harmoloid.core.model.Content
import com.sdercolin.harmoloid.core.model.HarmonicType
import com.sdercolin.harmoloid.core.model.Note
import com.sdercolin.harmoloid.core.model.NoteShift
import com.sdercolin.harmoloid.core.model.TimeSignature
import com.sdercolin.harmoloid.core.model.Track
import external.generateUUID
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import model.ExportResult
import model.Format
import model.Project
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.File
import util.asInt
import util.asIntOrNull
import util.asList
import util.asLong
import util.asLongOrNull
import util.asString
import util.asStringOrNull
import util.mapProperty
import util.maybeProperty
import util.nameWithoutExtension
import util.property
import util.readText
import util.withProperty

object Svp {
    suspend fun parse(file: File): Project {
        val text = readTextFromFile(file)
        val projectElement = jsonSerializer.parseToJsonElement(text)
        val timeSignatures = projectElement
            .maybeProperty("time")
            ?.maybeProperty("meter")
            ?.asList
            ?.map {
                TimeSignature(
                    measurePosition = it.property("index").asInt,
                    numerator = it.property("numerator").asInt,
                    denominator = it.property("denominator").asInt,
                )
            }
            ?.takeIf { it.isNotEmpty() }
            ?: listOf(TimeSignature.default)

        val tracks = parseTracks(projectElement, timeSignatures)
        return Project(
            format = Format.Svp,
            inputFiles = listOf(file),
            name = file.nameWithoutExtension,
            content = Content(tracks),
        )
    }

    private suspend fun readTextFromFile(file: File) = file.readText().let {
        val index = it.lastIndexOf('}')
        it.take(index + 1)
    }

    private fun parseTracks(
        projectElement: JsonElement,
        timeSignatures: List<TimeSignature>,
    ): List<Track> = projectElement.property("tracks").asList
        .sortedBy { it.maybeProperty("dispOrder")?.asIntOrNull ?: 0 }
        .mapIndexed { index, track ->
            Track.build(
                index,
                name = track.maybeProperty("name")?.asString ?: "Track ${index + 1}",
                notes = parseNotes(track, projectElement),
                timeSignatures,
            )
        }

    private fun parseNotes(trackElement: JsonElement, projectElement: JsonElement): List<Note> {
        val mainNotes = trackElement.maybeProperty("mainGroup")?.let { group ->
            val ref = trackElement.maybeProperty("mainRef") ?: return@let null
            parseNotesFromGroup(ref, group)
        }.orEmpty()
        val extraNotes = trackElement.maybeProperty("groups")?.asList?.flatMap { ref ->
            projectElement.findGroupElement(ref)
                ?.let { group -> parseNotesFromGroup(ref, group) }
                .orEmpty()
        }.orEmpty()
        return (mainNotes + extraNotes)
            .sortedBy { it.tickOn }
            .mapIndexed { index, note -> note.copy(index = index) }
    }

    private fun parseNotesFromGroup(refElement: JsonElement, groupElement: JsonElement): List<Note> =
        groupElement.property("notes").asList.map { note ->
            val tickOn = (
                note.property("onset").asLong +
                    (refElement.maybeProperty("blickOffset")?.asLongOrNull ?: 0L)
                ) / TICK_RATE
            Note(
                index = 0, // will be set later
                key = note.property("pitch").asInt +
                    (refElement.maybeProperty("pitchOffset")?.asIntOrNull ?: 0),
                tickOn = tickOn,
                tickOff = tickOn + note.property("duration").asLong / TICK_RATE,
                lyric = note.maybeProperty("lyrics")?.asStringOrNull ?: "",
            )
        }

    suspend fun generate(project: Project): ExportResult {
        val jsonText = generateContent(project)
        val blob = Blob(arrayOf(jsonText), BlobPropertyBag("application/octet-stream"))
        val name = project.name + Format.Svp.extension
        return ExportResult(blob, name)
    }

    private suspend fun generateContent(project: Project): String {
        val projectElement = jsonSerializer.parseToJsonElement(readTextFromFile(project.inputFiles.first()))

        val groupElements = projectElement.property("library").asList.toMutableList()

        var trackElements = projectElement.property("tracks").asList.toList()
            .sortedBy { it.maybeProperty("dispOrder")?.asIntOrNull ?: 0 }
        trackElements = trackElements.zip(project.content.tracks).fold(trackElements) { accumulator, item ->
            val (trackElement, trackModel) = item
            val trackChorus = project.chorus[project.content.tracks.indexOf(trackModel)]
            val newTrackElements = trackElement.mapTrackElementsWithGroups(trackModel, trackChorus, projectElement)
                .map { (newTrackElement, newGroupElements) ->
                    groupElements.addAll(newGroupElements)
                    newTrackElement
                }
            val index = accumulator.indexOf(trackElement)
            val result = accumulator.toMutableList()
            result.addAll(index + 1, newTrackElements)
            result
        }
        trackElements = trackElements.mapIndexed { index, trackElement ->
            trackElement.withProperty("dispOrder", index)
        }
        return projectElement
            .withProperty("tracks", trackElements)
            .withProperty("library", groupElements)
            .toString()
    }

    private fun JsonElement.mapTrackElementsWithGroups(
        trackModel: Track,
        trackChorus: Map<HarmonicType, List<NoteShift>>,
        projectElement: JsonElement,
    ): List<Pair<JsonElement, List<JsonElement>>> {
        if (trackModel.bars.isEmpty()) return listOf()
        if (trackChorus.isEmpty()) return listOf()
        return trackChorus.map { (harmony, noteShifts) ->
            var newTrackElement = withProperty(
                "name",
                harmony.getHarmonicTrackName(trackModel.name),
            )

            // Collect all notes with index
            val noteIndexMap = mutableMapOf<JsonElement, Int>()
            newTrackElement.maybeProperty("mainGroup")?.maybeProperty("notes")?.asList.orEmpty()
                .plus(
                    newTrackElement.maybeProperty("groups")?.asList.orEmpty()
                        .mapNotNull { refElement ->
                            projectElement.findGroupElement(refElement)
                        }
                        .flatMap { it.property("notes").asList },
                )
                .sortedBy { it.property("onset").asLong }
                .forEachIndexed { index, note -> noteIndexMap[note] = index }

            val noteShiftMap = noteShifts.map { it.noteIndex to it.keyDelta }.toMap()

            newTrackElement = newTrackElement.mapProperty("mainGroup") {
                it.mapGroupWithNotes(noteShiftMap, noteIndexMap)
            }

            val mainRefUUId = generateUUID()
            newTrackElement = newTrackElement
                .mapProperty("mainGroup") { it.mapProperty("uuid") { mainRefUUId } }
                .mapProperty("mainRef") { it.mapProperty("groupID") { mainRefUUId } }

            val newGroups = mutableListOf<JsonElement>()
            newTrackElement = newTrackElement.mapProperty("groups") { groupRefsElement ->
                groupRefsElement.asList.map { groupRefElement ->
                    val group = projectElement.findGroupElement(groupRefElement)
                    val newUuid = generateUUID()
                    group?.mapProperty("uuid") { newUuid }
                        ?.mapGroupWithNotes(noteShiftMap, noteIndexMap)
                        ?.let { newGroups.add(it) }
                    groupRefElement.mapProperty("groupID") { newUuid }
                }
            }

            newTrackElement to newGroups
        }
    }

    private fun JsonElement.findGroupElement(
        refElement: JsonElement,
    ) = this.property("library").asList.find {
        it.property("uuid") == refElement.property("groupID")
    }

    private fun JsonElement.mapGroupWithNotes(
        noteShiftMap: Map<Int, Int>,
        noteIndexMap: Map<JsonElement, Int>,
    ): JsonElement {
        return this.mapProperty("notes") { notesElement ->
            notesElement.asList.mapNotNull { note ->
                val index = noteIndexMap[note]
                val shift = noteShiftMap[index] ?: return@mapNotNull null
                note.mapProperty("pitch") { it.asInt + shift }
            }
        }
    }

    private const val TICK_RATE = 1470000L

    private val jsonSerializer = Json {
        isLenient = true
    }
}
