package ui.common

import csstype.AlignItems
import csstype.Display
import csstype.JustifyContent
import emotion.react.css
import mui.material.Backdrop
import mui.material.CircularProgress
import mui.material.CircularProgressColor
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div

fun ChildrenBuilder.progress(isShowing: Boolean) {
    Backdrop {
        open = isShowing
        div {
            css {
                display = Display.flex
                alignItems = AlignItems.center
                justifyContent = JustifyContent.center
            }
            CircularProgress {
                color = CircularProgressColor.secondary
                disableShrink = true
            }
        }
    }
}
