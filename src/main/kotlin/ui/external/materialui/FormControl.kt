@file:JsModule("@material-ui/core/FormControl")
@file:JsNonModule

package ui.external.materialui

import react.RClass
import react.RProps

@JsName("default")
external val formControl: RClass<FormControlProps>

external interface FormControlProps : RProps {
    var margin: String
    var disabled: Boolean
    var focused: Boolean
    var style: Style
    var size: String
}
