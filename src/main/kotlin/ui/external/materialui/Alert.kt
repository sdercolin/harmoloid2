@file:JsModule("@material-ui/lab/Alert")
@file:JsNonModule

package ui.external.materialui

import react.ComponentClass
import react.Props

@JsName("default")
external val alert: ComponentClass<AlertProps>

external interface AlertProps : Props {
    var severity: String
    var variant: String
    var style: Style
}
