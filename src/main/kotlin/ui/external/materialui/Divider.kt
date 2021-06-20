@file:JsModule("@material-ui/core/Divider")
@file:JsNonModule

package ui.external.materialui

import react.RClass
import react.RProps

@JsName("default")
external val divider: RClass<DividerProps>

external interface DividerProps : RProps {
    var light: Boolean
    var variant: String
}
