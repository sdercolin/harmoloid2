@file:JsModule("@material-ui/core/FormGroup")
@file:JsNonModule

package ui.external.materialui

import react.ComponentClass
import react.Props

@JsName("default")
external val formGroup: ComponentClass<FormGroupProps>

external interface FormGroupProps : Props {
    var row: Boolean
}
