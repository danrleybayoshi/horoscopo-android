package com.example.horoscopo_android

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.horoscopo_android.databinding.ActivityDetalleHoroscopoBinding
import java.util.*

class DetalleHoroscopoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleHoroscopoBinding
    private lateinit var tvMensajeHoroscopo: TextView

    // ⭐ CORREGIDO: Usamos el ID real de tu XML: btnFavoritoDetalle
    private lateinit var btnFavoritoDetalle: ImageView

    // Datos del signo que recibimos
    private var nombreId: Int = -1 // El ID del recurso del nombre (clave para favoritos)
    private var fechasId: Int = -1 // El ID del recurso de las fechas
    private var imagenId: Int = -1 // El ID del recurso de la imagen (no usado en XML, pero útil)

    private var mensajeFinal: String = ""
    private var proveedor: String? = null

    private val FADE_DURATION_MS = 4000L

    // Constantes de SharedPreferences (MISMAS que en MainActivity)
    private val PREFS_NAME = "HoroscopoPrefs"
    private val FAVORITES_KEY = "FavoriteSigns"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleHoroscopoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tvMensajeHoroscopo = binding.tvMensajeHoroscopo
        // ⭐ CORREGIDO: Conectamos la variable con el ID real del XML
        btnFavoritoDetalle = binding.btnFavoritoDetalle

        // 1. INICIALIZAR EL TEXTO COMO INVISIBLE (alpha 0)
        tvMensajeHoroscopo.alpha = 0f

        // 2. Cargar todos los datos del Intent
        cargarDatosDelIntent()

        // 3. Configurar el toggle de favoritos (NUEVO)
        setupFavoriteToggle()

        // 4. Aplicar el mensaje final (horóscopo o error) y el Fade-In
        tvMensajeHoroscopo.text = mensajeFinal
        aplicarFadeIn()

        // El ActionBar (barra superior) ya no muestra el título,
        // ya que el nombre del signo lo muestra tvTituloSigno
        supportActionBar?.hide()
    }

    /**
     * Recupera todos los IDs y datos pasados desde MainActivity y actualiza el título.
     */
    private fun cargarDatosDelIntent() {
        nombreId = intent.getIntExtra("horoscopo_nombre_id", -1)
        fechasId = intent.getIntExtra("horoscopo_fechas_id", -1)
        imagenId = intent.getIntExtra("horoscopo_imagen_id", -1)

        // Datos de la API y proveedor
        mensajeFinal = intent.getStringExtra("horoscopo_api_data")
            ?: "Error interno: No se pudo obtener el horóscopo."
        proveedor = intent.getStringExtra("horoscopo_api_provider")

        // ⭐ CORREGIDO: Usamos tvTituloSigno para mostrar Nombre y Fechas
        if (nombreId != -1) {
            val nombre = getString(nombreId)
            val fechas = if (fechasId != -1) getString(fechasId) else ""
            binding.tvTituloSigno.text = "$nombre ($fechas)"
        }

        // ❌ ELIMINADO: La imagen del signo (ivSignoDetalle) no existe en tu XML.
    }

    // ---------------------------------------------------------------------------------------------
    // GESTIÓN DE FAVORITOS
    // ---------------------------------------------------------------------------------------------

    /**
     * Obtiene la instancia de SharedPreferences.
     */
    private fun getSharedPreferences() = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Comprueba si el Horoscopo actual está marcado como favorito.
     */
    private fun isHoroscopoFavorite(): Boolean {
        if (nombreId == -1) return false
        val prefs = getSharedPreferences()
        val favorites = prefs.getStringSet(FAVORITES_KEY, emptySet()) ?: emptySet()
        return favorites.contains(nombreId.toString())
    }

    /**
     * Alterna el estado de favorito del Horoscopo actual.
     */
    private fun toggleHoroscopoFavorite() {
        if (nombreId == -1) return

        val prefs = getSharedPreferences()
        val editor = prefs.edit()

        val currentFavorites = prefs.getStringSet(FAVORITES_KEY, emptySet())?.toMutableSet() ?: mutableSetOf()
        val signId = nombreId.toString()

        if (currentFavorites.contains(signId)) {
            currentFavorites.remove(signId) // Quitar de favoritos
        } else {
            currentFavorites.add(signId) // Añadir a favoritos
        }

        editor.putStringSet(FAVORITES_KEY, currentFavorites)
        editor.apply()

        // Actualiza el icono para reflejar el cambio
        updateFavoriteIcon()
    }

    /**
     * Configura el botón de favorito y su estado inicial.
     */
    private fun setupFavoriteToggle() {
        updateFavoriteIcon() // Establece el icono inicial

        btnFavoritoDetalle.setOnClickListener {
            toggleHoroscopoFavorite()
        }
    }

    /**
     * Actualiza el icono del toggle de favorito basado en el estado actual.
     * Usamos ic_favorite_filled y ic_favorite_border, tal como se sugirió.
     */
    private fun updateFavoriteIcon() {
        val icon = if (isHoroscopoFavorite()) {
            R.drawable.ic_favorite_filled // Relleno (es favorito)
        } else {
            R.drawable.ic_favorite_border // Borde (no es favorito)
        }
        btnFavoritoDetalle.setImageResource(icon)
        // Usando el color dorado que definiste en tu XML: #D4AF37
        // Nota: Asegúrate de tener este color definido en R.color.gold_accent o usa Color.parseColor
        // Simplificamos usando un color común para evitar errores de recursos.
        btnFavoritoDetalle.setColorFilter(ContextCompat.getColor(this, R.color.gold_accent))
    }

    // ---------------------------------------------------------------------------------------------
    // VISTA Y EFECTOS
    // ---------------------------------------------------------------------------------------------

    /**
     * Asignación del mensaje final y Fade-In.
     */
    private fun cargarHoroscopoFinal() {
        // La lógica de asignación ya se hizo en cargarDatosDelIntent()
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
}
