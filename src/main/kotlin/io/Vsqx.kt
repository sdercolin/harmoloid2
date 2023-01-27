package io

import com.sdercolin.harmoloid.core.model.Content
import com.sdercolin.harmoloid.core.model.Note
import com.sdercolin.harmoloid.core.model.NoteShift
import com.sdercolin.harmoloid.core.model.TimeSignature
import com.sdercolin.harmoloid.core.model.Track
import exception.IllegalFileException
import model.ExportResult
import model.Format
import model.Project
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.XMLDocument
import org.w3c.dom.parsing.DOMParser
import org.w3c.dom.parsing.XMLSerializer
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.File
import util.clone
import util.getElementListByTagName
import util.getSingleElementByTagName
import util.getSingleElementByTagNameOrNull
import util.innerValue
import util.innerValueOrNull
import util.insertAfterThis
import util.nameWithoutExtension
import util.readText
import util.setSingleChildValue

object Vsqx {

    suspend fun parse(file: File): Project {
        val text = file.readText()
        return when {
            text.contains("xmlns=\"http://www.yamaha.co.jp/vocaloid/schema/vsq3/\"") ->
                parse(file, text, TagNames.Vsq3)
            text.contains("xmlns=\"http://www.yamaha.co.jp/vocaloid/schema/vsq4/\"") ->
                parse(file, text, TagNames.Vsq4)
            else -> throw IllegalFileException.UnknownVsqVersion()
        }
    }

    private fun parse(file: File, textRead: String, tagNames: TagNames): Project {
        val format = when (tagNames) {
            TagNames.Vsq3 -> Format.Vsq3
            TagNames.Vsq4 -> Format.Vsq4
        }
        val projectName = file.nameWithoutExtension
        val parser = DOMParser()
        val document = parser.parseFromString(textRead, "text/xml") as XMLDocument

        val root = document.documentElement ?: throw IllegalFileException.XmlRootNotFound()

        val masterTrack = root.getSingleElementByTagName(tagNames.masterTrack)
        val timeSignatures = parseTimeSignatures(masterTrack, tagNames)

        val tracks = root.getElementListByTagName(tagNames.vsTrack).mapIndexed { index, element ->
            parseTrack(element, index, tagNames, timeSignatures)
        }

        return Project(
            format = format,
            inputFiles = listOf(file),
            name = projectName,
            content = Content(tracks),
        )
    }

    private fun parseTimeSignatures(
        masterTrack: Element,
        tagNames: TagNames,
    ): List<TimeSignature> {
        return masterTrack.getElementListByTagName(tagNames.timeSig, allowEmpty = false)
            .mapNotNull {
                val posMes = it.getSingleElementByTagNameOrNull(tagNames.posMes)?.innerValueOrNull?.toIntOrNull()
                    ?: return@mapNotNull null
                val nume = it.getSingleElementByTagNameOrNull(tagNames.nume)?.innerValueOrNull?.toIntOrNull()
                    ?: return@mapNotNull null
                val denomi = it.getSingleElementByTagNameOrNull(tagNames.denomi)?.innerValueOrNull?.toIntOrNull()
                    ?: return@mapNotNull null
                TimeSignature(
                    measurePosition = posMes,
                    numerator = nume,
                    denominator = denomi,
                )
            }
            .let {
                if (it.isEmpty()) {
                    listOf(TimeSignature.default)
                } else it
            }
    }

    private fun parseTrack(
        trackNode: Element,
        trackIndex: Int,
        tagNames: TagNames,
        timeSignatures: List<TimeSignature>,
    ): Track {
        val trackName = trackNode.getSingleElementByTagNameOrNull(tagNames.trackName)?.innerValueOrNull
            ?: "Track ${trackIndex + 1}"
        val partNodes = trackNode.getElementListByTagName(tagNames.musicalPart)
        val notes = partNodes
            .flatMap { partNode ->
                val tickOffset = partNode.getSingleElementByTagName(tagNames.posTick).innerValue.toInt()
                partNode.getElementListByTagName(tagNames.note).map { tickOffset to it }
            }
            .mapIndexed { index, (tickOffset, noteNode) ->
                val key = noteNode.getSingleElementByTagName(tagNames.noteNum).innerValue.toInt()
                val tickOn = noteNode.getSingleElementByTagName(tagNames.posTick).innerValue.toLong()
                val length = noteNode.getSingleElementByTagName(tagNames.duration).innerValue.toLong()
                val lyric = noteNode.getSingleElementByTagName(tagNames.lyric).innerValueOrNull ?: ""
                Note(
                    index = index,
                    key = key,
                    tickOn = tickOn + tickOffset,
                    tickOff = tickOn + tickOffset + length,
                    lyric = lyric,
                )
            }
        return Track.build(
            trackIndex,
            trackName,
            notes,
            timeSignatures,
        )
    }

    suspend fun generate(project: Project, format: Format): ExportResult {
        val tagNames = when (format) {
            Format.Vsq3 -> TagNames.Vsq3
            Format.Vsq4 -> TagNames.Vsq4
            else -> throw IllegalArgumentException("Unacceptable format: $format")
        }
        val document = generateContent(project, tagNames)
        val serializer = XMLSerializer()
        val content = serializer.serializeToString(document).cleanEmptyXmlns()
        val blob = Blob(arrayOf(content), BlobPropertyBag("application/octet-stream"))
        val name = project.name + format.extension
        return ExportResult(
            blob,
            name,
        )
    }

    private fun String.cleanEmptyXmlns() = replace(" xmlns=\"\"", "")

    private suspend fun generateContent(project: Project, tagNames: TagNames): Document {
        val text = project.inputFiles.first().readText()
        val parser = DOMParser()
        val document = parser.parseFromString(text, "text/xml") as XMLDocument
        val root = requireNotNull(document.documentElement)
        val mixer = root.getSingleElementByTagName(tagNames.mixer)

        val trackNodes = root.getElementListByTagName(tagNames.vsTrack)
        val unitNodes = mixer.getElementListByTagName(tagNames.vsUnit)
        val trackToUnitNodePairs = trackNodes.zip(unitNodes)
        var trackIndex = -1
        trackToUnitNodePairs.zip(project.content.tracks).forEach { (nodePair, track) ->
            val (trackNode, unitNode) = nodePair
            var currentTrackNode = trackNode
            var currentUnitNode = unitNode
            trackIndex++
            trackNode.setSingleChildValue(tagNames.trackNum, trackIndex)
            unitNode.setSingleChildValue(tagNames.trackNum, trackIndex)

            val trackChorus = project.chorus[project.content.tracks.indexOf(track)]
            if (trackChorus.isEmpty()) return@forEach
            if (track.bars.isEmpty()) return@forEach

            trackChorus.forEach { (harmony, noteShifts) ->
                trackIndex++
                val newTrackName = harmony.getHarmonicTrackName(track.name)

                val newTrackNode = trackNode.clone()
                newTrackNode.setSingleChildValue(tagNames.trackName, newTrackName)
                newTrackNode.setSingleChildValue(tagNames.trackNum, trackIndex)
                applyNoteShiftsToTrackNode(newTrackNode, tagNames, noteShifts)
                currentTrackNode.insertAfterThis(newTrackNode)
                currentTrackNode = newTrackNode

                val newUnitNode = unitNode.clone()
                newUnitNode.setSingleChildValue(tagNames.trackNum, trackIndex)
                currentUnitNode.insertAfterThis(newUnitNode)
                currentUnitNode = newUnitNode
            }
        }

        return document
    }

    private fun applyNoteShiftsToTrackNode(
        trackNode: Element,
        tagNames: TagNames,
        noteShifts: List<NoteShift>,
    ) {
        val noteShiftsMap = noteShifts.map { it.noteIndex to it.keyDelta }.toMap()
        trackNode.getElementListByTagName(tagNames.musicalPart)
            .flatMap { it.getElementListByTagName(tagNames.note) }
            .forEachIndexed { index, noteNode ->
                val keyDelta = noteShiftsMap[index]
                if (keyDelta == null) {
                    noteNode.parentElement?.removeChild(noteNode)
                } else {
                    val key = noteNode.getSingleElementByTagName(tagNames.noteNum).innerValue.toInt()
                    noteNode.setSingleChildValue(tagNames.noteNum, key + keyDelta)
                }
            }
    }

    private enum class TagNames(
        val masterTrack: String = "masterTrack",
        val timeSig: String = "timeSig",
        val posMes: String = "posMes",
        val nume: String = "nume",
        val denomi: String = "denomi",
        val posTick: String = "posTick",
        val vsTrack: String = "vsTrack",
        val trackName: String = "trackName",
        val musicalPart: String = "musicalPart",
        val note: String = "note",
        val duration: String = "durTick",
        val noteNum: String = "noteNum",
        val lyric: String = "lyric",
        val mixer: String = "mixer",
        val vsUnit: String = "vsUnit",
        val trackNum: String = "vsTrackNo",
    ) {
        Vsq3,
        Vsq4(
            posMes = "m",
            nume = "nu",
            denomi = "de",
            posTick = "t",
            trackName = "name",
            musicalPart = "vsPart",
            duration = "dur",
            noteNum = "n",
            lyric = "y",
            trackNum = "tNo",
        )
    }
}
