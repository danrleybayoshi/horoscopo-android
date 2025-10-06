package com.example.horoscopo_android.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interfaz Retrofit para la API de Vercel simple.
 * Solo requiere 'sign' y 'timeframe'.
 */
interface HoroscopeService {

    @GET("api/horoscope") // Endpoint para la API de Vercel
    suspend fun getHoroscope(
        @Query("sign") sign: String,
        @Query("timeframe") timeframe: String
        // No se requiere 'api_key' para esta API
    ): Response<HoroscopoResponse>
}
