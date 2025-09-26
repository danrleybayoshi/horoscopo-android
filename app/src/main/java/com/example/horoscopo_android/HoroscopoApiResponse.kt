package com.example.horoscopo_android

import com.google.gson.annotations.SerializedName

// Modelo adaptado para la API 'AstroPredict Daily Horoscopes'
data class HoroscopoApiResponse(
    // La clave JSON es "horoscope" y la mapeamos a la variable 'predictionText'
    @SerializedName("horoscope") val predictionText: String,

    // La clave JSON es "zodiac" y la mapeamos a la variable 'zodiacSign'
    @SerializedName("zodiac") val zodiacSign: String,

    // Claves opcionales
    @SerializedName("language") val language: String? = null,
    @SerializedName("type") val type: String? = null
)