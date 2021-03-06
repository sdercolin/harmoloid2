package ui

import kotlinx.css.LinearDimension
import kotlinx.css.TextAlign
import kotlinx.css.color
import kotlinx.css.marginBottom
import kotlinx.css.marginTop
import kotlinx.css.textAlign
import kotlinx.css.width
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import styled.css
import styled.styledFooter
import ui.external.materialui.Color
import ui.external.materialui.TypographyVariant
import ui.external.materialui.link
import ui.external.materialui.typography
import ui.strings.Strings
import ui.strings.string

class CustomFooter : RComponent<Props, State>() {
    override fun RBuilder.render() {
        styledFooter {
            css {
                width = LinearDimension("100%")
                textAlign = TextAlign.center
                color = kotlinx.css.Color.grey
                marginBottom = LinearDimension("15px")
                marginTop = LinearDimension("5px")
            }
            typography {
                attrs {
                    variant = TypographyVariant.body2
                    color = Color.inherit
                }
                +"HARMOLOID © 2014 - 2022　|　"
                link {
                    attrs {
                        href = "https://github.com/sdercolin/harmoloid2"
                        target = "_blank"
                        color = Color.inherit
                    }
                    +"GitHub"
                }
                +"　|　"
                link {
                    attrs {
                        href = "https://discord.gg/TyEcQ6P73y"
                        target = "_blank"
                        color = Color.inherit
                    }
                    +"Discord"
                }
                +"　|　"
                link {
                    attrs {
                        href = string(Strings.ReleaseNotesUrl)
                        target = "_blank"
                        color = Color.inherit
                    }
                    +"Release Notes"
                }
            }
        }
    }
}
