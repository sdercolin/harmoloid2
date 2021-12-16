@file:JsModule("@material-ui/core/Menu")
@file:JsNonModule

package ui.external.materialui

import org.w3c.dom.Element
import react.ComponentClass
import react.Props

@JsName("default")
external val menu: ComponentClass<MenuProps>

external interface MenuProps : Props {
    var onClose: () -> Unit
    var keepMounted: Boolean
    var open: Boolean
    var anchorEl: Element?
}
