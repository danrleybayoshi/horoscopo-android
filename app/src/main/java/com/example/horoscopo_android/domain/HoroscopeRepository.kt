package com.example.horoscopo_android.domain

import com.example.horoscopo_android.network.HoroscopoResponse
import com.example.horoscopo_android.network.HoroscopeService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

// El Base URL del API Vercel
const val BASE_URL_HOROSCOPE = "https://horoscope-app-api.vercel.app/"

class HoroscopeRepository {

    // Instancia Lazy para crear Retrofit solo una vez
    private val api: HoroscopeService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_HOROSCOPE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HoroscopeService::class.java)
    }

    /**
     * Obtiene el horóscopo usando la API Vercel simple.
     * @param sign Nombre del signo en minúsculas (ej: 'aries').
     * @param timeframe Marco de tiempo (ej: 'daily').
     */
    suspend fun getHoroscope(sign: String, timeframe: String): Result<HoroscopoResponse> {
        try {
            // LLAMADA: Retrofit resuelve 'getHoroscope'
            val response = api.getHoroscope(sign = sign.lowercase(), timeframe = timeframe)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                return Result.success(body)
            } else {
                return Result.failure(IOException("Error de API: ${response.code()}"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
