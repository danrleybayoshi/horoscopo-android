package com.example.horoscopo_android

    data class RewriteRequest(
        // Idioma del texto original (nuestra predicción base)
        val language: String = "en",
        // Fuerza de la reescritura (3 es un buen valor intermedio)
        val strength: Int = 3,
        // La predicción base que la API va a reescribir/traducir.
        val text: String
    )
