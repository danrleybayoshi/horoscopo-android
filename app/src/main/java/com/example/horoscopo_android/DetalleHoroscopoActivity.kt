package com.example.horoscopo_android

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.horoscopo_android.databinding.ActivityDetalleHoroscopoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.util.*

class DetalleHoroscopoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleHoroscopoBinding
    private lateinit var tvMensajeHoroscopo: TextView

    private var mensajeOriginal: String = ""

    // horoscopoNombre todavía es necesario para hacer la llamada a la API
    private lateinit var horoscopoNombre: String

    private val RAPIDAPI_KEY = BuildConfig.RAPIDAPI_KEY

    // Asumo que estas constantes existen en otro archivo o como variables globales
    private val HOROSCOPE_HOST = HOROSCOPO_API_HOST
    private val HOROSCOPE_BASE_URL = HOROSCOPO_BASE_URL


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleHoroscopoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tvMensajeHoroscopo = binding.tvMensajeHoroscopo

        // Se ejecuta la carga de datos (solo para obtener el nombre del signo)
        // y la llamada a la API.
        cargarDatosDetalle()
        cargarHoroscopoDesdeAPI()
    }

    /**
     * Recupera el ID del nombre del signo para obtener el nombre en inglés
     * necesario para la API, ignorando la imagen y las fechas que ya no están en la UI.
     */
    private fun cargarDatosDetalle() {
        val nombreId = intent.getIntExtra("horoscopo_nombre_id", -1)

        // El resto de los IDs se ignoran porque no hay views para ellos:
        // val imagenId = intent.getIntExtra("horoscopo_imagen_id", -1)
        // val fechasId = intent.getIntExtra("horoscopo_fechas_id", -1)

        if (nombreId != -1) {
            // 1. Mapeo y asignación del nombre para la API
            val nombreSignoEspanol = getString(nombreId).lowercase(Locale.ROOT)
            horoscopoNombre = mapSignoToApiName(nombreSignoEspanol)

            // ❌ Eliminamos las referencias a los TextViews y ImageView que ya no existen:
            // binding.tvDetalleNombre.setText(nombreId)
            // if (fechasId != -1) {
            //     binding.tvDetalleFechas.setText(fechasId)
            // }
            // if (imagenId != -1) {
            //     binding.ivDetalleSigno.setImageResource(imagenId)
            // }

        } else {
            // Valor por defecto si no se pudo obtener el signo
            horoscopoNombre = "aries"
        }
    }

    /**
     * Mapea el nombre del signo en español al nombre en inglés requerido por la API.
     */
    private fun mapSignoToApiName(spanishSign: String): String {
        return when (spanishSign) {
            "aries" -> "aries"
            "tauro" -> "taurus"
            "géminis" -> "gemini"
            "cáncer" -> "cancer"
            "leo" -> "leo"
            "virgo" -> "virgo"
            "libra" -> "libra"
            "escorpio" -> "scorpio"
            "sagitario" -> "sagittarius"
            "capricornio" -> "capricorn"
            "acuario" -> "aquarius"
            "piscis" -> "pisces"
            else -> "aries"
        }
    }

    /**
     * Crea un cliente OkHttp con logging y timeouts extendidos (30s).
     */
    private fun getOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    // ---------------------------------------------------------------------------------
    // 🎯 FUNCIÓN PRINCIPAL: CARGAR HORÓSCOPO DESDE API
    // ---------------------------------------------------------------------------------

    private fun cargarHoroscopoDesdeAPI() {
        // Usamos el nombre del signo para el mensaje de carga, incluso si no se muestra en la UI.
        tvMensajeHoroscopo.text = "Cargando horóscopo de ${horoscopoNombre.replaceFirstChar { it.titlecase(Locale.ROOT) }}..."
        lifecycleScope.launch {
            try {
                val api = Retrofit.Builder()
                    .baseUrl(HOROSCOPE_BASE_URL)
                    .client(getOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(HoroscopoService::class.java)

                val response = withContext(Dispatchers.IO) {
                    api.getDailyHoroscope(
                        sunsign = horoscopoNombre,
                        language = "es",
                        rapidApiHost = HOROSCOPE_HOST,
                        rapidApiKey = RAPIDAPI_KEY
                    )
                }

                mensajeOriginal = response.predictionText
                tvMensajeHoroscopo.text = mensajeOriginal

            } catch (e: Exception) {
                mensajeOriginal = "Error al cargar el horóscopo: ${e.message}. Host: $HOROSCOPE_HOST"
                tvMensajeHoroscopo.text = mensajeOriginal
                Log.e("HOROSCOPE_API", "Excepción de Red/API: ${e.message}", e)
            }
        }
    }
}
