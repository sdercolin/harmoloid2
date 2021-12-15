@file:JsModule("@material-ui/core/Fab")
@file:JsNonModule

package ui.external.materialui

import org.w3c.dom.events.Event
import react.ComponentClass
import react.Props

@JsName("default")
external val fab: ComponentClass<FabProps>

external interface FabProps : Props {
    var onClick: (Event) -> Unit
    var color: String
    var size: String
    var style: Style
    var disabled: Boolean
}
