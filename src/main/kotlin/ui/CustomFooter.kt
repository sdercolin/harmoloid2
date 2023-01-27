package ui

import csstype.NamedColor
import emotion.react.css
import mui.material.Link
import mui.material.LinkUnderline
import mui.material.Typography
import mui.material.TypographyAlign
import mui.material.styles.TypographyVariant
import react.FC
import react.Props
import react.dom.html.AnchorTarget
import react.dom.html.ReactHTML
import ui.strings.Strings
import ui.strings.string

val CustomFooter = FC<Props> {
    ReactHTML.footer {
        Typography {
            align = TypographyAlign.center
            variant = TypographyVariant.body2
            css {
                color = NamedColor.grey
            }
            +"HARMOLOID © 2014 - 2023　|　"
            Link {
                color = NamedColor.grey
                underline = LinkUnderline.hover
                href = "https://github.com/sdercolin/harmoloid2"
                target = AnchorTarget._blank
                +"GitHub"
            }
            +"　|　"
            Link {
                color = NamedColor.grey
                underline = LinkUnderline.hover
                href = "https://discord.gg/TyEcQ6P73y"
                target = AnchorTarget._blank
                +"Discord"
            }
            +"　|　"
            Link {
                color = NamedColor.grey
                underline = LinkUnderline.hover
                href = string(Strings.ReleaseNotesUrl)
                target = AnchorTarget._blank
                +"Release Notes"
            }
            // TODO: About Usage of Google Analytics
            /*
            +"　|　"
            Link {
                color = NamedColor.grey
                underline = LinkUnderline.hover
                onClick = { props.onOpenEmbeddedPage(Strings.GoogleAnalyticsUsageInfoUrl) }
                +"About Usage of Google Analytics"
            }
             */
        }
    }
}
