@file:JsModule("@material-ui/core/Typography")
@file:JsNonModule

package ui.external.materialui

import react.ComponentClass
import react.Props

@JsName("default")
external val typography: ComponentClass<TypographyProps>

external interface TypographyProps : Props {
    var variant: String
    var component: String
    var color: String
    var style: Style
}
