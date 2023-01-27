package ui

import com.sdercolin.harmoloid.core.Core
import com.sdercolin.harmoloid.core.exception.NoteOverlappingException
import csstype.px
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.Format
import model.Project
import mui.material.AlertColor
import mui.material.Typography
import mui.material.styles.TypographyVariant
import org.w3c.files.File
import react.ChildrenBuilder
import react.Props
import react.css.css
import react.dom.html.ReactHTML.div
import react.useState
import ui.common.DialogErrorState
import ui.common.SnackbarErrorState
import ui.common.errorDialog
import ui.common.messageBar
import ui.common.progress
import ui.common.scopedFC
import ui.external.react.FileDrop
import ui.strings.Strings
import ui.strings.string
import util.extensionName
import util.runCatchingCancellable
import util.toList
import util.waitFileSelection

val Importer = scopedFC<ImporterProps> { props, scope ->
    var isLoading by useState(false)
    var snackbarError by useState(SnackbarErrorState())
    var dialogError by useState(DialogErrorState())

    fun getFileFormat(files: List<File>): Format? {
        val extensions = files.map { it.extensionName }.distinct()

        return if (extensions.count() > 1) null
        else props.formats.find { it.extension == ".${extensions.first()}" }
    }

    fun import(files: List<File>, format: Format) {
        isLoading = true
        scope.launch {
            runCatchingCancellable {
                delay(100)
                val parseFunction = format.parser
                val project = parseFunction(files)
                Core(project.content)
                console.log("Project was imported successfully.")
                props.onImported.invoke(project)
            }.onFailure { t ->
                console.log(t)
                isLoading = false
                if (t is NoteOverlappingException) {
                    snackbarError = SnackbarErrorState(
                        true,
                        string(
                            Strings.NoteOverlappingImportError,
                            "trackNumber" to (t.trackIndex + 1).toString(),
                            "notesDescription" to t.notes.toString(),
                        ),
                    )
                } else {
                    dialogError = DialogErrorState(
                        isShowing = true,
                        title = string(Strings.ImportErrorDialogTitle),
                        message = t.message ?: t.toString(),
                    )
                }
            }
        }
    }

    fun checkFilesToImport(files: List<File>) {
        val fileFormat = getFileFormat(files)
        when {
            fileFormat == null -> {
                snackbarError = SnackbarErrorState(true, string(Strings.UnsupportedFileTypeImportError))
            }

            !fileFormat.multipleFile && files.count() > 1 -> {
                snackbarError = SnackbarErrorState(
                    true,
                    string(Strings.MultipleFileImportError, "format" to fileFormat.name),
                )
            }

            else -> {
                import(files, fileFormat)
            }
        }
    }

    fun closeMessageBar() {
        snackbarError = snackbarError.copy(isShowing = false)
    }

    fun closeErrorDialog() {
        dialogError = dialogError.copy(isShowing = false)
    }

    div {
        css {
            marginTop = 40.px
        }
        onClick = {
            scope.launch {
                val accept = props.formats.joinToString(",") { it.extension }
                val files = waitFileSelection(accept = accept, multiple = true)
                checkFilesToImport(files)
            }
        }
        buildFileDrop { checkFilesToImport(it) }
    }

    messageBar(
        isShowing = snackbarError.isShowing,
        message = snackbarError.message,
        close = { closeMessageBar() },
        color = AlertColor.error,
    )

    errorDialog(
        state = dialogError,
        close = { closeErrorDialog() },
    )

    progress(isLoading)
}

private fun ChildrenBuilder.buildFileDrop(onFiles: (List<File>) -> Unit) {
    FileDrop {
        onDrop = { files, _ ->
            onFiles(files.toList())
        }
        Typography {
            variant = TypographyVariant.h5
            +string(Strings.ImportFileDescription)
        }
        div {
            css {
                marginTop = 5.px
            }
            Typography {
                variant = TypographyVariant.body2
                +string(Strings.ImportFileSubDescription)
            }
        }
    }
}

external interface ImporterProps : Props {
    var formats: List<Format>
    var onImported: (Project) -> Unit
}
