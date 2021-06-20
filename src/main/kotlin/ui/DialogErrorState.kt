package ui

data class DialogErrorState(
    val open: Boolean = false,
    val title: String = "",
    val message: String = ""
)
