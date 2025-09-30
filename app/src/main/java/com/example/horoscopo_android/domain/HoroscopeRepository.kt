package com.example.horoscopo_android.domain

import com.example.horoscopo_android.BuildConfig
import com.example.horoscopo_android.network.HoroscopeResponse
import com.example.horoscopo_android.network.HoroscopeService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

// 1. Definición de la Fuente API
data class ApiSource(
    val id: String,         // Nombre de la fuente (ej: Principal-A)
    val apiKey: String,     // Clave API asociada
    val baseUrl: String,    // URL base de la API asociada
    var isRateLimited: Boolean = false
)

class HoroscopeRepository {

    // 2. LISTA PRINCIPAL DE APIs CON CLAVES DE BUILDCONFIG
    private val apiSources = mutableListOf(
        ApiSource("Principal-A", BuildConfig.API_KEY_PRINCIPAL, "https://api.proveedor1.com/v1/"),
        ApiSource("Respaldo-1", BuildConfig.API_KEY_RESPALDO_1, "https://api.proveedor2.com/"),
        ApiSource("Respaldo-2", BuildConfig.API_KEY_RESPALDO_2, "https://api.proveedor3.com/"),
        ApiSource("Respaldo-3", BuildConfig.API_KEY_RESPALDO_3, "https://api.proveedor4.com/"),
        ApiSource("Respaldo-4", BuildConfig.API_KEY_RESPALDO_4, "https://api.proveedor5.com/"),
        ApiSource("Respaldo-5", BuildConfig.API_KEY_RESPALDO_5, "https://api.proveedor6.com/"),
        ApiSource("Respaldo-6", BuildConfig.API_KEY_RESPALDO_6, "https://api.proveedor7.com/"),
        ApiSource("Respaldo-7", BuildConfig.API_KEY_RESPALDO_7, "https://api.proveedor8.com/"),
        ApiSource("Respaldo-8", BuildConfig.API_KEY_RESPALDO_8, "https://api.proveedor9.com/"),
        ApiSource("Respaldo-9", BuildConfig.API_KEY_RESPALDO_9, "https://api.proveedor10.com/"),
        ApiSource("Respaldo-10", BuildConfig.API_KEY_RESPALDO_10, "https://api.proveedor11.com/"),
        ApiSource("Respaldo-11", BuildConfig.API_KEY_RESPALDO_11, "https://api.proveedor12.com/"),
        ApiSource("Respaldo-12", BuildConfig.API_KEY_RESPALDO_12, "https://api.proveedor13.com/")
    )

    // Función auxiliar para crear la instancia Retrofit
    private fun createService(baseUrl: String): HoroscopeService {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(HoroscopeService::class.java)
    }

    /**
     * Intenta obtener el horóscopo secuencialmente usando todas las claves API disponibles (Failover).
     */
    suspend fun getHoroscopeWithFailover(sign: String): Result<HoroscopeResponse> {

        // Ordena las fuentes para que las no limitadas vayan primero
        val prioritizedSources = apiSources.sortedBy { it.isRateLimited }

        for (source in prioritizedSources) {
            if (source.isRateLimited) {
                continue // Salta si ya falló por cuota/clave
            }

            // Validación de clave: salta si la clave está vacía
            if (source.apiKey.isBlank()) {
                println("⚠️ API ${source.id} saltada: Clave API vacía.")
                continue
            }

            try {
                // Inicializa el servicio para esta fuente
                val service = createService(source.baseUrl)

                // Intento de llamada
                val response = service.getHoroscope(sign, source.apiKey)

                if (response.isSuccessful && response.body() != null) {
                    println("✅ Éxito con API: ${source.id}")
                    return Result.success(response.body()!!.copy(provider = source.id))

                } else {
                    // Fallo HTTP
                    when (response.code()) {
                        401, 403, 429 -> {
                            println("⚠️ API ${source.id} falló (${response.code()}). Marcada como Rate Limited.")
                            source.isRateLimited = true
                            continue
                        }
                        else -> {
                            println("❌ Error en ${source.id}: ${response.code()}. Intentando Failover.")
                            continue
                        }
                    }
                }
            } catch (e: IOException) {
                // Fallo de red (sin conexión, timeout, DNS)
                println("❌ API ${source.id} falló por excepción de red: ${e.message}. Intentando Failover.")
                continue
            } catch (e: Exception) {
                // Otro error crítico
                println("❌ Fallo crítico en ${source.id}: ${e.message}. Intentando Failover.")
                continue
            }
        }

        // Si el bucle termina, es que ninguna API funcionó.
        return Result.failure(Exception("Todas las fuentes de API fallaron o agotaron la cuota."))
    }
}