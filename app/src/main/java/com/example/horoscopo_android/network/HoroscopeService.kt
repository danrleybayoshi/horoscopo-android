package com.example.horoscopo_android.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// Clase de datos para la respuesta (Ajusta esto a la estructura real de tu API)
data class HoroscopeResponse(
    val date: String,
    val horoscope: String,
    val sign: String,
    val provider: String? = null // Útil para saber qué API respondió
)

/**
 * Interfaz Retrofit para definir el endpoint de consulta del horóscopo.
 */
interface HoroscopeService {

    @GET("getHoroscope") // Reemplaza con el endpoint real de tu API
    suspend fun getHoroscope(
        @Query("sign") sign: String,
        @Query("api_key") apiKey: String // La clave se pasa como query parameter
    ): Response<HoroscopeResponse>
}