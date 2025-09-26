package com.example.horoscopo_android

import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.horoscopo_android.RetrofitClient
import com.example.horoscopo_android.databinding.ActivityDetalleHoroscopoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class DetalleHoroscopoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleHoroscopoBinding
    private var nombreSigno: String? = null

    // PREDICCIONES BASE: Frases en inglés que la API reescribirá y traducirá.
    private val basePredictions = mapOf(
        "Aries" to listOf(
            "A sudden opportunity will arise in your career, requiring a quick decision.",
            "Today's energy calls for decisive action, don't overthink your next move.",
            "Be careful with money matters, a small investment could lead to big returns."
        ),
        "Tauro" to listOf(
            "Patience is your key to success today, especially in creative endeavors.",
            "You may encounter surprising opposition, stand firm on your values.",
            "A hidden admirer will make their presence known by the end of the day."
        ),
        "Géminis" to listOf("Expect great news in your social life, a new friendship blooms.", "A moment of clarity will help you solve an old problem."),
        "Cáncer" to listOf("Family harmony is prioritized, listen to the needs of your loved ones.", "A nostalgic memory will bring you comfort and inspiration."),
        "Leo" to listOf("Your natural leadership qualities shine, step forward confidently.", "An unexpected gift or compliment will brighten your afternoon."),
        "Virgo" to listOf("Focus on the details today, a small error could have large consequences.", "Health and wellness routines will bring significant benefits."),
        "Libra" to listOf("Seek balance in all things, especially work and rest. Don't compromise.", "A romantic relationship moves to a deeper, more committed level."),
        "Escorpio" to listOf("Hidden truths surface today, trust your intuition over logic.", "A financial matter is resolved in your favor, bringing relief."),
        "Sagitario" to listOf("Adventure calls, plan a spontaneous trip or explore a new subject.", "Your optimistic outlook will attract good fortune from strangers."),
        "Capricornio" to listOf("Hard work pays off, a goal you set long ago is finally within reach.", "A mentor or older figure offers valuable guidance."),
        "Acuario" to listOf("Your innovative ideas inspire others, share your vision freely.", "A chance encounter leads to an exciting professional partnership."),
        "Piscis" to listOf("Your dreams hold a message for you, write them down immediately.", "An artistic project brings you deep personal satisfaction.")
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetalleHoroscopoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nombreId = intent.getIntExtra("horoscopo_nombre_id", -1)
        val fechasId = intent.getIntExtra("horoscopo_fechas_id", -1)
        val imagenId = intent.getIntExtra("horoscopo_imagen_id", -1)

        nombreSigno = intent.getStringExtra("horoscopo_nombre_string")

        if (nombreId != -1) {
            binding.tvDetalleNombre.setText(nombreId)
        }
        if (fechasId != -1) {
            binding.tvDetalleFechas.setText(fechasId)
        }
        if (imagenId != -1) {
            binding.ivDetalleSigno.setImageResource(imagenId)
        }

        fetchRewrittenPrediction()
    }

    private fun getBasePrediction(sign: String): String {
        val predictions = basePredictions[sign] ?: listOf("Your future is bright, embrace the day.", "Unexpected changes bring growth.")
        return predictions[Random.nextInt(predictions.size)]
    }

    private fun fetchRewrittenPrediction() {
        val signName = nombreSigno ?: "tu signo"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val baseText = getBasePrediction(signName)

                // 🛑 AJUSTE CLAVE 1: Añadimos language = "es" para forzar la traducción.
                val requestBody = RewriteRequest(language = "es", text = baseText)

                val response = RetrofitClient.apiService.rewriteText(requestBody)

                val rewrittenMessage = response.rewrite
                    ?: "El cosmos está en silencio. Un mensaje está por llegar."

                // 🛑 AJUSTE CLAVE 2: Eliminamos la variable 'header' y usamos solo 'rewrittenMessage'.
                val mensajeFinal = rewrittenMessage

                withContext(Dispatchers.Main) {
                    showHoroscopeMessage(mensajeFinal)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showError("¡Error de conexión! No se pudo obtener la predicción. Inténtalo de nuevo.")
                }
            }
        }
    }

    private fun showHoroscopeMessage(text: String) {
        val scrollView = findViewById<ScrollView>(R.id.mensajeScrollView)
        if (scrollView != null) {
            startCrystalBallEffect(binding.tvMensajeHoroscopo, scrollView, text)
        } else {
            binding.tvMensajeHoroscopo.text = "Error interno: ScrollView no encontrado."
        }
    }

    private fun showError(message: String) {
        val scrollView = findViewById<ScrollView>(R.id.mensajeScrollView)
        if (scrollView != null) {
            startCrystalBallEffect(
                binding.tvMensajeHoroscopo,
                scrollView,
                message
            )
        }
    }

    private fun startCrystalBallEffect(textView: TextView, scrollView: ScrollView, text: String) {
        textView.text = text
        textView.alpha = 0f

        textView.animate()
            .alpha(1f)
            .setDuration(7000)
            .withEndAction {
                scrollView.post {
                    val contentHeight = scrollView.getChildAt(0).height
                    val viewHeight = scrollView.height

                    if (contentHeight > viewHeight) {
                        scrollView.fullScroll(View.FOCUS_DOWN)
                    }
                }
            }
            .start()
    }
}