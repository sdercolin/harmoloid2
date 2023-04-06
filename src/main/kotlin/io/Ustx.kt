package io

import com.sdercolin.harmoloid.core.model.Content
import com.sdercolin.harmoloid.core.model.Note
import com.sdercolin.harmoloid.core.model.NoteShift
import com.sdercolin.harmoloid.core.model.TimeSignature
import com.sdercolin.harmoloid.core.model.Track
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import model.ExportResult
import model.Format
import model.Project
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.File
import ui.external.JsYaml
import util.asInt
import util.asIntOrNull
import util.asList
import util.asLong
import util.asStringOrNull
import util.clone
import util.mapProperty
import util.maybeProperty
import util.nameWithoutExtension
import util.property
import util.readText
import util.withProperty

object Ustx {

    suspend fun parse(file: File): Project {
        val yamlText = file.readText()
        val project = parseToJsonElement(yamlText)
        val timeSignatures = parseTimeSignatures(project)
        val tracks = parseTracks(project, timeSignatures)
        return Project(
            format = Format.Ustx,
            inputFiles = listOf(file),
            name = file.nameWithoutExtension,
            content = Content(tracks),
        )
    }

    private fun parseToJsonElement(text: String): JsonElement {
        val yaml = JsYaml.load(text)
        val jsonText = JSON.stringify(yaml)
        return jsonSerializer.parseToJsonElement(jsonText)
    }

    private fun parseTimeSignatures(project: JsonElement): List<TimeSignature> {
        val list = project.maybeProperty("time_signatures")
            ?.asList
            ?.mapNotNull {
                val barPosition = it.maybeProperty("bar_position")?.asIntOrNull
                val beatPerBar = it.maybeProperty("beat_per_bar")?.asIntOrNull
                val beatUnit = it.maybeProperty("beat_unit")?.asIntOrNull
                if (barPosition != null && beatPerBar != null && beatUnit != null) {
                    TimeSignature(
                        measurePosition = barPosition,
                        numerator = beatPerBar,
                        denominator = beatUnit,
                    )
                } else null
            }
        return list?.takeUnless { it.isEmpty() } ?: listOf(TimeSignature.default)
    }

    private fun parseTracks(
        projectElement: JsonElement,
        timeSignatures: List<TimeSignature>,
    ): List<Track> = List(projectElement.property("tracks").asList.size) { index ->
        Track.build(
            index,
            name = "Track ${index + 1}",
            notes = parseNotes(index, projectElement),
            timeSignatures = timeSignatures,
        )
    }

    private fun parseNotes(trackIndex: Int, projectElement: JsonElement): List<Note> =
        projectElement.property("voice_parts").asList
            .filter { it.maybeProperty("track_no")?.asInt == trackIndex }
            .flatMap { partElement ->
                partElement.property("notes").asList.map { partElement.property("position").asLong to it }
            }
            .mapIndexed { index, (tickOffset, note) ->
                Note(
                    index = index,
                    tickOn = tickOffset + note.property("position").asLong,
                    tickOff = tickOffset + note.property("position").asLong + note.property("duration").asLong,
                    lyric = note.maybeProperty("lyric")?.asStringOrNull.takeUnless { it.isNullOrBlank() } ?: "",
                    key = note.property("tone").asInt,
                )
            }

    suspend fun generate(project: Project): ExportResult {
        val jsonText = generateContent(project)
        val yamlText = JsYaml.dump(JSON.parse(jsonText))
        val blob = Blob(arrayOf(yamlText), BlobPropertyBag("application/octet-stream"))
        val name = project.name + Format.Ustx.extension
        return ExportResult(blob, name)
    }

    private suspend fun generateContent(project: Project): String {
        val projectElement = parseToJsonElement(project.inputFiles.first().readText())

        data class TrackElementGroup(
            val track: JsonElement,
            val trackId: Int,
            val chorusTracks: List<JsonElement>,
            val chorusTrackIds: List<Int>,
            val voiceParts: List<JsonElement>,
            val model: Track,
        )

        var trackElementGroups = projectElement.property("tracks").asList.toList()
            .zip(project.content.tracks)
            .map { (trackElement, trackModel) ->
                val voiceParts = projectElement.property("voice_parts").asList
                    .filter { it.maybeProperty("track_no")?.asInt == trackModel.index }
                TrackElementGroup(
                    track = trackElement,
                    trackId = 0, // to be filled later
                    chorusTracks = emptyList(),
                    chorusTrackIds = emptyList(),
                    voiceParts = voiceParts,
                    model = trackModel,
                )
            }

        var trackElementCount = 0
        // generate tracks
        trackElementGroups = trackElementGroups.map { group ->
            val trackChorus = project.chorus[project.content.tracks.indexOf(group.model)]
            val chorusTrackElements = group.track.mapChorusTrackElements(group.model, trackChorus.size)
            val trackId = trackElementCount++
            val chorusTrackIds = chorusTrackElements.map { trackElementCount++ }
            group.copy(
                trackId = trackId,
                chorusTracks = chorusTrackElements,
                chorusTrackIds = chorusTrackIds,
            )
        }

        // generate parts
        trackElementGroups = trackElementGroups.map { group ->
            val trackChorus = project.chorus[project.content.tracks.indexOf(group.model)]
            val trackId = group.trackId
            val originalVoiceParts = group.voiceParts.map { part ->
                part.mapProperty("track_no") { trackId }
            }
            val chorusVoiceParts = trackChorus.toList().zip(group.chorusTrackIds)
                .flatMap { (chorus, chorusTrackId) ->
                    group.voiceParts.mapChorusPartElements(group.model, chorusTrackId, chorus.second)
                }
            group.copy(voiceParts = originalVoiceParts + chorusVoiceParts)
        }

        val newTracks = trackElementGroups.flatMap { listOf(it.track) + it.chorusTracks }
        val newVoiceParts = trackElementGroups.flatMap { it.voiceParts }
        val newWaveParts = projectElement.property("wave_parts").asList
            .map { wavePart ->
                wavePart.mapProperty("track_no") { trackElementGroups[it.asInt].trackId }
            }

        return projectElement
            .withProperty("tracks", newTracks)
            .withProperty("voice_parts", newVoiceParts)
            .withProperty("wave_parts", newWaveParts)
            .toString()
    }

    private fun JsonElement.mapChorusTrackElements(
        trackModel: Track,
        chorusCount: Int,
    ): List<JsonElement> {
        if (trackModel.bars.isEmpty()) return listOf()
        return List(chorusCount) {
            // the track object does not contain any content-related information
            // we just need to copy the track object
            clone()
        }
    }

    private fun List<JsonElement>.mapChorusPartElements(
        trackModel: Track,
        trackId: Int,
        noteShifts: List<NoteShift>,
    ): List<JsonElement> {
        if (trackModel.bars.isEmpty()) return listOf()

        // Collect all notes with index
        val noteIndexMap = mutableMapOf<JsonElement, Int>()
        this.flatMap { it.maybeProperty("notes")?.asList.orEmpty() }
            .forEachIndexed { index, note -> noteIndexMap[note] = index }

        val noteShiftMap = noteShifts.associate { it.noteIndex to it.keyDelta }

        return map { partElement ->
            partElement.mapProperty("track_no") { trackId }
                .mapProperty("notes") { notesElement ->
                    notesElement.asList.mapNotNull { note ->
                        val index = noteIndexMap[note]
                        val shift = noteShiftMap[index] ?: return@mapNotNull null
                        note.mapProperty("tone") { it.asInt + shift }
                    }
                }
        }
    }

    private val jsonSerializer = Json {
        isLenient = true
    }
}
