package ui

import external.saveAs
import kotlinx.css.LinearDimension
import kotlinx.css.margin
import kotlinx.css.marginTop
import kotlinx.css.padding
import model.ExportResult
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import styled.css
import styled.styledDiv
import ui.external.materialui.ButtonVariant
import ui.external.materialui.Color
import ui.external.materialui.Icons
import ui.external.materialui.Style
import ui.external.materialui.TypographyVariant
import ui.external.materialui.button
import ui.external.materialui.typography
import ui.strings.Strings
import ui.strings.string

class Exporter : RComponent<ExporterProps, State>() {

    override fun RBuilder.render() {
        styledDiv {
            css {
                margin(vertical = LinearDimension("40px"))
            }
            typography {
                attrs {
                    variant = TypographyVariant.h3
                }
                +(string(Strings.ExporterTitleSuccess))
            }
        }
        buildButtons()
    }

    private fun RBuilder.buildButtons() {
        styledDiv {
            css {
                marginTop = LinearDimension("32px")
            }
            button {
                attrs {
                    variant = ButtonVariant.contained
                    onClick = {
                        download()
                    }
                }
                Icons.save {}
                styledDiv {
                    css {
                        padding = "8px"
                    }
                    +(string(Strings.ExportButton))
                }
            }
            button {
                attrs {
                    style = Style(marginLeft = "16px")
                    variant = ButtonVariant.contained
                    color = Color.primary
                    onClick = {
                        props.onRestart()
                    }
                }
                Icons.refresh {}
                styledDiv {
                    css {
                        padding = "8px"
                    }
                    +(string(Strings.RestartButton))
                }
            }
        }
    }

    private fun download() {
        saveAs(props.result.blob, props.result.fileName)
    }
}

external interface ExporterProps : Props {
    var result: ExportResult
    var onRestart: () -> Unit
}
