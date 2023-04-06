package io

import com.sdercolin.harmoloid.core.model.Content
import com.sdercolin.harmoloid.core.model.HarmonicType
import com.sdercolin.harmoloid.core.model.Note
import com.sdercolin.harmoloid.core.model.NoteShift
import com.sdercolin.harmoloid.core.model.TimeSignature
import com.sdercolin.harmoloid.core.model.Track
import com.sdercolin.utaformatix.data.Document
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.ExportResult
import model.Format
import model.Project
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.File
import util.readText

private typealias UfDataTrack = com.sdercolin.utaformatix.data.Track
private typealias UfDataTimeSignature = com.sdercolin.utaformatix.data.TimeSignature

@OptIn(ExperimentalSerializationApi::class)
object UfData {

    suspend fun parse(file: File): Project {
        val text = file.readText()
        val document = jsonSerializer.decodeFromString<Document>(text)
        val timeSignatures = document.project.timeSignatures.map { parseTimeSignature(it) }
        val tracks = document.project.tracks.mapIndexed { index, track -> parseTrack(index, track, timeSignatures) }
        return Project(
            format = Format.UfData,
            inputFiles = listOf(file),
            name = document.project.name,
            content = Content(tracks),
        )
    }

    private fun parseTimeSignature(timeSignature: UfDataTimeSignature) = TimeSignature(
        measurePosition = timeSignature.measurePosition,
        denominator = timeSignature.denominator,
        numerator = timeSignature.numerator,
    )

    private fun parseTrack(index: Int, track: UfDataTrack, timeSignatures: List<TimeSignature>) =
        Track.build(
            index,
            name = track.name,
            notes = parseNotes(track),
            timeSignatures,
        )

    private fun parseNotes(track: UfDataTrack): List<Note> = track.notes.mapIndexed { index, note ->
        Note(
            index = index,
            key = note.key,
            tickOn = note.tickOn,
            tickOff = note.tickOff,
            lyric = note.lyric,
        )
    }

    suspend fun generate(project: Project): ExportResult {
        val jsonText = generateContent(project)
        val blob = Blob(arrayOf(jsonText), BlobPropertyBag("application/octet-stream"))
        val name = project.name + Format.UfData.extension
        return ExportResult(blob, name)
    }

    private suspend fun generateContent(project: Project): String {
        val document = jsonSerializer.decodeFromString<Document>(project.inputFiles.first().readText())
        val newTracks = document.project.tracks.flatMapIndexed { index, originalTrack ->
            val chorus = project.chorus[index]
            mapTracks(originalTrack, chorus)
        }
        val newDocument = document.copy(
            project = document.project.copy(
                tracks = newTracks,
            ),
        )
        return jsonSerializer.encodeToString(newDocument)
    }

    private fun mapTracks(originalTrack: UfDataTrack, chorus: Map<HarmonicType, List<NoteShift>>): List<UfDataTrack> {
        val chorusTracks = chorus.map { (harmony, noteShifts) ->
            originalTrack.copy(
                name = originalTrack.name + " " + harmony.getHarmonicTrackName(originalTrack.name),
                notes = originalTrack.notes.mapIndexedNotNull { index, note ->
                    val noteShift = noteShifts.find { it.noteIndex == index } ?: return@mapIndexedNotNull null
                    note.copy(key = note.key + noteShift.keyDelta)
                },
            )
        }
        return listOf(originalTrack) + chorusTracks
    }

    private val jsonSerializer = Json {
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }
}
