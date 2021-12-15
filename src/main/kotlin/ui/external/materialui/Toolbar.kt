@file:JsModule("@material-ui/core/Toolbar")
@file:JsNonModule

package ui.external.materialui

import react.ComponentClass
import react.Props

@JsName("default")
external val toolbar: ComponentClass<ToolbarProps>

external interface ToolbarProps : Props
