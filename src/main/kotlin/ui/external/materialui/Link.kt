@file:JsModule("@material-ui/core/Link")
@file:JsNonModule

package ui.external.materialui

import react.ComponentClass
import react.Props

@JsName("default")
external val link: ComponentClass<LinkProps>

external interface LinkProps : Props {
    var href: String
    var target: String
    var color: String
    var onClick: () -> Unit
}
