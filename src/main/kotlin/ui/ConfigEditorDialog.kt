package ui

import com.sdercolin.harmoloid.core.Config
import com.sdercolin.harmoloid.core.model.Solfege
import external.saveAs
import io.ConfigJson
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.css.LinearDimension
import kotlinx.css.margin
import mainScope
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import react.Props
import react.RBuilder
import react.StateSetter
import react.createElement
import react.dom.div
import react.fc
import react.useState
import styled.css
import styled.styledDiv
import ui.ConfigState.ConfigStateDiff
import ui.external.materialui.ButtonSize
import ui.external.materialui.ButtonVariant
import ui.external.materialui.Color
import ui.external.materialui.FormControlMargin
import ui.external.materialui.Icons
import ui.external.materialui.LabelPlacement
import ui.external.materialui.Severity
import ui.external.materialui.Style
import ui.external.materialui.TextFieldVariant
import ui.external.materialui.TypographyVariant
import ui.external.materialui.alert
import ui.external.materialui.button
import ui.external.materialui.checkbox
import ui.external.materialui.dialog
import ui.external.materialui.dialogActions
import ui.external.materialui.dialogContent
import ui.external.materialui.dialogTitle
import ui.external.materialui.formControl
import ui.external.materialui.formControlLabel
import ui.external.materialui.formGroup
import ui.external.materialui.iconButton
import ui.external.materialui.inputLabel
import ui.external.materialui.link
import ui.external.materialui.menuItem
import ui.external.materialui.select
import ui.external.materialui.textField
import ui.external.materialui.tooltip
import ui.external.materialui.typography
import ui.strings.Strings
import ui.strings.string
import util.readText
import util.waitFileSelection

fun RBuilder.configEditorDialog(
    isShowing: Boolean,
    close: () -> Unit,
    saveAndClose: (Config) -> Unit,
    currentConfig: Config
) = CONFIG_EDITOR_DIALOG.invoke {
    attrs.isShowing = isShowing
    attrs.close = close
    attrs.saveAndClose = saveAndClose
    attrs.currentConfig = currentConfig
}

val CONFIG_EDITOR_DIALOG = fc<ConfigEditorDialogProps>("ConfigEditorDialog") { props ->

    val (state, onChangeState) = useState(ConfigState.from(props.currentConfig))

    dialog {
        attrs {
            open = props.isShowing
            onClose = {
                props.close()
                onChangeState(ConfigState.from(props.currentConfig))
            }
            fullWidth = false
            maxWidth = "xl"
        }
        dialogTitle {
            +string(Strings.ConfigEditorDialogTitle)
            tooltip {
                attrs {
                    title = string(Strings.ImportConfigButton)
                    interactive = false
                }
                iconButton {
                    attrs {
                        style = Style(marginLeft = "40px", marginRight = "8px")
                        onClick = { uploadConfigFile(onChangeState) }
                    }
                    Icons.fetch { }
                }
            }
            tooltip {
                attrs {
                    title = string(Strings.ExportConfigButton)
                    interactive = false
                }
                iconButton {
                    attrs {
                        style = Style(marginLeft = "8px", marginRight = "8px")
                        onClick = { downloadConfigFile(state) }
                    }
                    Icons.publish { }
                }
            }
        }
        alert {
            attrs {
                severity = Severity.warning
                style = Style(borderRadius = "0px")
            }
            typography {
                attrs.variant = TypographyVariant.body2
                +string(Strings.ConfigEditorWarning)
                link {
                    attrs {
                        color = Color.inherit
                        href = "#"
                        onClick = { window.open(url = string(Strings.ConfigDescriptionUrl), target = "_blank") }
                    }
                    +string(Strings.ConfigEditorWarningLearnMore)
                }
            }
        }
        div {
            dialogContent {
                buildTextFieldItem(
                    name = string(Strings.ConfigParamMinLengthRatioOfNoteForValidBar),
                    state = state,
                    valueValidPair = state.minLengthRatioOfNoteForValidBar,
                    createDiff = { ConfigStateDiff(minLengthRatioOfNoteForValidBar = it) },
                    onChangeState = onChangeState
                )
                buildTextFieldItem(
                    name = string(Strings.ConfigParamMinProbabilityForCertainTonality),
                    state = state,
                    valueValidPair = state.minProbabilityForCertainTonality,
                    createDiff = { ConfigStateDiff(minProbabilityForCertainTonality = it) },
                    onChangeState = onChangeState
                )
                buildTextFieldItem(
                    name = string(Strings.ConfigParamMaxProbabilityDifferenceForSimilarlyCertainTonalities),
                    state = state,
                    valueValidPair = state.maxProbabilityDifferenceForSimilarlyCertainTonalities,
                    createDiff = { ConfigStateDiff(maxProbabilityDifferenceForSimilarlyCertainTonalities = it) },
                    onChangeState = onChangeState
                )
                buildTextFieldItem(
                    name = string(Strings.ConfigParamMinUncertaintyForInvalidAnalysisResult),
                    state = state,
                    valueValidPair = state.minUncertaintyForInvalidAnalysisResult,
                    createDiff = { ConfigStateDiff(minUncertaintyForInvalidAnalysisResult = it) },
                    onChangeState = onChangeState
                )
                buildTextFieldItem(
                    name = string(Strings.ConfigParamMinScoreForBarBelongingToPassage),
                    state = state,
                    valueValidPair = state.minScoreForBarBelongingToPassage,
                    createDiff = { ConfigStateDiff(minScoreForBarBelongingToPassage = it) },
                    onChangeState = onChangeState
                )
                buildTextFieldItem(
                    name = string(Strings.ConfigParamMinBarCountForPassageAutoDivision),
                    state = state,
                    valueValidPair = state.minBarCountForPassageAutoDivision,
                    createDiff = { ConfigStateDiff(minBarCountForPassageAutoDivision = it) },
                    onChangeState = onChangeState
                )
                buildKeyShiftSection(
                    name = string(Strings.ConfigParamKeyShiftForUpperThirdHarmony),
                    isPositiveShift = true,
                    controlIdPrefix = "upper",
                    state = state,
                    valueList = state.keyShiftForUpperThirdHarmony,
                    createDiff = { ConfigStateDiff(keyShiftForUpperThirdHarmony = it) },
                    onChangeState = onChangeState
                )
                buildKeyShiftSection(
                    name = string(Strings.ConfigParamKeyShiftForLowerThirdHarmony),
                    isPositiveShift = false,
                    controlIdPrefix = "lower",
                    state = state,
                    valueList = state.keyShiftForLowerThirdHarmony,
                    createDiff = { ConfigStateDiff(keyShiftForLowerThirdHarmony = it) },
                    onChangeState = onChangeState
                )
                buildValidSolfegeSection(
                    name = string(Strings.ConfigParamValidSolfegeSyllablesInOctave),
                    state = state,
                    valueList = state.validSolfegeSyllablesInOctave,
                    createDiff = { ConfigStateDiff(validSolfegeSyllablesInOctave = it) },
                    onChangeState = onChangeState
                )
            }
        }
        dialogActions {
            button {
                attrs.onClick = { props.close() }
                +string(Strings.CancelButton)
            }
            button {
                attrs.onClick = { onChangeState(ConfigState.from(Config())) }
                +string(Strings.ResetAllButton)
            }
            button {
                attrs {
                    color = Color.secondary
                    disabled = state.isValid.not()
                    onClick = { props.saveAndClose(state.toConfig()) }
                }
                +string(Strings.ConfirmButton)
            }
        }
    }
}

private fun RBuilder.buildTextFieldItem(
    name: String,
    state: ConfigState,
    valueValidPair: Pair<String, Boolean>,
    createDiff: (String) -> ConfigStateDiff,
    onChangeState: StateSetter<ConfigState>
) {
    div {
        formControlLabel {
            attrs {
                label = name
                control = createElement {
                    textField {
                        attrs {
                            value = valueValidPair.first
                            variant = TextFieldVariant.outlined
                            style = Style(
                                marginLeft = "24px",
                                marginTop = "12px",
                                marginBottom = "12px",
                                width = "100px"
                            )
                            size = "small"
                            error = valueValidPair.second.not()
                            onChange = {
                                val newValue = it.target.asDynamic().value as String
                                val diff = createDiff(newValue)
                                onChangeState(state.update(diff))
                            }
                        }
                    }
                    size = "small"
                    labelPlacement = LabelPlacement.start
                }!!
            }
        }
    }
}

private fun RBuilder.buildKeyShiftSection(
    name: String,
    isPositiveShift: Boolean,
    controlIdPrefix: String,
    state: ConfigState,
    valueList: List<Int>,
    createDiff: (List<Int>) -> ConfigStateDiff,
    onChangeState: StateSetter<ConfigState>
) {
    styledDiv {
        css {
            margin(top = LinearDimension("16px"))
        }
        formControlLabel {
            attrs {
                label = name
                control = createElement {
                    div {
                        button {
                            attrs {
                                size = ButtonSize.small
                                style = Style(marginLeft = "24px")
                                variant = ButtonVariant.outlined
                                onClick = {
                                    val diff = createDiff(
                                        if (isPositiveShift) Config.keyShiftForUpperThirdHarmonyDefault
                                        else Config.keyShiftForLowerThirdHarmonyDefault
                                    )
                                    onChangeState(state.update(diff))
                                }
                            }
                            +string(Strings.ConfigEditorUseDefaultButton)
                        }
                        button {
                            attrs {
                                size = ButtonSize.small
                                style = Style(marginLeft = "24px")
                                variant = ButtonVariant.outlined
                                onClick = {
                                    val diff = createDiff(
                                        if (isPositiveShift) Config.keyShiftForUpperThirdHarmonyStandard
                                        else Config.keyShiftForLowerThirdHarmonyStandard
                                    )
                                    onChangeState(state.update(diff))
                                }
                            }
                            +string(Strings.ConfigEditorUseStandardButton)
                        }
                    }
                    labelPlacement = LabelPlacement.start
                }!!
            }
        }
    }
    styledDiv {
        css {
            margin(
                left = LinearDimension("16px"),
                right = LinearDimension("16px"),
                bottom = LinearDimension("24px")
            )
        }
        formGroup {
            attrs.row = true
            for (index in Solfege.values().indices) {
                val controlId = "$controlIdPrefix-$index"
                val solfege = Solfege.values()[index]
                formControl {
                    attrs {
                        style = Style(width = "90px", marginLeft = "10px", marginRight = "10px")
                        margin = FormControlMargin.normal
                        focused = false
                        size = "small"
                    }
                    inputLabel {
                        attrs {
                            id = controlId
                            focused = false
                        }
                        +solfege.displayName
                    }
                    select {
                        attrs {
                            labelId = controlId
                            value = valueList[index].toString()
                            onChange = { event ->
                                val newValue = event.target.asDynamic().value as String
                                val newValueList = valueList.indices.map {
                                    if (it == index) newValue.toInt() else valueList[it]
                                }
                                val diff = createDiff(newValueList)
                                onChangeState(state.update(diff))
                            }
                        }
                        Solfege.values().indices.forEach { absDelta ->
                            val delta = if (isPositiveShift) absDelta else -absDelta
                            val targetSolfege = solfege.shift(delta)
                            val deltaDisplayName = if (delta >= 0) "+$delta" else "$delta"
                            menuItem {
                                attrs.value = delta.toString()
                                +"${targetSolfege.displayName}($deltaDisplayName)"
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun RBuilder.buildValidSolfegeSection(
    name: String,
    state: ConfigState,
    valueList: List<Boolean>,
    createDiff: (List<Boolean>) -> ConfigStateDiff,
    onChangeState: StateSetter<ConfigState>
) {
    styledDiv {
        css {
            margin(top = LinearDimension("16px"))
        }
        formControlLabel {
            attrs {
                label = name
                control = createElement {
                    div {
                        button {
                            attrs {
                                size = ButtonSize.small
                                style = Style(marginLeft = "24px")
                                variant = ButtonVariant.outlined
                                onClick = {
                                    val diff = createDiff(
                                        ConfigState.getValidSolfegeSyllablesInOctave(
                                            Config.validSolfegeSyllablesInOctaveDefault
                                        )
                                    )
                                    onChangeState(state.update(diff))
                                }
                            }
                            +string(Strings.ConfigEditorUseDefaultButton)
                        }
                    }
                }!!
                labelPlacement = LabelPlacement.start
            }
        }
    }
    styledDiv {
        css {
            margin(
                left = LinearDimension("16px"),
                right = LinearDimension("16px"),
                bottom = LinearDimension("24px")
            )
        }
        formGroup {
            attrs.row = true
            Solfege.values().forEach { solfege ->
                formControlLabel {
                    attrs {
                        label = solfege.displayName
                        control = createElement {
                            checkbox {
                                attrs {
                                    style = Style(marginLeft = "16px")
                                    checked = valueList[solfege.ordinal]
                                    onChange = { event ->
                                        val checked = event.target.asDynamic().checked as Boolean
                                        val newValueList = valueList.indices.map {
                                            if (it == solfege.ordinal) checked else valueList[it]
                                        }
                                        val diff = createDiff(newValueList)
                                        onChangeState(state.update(diff))
                                    }
                                }
                            }
                        }!!
                    }
                }
            }
        }
    }
}

private fun downloadConfigFile(state: ConfigState) {
    val config = state.toConfig()
    console.log("exporting config: $config")
    val content = ConfigJson.generate(config)
    val blog = Blob(arrayOf(content), BlobPropertyBag("text/json"))
    saveAs(blog, "harmoloid-config.json")
}

private fun uploadConfigFile(onChangeState: StateSetter<ConfigState>) {
    mainScope.launch {
        val file = waitFileSelection(accept = "json", multiple = false).firstOrNull() ?: return@launch
        val content = file.readText()
        val config = ConfigJson.parse(content)
        onChangeState(ConfigState.from(config))
    }
}

data class ConfigState(
    val minLengthRatioOfNoteForValidBar: Pair<String, Boolean>,
    val minProbabilityForCertainTonality: Pair<String, Boolean>,
    val maxProbabilityDifferenceForSimilarlyCertainTonalities: Pair<String, Boolean>,
    val minUncertaintyForInvalidAnalysisResult: Pair<String, Boolean>,
    val minScoreForBarBelongingToPassage: Pair<String, Boolean>,
    val minBarCountForPassageAutoDivision: Pair<String, Boolean>,
    val keyShiftForUpperThirdHarmony: List<Int>,
    val keyShiftForLowerThirdHarmony: List<Int>,
    val validSolfegeSyllablesInOctave: List<Boolean>
) {

    val isValid: Boolean
        get() = minLengthRatioOfNoteForValidBar.second &&
                minProbabilityForCertainTonality.second &&
                maxProbabilityDifferenceForSimilarlyCertainTonalities.second &&
                minUncertaintyForInvalidAnalysisResult.second &&
                minScoreForBarBelongingToPassage.second &&
                minBarCountForPassageAutoDivision.second

    data class ConfigStateDiff(
        val minLengthRatioOfNoteForValidBar: String? = null,
        val minProbabilityForCertainTonality: String? = null,
        val maxProbabilityDifferenceForSimilarlyCertainTonalities: String? = null,
        val minUncertaintyForInvalidAnalysisResult: String? = null,
        val minScoreForBarBelongingToPassage: String? = null,
        val minBarCountForPassageAutoDivision: String? = null,
        val keyShiftForUpperThirdHarmony: List<Int>? = null,
        val keyShiftForLowerThirdHarmony: List<Int>? = null,
        val validSolfegeSyllablesInOctave: List<Boolean>? = null
    )

    fun update(diff: ConfigStateDiff): ConfigState {
        var result = this
        if (diff.minLengthRatioOfNoteForValidBar != null) {
            val valid = diff.minLengthRatioOfNoteForValidBar.isValidDouble(0.0, 1.0)
            result = result.copy(minLengthRatioOfNoteForValidBar = diff.minLengthRatioOfNoteForValidBar to valid)
        }
        if (diff.minProbabilityForCertainTonality != null) {
            val valid = diff.minProbabilityForCertainTonality.isValidDouble(0.0, 1.0)
            result = result.copy(minProbabilityForCertainTonality = diff.minProbabilityForCertainTonality to valid)
        }
        if (diff.maxProbabilityDifferenceForSimilarlyCertainTonalities != null) {
            val valid = diff.maxProbabilityDifferenceForSimilarlyCertainTonalities.isValidDouble(0.0, 1.0)
            result = result.copy(
                maxProbabilityDifferenceForSimilarlyCertainTonalities =
                diff.maxProbabilityDifferenceForSimilarlyCertainTonalities to valid
            )
        }
        if (diff.minUncertaintyForInvalidAnalysisResult != null) {
            val valid = diff.minUncertaintyForInvalidAnalysisResult.isValidInt(0, 11)
            result = result.copy(
                minUncertaintyForInvalidAnalysisResult =
                diff.minUncertaintyForInvalidAnalysisResult to valid
            )
        }
        if (diff.minScoreForBarBelongingToPassage != null) {
            val valid = diff.minScoreForBarBelongingToPassage.isValidDouble(0.0, 1.0)
            result = result.copy(minScoreForBarBelongingToPassage = diff.minScoreForBarBelongingToPassage to valid)
        }
        if (diff.keyShiftForUpperThirdHarmony != null) {
            result = result.copy(keyShiftForUpperThirdHarmony = diff.keyShiftForUpperThirdHarmony)
        }
        if (diff.keyShiftForLowerThirdHarmony != null) {
            result = result.copy(keyShiftForLowerThirdHarmony = diff.keyShiftForLowerThirdHarmony)
        }
        if (diff.validSolfegeSyllablesInOctave != null) {
            result = result.copy(validSolfegeSyllablesInOctave = diff.validSolfegeSyllablesInOctave)
        }
        if (diff.minBarCountForPassageAutoDivision != null) {
            val valid = diff.minBarCountForPassageAutoDivision.isValidInt(1, 64)
            result = result.copy(minBarCountForPassageAutoDivision = diff.minBarCountForPassageAutoDivision to valid)
        }
        return result
    }

    private fun String.isValidDouble(min: Double, max: Double): Boolean {
        val double = toDoubleOrNull() ?: return false
        return double in min..max
    }

    private fun String.isValidInt(min: Int, max: Int): Boolean {
        val int = toIntOrNull() ?: return false
        return int in min..max
    }

    fun toConfig() = Config(
        minLengthRatioOfNoteForValidBar = minLengthRatioOfNoteForValidBar.first.toDouble(),
        minProbabilityForCertainTonality = minProbabilityForCertainTonality.first.toDouble(),
        maxProbabilityDifferenceForSimilarlyCertainTonalities =
        maxProbabilityDifferenceForSimilarlyCertainTonalities.first.toDouble(),
        minUncertaintyForInvalidAnalysisResult = minUncertaintyForInvalidAnalysisResult.first.toInt(),
        minScoreForBarBelongingToPassage = minScoreForBarBelongingToPassage.first.toDouble(),
        minBarCountForPassageAutoDivision = minBarCountForPassageAutoDivision.first.toInt(),
        keyShiftForLowerThirdHarmony = keyShiftForLowerThirdHarmony,
        keyShiftForUpperThirdHarmony = keyShiftForUpperThirdHarmony,
        validSolfegeSyllablesInOctave = (0..11).filter(validSolfegeSyllablesInOctave::get).toSet()
    )

    companion object {
        fun from(config: Config) = ConfigState(
            minLengthRatioOfNoteForValidBar = config.minLengthRatioOfNoteForValidBar.toString() to true,
            minProbabilityForCertainTonality = config.minProbabilityForCertainTonality.toString() to true,
            maxProbabilityDifferenceForSimilarlyCertainTonalities =
            config.maxProbabilityDifferenceForSimilarlyCertainTonalities.toString() to true,
            minUncertaintyForInvalidAnalysisResult = config.minUncertaintyForInvalidAnalysisResult.toString() to true,
            minScoreForBarBelongingToPassage = config.minScoreForBarBelongingToPassage.toString() to true,
            minBarCountForPassageAutoDivision = config.minBarCountForPassageAutoDivision.toString() to true,
            keyShiftForUpperThirdHarmony = config.keyShiftForUpperThirdHarmony,
            keyShiftForLowerThirdHarmony = config.keyShiftForLowerThirdHarmony,
            validSolfegeSyllablesInOctave = getValidSolfegeSyllablesInOctave(config.validSolfegeSyllablesInOctave)
        )

        fun getValidSolfegeSyllablesInOctave(rawValue: Set<Int>) = List(Solfege.values().size, rawValue::contains)
    }
}

external interface ConfigEditorDialogProps : Props {
    var isShowing: Boolean
    var close: () -> Unit
    var saveAndClose: (Config) -> Unit
    var currentConfig: Config
}
