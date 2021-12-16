@file:JsModule("@material-ui/core/Dialog")
@file:JsNonModule

package ui.external.materialui

import react.ComponentClass
import react.Props

@JsName("default")
external val dialog: ComponentClass<DialogProps>

external interface DialogProps : Props {
    var open: Boolean
    var onClose: () -> Unit
    var fullWidth: Boolean
    var maxWidth: String
}
