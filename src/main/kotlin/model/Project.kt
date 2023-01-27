package model

import com.sdercolin.harmoloid.core.model.Content
import com.sdercolin.harmoloid.core.model.HarmonicType
import com.sdercolin.harmoloid.core.model.NoteShift
import org.w3c.files.File

data class Project(
    val format: Format,
    val inputFiles: List<File>,
    val name: String,
    val content: Content,
    val chorus: List<Map<HarmonicType, List<NoteShift>>> = listOf(),
)
