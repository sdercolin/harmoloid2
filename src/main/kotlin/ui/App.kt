package ui

import APP_NAME
import APP_VERSION
import com.sdercolin.harmoloid.core.Config
import com.sdercolin.harmoloid.core.model.Content
import io.ConfigJson
import kotlinx.browser.window
import kotlinx.css.FontWeight
import kotlinx.css.LinearDimension
import kotlinx.css.color
import kotlinx.css.fontSize
import kotlinx.css.fontWeight
import kotlinx.css.height
import kotlinx.css.margin
import kotlinx.css.marginLeft
import kotlinx.css.minHeight
import kotlinx.css.width
import model.Format
import model.Project
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.createRef
import react.setState
import styled.css
import styled.styledDiv
import styled.styledSpan
import ui.external.Cookies
import ui.external.materialui.Breakpoint
import ui.external.materialui.Color
import ui.external.materialui.Icons
import ui.external.materialui.Position
import ui.external.materialui.Style
import ui.external.materialui.TypographyVariant
import ui.external.materialui.appBar
import ui.external.materialui.button
import ui.external.materialui.container
import ui.external.materialui.cssBaseline
import ui.external.materialui.fab
import ui.external.materialui.toolbar
import ui.external.materialui.tooltip
import ui.external.materialui.typography
import ui.model.Page
import ui.strings.Strings
import ui.strings.string

class App : RComponent<Props, AppState>() {

    private var mainProcessorRef = createRef<MainProcessor>()

    override fun AppState.init() {
        pageStack = listOf(Page.Import)
        isShowingConfigEditorDialog = false
        config = loadConfigFromCookies() ?: Config()
    }

    override fun RBuilder.render() {
        cssBaseline {}
        styledDiv {
            css {
                height = LinearDimension("100vh")
            }
            styledDiv {
                css {
                    height = LinearDimension.fitContent
                    minHeight = LinearDimension("95vh")
                }
                container {
                    attrs {
                        maxWidth = Breakpoint.lg
                    }
                    buildAppBar()
                    buildBody()
                }
            }
            child(CustomFooter::class) {}
            buildFabs()
            buildDialogs()
        }
    }

    private fun RBuilder.buildAppBar() {
        appBar {
            attrs {
                position = Position.fixed
            }
            toolbar {
                styledDiv {
                    css {
                        width = LinearDimension.fillAvailable
                    }
                    typography {
                        attrs {
                            variant = TypographyVariant.h6
                            color = Color.inherit
                        }
                        +APP_NAME
                        styledSpan {
                            css {
                                fontSize = LinearDimension("0.8rem")
                                marginLeft = LinearDimension("5px")
                                fontWeight = FontWeight("400")
                                color = kotlinx.css.Color.lightGrey
                            }
                            +"v$APP_VERSION"
                        }
                    }
                }
                tooltip {
                    attrs {
                        title = string(Strings.FrequentlyAskedQuestionTooltip)
                        interactive = false
                    }
                    button {
                        attrs {
                            color = Color.inherit
                            onClick = { window.open(string(Strings.FaqUrl), target = "_blank") }
                        }
                        Icons.liveHelp {}
                    }
                }
                tooltip {
                    attrs {
                        title = string(Strings.ReportFeedbackTooltip)
                        interactive = false
                    }
                    button {
                        attrs {
                            color = Color.inherit
                            onClick = { window.open(string(Strings.ReportUrl), target = "_blank") }
                        }
                        Icons.feedback {}
                    }
                }
                child(LanguageSelector::class) {
                    attrs {
                        onChangeLanguage = { setState { } }
                    }
                }
                tooltip {
                    attrs {
                        title = string(Strings.ConfigTooltip)
                        interactive = false
                    }
                    button {
                        attrs {
                            color = Color.inherit
                            onClick = { setState { isShowingConfigEditorDialog = true } }
                        }
                        Icons.settings {}
                    }
                }
            }
        }
        // Append toolbar for fixing style problems
        toolbar {}
    }

    private fun RBuilder.buildBody() {
        styledDiv {
            css {
                margin(horizontal = LinearDimension("24px"))
            }
            when (val current = state.currentPage) {
                is Page.Import -> {
                    child(Importer::class) {
                        attrs {
                            formats = Format.values().toList()
                            onImported = {
                                setState { project = it }
                                pushPage(Page.Main)
                            }
                        }
                    }
                }
                is Page.Main -> {
                    child(MainProcessor::class) {
                        attrs {
                            ref = mainProcessorRef
                            onUpdateProject = { setState { project = it } }
                            onFinish = { pushPage(Page.Export(it)) }
                            project = requireNotNull(state.project)
                            config = state.config
                        }
                    }
                }
                is Page.Export -> {
                    child(Exporter::class) {
                        attrs {
                            result = current.result
                            onRestart = { restart() }
                        }
                    }
                }
            }
        }
    }

    private fun RBuilder.buildFabs() {
        if (state.pageStack.count() <= 1) return

        if (state.currentPage is Page.Main) {
            fab {
                attrs {
                    size = "large"
                    color = Color.primary
                    onClick = {
                        restart()
                    }
                    style = Style(
                        position = "fixed",
                        top = "auto",
                        left = "auto",
                        bottom = "120px",
                        right = "32px"
                    )
                }
                Icons.replay { }
            }
            fab {
                attrs {
                    size = "large"
                    color = Color.secondary
                    onClick = {
                        requireNotNull(mainProcessorRef.current).export()
                    }
                    style = Style(
                        position = "fixed",
                        top = "auto",
                        left = "auto",
                        bottom = "32px",
                        right = "32px"
                    )
                    disabled = state.project?.content?.isReady != true
                }
                Icons.check {}
            }
        } else {
            fab {
                attrs {
                    size = "large"
                    color = Color.primary
                    onClick = {
                        popPage()
                    }
                    style = Style(
                        position = "fixed",
                        top = "auto",
                        left = "auto",
                        bottom = "32px",
                        right = "32px"
                    )
                }
                Icons.arrowBack {}
            }
        }
    }

    private fun RBuilder.buildDialogs() {
        configEditorDialog(
            isShowing = state.isShowingConfigEditorDialog,
            close = { setState { isShowingConfigEditorDialog = false } },
            saveAndClose = {
                setState {
                    config = it
                    isShowingConfigEditorDialog = false
                    mainProcessorRef.current?.updateConfig(it)
                    saveConfigToCookies(it)
                }
            },
            currentConfig = state.config
        )
    }

    private fun pushPage(page: Page) = setState {
        pageStack = pageStack + page
    }

    private fun popPage() = setState {
        pageStack = pageStack.dropLast(1)
    }

    private fun restart() = setState {
        pageStack = pageStack.take(1)
        project = null
    }

    private fun loadConfigFromCookies() = Cookies.get("config")
        ?.takeIf { it.isNotBlank() }
        ?.let(ConfigJson::parse)

    private fun saveConfigToCookies(config: Config) = Cookies.set("config", ConfigJson.generate(config))

    private val AppState.currentPage get() = pageStack.last()
}

external interface AppState : State {
    var pageStack: List<Page>
    var isShowingConfigEditorDialog: Boolean
    var project: Project?
    var config: Config
}

val Content.isReady get() = tracks.any { it.isTonalityMarked && it.harmonies?.isNotEmpty() == true }
