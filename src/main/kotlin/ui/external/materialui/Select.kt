@file:JsModule("@material-ui/core/Select")
@file:JsNonModule

package ui.external.materialui

import org.w3c.dom.events.Event
import react.ComponentClass
import react.Props

@JsName("default")
external val select: ComponentClass<SelectProps>

external interface SelectProps : Props {
    var onChange: (Event) -> Unit
    var value: String
    var labelId: String
}
