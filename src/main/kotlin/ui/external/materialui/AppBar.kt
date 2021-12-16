@file:JsModule("@material-ui/core/AppBar")
@file:JsNonModule

package ui.external.materialui

import react.ComponentClass
import react.Props

@JsName("default")
external val appBar: ComponentClass<AppBarProps>

external interface AppBarProps : Props {
    var position: String
}
