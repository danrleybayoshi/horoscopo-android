package com.example.horoscopo_android

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.horoscopo_android.databinding.ActivityDetalleHoroscopoBinding
import kotlin.random.Random

class DetalleHoroscopoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleHoroscopoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleHoroscopoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nombreId = intent.getIntExtra("horoscopo_nombre_id", -1)
        val fechasId = intent.getIntExtra("horoscopo_fechas_id", -1)
        val imagenId = intent.getIntExtra("horoscopo_imagen_id", -1)
        val mensajeId = intent.getIntExtra("horoscopo_mensaje_id", -1)

        if (nombreId != -1) {
            binding.tvDetalleNombre.setText(nombreId)
        }
        if (fechasId != -1) {
            binding.tvDetalleFechas.setText(fechasId)
        }
        if (imagenId != -1) {
            binding.ivDetalleSigno.setImageResource(imagenId)
        }

        if (mensajeId != -1) {
            // 1. Obtiene el mensaje aleatorio
            val mensajes = resources.getStringArray(mensajeId)
            val mensajeAleatorio = mensajes[Random.nextInt(mensajes.size)]

            // 2. Llama a la función de animación usando la vista del binding
            startCrystalBallEffect(binding.tvMensajeHoroscopo, mensajeAleatorio)
        }
    }

    private fun startCrystalBallEffect(textView: TextView, text: String) {
        // Establece el texto
        textView.text = text

        // Inicialmente, el TextView es invisible
        textView.alpha = 0f

        // Anima la opacidad para que se muestre gradualmente
        textView.animate()
            .alpha(1f)
            .setDuration(7000) // 1.5 segundos para la animación
            .start()
    }
}