@file:JsModule("@material-ui/core/Container")
@file:JsNonModule

package ui.external.materialui

import react.ComponentClass
import react.Props

@JsName("default")
external val container: ComponentClass<ContainerProps>

external interface ContainerProps : Props {
    var maxWidth: dynamic
}
