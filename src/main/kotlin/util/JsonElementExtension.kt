@file:Suppress("unused")

package util

import exception.IllegalFileException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull

fun JsonElement.property(name: String) = maybeProperty(name)
    ?: throw IllegalFileException.JsonElementNotFound(name)

fun JsonElement.maybeProperty(name: String) = jsonObject[name]

fun JsonElement.clone(): JsonObject {
    val content = jsonObject.toMutableMap()
    return JsonObject(content)
}

fun JsonElement.withProperty(name: String, value: Any?): JsonObject {
    val content = jsonObject.toMutableMap()
    content[name] = value.json()
    return JsonObject(content)
}

fun JsonElement.mapProperty(name: String, mapper: (JsonElement) -> Any?): JsonObject {
    val property = maybeProperty(name) ?: return JsonObject(this.jsonObject)
    val newProperty = mapper(property)
    return withProperty(name, newProperty)
}

fun Any?.json(): JsonElement =
    when (this) {
        null -> JsonNull
        is JsonElement -> this
        is List<*> -> this.mapNotNull { it?.json() }.json()
        is Int -> this.json()
        is Long -> this.json()
        is Double -> this.json()
        is Boolean -> this.json()
        is String -> this.json()
        else -> throw NotImplementedError(
            "${this::class.simpleName ?: this.toString()}.json() is not implemented.",
        )
    }

val JsonElement.asList get() = jsonArray
fun List<JsonElement>.json() = JsonArray(this)

val JsonElement.asInt get() = jsonPrimitive.int
val JsonElement.asIntOrNull get() = jsonPrimitive.intOrNull
fun Int.json() = JsonPrimitive(this)

val JsonElement.asLong get() = jsonPrimitive.long
val JsonElement.asLongOrNull get() = jsonPrimitive.longOrNull
fun Long.json() = JsonPrimitive(this)

val JsonElement.asBoolean get() = jsonPrimitive.boolean
val JsonElement.asBooleanOrNull get() = jsonPrimitive.booleanOrNull
fun Boolean.json() = JsonPrimitive(this)

val JsonElement.asDouble get() = jsonPrimitive.double
val JsonElement.asDoubleOrNull get() = jsonPrimitive.doubleOrNull
fun Double.json() = JsonPrimitive(this)

val JsonElement.asString get() = requireNotNull(asStringOrNull)
val JsonElement.asStringOrNull
    get() = if (jsonPrimitive.isString) jsonPrimitive.toString().trimFirstAndLast() else null

fun String.json() = JsonPrimitive(this)

private fun String.trimFirstAndLast() = this
    .let { if (it.startsWith("\"")) it.drop(1) else it }
    .let { if (it.endsWith("\"")) it.dropLast(1) else it }
