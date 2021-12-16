@file:JsModule("@material-ui/core/Card")
@file:JsNonModule

package ui.external.materialui

import react.ComponentClass
import react.Props

@JsName("default")
external val card: ComponentClass<CardProps>

external interface CardProps : Props {
    var variant: String
}
