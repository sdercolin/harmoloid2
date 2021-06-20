@file:JsModule("@material-ui/core/TextField")
@file:JsNonModule

package ui.external.materialui

import org.w3c.dom.events.Event
import react.RClass
import react.RProps

@JsName("default")
external val textField: RClass<TextFieldProps>

external interface TextFieldProps : RProps {
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
