package org.example

import kotlinx.serialization.json.Json

object JsonConfig {
    val default: Json = Json {
        classDiscriminator = "tipo"
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }
}


