@file:JsModule("@material-ui/core/Divider")
@file:JsNonModule

package ui.external.materialui

import react.ComponentClass
import react.Props

@JsName("default")
external val divider: ComponentClass<DividerProps>

external interface DividerProps : Props {
    var light: Boolean
    var variant: String
}
