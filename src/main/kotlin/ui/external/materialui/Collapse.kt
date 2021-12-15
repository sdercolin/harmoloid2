@file:JsModule("@material-ui/core/Collapse")
@file:JsNonModule

package ui.external.materialui

import react.ComponentClass
import react.Props

@JsName("default")
external val collapse: ComponentClass<CollapseProps>

external interface CollapseProps : Props {
    var `in`: Boolean
    var unmountOnExit: Boolean
}
