package ui

import APP_NAME
import APP_VERSION
import com.sdercolin.harmoloid.core.Config
import com.sdercolin.harmoloid.core.model.Content
import csstype.Auto
import csstype.Length
import csstype.Margin
import csstype.NamedColor
import csstype.Position
import csstype.number
import csstype.px
import csstype.rem
import csstype.vh
import io.ConfigJson
import kotlinx.browser.window
import kotlinx.js.jso
import model.ExportResult
import model.Format
import model.MainHandler
import model.Project
import mui.icons.material.ArrowBack
import mui.icons.material.Check
import mui.icons.material.Feedback
import mui.icons.material.LiveHelp
import mui.icons.material.Replay
import mui.icons.material.Settings
import mui.material.AppBar
import mui.material.AppBarPosition
import mui.material.Button
import mui.material.ButtonColor
import mui.material.Container
import mui.material.CssBaseline
import mui.material.Fab
import mui.material.FabColor
import mui.material.Size
import mui.material.Toolbar
import mui.material.Tooltip
import mui.material.Typography
import mui.material.styles.ThemeProvider
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.ChildrenBuilder
import react.FC
import react.Props
import react.ReactNode
import react.css.css
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.useState
import ui.external.Cookies
import ui.model.Page
import ui.strings.Language
import ui.strings.Strings
import ui.strings.string

val App = FC<Props> {

    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    var language: Language? by useState(null)

    var pageStack: List<Page> by useState(listOf(Page.Import))
    var isShowingConfigEditorDialog: Boolean by useState(false)
    var project: Project? by useState(null)
    var config: Config by useState(loadConfigFromCookies() ?: Config())
    val handler by useState(MainHandler())

    fun pushPage(page: Page) {
        val stack = pageStack.toMutableList()
        if (pageStack.last() == page) {
            stack.removeAt(stack.lastIndex)
        }
        stack.add(page)
        pageStack = stack
    }

    fun popPage() {
        pageStack = pageStack.dropLast(1)
    }

    fun restart() {
        pageStack = pageStack.take(1)
        project = null
    }

    ThemeProvider {
        theme = appTheme
        CssBaseline {}
        div {
            css {
                height = 100.vh
            }
            div {
                css {
                    height = Length.fitContent
                    minHeight = 95.vh
                }
                Container {
                    maxWidth = "xl"
                    buildAppBar(
                        onChangeLanguage = { language = it },
                        onOpenConfigEditor = { isShowingConfigEditorDialog = true },
                    )
                    buildBody(
                        handler = handler,
                        project = project,
                        setProject = { project = it },
                        page = pageStack.last(),
                        config = config,
                        onImported = {
                            project = it
                            pushPage(Page.Main)
                        },
                        onFinish = {
                            pushPage(Page.Export(it))
                        },
                        restart = ::restart,
                    )
                }
            }
            CustomFooter {}
            buildFabs(
                pageStack = pageStack,
                isReady = project?.content?.isReady ?: false,
                confirm = { handler.export() },
                back = ::popPage,
                restart = ::restart,
            )
            ConfigEditorDialog {
                isShowing = isShowingConfigEditorDialog
                close = { isShowingConfigEditorDialog = false }
                saveAndClose = {
                    config = it
                    isShowingConfigEditorDialog = false
                    handler.updateConfig(it)
                    saveConfigToCookies(it)
                }
                currentConfig = config
            }
        }
    }
}

private fun ChildrenBuilder.buildAppBar(onChangeLanguage: (Language) -> Unit, onOpenConfigEditor: () -> Unit) {
    AppBar {
        position = AppBarPosition.fixed
        style = jso {
            background = appTheme.palette.primary.main
        }
        Toolbar {
            Typography {
                css {
                    color = appTheme.palette.primary.contrastText
                }
                variant = TypographyVariant.h6
                +APP_NAME
                span {
                    css {
                        fontSize = 0.8.rem
                        marginLeft = 5.px
                        color = NamedColor.lightgrey
                    }
                    +"v$APP_VERSION"
                }
                sx { flexGrow = number(1.0) }
            }
            Tooltip {
                title = ReactNode(string(Strings.FrequentlyAskedQuestionTooltip))
                disableInteractive = true
                Button {
                    color = ButtonColor.inherit
                    onClick = { window.open(string(Strings.FaqUrl), target = "_blank") }
                    LiveHelp()
                }
            }
            Tooltip {
                title = ReactNode(string(Strings.ReportFeedbackTooltip))
                disableInteractive = true
                Button {
                    color = ButtonColor.inherit
                    onClick = { window.open(string(Strings.ReportUrl), target = "_blank") }
                    Feedback()
                }
            }
            LanguageSelector {
                this.onChangeLanguage = onChangeLanguage
            }
            Tooltip {
                title = ReactNode(string(Strings.ConfigTooltip))
                Button {
                    color = ButtonColor.inherit
                    onClick = { onOpenConfigEditor() }
                    Settings()
                }
            }
        }
    }
    // Append toolbar for fixing style problems
    Toolbar {}
}

private fun ChildrenBuilder.buildBody(
    handler: MainHandler,
    page: Page,
    project: Project?,
    setProject: (Project) -> Unit,
    config: Config,
    onImported: (Project) -> Unit,
    onFinish: (ExportResult) -> Unit,
    restart: () -> Unit,
) {
    div {
        css {
            margin = Margin(horizontal = 24.px, vertical = 0.px)
        }
        when (page) {
            is Page.Import -> {
                Importer {
                    formats = Format.values().toList()
                    this.onImported = onImported
                }
            }

            is Page.Main -> {
                MainProcessor {
                    onUpdateProject = setProject
                    this.handler = handler
                    this.onFinish = onFinish
                    this.project = requireNotNull(project)
                    this.config = config
                }
            }

            is Page.Export -> {
                Exporter {
                    result = page.result
                    onRestart = { restart() }
                }
            }
        }
    }
}

private fun ChildrenBuilder.buildFabs(
    pageStack: List<Page>,
    isReady: Boolean,
    confirm: () -> Unit,
    back: () -> Unit,
    restart: () -> Unit,
) {
    if (pageStack.count() <= 1) return
    val currentPage = pageStack.last()
    if (currentPage is Page.Main) {
        Fab {
            size = Size.large
            color = FabColor.primary
            onClick = { restart() }
            style = jso {
                position = Position.fixed
                top = Auto.auto
                left = Auto.auto
                bottom = 120.px
                right = 32.px
            }
            Replay()
        }
        Fab {
            size = Size.large
            color = FabColor.secondary
            onClick = { confirm() }
            style = jso {
                position = Position.fixed
                top = Auto.auto
                left = Auto.auto
                bottom = 32.px
                right = 32.px
            }
            disabled = !isReady
            Check()
        }
    } else {
        Fab {
            size = Size.large
            color = FabColor.primary
            onClick = { back() }
            style = jso {
                position = Position.fixed
                top = Auto.auto
                left = Auto.auto
                bottom = 32.px
                right = 32.px
            }
            ArrowBack()
        }
    }
}

private fun loadConfigFromCookies() = Cookies.get("config")
    ?.takeIf { it.isNotBlank() }
    ?.let(ConfigJson::parse)

private fun saveConfigToCookies(config: Config) = Cookies.set("config", ConfigJson.generate(config))
val Content.isReady get() = tracks.any { it.isTonalityMarked && it.harmonies?.isNotEmpty() == true }
