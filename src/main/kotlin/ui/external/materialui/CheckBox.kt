@file:JsModule("@material-ui/core/Checkbox")
@file:JsNonModule

package ui.external.materialui

import org.w3c.dom.events.Event
import react.ComponentClass
import react.Props

@JsName("default")
external val checkbox: ComponentClass<CheckboxProps>

external interface CheckboxProps : Props {
    var checked: Boolean
    var name: String
    var onChange: (Event) -> Unit
    var style: Style
}
