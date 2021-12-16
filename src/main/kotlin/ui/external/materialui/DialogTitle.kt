@file:JsModule("@material-ui/core/DialogTitle")
@file:JsNonModule

package ui.external.materialui

import react.ComponentClass
import react.Props

@JsName("default")
external val dialogTitle: ComponentClass<DialogTitleProps>

external interface DialogTitleProps : Props {
    var variant: String
}
