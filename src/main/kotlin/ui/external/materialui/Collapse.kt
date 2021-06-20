@file:JsModule("@material-ui/core/Collapse")
@file:JsNonModule

package ui.external.materialui

import react.RClass
import react.RProps

@JsName("default")
external val collapse: RClass<CollapseProps>

external interface CollapseProps : RProps {
    var `in`: Boolean
    var unmountOnExit: Boolean
}
