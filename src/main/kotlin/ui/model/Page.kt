package ui.model

import model.ExportResult

sealed class Page {
    object Import : Page()
    object Main : Page()
    data class Export(val result: ExportResult) : Page()
}
