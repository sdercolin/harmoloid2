package util

fun String.linesNotBlank() = this.lines().filter { it.isNotBlank() }
