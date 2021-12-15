@file:JsModule("@material-ui/core/FormControl")
@file:JsNonModule

package ui.external.materialui

import react.ComponentClass
import react.Props

@JsName("default")
external val formControl: ComponentClass<FormControlProps>

external interface FormControlProps : Props {
    var margin: String
    var disabled: Boolean
    var focused: Boolean
    var style: Style
    var size: String
}
