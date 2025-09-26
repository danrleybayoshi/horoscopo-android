package com.example.horoscopo_android

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// 🛑 Constantes para la API 'AstroPredict Daily Horoscopes'
const val HOROSCOPO_BASE_URL = "https://astropredict-daily-horoscopes-lucky-insights.p.rapidapi.com/"
const val HOROSCOPO_API_HOST = "astropredict-daily-horoscopes-lucky-insights.p.rapidapi.com"

interface HoroscopoService {

    @GET("horoscope") // La ruta es simplemente "horoscope"
    suspend fun getDailyHoroscope(
        // Parámetro de la API: el signo (ej: "aquarius")
        @Query("zodiac") sunsign: String,

        // Parámetro de la API: el tipo de predicción ("daily" o "weekly")
        @Query("type") type: String = "daily",

        // Parámetro de la API: el idioma ("en" por defecto, pero se puede cambiar)
        @Query("lang") language: String = "es",

        // Parámetro de la API: la zona horaria
        @Query("timezone") timezone: String = "UTC",

        // Headers de RapidAPI
        @Header("x-rapidapi-host") rapidApiHost: String,
        @Header("x-rapidapi-key") rapidApiKey: String
    ): HoroscopoApiResponse
}