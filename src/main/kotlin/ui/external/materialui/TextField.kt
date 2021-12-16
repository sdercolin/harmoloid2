@file:JsModule("@material-ui/core/TextField")
@file:JsNonModule

package ui.external.materialui

import org.w3c.dom.events.Event
import react.ComponentClass
import react.Props

@JsName("default")
external val textField: ComponentClass<TextFieldProps>

external interface TextFieldProps : Props {
    var value: String
    var error: Boolean
    var id: String
    var variant: String
    var label: String
    var helperText: String?
    var onChange: (Event) -> Unit
    var type: String
    var style: Style
    var disabled: Boolean
    var select: Boolean
    var fullWidth: Boolean
    var size: String
}
