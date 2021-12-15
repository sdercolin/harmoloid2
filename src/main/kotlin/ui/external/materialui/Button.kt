@file:JsModule("@material-ui/core/Button")
@file:JsNonModule

package ui.external.materialui

import org.w3c.dom.events.Event
import react.ComponentClass
import react.Props

@JsName("default")
external val button: ComponentClass<ButtonProps>

external interface ButtonProps : Props {
    var onClick: (Event) -> Unit
    var color: String
    var size: String
    var variant: String
    var disabled: Boolean
    var style: Style
}
