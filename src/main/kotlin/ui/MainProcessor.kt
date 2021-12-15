package ui

import com.sdercolin.harmoloid.core.Config
import com.sdercolin.harmoloid.core.Core
import com.sdercolin.harmoloid.core.model.HarmonicType
import com.sdercolin.harmoloid.core.model.Passage
import com.sdercolin.harmoloid.core.model.PassageTonalityAnalysisResult
import com.sdercolin.harmoloid.core.model.Tonality
import com.sdercolin.harmoloid.core.model.Track
import com.sdercolin.harmoloid.core.model.TrackTonalityAnalysisResult
import com.sdercolin.harmoloid.core.util.update
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.Align
import kotlinx.css.Display
import kotlinx.css.LinearDimension
import kotlinx.css.VerticalAlign
import kotlinx.css.display
import kotlinx.css.marginBottom
import kotlinx.css.marginLeft
import kotlinx.css.marginRight
import kotlinx.css.marginTop
import model.ExportResult
import model.Project
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.createElement
import react.setState
import styled.css
import styled.styledDiv
import ui.TrackCardState.Expanded
import ui.external.materialui.ButtonSize
import ui.external.materialui.ButtonVariant
import ui.external.materialui.Color
import ui.external.materialui.DividerVariant
import ui.external.materialui.FontSize
import ui.external.materialui.Icons
import ui.external.materialui.Severity
import ui.external.materialui.Style
import ui.external.materialui.TextFieldVariant
import ui.external.materialui.TypographyVariant
import ui.external.materialui.button
import ui.external.materialui.card
import ui.external.materialui.cardActions
import ui.external.materialui.cardContent
import ui.external.materialui.checkbox
import ui.external.materialui.collapse
import ui.external.materialui.divider
import ui.external.materialui.formControlLabel
import ui.external.materialui.formGroup
import ui.external.materialui.iconButton
import ui.external.materialui.menuItem
import ui.external.materialui.textField
import ui.external.materialui.typography
import ui.strings.Strings
import ui.strings.string

class MainProcessor(props: MainProcessorProps) : RComponent<MainProcessorProps, MainProcessorState>(props) {

    private lateinit var core: Core

    fun export() {
        setState { isProcessing = true }
        GlobalScope.launch {
            try {
                val exportProject = props.project.copy(
                    content = core.content,
                    chorus = core.content.tracks.indices.map { trackIndex ->
                        core.getAllChorusTracks(trackIndex)
                    }
                )
                val result = props.project.format.generator.invoke(exportProject)
                console.log(result)
                props.onFinish.invoke(result)
            } catch (t: Throwable) {
                showErrorDialog(string(Strings.ExportErrorTitle), t)
            }
            setState { isProcessing = false }
        }
    }

    fun updateConfig(config: Config) {
        core.reloadConfig(config)
        console.log("config reloaded: ${core.config}")
    }

    override fun MainProcessorState.init(props: MainProcessorProps) {
        isProcessing = false
        dialogError = DialogErrorState()
        snackbarError = SnackbarErrorState()
        try {
            core = Core(props.project.content, props.config)
            console.log("core is initialized with config: ${core.config}")
            trackCards = core.content.tracks.mapIndexed { index, track ->
                val expanded = if (index == 0) Expanded.MarkTonality
                else Expanded.None
                TrackCardState(index, track, mapOf(), mapOf(), expanded)
            }
        } catch (t: Throwable) {
            showErrorDialog(string(Strings.ImportErrorDialogTitle), t)
        }
    }

    override fun RBuilder.render() {

        state.trackCards.forEach { trackState ->
            styledDiv {
                css {
                    marginTop = LinearDimension("30px")
                }
                card {
                    cardContent {
                        typography {
                            attrs {
                                variant = TypographyVariant.subtitle2
                                color = Color.secondary
                            }
                            +string(Strings.TrackLabel, "number" to trackState.track.number.toString())
                        }
                        typography {
                            attrs {
                                variant = TypographyVariant.h5
                                color = Color.inherit
                            }
                            +trackState.track.name
                        }
                        typography {
                            attrs {
                                variant = TypographyVariant.body2
                                component = "p"
                                color = Color.inherit
                            }
                            +string(
                                Strings.TrackSummary,
                                "barCount" to trackState.track.bars.size.toString(),
                                "noteCount" to trackState.track.notes.size.toString()
                            )
                        }
                    }
                    cardActions {
                        button {
                            attrs {
                                onClick = {
                                    val requestedExpanded =
                                        if (trackState.expanded == Expanded.MarkTonality)
                                            Expanded.None
                                        else Expanded.MarkTonality
                                    toggleCollapse(trackState.index, requestedExpanded)
                                }
                            }
                            if (trackState.track.isTonalityMarked) Icons.checkCircle {}
                            else Icons.edit {}
                            styledDiv {
                                css { marginLeft = LinearDimension("5px") }
                                +string(Strings.MarkTonalityButton)
                            }
                        }
                        button {
                            attrs {
                                onClick = {
                                    val requestedExpanded =
                                        if (trackState.expanded == Expanded.SelectHarmony)
                                            Expanded.None
                                        else Expanded.SelectHarmony
                                    toggleCollapse(trackState.index, requestedExpanded)
                                }
                                disabled = !trackState.track.isTonalityMarked
                            }
                            if (trackState.track.harmonies?.isNotEmpty() == true) Icons.checkCircle {}
                            else Icons.edit {}
                            styledDiv {
                                css { marginLeft = LinearDimension("5px") }
                                +string(Strings.SelectHarmonyButton)
                            }
                        }
                    }
                    collapse {
                        attrs {
                            `in` = trackState.expanded != Expanded.None
                            unmountOnExit = true
                        }
                        when (trackState.expanded) {
                            Expanded.None -> Unit
                            Expanded.MarkTonality -> {
                                buildTonalityMarking(trackState)
                            }
                            Expanded.SelectHarmony -> {
                                buildHarmonySelection(trackState)
                            }
                        }
                    }
                }
            }
        }

        messageBar(
            open = state.snackbarError.open,
            message = state.snackbarError.message,
            onClose = { closeMessageBar() },
            severityString = Severity.error
        )

        errorDialog(
            isShowing = state.dialogError.open,
            title = state.dialogError.title,
            errorMessage = state.dialogError.message,
            close = { closeErrorDialog() }
        )

        if (state.isProcessing) {
            progress()
        }
    }

    private fun showMessageBar(message: String) {
        setState { snackbarError = SnackbarErrorState(true, message) }
    }

    private fun closeMessageBar() {
        setState {
            snackbarError = snackbarError.copy(open = false)
        }
    }

    private fun showErrorDialog(title: String, t: Throwable) {
        console.log(t)
        setState {
            dialogError = DialogErrorState(
                open = true,
                title = title,
                message = t.toString()
            )
        }
    }

    private fun closeErrorDialog() {
        setState {
            dialogError = dialogError.copy(open = false)
        }
    }

    private fun toggleCollapse(trackIndex: Int, requestedExpanded: Expanded) {
        setState {
            trackCards = trackCards.map {
                if (it.index == trackIndex) {
                    it.copy(expanded = requestedExpanded)
                } else {
                    if (requestedExpanded == Expanded.None) it
                    else it.copy(expanded = Expanded.None)
                }
            }
        }
    }

    private fun RBuilder.buildTonalityMarking(trackCard: TrackCardState) {
        val passages = trackCard.track.passages?.takeIf { it.isNotEmpty() } ?: return

        buildDivider()

        styledDiv {
            css {
                marginLeft = LinearDimension("24px")
                marginRight = LinearDimension("24px")
            }

            passages.forEach { passage ->

                styledDiv {
                    css {
                        display = Display.flex
                        marginTop = LinearDimension("16px")
                        marginBottom = LinearDimension("16px")
                    }

                    typography {
                        attrs {
                            variant = TypographyVariant.subtitle2
                            component = "span"
                            color = Color.inherit
                            style = Style(width = "140px", marginRight = "24px", alignSelf = Align.center)
                        }
                        +string(Strings.PassageLabel, "number" to passage.number.toString())
                    }

                    val isStartInputError = trackCard.errorPassageStartInputs.containsKey(passage.index)
                    val isStartInputDisabled = passage.index == 0 ||
                            (!isStartInputError && trackCard.errorPassageStartInputs.isNotEmpty())
                    val start = trackCard.errorPassageStartInputs[passage.index]
                        ?: passage.bars.firstOrNull()?.number?.toString()
                    val end = passage.bars.lastOrNull()?.number?.toString()
                    textField {
                        attrs {
                            value = start ?: ""
                            variant = TextFieldVariant.outlined
                            disabled = isStartInputDisabled
                            style = Style(width = "140px", marginRight = "24px")
                            error = isStartInputError
                            label = string(Strings.StartingBarLabel)
                            onChange = {
                                onChangePassageStartInput(
                                    input = it.target.asDynamic().value as String,
                                    trackIndex = trackCard.index,
                                    passageIndex = passage.index,
                                    passages = passages
                                )
                            }
                        }
                    }
                    textField {
                        attrs {
                            value = end ?: ""
                            variant = TextFieldVariant.outlined
                            disabled = true
                            style = Style(width = "140px", marginRight = "24px")
                            label = string(Strings.EndingBarLabel)
                        }
                    }

                    var optionText = ""
                    val certainties = passage.tonalityCertainties
                    if (passage.tonality == null &&
                        certainties != null &&
                        certainties.size > 1
                    ) {
                        val options = certainties.entries
                            .sortedByDescending { it.value }
                            .joinToString("/") { it.key.displayName }
                        optionText = string(Strings.ProbableTonalitiesDescription, "options" to options)
                    }

                    textField {
                        attrs {
                            select = true
                            variant = TextFieldVariant.filled
                            style = Style(width = "180px", marginRight = "24px")
                            label = string(Strings.TonalityLabel)
                            value = passage.tonality?.displayName.orEmpty()
                            error = optionText.isNotEmpty()
                            helperText = optionText
                            onChange = { event ->
                                val value = event.target.asDynamic().value as String
                                setTonality(value, trackCard.index, passages, passage.index)
                            }
                        }
                        menuItem {
                            attrs {
                                value = ""
                            }
                            +"　"
                        }
                        Tonality.values().forEach {
                            menuItem {
                                attrs {
                                    value = it.displayName
                                }
                                +it.displayName
                            }
                        }
                    }

                    val lyricsPreview = passage.notes.joinToString(" ") { it.lyric }.ifEmpty { "-" }
                    textField {
                        attrs {
                            value = lyricsPreview
                            variant = TextFieldVariant.outlined
                            disabled = true
                            label = string(Strings.LyricsLabel)
                            fullWidth = true
                            style = Style(marginRight = "64px")
                        }
                    }
                }
            }
        }

        styledDiv {
            css {
                marginTop = LinearDimension("24px")
                marginLeft = LinearDimension("8px")
                marginBottom = LinearDimension("8px")
            }

            iconButton {
                attrs {
                    color = Color.secondary
                    style = Style(marginLeft = "8px", marginRight = "8px")
                    disabled = passages.last().bars.size <= 1
                    onClick = { addPassage(trackCard.index, passages) }
                }
                Icons.addCircle {
                    attrs {
                        style = Style(
                            fontSize = FontSize.initial,
                            verticalAlign = VerticalAlign.middle
                        )
                    }
                }
            }

            iconButton {
                attrs {
                    color = Color.secondary
                    style = Style(marginLeft = "8px", marginRight = "8px")
                    disabled = passages.size <= 1
                    onClick = { mergeLastTwoPassages(trackCard.index, passages) }
                }
                Icons.removeCircle {
                    attrs {
                        style = Style(
                            fontSize = FontSize.initial,
                            verticalAlign = VerticalAlign.middle
                        )
                    }
                }
            }

            button {
                attrs {
                    style = Style(marginLeft = "8px", marginRight = "8px")
                    size = ButtonSize.small
                    color = Color.secondary
                    variant = ButtonVariant.outlined
                    onClick = { applyAutoTonality(trackCard.index) }
                }
                +string(Strings.AutoTonalityButton)
            }

            button {
                attrs {
                    style = Style(marginLeft = "8px", marginRight = "8px")
                    size = ButtonSize.small
                    color = Color.secondary
                    variant = ButtonVariant.outlined
                    onClick = { applyAutoPassageDivision(trackCard.index) }
                }
                +string(Strings.AutoPassageDivisionButton)
            }

            button {
                attrs {
                    style = Style(marginLeft = "8px", marginRight = "8px")
                    size = ButtonSize.small
                    color = Color.secondary
                    variant = ButtonVariant.outlined
                    disabled = !trackCard.track.isTonalityMarked || state.trackCards.size <= 1
                    onClick = { applyPassageSettingsToAllTracks(trackCard.index) }
                }
                +string(Strings.ApplyToAllTracksButton)
            }

            button {
                attrs {
                    style = Style(marginLeft = "8px", marginRight = "8px")
                    size = ButtonSize.small
                    color = Color.secondary
                    variant = ButtonVariant.outlined
                    onClick = { resetPassages(trackCard.index) }
                }
                +string(Strings.ResetAllButton)
            }
        }
    }

    private fun RBuilder.buildHarmonySelection(trackCard: TrackCardState) {
        val harmonies = trackCard.track.harmonies.orEmpty()

        buildDivider()

        styledDiv {
            css {
                marginLeft = LinearDimension("24px")
                marginRight = LinearDimension("24px")
            }

            formGroup {
                attrs.row = true
                HarmonicType.values().forEach { harmony ->
                    formControlLabel {
                        attrs {
                            label = string(
                                when (harmony) {
                                    HarmonicType.Copy -> Strings.HarmonicTypeCopy
                                    HarmonicType.UpperThird -> Strings.HarmonicTypeUpperThird
                                    HarmonicType.LowerThird -> Strings.HarmonicTypeLowerThird
                                    HarmonicType.UpperSixth -> Strings.HarmonicTypeUpperSixth
                                    HarmonicType.LowerSixth -> Strings.HarmonicTypeLowerSixth
                                    HarmonicType.UpperOctave -> Strings.HarmonicTypeUpperOctave
                                    HarmonicType.LowerOctave -> Strings.HarmonicTypeLowerOctave
                                }
                            )
                            control = createElement {
                                checkbox {
                                    attrs {
                                        checked = harmonies.contains(harmony)
                                        onChange = {
                                            val checked = it.target.asDynamic().checked as Boolean
                                            selectHarmony(trackCard.index, harmony, checked)
                                        }
                                    }
                                }
                            }!!
                        }
                    }
                }
            }
        }

        styledDiv {
            css {
                marginTop = LinearDimension("24px")
                marginLeft = LinearDimension("8px")
                marginBottom = LinearDimension("8px")
            }

            button {
                attrs {
                    style = Style(marginLeft = "8px", marginRight = "8px")
                    size = ButtonSize.small
                    color = Color.secondary
                    variant = ButtonVariant.outlined
                    disabled = !trackCard.track.isTonalityMarked || state.trackCards.size <= 1
                    onClick = { applySelectedHarmoniesToAllTracks(trackCard.index) }
                }
                +string(Strings.ApplyToAllTracksButton)
            }

            button {
                attrs {
                    style = Style(marginLeft = "8px", marginRight = "8px")
                    size = ButtonSize.small
                    color = Color.secondary
                    variant = ButtonVariant.outlined
                    onClick = { resetHarmonies(trackCard.index) }
                }
                +string(Strings.ResetAllButton)
            }
        }
    }

    private fun RBuilder.buildDivider() {
        styledDiv {
            css {
                marginTop = LinearDimension("8px")
                marginBottom = LinearDimension("24px")
                marginLeft = LinearDimension("16px")
                marginRight = LinearDimension("16px")
            }
            divider {
                attrs.light = true
                attrs.variant = DividerVariant.fullWidth
            }
        }
    }

    private fun onChangePassageStartInput(input: String, trackIndex: Int, passageIndex: Int, passages: List<Passage>) {
        val barIndex = input.toIntOrNull()?.minus(1)
        val barSize = state.trackCards[trackIndex].track.bars.size
        val thisPassage = passages[passageIndex]
        val lastPassage = if (passageIndex == 0) null else passages[passageIndex - 1]
        val minStart = lastPassage?.bars?.first()?.index?.plus(1) ?: 0
        val nextPassage = if (passageIndex + 1 > passages.lastIndex) null else passages[passageIndex + 1]
        val maxStart = if (lastPassage == null) 0 else {
            nextPassage?.bars?.first()?.index?.minus(1) ?: barSize - 1
        }
        updateTrack(trackIndex) { trackCard ->
            val errorInputs = trackCard.errorPassageStartInputs.toMutableMap()
            if (barIndex == null || barIndex !in minStart..maxStart) {
                errorInputs[passageIndex] = input
                trackCard.copy(errorPassageStartInputs = errorInputs.toMap())
            } else {
                val allBars = lastPassage?.bars.orEmpty() + thisPassage.bars
                val newLastPassage = lastPassage?.copy(bars = allBars.filter { it.index < barIndex })
                val newThisPassage = thisPassage.copy(bars = allBars.filter { it.index >= barIndex })
                val newPassages = passages.toMutableList()
                if (newLastPassage != null) newPassages[passageIndex - 1] = newLastPassage
                newPassages[passageIndex] = newThisPassage
                errorInputs.remove(passageIndex)
                trackCard.copy(
                    errorPassageStartInputs = errorInputs.toMap(),
                    track = trackCard.track.copy(passages = newPassages.toList())
                )
            }
        }
    }

    private fun addPassage(trackIndex: Int, passages: List<Passage>) {
        val finalPassage = passages.last()
        val updatedFinalPassage = finalPassage.copy(bars = finalPassage.bars.take(1))
        val newPassage = Passage(
            index = finalPassage.index + 1,
            bars = finalPassage.bars.drop(1)
        )
        val newPassages = passages.dropLast(1) + updatedFinalPassage + newPassage
        updateTrack(trackIndex) { trackCard ->
            trackCard.copy(
                track = trackCard.track.copy(passages = newPassages.toList())
            )
        }
    }

    private fun mergeLastTwoPassages(trackIndex: Int, passages: List<Passage>) {
        val lastTwoPassages = passages.takeLast(2)
        val newFinalPassage = lastTwoPassages.first().copy(
            bars = lastTwoPassages.first().bars + lastTwoPassages.last().bars
        )
        val newPassages = passages.dropLast(2) + newFinalPassage
        updateTrack(trackIndex) { trackCard ->
            trackCard.copy(
                track = trackCard.track.copy(passages = newPassages.toList())
            )
        }
    }

    private fun resetPassages(trackIndex: Int) {
        updateTrack(trackIndex) { trackCard ->
            trackCard.copy(
                track = trackCard.track.passagesInitialized()
            )
        }
    }

    private fun resetHarmonies(trackIndex: Int) {
        updateTrack(trackIndex) { trackCard ->
            trackCard.copy(
                track = trackCard.track.copy(harmonies = setOf())
            )
        }
    }

    private fun setTonality(tonalityText: String, trackIndex: Int, passages: List<Passage>, passageIndex: Int) {
        val tonality = Tonality.values().find { it.displayName == tonalityText }
        updateTrack(trackIndex) { trackCard ->
            trackCard.copy(
                track = trackCard.track.copy(
                    passages = passages.update<Passage>(passageIndex) {
                        it.copy(tonality = tonality)
                    }
                )
            )
        }
    }

    private fun selectHarmony(trackIndex: Int, harmonicType: HarmonicType, isSelected: Boolean) {
        updateTrack(trackIndex) { trackCard ->
            val newHarmonies = trackCard.track.harmonies.orEmpty().toMutableSet()
            if (isSelected) newHarmonies.add(harmonicType)
            else newHarmonies.remove(harmonicType)
            trackCard.copy(
                track = trackCard.track.copy(
                    harmonies = newHarmonies.toSet()
                )
            )
        }
    }

    private fun applyPassageSettingsToAllTracks(trackIndex: Int) {
        try {
            core.copyPassageSettingsToAllTracks(trackIndex)
        } catch (t: Throwable) {
            showErrorDialog(string(Strings.ApplyToAllTracksErrorTitle), t)
        }
        state.trackCards.indices.filter { it != trackIndex }.forEach {
            updateTrackUsingCoreData(it)
        }
    }

    private fun applySelectedHarmoniesToAllTracks(trackIndex: Int) {
        if (state.trackCards.any { !it.track.isTonalityMarked }) {
            showMessageBar(string(Strings.PassageNotSetMessageBar))
            return
        }
        val harmonies = state.trackCards[trackIndex].track.harmonies.orEmpty()
        state.trackCards.indices.filter { it != trackIndex }.forEach {
            updateTrack(it) { trackCard ->
                trackCard.copy(track = trackCard.track.copy(harmonies = harmonies))
            }
        }
    }

    private fun updateTrack(trackIndex: Int, updater: (TrackCardState) -> TrackCardState) {
        setState {
            trackCards = trackCards.update<TrackCardState>(trackIndex) {
                val newTrackCardState = updater.invoke(it)
                onUpdateTrack(trackIndex, newTrackCardState.track)
                newTrackCardState
            }
        }
    }

    private fun applyAutoTonality(trackIndex: Int) {
        try {
            val passages = requireNotNull(state.trackCards[trackIndex].track.passages)
            handleTonalityAnalysisResult(core.setPassagesSemiAuto(trackIndex, passages))
        } catch (t: Throwable) {
            showErrorDialog(string(Strings.AutoTonalityErrorTitle), t)
        }
        updateTrackUsingCoreData(trackIndex)
    }

    private fun applyAutoPassageDivision(trackIndex: Int) {
        try {
            handleTonalityAnalysisResult(core.setPassagesAuto(trackIndex))
        } catch (t: Throwable) {
            showErrorDialog(string(Strings.AutoPassageDivisionErrorTitle), t)
        }
        updateTrackUsingCoreData(trackIndex)
    }

    private fun handleTonalityAnalysisResult(result: TrackTonalityAnalysisResult) {
        when (result) {
            is TrackTonalityAnalysisResult.Failure -> showMessageBar(result.getMessage())
            is TrackTonalityAnalysisResult.Success -> {
                val warnings = result.passageResults.mapIndexedNotNull { index, passageResult ->
                    when (passageResult) {
                        is PassageTonalityAnalysisResult.Certain -> null
                        is PassageTonalityAnalysisResult.SimilarlyCertain -> string(
                            Strings.PassageAnalysisWarningSimilarlyCertain,
                            "number" to (index + 1).toString(),
                            "description" to passageResult.tonalities.joinToString("/") { it.displayName }
                        )
                        is PassageTonalityAnalysisResult.Unknown -> string(
                            Strings.PassageAnalysisWarningUnknown,
                            "number" to (index + 1).toString()
                        )
                    }
                }
                if (warnings.isNotEmpty()) {
                    showMessageBar(
                        (listOf(string(Strings.PassageAnalysisWarning)) + warnings).joinToString("\n")
                    )
                }
            }
        }
    }

    private fun updateTrackUsingCoreData(trackIndex: Int) {
        setState {
            trackCards = trackCards.update<TrackCardState>(trackIndex) {
                val newTrackCardState = it.copy(track = core.content.getTrack(trackIndex))
                onUpdateTrack(trackIndex, newTrackCardState.track, fromCore = true)
                newTrackCardState
            }
        }
    }

    private fun onUpdateTrack(trackIndex: Int, newTrack: Track, fromCore: Boolean = false) {
        if (!fromCore) {
            core.savePassages(trackIndex, requireNotNull(newTrack.passages))
            core.saveHarmonicTypes(trackIndex, newTrack.harmonies.orEmpty())
        }
        props.onUpdateProject(props.project.copy(content = core.content))
    }

    private fun TrackTonalityAnalysisResult.Failure.getMessage() = when (this) {
        TrackTonalityAnalysisResult.Failure.TrackIsTooShort -> string(Strings.TrackToShortMessage)
    }
}

external interface MainProcessorProps : Props {
    var project: Project
    var config: Config
    var onUpdateProject: (Project) -> Unit
    var onFinish: (ExportResult) -> Unit
}

external interface MainProcessorState : State {
    var isProcessing: Boolean
    var dialogError: DialogErrorState
    var snackbarError: SnackbarErrorState
    var trackCards: List<TrackCardState>
}

data class TrackCardState(
    val index: Int,
    val track: Track,
    val errorPassageStartInputs: Map<Int, String>,
    val errorPassageTonalityInputs: Map<Int, String>,
    val expanded: Expanded
) {
    enum class Expanded {
        None,
        MarkTonality,
        SelectHarmony
    }
}
