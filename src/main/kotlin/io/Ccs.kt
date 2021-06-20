package io

import com.sdercolin.harmoloid.core.model.Content
import com.sdercolin.harmoloid.core.model.KEY_IN_OCTAVE
import com.sdercolin.harmoloid.core.model.Note
import com.sdercolin.harmoloid.core.model.NoteShift
import com.sdercolin.harmoloid.core.model.TimeSignature
import com.sdercolin.harmoloid.core.model.Track
import exception.IllegalFileException
import external.generateUUID
import model.ExportResult
import model.Format
import model.Project
import model.TickCounter
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
import util.getRequiredAttribute
import util.getRequiredAttributeAsInteger
import util.getRequiredAttributeAsLong
import util.getSingleElementByTagName
import util.getSingleElementByTagNameOrNull
import util.insertAfterThis
import util.nameWithoutExtension
import util.readText

object Ccs {
    suspend fun parse(file: File): Project {
        val projectName = file.nameWithoutExtension
        val text = file.readText()
        val parser = DOMParser()
        val document = parser.parseFromString(text, "text/xml") as XMLDocument

        val scenarioNode = document.documentElement ?: throw IllegalFileException.XmlRootNotFound()
        val sceneNode = scenarioNode
            .getSingleElementByTagName("Sequence")
            .getSingleElementByTagName("Scene")
        val unitNodes = sceneNode
            .getSingleElementByTagName("Units")
            .getElementListByTagName("Unit")
            .filter { it.getAttribute("Category") == "SingerSong" }
        val groupNodes = sceneNode
            .getSingleElementByTagName("Groups")
            .getElementListByTagName("Group")
            .filter { it.getAttribute("Category") == "SingerSong" }

        val results = unitNodes.mapIndexed { index, unitNode ->
            val groupId = unitNode.getAttribute("Group")
            val group = groupId?.let { id ->
                groupNodes.find { it.getAttribute("Id") == id }
            }
            val trackName = group?.getAttribute("Name")
            parseTrack(index, unitNode, trackName)
        }

        val timeSignatures = mergeTimeSignatures(results)
        val tracks = results.map {
            Track.build(
                it.index,
                it.name,
                it.notes,
                timeSignatures
            )
        }

        return Project(
            format = Format.Ccs,
            inputFiles = listOf(file),
            name = projectName,
            content = Content(tracks)
        )
    }

    private fun mergeTimeSignatures(
        results: List<TrackParseResult>
    ): List<TimeSignature> {
        return results.firstOrNull { it.timeSignatures.isNotEmpty() }
            ?.timeSignatures.let { it ?: listOf(TimeSignature.default) }
    }

    private fun parseTrack(index: Int, unitNode: Element, name: String?): TrackParseResult {
        val timeNodes = unitNode
            .getSingleElementByTagNameOrNull("Song")
            ?.getSingleElementByTagNameOrNull("Beat")
            ?.getElementListByTagName("Time").orEmpty()

        val tickCounter = TickCounter(TICK_RATE)
        val timeSignatures = timeNodes.mapNotNull { timeNode ->
            val tick = timeNode.getAttribute("Clock")?.toLongOrNull() ?: return@mapNotNull null
            val numerator = timeNode.getAttribute("Beats")?.toIntOrNull() ?: return@mapNotNull null
            val denominator = timeNode.getAttribute("BeatType")?.toIntOrNull() ?: return@mapNotNull null

            tickCounter.goToTick(tick, numerator, denominator)
            TimeSignature(tickCounter.measure, numerator, denominator)
        }

        val notes = unitNode
            .getSingleElementByTagNameOrNull("Song")
            ?.getSingleElementByTagNameOrNull("Score")
            ?.getElementListByTagName("Note").orEmpty()
            .mapIndexed { noteIndex, element ->
                val tickOn = (element.getRequiredAttributeAsLong("Clock") / TICK_RATE).toLong()
                val tickOff = tickOn +
                        (element.getRequiredAttributeAsLong("Duration") / TICK_RATE).toLong()
                val pitchStep = element.getRequiredAttributeAsInteger("PitchStep")
                val pitchOctave = element.getRequiredAttributeAsInteger("PitchOctave") - OCTAVE_OFFSET
                val key = pitchStep + pitchOctave * KEY_IN_OCTAVE
                val lyric = element.getRequiredAttribute("Lyric")
                Note(noteIndex, key, tickOn, tickOff, lyric)
            }

        return TrackParseResult(index, name ?: "Track ${index + 1}", notes, timeSignatures)
    }

    private data class TrackParseResult(
        val index: Int,
        val name: String,
        val notes: List<Note>,
        val timeSignatures: List<TimeSignature>
    )

    suspend fun generate(project: Project): ExportResult {
        val document = generateContent(project)
        val serializer = XMLSerializer()
        val content = serializer.serializeToString(document)
        val blob = Blob(arrayOf(content), BlobPropertyBag("application/octet-stream"))
        val name = project.name + Format.Ccs.extension
        return ExportResult(blob, name)
    }

    private suspend fun generateContent(project: Project): Document {
        val text = project.inputFiles.first().readText()
        val parser = DOMParser()
        val document = parser.parseFromString(text, "text/xml") as XMLDocument
        val scenarioNode = requireNotNull(document.documentElement)
        val sceneNode = scenarioNode
            .getSingleElementByTagName("Sequence")
            .getSingleElementByTagName("Scene")

        val unitNodes = sceneNode
            .getSingleElementByTagName("Units")
            .getElementListByTagName("Unit")
            .filter { it.getAttribute("Category") == "SingerSong" }
        val groupNodes = sceneNode
            .getSingleElementByTagName("Groups")
            .getElementListByTagName("Group")
            .filter { it.getAttribute("Category") == "SingerSong" }

        val unitToGroupNodePairs = unitNodes.map { unitNode ->
            val groupNode = groupNodes.first {
                it.getRequiredAttribute("Id") == unitNode.getRequiredAttribute("Group")
            }
            unitNode to groupNode
        }
        unitToGroupNodePairs.zip(project.content.tracks).forEach { (nodePair, track) ->
            val (unitNode, groupNode) = nodePair
            var currentUnitNode = unitNode
            var currentGroupNode = groupNode

            val trackChorus = project.chorus[project.content.tracks.indexOf(track)]
            if (trackChorus.isEmpty()) return@forEach
            if (track.bars.isEmpty()) return@forEach

            trackChorus.forEach { (harmony, noteShifts) ->
                val id = generateUUID()
                val newTrackName = harmony.getHarmonicTrackName(track.name)

                val newGroupNode = groupNode.clone()
                newGroupNode.setAttribute("Name", newTrackName)
                newGroupNode.setAttribute("Id", id)
                currentGroupNode.insertAfterThis(newGroupNode)
                currentGroupNode = newGroupNode

                val newUnitNode = unitNode.clone()
                newUnitNode.setAttribute("Group", id)
                applyNoteShiftsToTrackNode(newUnitNode, noteShifts)
                currentUnitNode.insertAfterThis(newUnitNode)
                currentUnitNode = newUnitNode
            }
        }

        return document
    }

    private fun applyNoteShiftsToTrackNode(
        unitNode: Element,
        noteShifts: List<NoteShift>
    ) {
        val noteShiftsMap = noteShifts.map { it.noteIndex to it.keyDelta }.toMap()
        unitNode.getSingleElementByTagName("Song")
            .getSingleElementByTagName("Score")
            .getElementListByTagName("Note")
            .forEachIndexed { index, noteNode ->
                val keyDelta = noteShiftsMap[index]
                if (keyDelta == null) {
                    noteNode.parentElement?.removeChild(noteNode)
                } else {
                    val pitchStep = noteNode.getRequiredAttributeAsInteger("PitchStep")
                    val pitchOctave = noteNode.getRequiredAttributeAsInteger("PitchOctave")
                    val key = pitchStep + pitchOctave * KEY_IN_OCTAVE + keyDelta
                    noteNode.setAttribute("PitchStep", (key % KEY_IN_OCTAVE).toString())
                    noteNode.setAttribute("PitchOctave", (key / KEY_IN_OCTAVE).toString())
                }
            }
    }

    private const val TICK_RATE = 2.0
    private const val OCTAVE_OFFSET = -1
}
