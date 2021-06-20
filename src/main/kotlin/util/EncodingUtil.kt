package util

import external.Encoding

fun String.asByteTypedArray() = indices.map { i -> this.asDynamic().charCodeAt(i) as Byte }.toTypedArray()

fun String.encode(encoding: String) = Encoding.convert(asByteTypedArray(), encoding)
