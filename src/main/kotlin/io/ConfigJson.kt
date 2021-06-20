package io

import com.sdercolin.harmoloid.core.Config
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ConfigJson {

    fun parse(content: String): Config {
        return jsonSerializer.decodeFromString(content)
    }

    fun generate(config: Config): String {
        return jsonSerializer.encodeToString(config)
    }

    private val jsonSerializer = Json {
        encodeDefaults = true
        isLenient = true
    }
}
