@file:JsModule("@material-ui/core/FormControlLabel")
@file:JsNonModule

package ui.external.materialui

import react.ComponentClass
import react.Props
import react.ReactElement

@JsName("default")
external val formControlLabel: ComponentClass<FormControlLabelProps>

external interface FormControlLabelProps : Props {
    var label: dynamic
    var control: ReactElement
    var labelPlacement: String
    var value: String
    var size: String
}
