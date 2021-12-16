@file:JsModule("@material-ui/core/InputLabel")
@file:JsNonModule

package ui.external.materialui

import react.ComponentClass
import react.Props

@JsName("default")
external val inputLabel: ComponentClass<InputLabelProps>

external interface InputLabelProps : Props {
    var id: String
    var style: Style
    var focused: Boolean
}
