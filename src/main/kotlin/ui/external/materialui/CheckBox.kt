@file:JsModule("@material-ui/core/Checkbox")
@file:JsNonModule

package ui.external.materialui

import org.w3c.dom.events.Event
import react.RClass
import react.RProps

@JsName("default")
external val checkbox: RClass<CheckboxProps>

external interface CheckboxProps : RProps {
    var checked: Boolean
    var name: String
    var onChange: (Event) -> Unit
    var style: Style
}
