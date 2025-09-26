package com.example.horoscopo_android

import com.google.gson.annotations.SerializedName

data class RewriteResponse(
    // El campo que contiene el texto reescrito/traducido.
    @SerializedName("rewrite")
    val rewrite: String?,
    // El texto original (opcional)
    val original: String?,
    // El idioma detectado (opcional)
    val language: String?
)