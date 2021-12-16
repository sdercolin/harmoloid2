@file:JsModule("@material-ui/core/Snackbar")
@file:JsNonModule

package ui.external.materialui

import react.ComponentClass
import react.Props

@JsName("default")
external val snackbar: ComponentClass<SnackbarProps>

external interface SnackbarProps : Props {
    var anchorOrigin: SnackbarAnchorOrigin
    var open: Boolean
    var autoHideDuration: Int
    var onClose: () -> Unit
}
