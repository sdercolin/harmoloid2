@file:JsModule("@material-ui/core/MenuItem")
@file:JsNonModule

package ui.external.materialui

import react.ComponentClass
import react.Props

@JsName("default")
external val menuItem: ComponentClass<MenuItemProps>

external interface MenuItemProps : Props {
    var onClick: () -> Unit
    var value: String
}
