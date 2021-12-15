@file:JsModule("@material-ui/core/Tooltip")
@file:JsNonModule

package ui.external.materialui

import react.ComponentClass
import react.Props

@JsName("default")
external val tooltip: ComponentClass<TooltipProps>

external interface TooltipProps : Props {
    var title: String
    var interactive: Boolean
}
