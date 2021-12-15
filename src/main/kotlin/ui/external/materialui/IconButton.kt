@file:JsModule("@material-ui/core/IconButton")
@file:JsNonModule

package ui.external.materialui

import org.w3c.dom.events.Event
import react.ComponentClass
import react.Props

@JsName("default")

external val iconButton: ComponentClass<IconButtonProps>

external interface IconButtonProps : Props {
    var onClick: (Event) -> Unit
    var disabled: Boolean
    var color: String
    var style: Style
}
