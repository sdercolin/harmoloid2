package io

import com.sdercolin.harmoloid.core.model.Content
import com.sdercolin.harmoloid.core.model.HarmonicType
import com.sdercolin.harmoloid.core.model.Note
import com.sdercolin.harmoloid.core.model.NoteShift
import com.sdercolin.harmoloid.core.model.TimeSignature
import com.sdercolin.harmoloid.core.model.Track
import external.Encoding
import external.JsZip
import external.JsZipOption
import kotlinx.coroutines.await
import model.ExportResult
import model.Format
import model.Project
import org.khronos.webgl.Uint8Array
import org.w3c.files.Blob
import org.w3c.files.File
import util.encode
import util.getSafeFileName
import util.linesNotBlank
import util.nameWithoutExtension
import util.readBinary
import util.readText

object Ust {
    suspend fun parse(files: List<File>): Project {
        val results = files.map {
            parseFile(it)
        }
        val projectName = results
            .mapNotNull { it.projectName }
            .firstOrNull()
            ?: files.first().nameWithoutExtension
        val tracks = results.mapIndexed { index, result ->
            Track.build(
                index,
                name = result.file.nameWithoutExtension,
                result.notes,
                timeSignatures = listOf(TimeSignature.default)
            )
        }
        return Project(
            format = Format.Ust,
            inputFiles = files,
            name = projectName,
            content = Content(tracks)
        )
    }

    private suspend fun readFileContent(file: File): String {
        val binary = file.readBinary()
        val encoding = Encoding.detect(binary)
        return if (encoding == "UTF8") {
            file.readText()
        } else {
            file.readText("shift-jis")
        }
    }

    private suspend fun parseFile(file: File): FileParseResult {
        val lines = readFileContent(file).linesNotBlank()
        var projectName: String? = null
        val notes = mutableListOf<Note>()
        var time = 0L
        var pendingNoteKey: Int? = null
        var pendingNoteLyric: String? = null
        var pendingNoteTickOn: Long? = null
        var pendingNoteTickOff: Long? = null
        for (line in lines) {
            line.tryGetValue("ProjectName")?.let {
                projectName = it
            }
            if (line.startsWith("[#")) {
                if (pendingNoteKey != null &&
                    pendingNoteLyric != null &&
                    pendingNoteTickOn != null &&
                    pendingNoteTickOff != null
                ) {
                    notes.add(
                        Note(
                            index = notes.size,
                            key = pendingNoteKey,
                            lyric = pendingNoteLyric,
                            tickOn = pendingNoteTickOn,
                            tickOff = pendingNoteTickOff
                        )
                    )
                }
                pendingNoteKey = null
                pendingNoteLyric = null
                pendingNoteTickOn = null
                pendingNoteTickOff = null
            }
            line.tryGetValue("Length")?.let {
                val length = it.toLongOrNull() ?: return@let
                pendingNoteTickOn = time
                time += length
                pendingNoteTickOff = time
            }
            line.tryGetValue("Lyric")?.let {
                val validLyric = it.takeIf { lyric -> lyric.isValidLyric } ?: return@let
                pendingNoteLyric = validLyric
            }
            line.tryGetValue("NoteNum")?.let {
                val key = it.toIntOrNull() ?: return@let
                pendingNoteKey = key
            }
        }
        return FileParseResult(file, projectName, notes)
    }

    suspend fun generate(project: Project): ExportResult {
        val zip = JsZip()
        for (index in project.content.tracks.indices) {
            val track = project.content.tracks[index]
            val trackChorus = project.chorus[index]
            val originalContent = readFileContent(project.inputFiles[index])
            val contentsWithNames = generateTrackContentsWithNames(originalContent, track, trackChorus)
            for ((content, name) in contentsWithNames) {
                val contentEncodedArray = content.encode("SJIS")
                val trackNameUrlSafe = getSafeFileName(name)
                val trackFileName = "$trackNameUrlSafe${Format.Ust.extension}"
                zip.file(trackFileName, Uint8Array(contentEncodedArray))
            }
        }
        val option = JsZipOption().also { it.type = "blob" }
        val blob = zip.generateAsync(option).await() as Blob
        val name = project.name + ".zip"
        return ExportResult(blob, name)
    }

    private fun generateTrackContentsWithNames(
        originalContent: String,
        track: Track,
        trackChorus: Map<HarmonicType, List<NoteShift>>
    ): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        if (trackChorus.isEmpty() || track.notes.isEmpty()) return result

        val originalSections = groupContentWithSections(originalContent)
        val noteSectionIndexMap = originalSections
            .asSequence()
            .withIndex()
            .filter { it.value.first.startsWith("[#") }
            .filter { it.value.second.any { line -> line.tryGetValue("Lyric")?.isValidLyric == true } }
            .withIndex()
            .map { it.value.index to it.index }
            .toList()
            .toMap()

        for ((harmony, noteShifts) in trackChorus) {
            val harmonyTrackName = harmony.getHarmonicTrackName(track.name)
            val noteShiftMap = noteShifts.map { it.noteIndex to it.keyDelta }.toMap()
            val sectionContents = originalSections
                .map { it.second }
                .mapIndexed { index, section ->
                    val noteIndex = noteSectionIndexMap[index]
                    if (noteIndex == null) {
                        section
                    } else {
                        val shift = noteShiftMap[noteIndex]
                        if (shift != null) {
                            section.map {
                                it.tryGetValue("NoteNum")?.let { key -> "NoteNum=${key.toInt() + shift}" } ?: it
                            }
                        } else {
                            section.map {
                                it.tryGetValue("Lyric")?.let { "Lyric=R" } ?: it
                            }
                        }
                    }
                }
            result.add(sectionContents.flatten().joinToString(LINE_SEPARATOR) to harmonyTrackName)
        }
        return result
    }

    private fun groupContentWithSections(content: String): List<Pair<String, List<String>>> {
        val lines = content.lines()
        val titleIndexes = lines.withIndex().filter { it.value.startsWith("[") }.map { it.index }
        val groupIndexRanges = (titleIndexes + null).zipWithNext().map { it.first!! until (it.second ?: lines.size) }
        val groups = groupIndexRanges.map { lines.subList(it.first, it.last + 1) }
        return groups.map { it.first() to it }
    }

    private fun String.tryGetValue(key: String): String? {
        if (!startsWith("$key=")) return null
        val index = indexOf("=").takeIf { it in 0 until lastIndex } ?: return null
        return substring(index + 1).takeIf { it.isNotBlank() }
    }

    private val String.isValidLyric get() = this != "R" && this != "r"

    private data class FileParseResult(
        val file: File,
        val projectName: String?,
        val notes: List<Note>
    )

    private const val LINE_SEPARATOR = "\r\n"
}
