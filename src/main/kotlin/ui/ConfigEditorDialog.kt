package ui

import com.sdercolin.harmoloid.core.Config
import com.sdercolin.harmoloid.core.model.Solfege
import csstype.Color
import csstype.px
import emotion.react.css
import external.saveAs
import io.ConfigJson
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.js.jso
import mui.icons.material.GetApp
import mui.icons.material.Publish
import mui.material.Alert
import mui.material.AlertColor
import mui.material.BaseTextFieldProps
import mui.material.Button
import mui.material.ButtonColor
import mui.material.ButtonVariant
import mui.material.Checkbox
import mui.material.CheckboxColor
import mui.material.Dialog
import mui.material.DialogActions
import mui.material.DialogContent
import mui.material.DialogTitle
import mui.material.FormControl
import mui.material.FormControlLabel
import mui.material.FormControlMargin
import mui.material.FormControlVariant
import mui.material.FormGroup
import mui.material.FormLabel
import mui.material.IconButton
import mui.material.LabelPlacement
import mui.material.Link
import mui.material.MenuItem
import mui.material.OutlinedTextFieldProps
import mui.material.Paper
import mui.material.Size
import mui.material.StandardTextFieldProps
import mui.material.TextField
import mui.material.Tooltip
import mui.material.Typography
import mui.material.styles.TypographyVariant
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import react.ChildrenBuilder
import react.FC
import react.Props
import react.ReactNode
import react.StateSetter
import react.create
import react.dom.html.ReactHTML.div
import react.useState
import ui.ConfigState.ConfigStateDiff
import ui.strings.Strings
import ui.strings.string
import util.readText
import util.waitFileSelection

val ConfigEditorDialog = FC<ConfigEditorDialogProps>("ConfigEditorDialog") { props ->

    val (state, onChangeState) = useState(ConfigState.from(props.currentConfig))

    Dialog {
        open = props.isShowing
        onClose = { _, _ ->
            props.close()
            onChangeState(ConfigState.from(props.currentConfig))
        }
        fullWidth = false
        maxWidth = "xl"
        Paper {
            style = jso {
                background = Color("#424242")
            }
            DialogTitle {
                +string(Strings.ConfigEditorDialogTitle)
                Tooltip {
                    title = ReactNode(string(Strings.ImportConfigButton))
                    disableInteractive = true
                    IconButton {
                        style = jso {
                            marginLeft = 40.px
                            marginRight = 8.px
                        }
                        onClick = { uploadConfigFile(onChangeState) }

                        GetApp()
                    }
                }
                Tooltip {
                    title = ReactNode(string(Strings.ExportConfigButton))
                    disableInteractive = true
                    IconButton {
                        style = jso {
                            marginLeft = 9.px
                            marginRight = 8.px
                        }
                        onClick = { downloadConfigFile(state) }
                        Publish()
                    }
                }
            }
            Alert {
                severity = AlertColor.warning
                style = jso { borderRadius = 0.px }
                Typography {
                    variant = TypographyVariant.body2
                    +string(Strings.ConfigEditorWarning)
                    Link {
                        color = appTheme.palette.warning.main
                        href = "#"
                        onClick = { window.open(url = string(Strings.ConfigDescriptionUrl), target = "_blank") }
                        +string(Strings.ConfigEditorWarningLearnMore)
                    }
                }
            }
            div {
                DialogContent {
                    buildTextFieldItem(
                        name = string(Strings.ConfigParamMinLengthRatioOfNoteForValidBar),
                        state = state,
                        valueValidPair = state.minLengthRatioOfNoteForValidBar,
                        createDiff = { ConfigStateDiff(minLengthRatioOfNoteForValidBar = it) },
                        onChangeState = onChangeState,
                    )
                    buildTextFieldItem(
                        name = string(Strings.ConfigParamMinProbabilityForCertainTonality),
                        state = state,
                        valueValidPair = state.minProbabilityForCertainTonality,
                        createDiff = { ConfigStateDiff(minProbabilityForCertainTonality = it) },
                        onChangeState = onChangeState,
                    )
                    buildTextFieldItem(
                        name = string(Strings.ConfigParamMaxProbabilityDifferenceForSimilarlyCertainTonalities),
                        state = state,
                        valueValidPair = state.maxProbabilityDifferenceForSimilarlyCertainTonalities,
                        createDiff = { ConfigStateDiff(maxProbabilityDifferenceForSimilarlyCertainTonalities = it) },
                        onChangeState = onChangeState,
                    )
                    buildTextFieldItem(
                        name = string(Strings.ConfigParamMinUncertaintyForInvalidAnalysisResult),
                        state = state,
                        valueValidPair = state.minUncertaintyForInvalidAnalysisResult,
                        createDiff = { ConfigStateDiff(minUncertaintyForInvalidAnalysisResult = it) },
                        onChangeState = onChangeState,
                    )
                    buildTextFieldItem(
                        name = string(Strings.ConfigParamMinScoreForBarBelongingToPassage),
                        state = state,
                        valueValidPair = state.minScoreForBarBelongingToPassage,
                        createDiff = { ConfigStateDiff(minScoreForBarBelongingToPassage = it) },
                        onChangeState = onChangeState,
                    )
                    buildTextFieldItem(
                        name = string(Strings.ConfigParamMinBarCountForPassageAutoDivision),
                        state = state,
                        valueValidPair = state.minBarCountForPassageAutoDivision,
                        createDiff = { ConfigStateDiff(minBarCountForPassageAutoDivision = it) },
                        onChangeState = onChangeState,
                    )
                    buildKeyShiftSection(
                        name = string(Strings.ConfigParamKeyShiftForUpperThirdHarmony),
                        isPositiveShift = true,
                        controlIdPrefix = "upper",
                        state = state,
                        valueList = state.keyShiftForUpperThirdHarmony,
                        createDiff = { ConfigStateDiff(keyShiftForUpperThirdHarmony = it) },
                        onChangeState = onChangeState,
                    )
                    buildKeyShiftSection(
                        name = string(Strings.ConfigParamKeyShiftForLowerThirdHarmony),
                        isPositiveShift = false,
                        controlIdPrefix = "lower",
                        state = state,
                        valueList = state.keyShiftForLowerThirdHarmony,
                        createDiff = { ConfigStateDiff(keyShiftForLowerThirdHarmony = it) },
                        onChangeState = onChangeState,
                    )
                    buildValidSolfegeSection(
                        name = string(Strings.ConfigParamValidSolfegeSyllablesInOctave),
                        state = state,
                        valueList = state.validSolfegeSyllablesInOctave,
                        createDiff = { ConfigStateDiff(validSolfegeSyllablesInOctave = it) },
                        onChangeState = onChangeState,
                    )
                }
            }
            DialogActions {
                Button {
                    color = ButtonColor.inherit
                    onClick = { props.close() }
                    +string(Strings.CancelButton)
                }
                Button {
                    color = ButtonColor.inherit
                    onClick = { onChangeState(ConfigState.from(Config())) }
                    +string(Strings.ResetAllButton)
                }
                Button {
                    color = ButtonColor.secondary
                    disabled = state.isValid.not()
                    onClick = { props.saveAndClose(state.toConfig()) }
                    +string(Strings.ConfirmButton)
                }
            }
        }
    }
}

private fun ChildrenBuilder.buildTextFieldItem(
    name: String,
    state: ConfigState,
    valueValidPair: Pair<String, Boolean>,
    createDiff: (String) -> ConfigStateDiff,
    onChangeState: StateSetter<ConfigState>,
) {
    div {
        FormControlLabel {
            label = ReactNode(name)
            control = TextField.create {
                value = valueValidPair.first
                variant = FormControlVariant.outlined
                style = jso {
                    marginLeft = 24.px
                    marginTop = 12.px
                    marginBottom = 12.px
                    width = 100.px
                }
                size = Size.small
                error = valueValidPair.second.not()
                (this.unsafeCast<BaseTextFieldProps>()).variant = FormControlVariant.outlined
                (this.unsafeCast<OutlinedTextFieldProps>()).onChange = { event ->
                    val value = event.target.asDynamic().value as String
                    val diff = createDiff(value)
                    onChangeState(state.update(diff))
                }
            }
            labelPlacement = LabelPlacement.start
        }
    }
}

private fun ChildrenBuilder.buildKeyShiftSection(
    name: String,
    isPositiveShift: Boolean,
    controlIdPrefix: String,
    state: ConfigState,
    valueList: List<Int>,
    createDiff: (List<Int>) -> ConfigStateDiff,
    onChangeState: StateSetter<ConfigState>,
) {
    div {
        css {
            marginTop = 16.px
        }
        FormControlLabel {
            label = ReactNode(name)
            control = div.create {
                Button {
                    size = Size.small
                    style = jso { marginLeft = 24.px }
                    color = ButtonColor.inherit
                    variant = ButtonVariant.outlined
                    onClick = {
                        val diff = createDiff(
                            if (isPositiveShift) Config.keyShiftForUpperThirdHarmonyDefault
                            else Config.keyShiftForLowerThirdHarmonyDefault,
                        )
                        onChangeState(state.update(diff))
                    }
                    +string(Strings.ConfigEditorUseDefaultButton)
                }
                Button {
                    size = Size.small
                    style = jso { marginLeft = 24.px }
                    color = ButtonColor.inherit
                    variant = ButtonVariant.outlined
                    onClick = {
                        val diff = createDiff(
                            if (isPositiveShift) Config.keyShiftForUpperThirdHarmonyStandard
                            else Config.keyShiftForLowerThirdHarmonyStandard,
                        )
                        onChangeState(state.update(diff))
                    }
                    +string(Strings.ConfigEditorUseStandardButton)
                }
            }
            labelPlacement = LabelPlacement.start
        }
    }
    div {
        css {
            marginLeft = 16.px
            marginRight = 16.px
            marginBottom = 24.px
        }
        FormGroup {
            row = true
            for (index in Solfege.values().indices) {
                val controlId = "$controlIdPrefix-$index"
                val solfege = Solfege.values()[index]
                FormControl {
                    style = jso {
                        width = 90.px
                        marginLeft = 10.px
                        marginRight = 10.px
                    }
                    margin = FormControlMargin.normal
                    variant = FormControlVariant.standard
                    focused = false
                    size = Size.small
                    FormLabel {
                        id = controlId
                        focused = false
                        Typography {
                            variant = TypographyVariant.caption
                            +solfege.displayName
                        }
                    }
                    TextField {
                        id = controlId
                        select = true
                        value = valueList[index].toString().unsafeCast<Nothing?>()
                        (this.unsafeCast<BaseTextFieldProps>()).variant = FormControlVariant.standard
                        (this.unsafeCast<StandardTextFieldProps>()).onChange = { event ->
                            val newValue = event.target.asDynamic().value as String
                            val newValueList = valueList.indices.map {
                                if (it == index) newValue.toInt() else valueList[it]
                            }
                            val diff = createDiff(newValueList)
                            onChangeState(state.update(diff))
                        }
                        Solfege.values().indices.forEach { absDelta ->
                            val delta = if (isPositiveShift) absDelta else -absDelta
                            val targetSolfege = solfege.shift(delta)
                            val deltaDisplayName = if (delta >= 0) "+$delta" else "$delta"
                            MenuItem {
                                value = delta.toString()
                                +"${targetSolfege.displayName}($deltaDisplayName)"
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun ChildrenBuilder.buildValidSolfegeSection(
    name: String,
    state: ConfigState,
    valueList: List<Boolean>,
    createDiff: (List<Boolean>) -> ConfigStateDiff,
    onChangeState: StateSetter<ConfigState>,
) {
    div {
        css {
            marginTop = 16.px
        }
        FormControlLabel {
            label = ReactNode(name)
            control = Button.create {
                color = ButtonColor.inherit
                size = Size.small
                style = jso { marginLeft = 24.px }
                variant = ButtonVariant.outlined
                onClick = {
                    val diff = createDiff(
                        ConfigState.getValidSolfegeSyllablesInOctave(
                            Config.validSolfegeSyllablesInOctaveDefault,
                        ),
                    )
                    onChangeState(state.update(diff))
                }
                +string(Strings.ConfigEditorUseDefaultButton)
            }
            labelPlacement = LabelPlacement.start
        }
    }
    div {
        css {
            marginLeft = 16.px
            marginRight = 16.px
            marginBottom = 24.px
        }
        FormGroup {
            row = true
            Solfege.values().forEach { solfege ->
                FormControlLabel {
                    label = ReactNode(solfege.displayName)
                    control = Checkbox.create {
                        color = CheckboxColor.secondary
                        style = jso {
                            marginLeft = 16.px
                        }
                        checked = valueList[solfege.ordinal]
                        onChange = { event, _ ->
                            val checked = event.target.checked
                            val newValueList = valueList.indices.map {
                                if (it == solfege.ordinal) checked else valueList[it]
                            }
                            val diff = createDiff(newValueList)
                            onChangeState(state.update(diff))
                        }
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
    MainScope().launch {
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
    val validSolfegeSyllablesInOctave: List<Boolean>,
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
        val validSolfegeSyllablesInOctave: List<Boolean>? = null,
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
                diff.maxProbabilityDifferenceForSimilarlyCertainTonalities to valid,
            )
        }
        if (diff.minUncertaintyForInvalidAnalysisResult != null) {
            val valid = diff.minUncertaintyForInvalidAnalysisResult.isValidInt(0, 11)
            result = result.copy(
                minUncertaintyForInvalidAnalysisResult =
                diff.minUncertaintyForInvalidAnalysisResult to valid,
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
        validSolfegeSyllablesInOctave = (0..11).filter(validSolfegeSyllablesInOctave::get).toSet(),
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
            validSolfegeSyllablesInOctave = getValidSolfegeSyllablesInOctave(config.validSolfegeSyllablesInOctave),
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
