package ui

import csstype.Color
import csstype.px
import emotion.react.css
import external.saveAs
import kotlinx.js.jso
import model.ExportResult
import mui.icons.material.Refresh
import mui.icons.material.SaveAlt
import mui.material.Button
import mui.material.ButtonColor
import mui.material.ButtonVariant
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import ui.strings.Strings
import ui.strings.string

val Exporter = FC<ExporterProps> { props ->

    fun download() {
        saveAs(props.result.blob, props.result.fileName)
    }

    div {
        css {
            marginTop = 40.px
            marginBottom = 40.px
        }
        Typography {
            variant = TypographyVariant.h3
            +(string(Strings.ExporterTitleSuccess))
        }
    }

    div {
        css {
            marginTop = 32.px
        }
        Button {
            variant = ButtonVariant.contained
            color = ButtonColor.secondary
            sx { backgroundColor = Color("#e0e0e0") }
            onClick = { download() }
            SaveAlt()
            div {
                css { padding = 8.px }
                +string(Strings.ExportButton)
            }
        }
        Button {
            style = jso { marginLeft = 16.px }
            variant = ButtonVariant.contained
            color = ButtonColor.primary
            onClick = { props.onRestart() }
            Refresh()
            div {
                css { padding = 8.px }
                +string(Strings.RestartButton)
            }
        }
    }
}

external interface ExporterProps : Props {
    var result: ExportResult
    var onRestart: () -> Unit
}
