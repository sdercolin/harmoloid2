package ui.model

import model.ExportResult
import ui.strings.Strings

sealed class Page {
    object Import : Page()
    object Main : Page()
    data class Export(val result: ExportResult) : Page()
    data class ExtraPage(val urlKey: Strings) : Page()
}
