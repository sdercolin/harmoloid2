@file:JsModule("@material-ui/core/IconButton")
@file:JsNonModule

package ui.external.materialui

import org.w3c.dom.events.Event
import react.RClass
import react.RProps

@JsName("default")

external val iconButton: RClass<IconButtonProps>

external interface IconButtonProps : RProps {
    var onClick: (Event) -> Unit
    var disabled: Boolean
    var color: String
    var style: Style
}
