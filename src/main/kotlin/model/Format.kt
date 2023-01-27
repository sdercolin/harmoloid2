package model

import org.w3c.files.File

enum class Format(
    val extension: String,
    val multipleFile: Boolean,
    val parser: suspend (List<File>) -> Project,
    val generator: suspend (Project) -> ExportResult,
) {
    Vsq3(
        ".vsqx",
        multipleFile = false,
        parser = {
            io.Vsqx.parse(it.first())
        },
        generator = {
            io.Vsqx.generate(it, Vsq3)
        },
    ),
    Vsq4(
        ".vsqx",
        multipleFile = false,
        parser = {
            io.Vsqx.parse(it.first())
        },
        generator = {
            io.Vsqx.generate(it, Vsq4)
        },
    ),
    Vpr(
        ".vpr",
        multipleFile = false,
        parser = {
            io.Vpr.parse(it.first())
        },
        generator = {
            io.Vpr.generate(it)
        },
    ),
    Ust(
        ".ust",
        multipleFile = true,
        parser = {
            io.Ust.parse(it)
        },
        generator = {
            io.Ust.generate(it)
        },
    ),
    Ccs(
        ".ccs",
        multipleFile = false,
        parser = {
            io.Ccs.parse(it.first())
        },
        generator = {
            io.Ccs.generate(it)
        },
    ),
    Svp(
        ".svp",
        multipleFile = false,
        parser = {
            io.Svp.parse(it.first())
        },
        generator = {
            io.Svp.generate(it)
        },
    );
}
