@file:JsModule("@material-ui/core/Backdrop")
@file:JsNonModule

package ui.external.materialui

import react.ComponentClass
import react.Props

@JsName("default")
external val backdrop: ComponentClass<BackdropProps>

external interface BackdropProps : Props {
    var open: Boolean
    var style: Style
}
