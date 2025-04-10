package io

import com.sdercolin.harmoloid.core.model.Content
import com.sdercolin.harmoloid.core.model.HarmonicType
import com.sdercolin.harmoloid.core.model.Note
import com.sdercolin.harmoloid.core.model.NoteShift
import com.sdercolin.harmoloid.core.model.TimeSignature
import com.sdercolin.harmoloid.core.model.Track
import external.JsZip
import external.JsZipOption
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import model.ExportResult
import model.Format
import model.Project
import org.w3c.files.Blob
import org.w3c.files.File
import util.asInt
import util.asList
import util.asLong
import util.asString
import util.asStringOrNull
import util.mapProperty
import util.maybeProperty
import util.nameWithoutExtension
import util.property
import util.readBinary
import util.withProperty

object Vpr {
    suspend fun parse(file: File): Project {
        val text = readContent(file)
        val projectElement = jsonSerializer.parseToJsonElement(text)
        val timeSignatures = projectElement
            .maybeProperty("masterTrack")
            ?.maybeProperty("timeSig")
            ?.maybeProperty("events")
            ?.asList
            ?.map {
                TimeSignature(
                    measurePosition = it.property("bar").asInt,
                    numerator = it.property("numer").asInt,
                    denominator = it.property("denom").asInt,
                )
            }
            ?.takeIf { it.isNotEmpty() }
            ?: listOf(TimeSignature.default)

        val tracks = parseTracks(projectElement, timeSignatures)
        return Project(
            format = Format.Vpr,
            inputFiles = listOf(file),
            name = file.nameWithoutExtension,
            content = Content(tracks),
        )
    }

    private suspend fun readContent(file: File): String {
        val binary = file.readBinary()
        val zip = JsZip().loadAsync(binary).await()
        val vprEntry = possibleJsonPaths.let {
            it.forEach { path ->
                val vprFile = zip.file(path)
                if (vprFile != null) return@let vprFile
            }
            null
        }
        return requireNotNull(vprEntry).async("string").await() as String
    }

    private fun parseTracks(
        projectElement: JsonElement,
        timeSignatures: List<TimeSignature>,
    ): List<Track> = projectElement.property("tracks").asList
        .mapIndexed { index, track ->
            Track.build(
                index,
                name = track.maybeProperty("name")?.asString ?: "Track ${index + 1}",
                notes = parseNotes(track),
                timeSignatures,
            )
        }

    private fun parseNotes(trackElement: JsonElement): List<Note> =
        trackElement.maybeProperty("parts")?.asList
            ?.flatMap { partElement ->
                partElement.maybeProperty("notes")?.asList?.map { partElement.property("pos").asLong to it }
                    ?: emptyList()
            }
            ?.mapIndexed { index, (tickOffset, note) ->
                Note(
                    index = index,
                    tickOn = tickOffset + note.property("pos").asLong,
                    tickOff = tickOffset + note.property("pos").asLong + note.property("duration").asLong,
                    lyric = note.property("lyric").asStringOrNull.takeUnless { it.isNullOrBlank() } ?: "",
                    key = note.property("number").asInt,
                )
            } ?: emptyList()

    suspend fun generate(project: Project): ExportResult {
        val jsonText = generateContent(project)
        val zip = JsZip()
        zip.file(possibleJsonPaths.first(), jsonText)
        val option = JsZipOption().also {
            it.type = "blob"
            it.mimeType = "application/octet-stream"
        }
        val blob = zip.generateAsync(option).await() as Blob
        val name = project.name + Format.Vpr.extension
        return ExportResult(blob, name)
    }

    private suspend fun generateContent(project: Project): String {
        val projectElement = jsonSerializer.parseToJsonElement(readContent(project.inputFiles.first()))

        var trackElements = projectElement.property("tracks").asList.toList()
        trackElements = trackElements.zip(project.content.tracks).fold(trackElements) { accumulator, item ->
            val (trackElement, trackModel) = item
            val trackChorus = project.chorus[project.content.tracks.indexOf(trackModel)]
            val newTrackElements = trackElement.mapTrackElements(trackModel, trackChorus)
            val index = accumulator.indexOf(trackElement)
            val result = accumulator.toMutableList()
            result.addAll(index + 1, newTrackElements)
            result
        }
        trackElements = trackElements.mapIndexed { index, trackElement ->
            trackElement.withProperty("busNo", index)
        }
        return projectElement
            .withProperty("tracks", trackElements)
            .toString()
    }

    private fun JsonElement.mapTrackElements(
        trackModel: Track,
        trackChorus: Map<HarmonicType, List<NoteShift>>,
    ): List<JsonElement> {
        if (trackModel.bars.isEmpty()) return listOf()
        if (trackChorus.isEmpty()) return listOf()
        return trackChorus.map { (harmony, noteShifts) ->
            var newTrackElement = withProperty(
                "name",
                harmony.getHarmonicTrackName(trackModel.name),
            )

            // Collect all notes with index
            val noteIndexMap = mutableMapOf<JsonElement, Int>()
            newTrackElement.maybeProperty("parts")?.asList
                ?.flatMap { it.maybeProperty("notes")?.asList.orEmpty() }
                ?.forEachIndexed { index, note -> noteIndexMap[note] = index }

            val noteShiftMap = noteShifts.associate { it.noteIndex to it.keyDelta }

            newTrackElement = newTrackElement.mapProperty("parts") { partsElement ->
                partsElement.asList.map { partElement ->
                    partElement.mapProperty("notes") { notesElement ->
                        notesElement.asList.mapNotNull { note ->
                            val index = noteIndexMap[note]
                            val shift = noteShiftMap[index] ?: return@mapNotNull null
                            note.mapProperty("number") { it.asInt + shift }
                        }
                    }
                }
            }

            newTrackElement
        }
    }

    private val possibleJsonPaths = listOf(
        "Project\\sequence.json",
        "Project/sequence.json",
    )

    private val jsonSerializer = Json {
        isLenient = true
    }
}
