package ui

import com.sdercolin.harmoloid.core.exception.NoteOverlappingException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.css.LinearDimension
import kotlinx.css.marginTop
import kotlinx.html.js.onClickFunction
import model.Format
import model.Project
import org.w3c.files.File
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.attrs
import react.setState
import styled.css
import styled.styledDiv
import ui.external.materialui.Severity
import ui.external.materialui.TypographyVariant
import ui.external.materialui.typography
import ui.external.react.fileDrop
import ui.strings.Strings
import ui.strings.string
import util.extensionName
import util.toList
import util.waitFileSelection

class Importer : RComponent<ImporterProps, ImporterState>() {

    override fun ImporterState.init() {
        isLoading = false
        snackbarError = SnackbarErrorState()
        dialogError = DialogErrorState()
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                marginTop = LinearDimension("40px")
            }
            attrs {
                onClickFunction = {
                    GlobalScope.launch {
                        val accept = props.formats.joinToString(",") { it.extension }
                        val files = waitFileSelection(accept = accept, multiple = true)
                        checkFilesToImport(files)
                    }
                }
            }
            buildFileDrop()
        }

        messageBar(
            open = state.snackbarError.open,
            message = state.snackbarError.message,
            onClose = { closeMessageBar() },
            severityString = Severity.error
        )

        errorDialog(
            isShowing = state.dialogError.open,
            title = state.dialogError.title,
            errorMessage = state.dialogError.message,
            close = { closeErrorDialog() }
        )

        if (state.isLoading) {
            progress()
        }
    }

    private fun RBuilder.buildFileDrop() {
        fileDrop {
            attrs {
                onDrop = { files, _ ->
                    checkFilesToImport(files.toList())
                }
            }
            typography {
                attrs {
                    variant = TypographyVariant.h5
                }
                +(string(Strings.ImportFileDescription))
            }
            styledDiv {
                css {
                    marginTop = LinearDimension("5px")
                }
                typography {
                    attrs {
                        variant = TypographyVariant.body2
                    }
                    +(string(Strings.ImportFileSubDescription))
                }
            }
        }
    }

    private fun checkFilesToImport(files: List<File>) {
        val fileFormat = getFileFormat(files)
        when {
            fileFormat == null -> {
                setState {
                    snackbarError = SnackbarErrorState(true, string(Strings.UnsupportedFileTypeImportError))
                }
            }
            !fileFormat.multipleFile && files.count() > 1 -> {
                setState {
                    snackbarError = SnackbarErrorState(
                        true,
                        string(Strings.MultipleFileImportError, "format" to fileFormat.name)
                    )
                }
            }
            else -> {
                import(files, fileFormat)
            }
        }
    }

    private fun import(files: List<File>, format: Format) {
        setState {
            isLoading = true
        }
        GlobalScope.launch {
            try {
                delay(100)
                val parseFunction = format.parser
                val project = parseFunction(files)
                console.log("Project was imported successfully.")
                console.log(project)
                props.onImported.invoke(project)
            } catch (t: Throwable) {
                console.log(t)
                setState {
                    isLoading = false
                    if (t is NoteOverlappingException) {
                        snackbarError = SnackbarErrorState(
                            true,
                            string(
                                Strings.NoteOverlappingImportError,
                                "trackNumber" to (t.trackIndex + 1).toString(),
                                "notesDescription" to t.notes.toString()
                            )
                        )
                    } else {
                        dialogError = DialogErrorState(
                            open = true,
                            title = string(Strings.ImportErrorDialogTitle),
                            message = t.message ?: t.toString()
                        )
                    }
                }
            }
        }
    }

    private fun getFileFormat(files: List<File>): Format? {
        val extensions = files.map { it.extensionName }.distinct()

        return if (extensions.count() > 1) null
        else props.formats.find { it.extension == ".${extensions.first()}" }
    }

    private fun closeMessageBar() {
        setState {
            snackbarError = snackbarError.copy(open = false)
        }
    }

    private fun closeErrorDialog() {
        setState {
            dialogError = dialogError.copy(open = false)
        }
    }
}

external interface ImporterProps : Props {
    var formats: List<Format>
    var onImported: (Project) -> Unit
}

external interface ImporterState : State {
    var isLoading: Boolean
    var snackbarError: SnackbarErrorState
    var dialogError: DialogErrorState
}
