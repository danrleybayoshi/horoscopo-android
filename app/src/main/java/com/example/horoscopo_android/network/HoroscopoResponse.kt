package com.example.horoscopo_android.network

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para la respuesta del horóscopo de la API.
 */
data class HoroscopoResponse(
    @SerializedName("data") val data: HoroscopeDataApi
)

/**
 * Contenedor de los datos del horóscopo.
 */
data class HoroscopeDataApi(
    @SerializedName("horoscope") val description: String
)
