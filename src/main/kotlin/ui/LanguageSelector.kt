package ui

import org.w3c.dom.Element
import react.Props
import react.RBuilder
import react.State
import react.setState
import ui.external.materialui.Color
import ui.external.materialui.Icons
import ui.external.materialui.button
import ui.external.materialui.menu
import ui.external.materialui.menuItem
import ui.external.react.findDOMNode
import ui.strings.Language
import ui.strings.changeLanguage

class LanguageSelector : CoroutineRComponent<LanguageSelectorProps, LanguageSelectorState>() {
    override fun LanguageSelectorState.init() {
        anchorElement = null
    }

    override fun RBuilder.render() {
        button {
            attrs {
                color = Color.inherit
                onClick = { openMenu() }
            }
            Icons.language {}
        }
        menu {
            attrs {
                keepMounted = false
                anchorEl = state.anchorElement
                open = state.anchorElement != null
                onClose = { setState { anchorElement = null } }
            }
            Language.values().forEach {
                menuItem {
                    attrs {
                        onClick = {
                            selectLanguage(it)
                            closeMenu()
                        }
                    }
                    +it.displayName
                }
            }
        }
    }

    private fun selectLanguage(language: Language) {
        launch {
            changeLanguage(language.code)
            props.onChangeLanguage()
        }
    }

    private fun openMenu() {
        setState {
            anchorElement = findDOMNode(this@LanguageSelector)
        }
    }

    private fun closeMenu() {
        setState {
            anchorElement = null
        }
    }
}

external interface LanguageSelectorProps : Props {
    var onChangeLanguage: () -> Unit
}

external interface LanguageSelectorState : State {
    var anchorElement: Element?
}
