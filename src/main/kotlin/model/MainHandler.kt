package model

import com.sdercolin.harmoloid.core.Config
import com.sdercolin.harmoloid.core.Core
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ui.strings.Strings
import ui.strings.string
import util.runCatchingCancellable

class MainHandler {
    private var core: Core? = null
    private var onShowingProgress: ((Boolean) -> Unit)? = null
    private var onShowingError: ((String, Throwable) -> Unit)? = null
    private var scope: CoroutineScope? = null
    private var project: Project? = null
    private var onFinish: ((ExportResult) -> Unit)? = null

    fun bind(
        core: Core,
        onShowingProgress: (Boolean) -> Unit,
        onShowingError: (String, Throwable) -> Unit,
        scope: CoroutineScope,
        onFinish: (ExportResult) -> Unit,
        project: Project,
    ) {
        this.core = core
        this.onShowingProgress = onShowingProgress
        this.onShowingError = onShowingError
        this.scope = scope
        this.onFinish = onFinish
        this.project = project
    }

    fun export() {
        onShowingProgress?.invoke(true)
        scope?.launch {
            runCatchingCancellable {
                val core = requireNotNull(core)
                val exportProject = requireNotNull(project).copy(
                    content = core.content,
                    chorus = core.content.tracks.indices.map { trackIndex ->
                        core.getAllChorusTracks(trackIndex)
                    }
                )
                val result = requireNotNull(project).format.generator.invoke(exportProject)
                console.log(result)
                requireNotNull(onFinish).invoke(result)
            }.onFailure { t ->
                onShowingError?.invoke(string(Strings.ExportErrorTitle), t)
            }
            onShowingProgress?.invoke(false)
        }
    }

    fun updateConfig(config: Config) {
        val core = core ?: return
        core.reloadConfig(config)
        console.log("config reloaded: ${core.config}")
    }
}
