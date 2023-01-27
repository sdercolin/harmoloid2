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
import csstype.AlignSelf
import csstype.Display
import csstype.FontSize
import csstype.VerticalAlign
import csstype.px
import kotlinx.js.jso
import model.ExportResult
import model.MainHandler
import model.Project
import mui.icons.material.AddCircle
import mui.icons.material.CheckCircle
import mui.icons.material.Edit
import mui.icons.material.RemoveCircle
import mui.material.AlertColor
import mui.material.BaseTextFieldProps
import mui.material.Button
import mui.material.ButtonColor
import mui.material.ButtonVariant
import mui.material.Card
import mui.material.CardActions
import mui.material.CardContent
import mui.material.Checkbox
import mui.material.CheckboxColor
import mui.material.Collapse
import mui.material.Divider
import mui.material.DividerVariant
import mui.material.FilledTextFieldProps
import mui.material.FormControlLabel
import mui.material.FormControlVariant
import mui.material.FormGroup
import mui.material.IconButton
import mui.material.IconButtonColor
import mui.material.MenuItem
import mui.material.OutlinedTextFieldProps
import mui.material.Size
import mui.material.TextField
import mui.material.Typography
import mui.material.styles.TypographyVariant
import react.ChildrenBuilder
import react.ElementType
import react.Props
import react.ReactNode
import react.create
import react.css.css
import react.dom.html.ReactHTML.div
import react.useEffect
import react.useState
import ui.TrackCardState.Expanded
import ui.common.DialogErrorState
import ui.common.SnackbarErrorState
import ui.common.errorDialog
import ui.common.messageBar
import ui.common.progress
import ui.common.scopedFC
import ui.strings.Strings
import ui.strings.string

val MainProcessor = scopedFC<MainProcessorProps> { props, scope ->

    var isProcessing: Boolean by useState(false)
    var dialogError: DialogErrorState by useState(DialogErrorState())
    var snackbarError: SnackbarErrorState by useState(SnackbarErrorState())

    fun showMessageBar(message: String) {
        snackbarError = SnackbarErrorState(true, message)
    }

    fun closeMessageBar() {
        snackbarError = snackbarError.copy(isShowing = false)
    }

    fun showErrorDialog(title: String, t: Throwable) {
        console.log(t)
        dialogError = DialogErrorState(
            isShowing = true,
            title = title,
            message = t.toString()
        )
    }

    fun closeErrorDialog() {
        dialogError = dialogError.copy(isShowing = false)
    }

    var nullableCore: Core? by useState {
        try {
            Core(props.project.content, props.config)
                .also { console.log("core is initialized with config: ${it.config}") }
        } catch (t: Throwable) {
            showErrorDialog(string(Strings.ImportErrorDialogTitle), t)
            null
        }
    }

    var trackCards: List<TrackCardState> by useState(emptyList())

    useEffect(nullableCore) {
        nullableCore?.let {
            trackCards = it.content.tracks.mapIndexed { index, track ->
                val expanded = if (index == 0) Expanded.MarkTonality
                else Expanded.None
                TrackCardState(index, track, mapOf(), mapOf(), expanded)
            }
        }
    }

    nullableCore?.let {
        props.handler.bind(
            core = it,
            onShowingProgress = { isProcessing = it },
            onShowingError = ::showErrorDialog,
            scope = scope,
            onFinish = props.onFinish,
            project = props.project
        )
    }

    fun toggleCollapse(trackIndex: Int, requestedExpanded: Expanded) {
        trackCards = trackCards.map {
            if (it.index == trackIndex) {
                it.copy(expanded = requestedExpanded)
            } else {
                if (requestedExpanded == Expanded.None) it
                else it.copy(expanded = Expanded.None)
            }
        }
    }

    val core = nullableCore ?: return@scopedFC

    val context = Context(
        core = core,
        trackCards = trackCards,
        setTrackCards = { trackCards = it },
        showMessageBar = ::showMessageBar,
        showError = ::showErrorDialog,
        project = props.project,
        onUpdateProject = props.onUpdateProject,
    )

    trackCards.forEach { trackState ->
        div {
            css {
                marginTop = 30.px
            }
            Card {
                CardContent {
                    Typography {
                        css {
                            color = appTheme.palette.secondary.main
                        }
                        variant = TypographyVariant.subtitle2
                        +string(Strings.TrackLabel, "number" to trackState.track.number.toString())
                    }
                    Typography {
                        variant = TypographyVariant.h5
                        +trackState.track.name
                    }
                    Typography {
                        variant = TypographyVariant.body2
                        component = "p".asDynamic().unsafeCast<ElementType<*>>()
                        +string(
                            Strings.TrackSummary,
                            "barCount" to trackState.track.bars.size.toString(),
                            "noteCount" to trackState.track.notes.size.toString()
                        )
                    }
                }
                if (trackState.track.bars.isNotEmpty()) {
                    CardActions {
                        Button {
                            color = ButtonColor.inherit
                            onClick = {
                                val requestedExpanded =
                                    if (trackState.expanded == Expanded.MarkTonality)
                                        Expanded.None
                                    else Expanded.MarkTonality
                                toggleCollapse(trackState.index, requestedExpanded)
                            }
                            if (trackState.track.isTonalityMarked) CheckCircle()
                            else Edit()
                            div {
                                css { marginLeft = 5.px }
                                +string(Strings.MarkTonalityButton)
                            }
                        }
                        Button {
                            color = ButtonColor.inherit
                            onClick = {
                                val requestedExpanded =
                                    if (trackState.expanded == Expanded.SelectHarmony)
                                        Expanded.None
                                    else Expanded.SelectHarmony
                                toggleCollapse(trackState.index, requestedExpanded)
                            }
                            disabled = !trackState.track.isTonalityMarked

                            if (trackState.track.harmonies?.isNotEmpty() == true) CheckCircle()
                            else Edit()
                            div {
                                css { marginLeft = 5.px }
                                +string(Strings.SelectHarmonyButton)
                            }
                        }
                    }
                    Collapse {
                        `in` = trackState.expanded != Expanded.None
                        // unmountOnExit = true
                        when (trackState.expanded) {
                            Expanded.None -> Unit
                            Expanded.MarkTonality -> buildTonalityMarking(context, trackState)
                            Expanded.SelectHarmony -> buildHarmonySelection(context, trackState)
                        }
                    }
                }
            }
        }
    }

    messageBar(
        isShowing = snackbarError.isShowing,
        message = snackbarError.message,
        close = { closeMessageBar() },
        color = AlertColor.error
    )

    errorDialog(
        state = dialogError,
        close = { closeErrorDialog() }
    )

    progress(isProcessing)
}

private fun ChildrenBuilder.buildTonalityMarking(
    context: Context,
    trackCard: TrackCardState,
) {
    val passages = trackCard.track.passages?.takeIf { it.isNotEmpty() } ?: return

    buildDivider()

    div {
        css {
            marginLeft = 24.px
            marginRight = 24.px
        }

        passages.forEach { passage ->

            div {
                css {
                    display = Display.flex
                    marginTop = 16.px
                    marginBottom = 16.px
                }

                Typography {
                    variant = TypographyVariant.subtitle2
                    component = "span".asDynamic().unsafeCast<ElementType<*>>()
                    style = jso {
                        width = 140.px
                        marginRight = 24.px
                        alignSelf = AlignSelf.center
                    }

                    +string(Strings.PassageLabel, "number" to passage.number.toString())
                }

                val isStartInputError = trackCard.errorPassageStartInputs.containsKey(passage.index)
                val isStartInputDisabled = passage.index == 0 ||
                        (!isStartInputError && trackCard.errorPassageStartInputs.isNotEmpty())
                val start = trackCard.errorPassageStartInputs[passage.index]
                    ?: passage.bars.firstOrNull()?.number?.toString()
                val end = passage.bars.lastOrNull()?.number?.toString()
                TextField {
                    value = start ?: ""
                    disabled = isStartInputDisabled
                    style = jso {
                        width = 140.px
                        marginRight = 24.px
                    }
                    error = isStartInputError
                    label = ReactNode(string(Strings.StartingBarLabel))
                    (this.unsafeCast<BaseTextFieldProps>()).variant = FormControlVariant.outlined
                    (this.unsafeCast<OutlinedTextFieldProps>()).onChange = { event ->
                        context.onChangePassageStartInput(
                            input = event.target.asDynamic().value as String,
                            trackIndex = trackCard.index,
                            passageIndex = passage.index,
                            passages = passages
                        )
                    }
                }
                TextField {
                    value = end ?: ""
                    (this.unsafeCast<BaseTextFieldProps>()).variant = FormControlVariant.outlined
                    disabled = true
                    style = jso {
                        width = 140.px
                        marginRight = 24.px
                    }
                    label = ReactNode(string(Strings.EndingBarLabel))
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

                TextField {
                    select = true
                    style = jso {
                        width = 180.px
                        marginRight = 24.px
                    }
                    label = ReactNode(string(Strings.TonalityLabel))
                    value = passage.tonality?.displayName.orEmpty()
                    error = optionText.isNotEmpty()
                    helperText = ReactNode(optionText)
                    (this.unsafeCast<BaseTextFieldProps>()).variant = FormControlVariant.filled
                    (this.unsafeCast<FilledTextFieldProps>()).onChange = { event ->
                        val value = event.target.asDynamic().value as String
                        context.setTonality(value, trackCard.index, passages, passage.index)
                    }
                    MenuItem {
                        value = ""
                        +"　"
                    }
                    Tonality.values().forEach {
                        MenuItem {
                            value = it.displayName
                            +it.displayName
                        }
                    }
                }

                val lyricsPreview = passage.notes.joinToString(" ") { it.lyric }.ifEmpty { "-" }
                TextField {
                    value = lyricsPreview
                    (this.unsafeCast<BaseTextFieldProps>()).variant = FormControlVariant.outlined
                    disabled = true
                    style = jso { marginRight = 64.px }
                    label = ReactNode(string(Strings.LyricsLabel))
                    fullWidth = true
                }
            }
        }
    }

    div {
        css {
            marginTop = 24.px
            marginLeft = 8.px
            marginBottom = 8.px
        }

        IconButton {
            color = IconButtonColor.secondary
            style = jso {
                marginLeft = 8.px
                marginRight = 8.px
            }
            disabled = passages.last().bars.size <= 1
            onClick = { context.addPassage(trackCard.index, passages) }
            AddCircle {
                style = jso {
                    fontSize = 20.px
                    verticalAlign = VerticalAlign.middle
                }
            }
        }

        IconButton {
            color = IconButtonColor.secondary
            style = jso {
                marginLeft = 8.px
                marginRight = 8.px
            }
            disabled = passages.size <= 1
            onClick = { context.mergeLastTwoPassages(trackCard.index, passages) }

            RemoveCircle {
                style = jso {
                    fontSize = 20.px
                    verticalAlign = VerticalAlign.middle
                }
            }
        }

        Button {
            style = jso {
                marginLeft = 8.px
                marginRight = 8.px
            }
            size = Size.small
            color = ButtonColor.secondary
            variant = ButtonVariant.outlined
            onClick = { context.applyAutoTonality(trackCard.index) }

            +string(Strings.AutoTonalityButton)
        }

        Button {
            style = jso {
                marginLeft = 8.px
                marginRight = 8.px
            }
            size = Size.small
            color = ButtonColor.secondary
            variant = ButtonVariant.outlined
            onClick = { context.applyAutoPassageDivision(trackCard.index) }

            +string(Strings.AutoPassageDivisionButton)
        }

        Button {
            style = jso {
                marginLeft = 8.px
                marginRight = 8.px
            }
            size = Size.small
            color = ButtonColor.secondary
            variant = ButtonVariant.outlined
            disabled = !trackCard.track.isTonalityMarked || context.trackCards.size <= 1
            onClick = { context.applyPassageSettingsToAllTracks(trackCard.index) }

            +string(Strings.ApplyToAllTracksButton)
        }

        Button {
            style = jso {
                marginLeft = 8.px
                marginRight = 8.px
            }
            size = Size.small
            color = ButtonColor.secondary
            variant = ButtonVariant.outlined
            onClick = { context.resetPassages(trackCard.index) }

            +string(Strings.ResetAllButton)
        }
    }
}

private fun ChildrenBuilder.buildHarmonySelection(
    context: Context,
    trackCard: TrackCardState,
) {
    val harmonies = trackCard.track.harmonies.orEmpty()

    buildDivider()

    div {
        css {
            marginLeft = 24.px
            marginRight = 24.px
        }

        FormGroup {
            row = true
            HarmonicType.values().forEach { harmony ->
                FormControlLabel {
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
                    ).let { ReactNode(it) }
                    control = Checkbox.create {
                        checked = harmonies.contains(harmony)
                        color = CheckboxColor.secondary
                        onChange = { event, _ ->
                            val checked = event.target.asDynamic().checked as Boolean
                            context.selectHarmony(trackCard.index, harmony, checked)
                        }
                    }
                }
            }
        }

        div {
            css {
                marginTop = 24.px
                marginLeft = 8.px
                marginBottom = 8.px
            }

            Button {
                style = jso {
                    marginLeft = 8.px
                    marginRight = 8.px
                }
                size = Size.small
                color = ButtonColor.secondary
                variant = ButtonVariant.outlined
                disabled = !trackCard.track.isTonalityMarked || context.trackCards.size <= 1
                onClick = { context.applySelectedHarmoniesToAllTracks(trackCard.index) }
                +string(Strings.ApplyToAllTracksButton)
            }

            Button {
                style = jso {
                    marginLeft = 8.px
                    marginRight = 8.px
                }
                size = Size.small
                color = ButtonColor.secondary
                variant = ButtonVariant.outlined
                onClick = { context.resetHarmonies(trackCard.index) }
                +string(Strings.ResetAllButton)
            }
        }
    }
}

private fun ChildrenBuilder.buildDivider() {
    div {
        css {
            marginTop = 8.px
            marginBottom = 24.px
            marginLeft = 16.px
            marginRight = 16.px
        }
        Divider {
            light = true
            variant = DividerVariant.fullWidth
        }
    }
}

private fun Context.onChangePassageStartInput(
    input: String,
    trackIndex: Int,
    passageIndex: Int,
    passages: List<Passage>
) {
    val barIndex = input.toIntOrNull()?.minus(1)
    val barSize = trackCards[trackIndex].track.bars.size
    val thisPassage = passages[passageIndex]
    val lastPassage = if (passageIndex == 0) null else passages[passageIndex - 1]
    val minStart = lastPassage?.bars?.first()?.index?.plus(1) ?: 0
    val nextPassage = if (passageIndex + 1 > passages.lastIndex) null else passages[passageIndex + 1]
    val maxStart = if (lastPassage == null) 0 else {
        nextPassage?.bars?.first()?.index?.minus(1) ?: (barSize - 1)
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

private fun Context.addPassage(trackIndex: Int, passages: List<Passage>) {
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

private fun Context.mergeLastTwoPassages(trackIndex: Int, passages: List<Passage>) {
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

private fun Context.resetPassages(trackIndex: Int) {
    updateTrack(trackIndex) { trackCard ->
        trackCard.copy(
            track = trackCard.track.passagesInitialized()
        )
    }
}

private fun Context.resetHarmonies(trackIndex: Int) {
    updateTrack(trackIndex) { trackCard ->
        trackCard.copy(
            track = trackCard.track.copy(harmonies = setOf())
        )
    }
}

private fun Context.setTonality(
    tonalityText: String,
    trackIndex: Int,
    passages: List<Passage>,
    passageIndex: Int
) {
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

private fun Context.selectHarmony(trackIndex: Int, harmonicType: HarmonicType, isSelected: Boolean) {
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

private fun Context.applyPassageSettingsToAllTracks(trackIndex: Int) {
    try {
        core.copyPassageSettingsToAllTracks(trackIndex)
    } catch (t: Throwable) {
        showError(string(Strings.ApplyToAllTracksErrorTitle), t)
    }
    trackCards.indices.filter { it != trackIndex }.forEach {
        updateTrackUsingCoreData(it)
    }
}

private fun Context.applySelectedHarmoniesToAllTracks(trackIndex: Int) {
    if (trackCards.any { it.track.bars.isNotEmpty() && !it.track.isTonalityMarked }) {
        showMessageBar(string(Strings.PassageNotSetMessageBar))
        return
    }
    val harmonies = trackCards[trackIndex].track.harmonies.orEmpty()
    trackCards.indices.filter { it != trackIndex }.forEach {
        updateTrack(it) { trackCard ->
            if (trackCard.track.bars.isEmpty()) trackCard
            else trackCard.copy(track = trackCard.track.copy(harmonies = harmonies))
        }
    }
}

private fun Context.updateTrack(trackIndex: Int, updater: (TrackCardState) -> TrackCardState) {
    val newTrackCards = trackCards.update<TrackCardState>(trackIndex) {
        val newTrackCardState = updater.invoke(it)
        onUpdateTrack(trackIndex, newTrackCardState.track)
        newTrackCardState
    }
    setTrackCards(newTrackCards)
}

private fun Context.applyAutoTonality(trackIndex: Int) {
    try {
        val passages = requireNotNull(trackCards[trackIndex].track.passages)
        handleTonalityAnalysisResult(core.setPassagesSemiAuto(trackIndex, passages))
    } catch (t: Throwable) {
        showError(string(Strings.AutoTonalityErrorTitle), t)
    }
    updateTrackUsingCoreData(trackIndex)
}

private fun Context.applyAutoPassageDivision(trackIndex: Int) {
    try {
        handleTonalityAnalysisResult(core.setPassagesAuto(trackIndex))
    } catch (t: Throwable) {
        showError(string(Strings.AutoPassageDivisionErrorTitle), t)
    }
    updateTrackUsingCoreData(trackIndex)
}

private fun Context.handleTonalityAnalysisResult(result: TrackTonalityAnalysisResult) {
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

private fun Context.updateTrackUsingCoreData(trackIndex: Int) {
    val newTrackCards = trackCards.update<TrackCardState>(trackIndex) {
        val newTrackCardState = it.copy(track = core.content.getTrack(trackIndex))
        onUpdateTrack(trackIndex, newTrackCardState.track, fromCore = true)
        newTrackCardState
    }
    setTrackCards(newTrackCards)
}

private fun Context.onUpdateTrack(
    trackIndex: Int,
    newTrack: Track,
    fromCore: Boolean = false
) {
    if (!fromCore) {
        core.savePassages(trackIndex, requireNotNull(newTrack.passages))
        core.saveHarmonicTypes(trackIndex, newTrack.harmonies.orEmpty())
    }
    onUpdateProject(project.copy(content = core.content))
}

private fun TrackTonalityAnalysisResult.Failure.getMessage() = when (this) {
    TrackTonalityAnalysisResult.Failure.TrackIsTooShort -> string(Strings.TrackToShortMessage)
}


external interface MainProcessorProps : Props {
    var handler: MainHandler
    var project: Project
    var config: Config
    var onUpdateProject: (Project) -> Unit
    var onFinish: (ExportResult) -> Unit
}

private data class TrackCardState(
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

private class Context(
    val core: Core,
    val trackCards: List<TrackCardState>,
    val setTrackCards: (List<TrackCardState>) -> Unit,
    val project: Project,
    val onUpdateProject: (Project) -> Unit,
    val showMessageBar: (String) -> Unit,
    val showError: (String, Throwable) -> Unit
)
