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

    // Constante para la duración de la animación (4.0 segundos)
    private val FADE_DURATION_MS = 4000L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleHoroscopoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tvMensajeHoroscopo = binding.tvMensajeHoroscopo

        // 1. INICIALIZAR EL TEXTO COMO INVISIBLE (alpha 0)
        tvMensajeHoroscopo.alpha = 0f

        cargarDatosDetalle()
        cargarHoroscopoDesdeAPI()
    }

    /**
     * Recupera el ID del nombre del signo para obtener el nombre en inglés
     * necesario para la API.
     */
    private fun cargarDatosDetalle() {
        val nombreId = intent.getIntExtra("horoscopo_nombre_id", -1)

        if (nombreId != -1) {
            val nombreSignoEspanol = getString(nombreId).lowercase(Locale.ROOT)
            horoscopoNombre = mapSignoToApiName(nombreSignoEspanol)
        } else {
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

    /**
     * Aplica el efecto de aparición gradual (fade-in).
     */
    private fun aplicarFadeIn() {
        tvMensajeHoroscopo.animate()
            .alpha(1f) // Transparencia final: totalmente visible
            .setDuration(FADE_DURATION_MS) // 4.0 segundos
            .start()
    }

    /**
     * Carga el horóscopo desde la API e implementa el efecto de desvanecimiento.
     */
    private fun cargarHoroscopoDesdeAPI() {
        // Asigna el mensaje de carga internamente (por si se necesitara un spinner visual)
        // pero MANTENEMOS la transparencia en 0f (invisible) para evitar el parpadeo.
        tvMensajeHoroscopo.text = "Consultando el cosmos para ${horoscopoNombre.replaceFirstChar { it.titlecase(Locale.ROOT) }}..."
        // LÍNEA ELIMINADA: tvMensajeHoroscopo.alpha = 1f

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

                // Éxito: Ocultar (confirmamos alpha=0), cambiar texto y aplicar fade-in
                tvMensajeHoroscopo.alpha = 0f
                mensajeOriginal = response.predictionText
                tvMensajeHoroscopo.text = mensajeOriginal
                aplicarFadeIn()

            } catch (e: Exception) {

                // Error: Preparar mensaje de error
                val errorMessage = if (e is retrofit2.HttpException && e.code() == 429) {
                    "¡Límite de consultas excedido! El cosmos necesita descansar (Error 429). Intenta en unos minutos."
                } else {
                    "Error al cargar el horóscopo. Revisa tu clave API o la conexión a internet: ${e.message}"
                }

                // Aplicar el mensaje de error
                tvMensajeHoroscopo.alpha = 0f // Asegura que el texto esté oculto antes del fade-in
                mensajeOriginal = errorMessage
                tvMensajeHoroscopo.text = mensajeOriginal
                aplicarFadeIn() // Aplica fade-in al mensaje de error

                Log.e("HOROSCOPE_API", "Excepción de Red/API: ${e.message}", e)
            }
        }
    }
}
