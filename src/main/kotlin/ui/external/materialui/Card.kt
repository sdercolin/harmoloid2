@file:JsModule("@material-ui/core/Card")
@file:JsNonModule

package ui.external.materialui

import react.RClass
import react.RProps

@JsName("default")
external val card: RClass<CardProps>

external interface CardProps : RProps {
    var variant: String
}
