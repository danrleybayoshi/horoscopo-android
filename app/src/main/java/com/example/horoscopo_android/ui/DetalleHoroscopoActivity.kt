package com.example.horoscopo_android

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.horoscopo_android.databinding.ActivityDetalleHoroscopoBinding
import com.example.horoscopo_android.ui.HoroscopeViewModel
import com.example.horoscopo_android.ui.UiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DetalleHoroscopoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleHoroscopoBinding

    // Usamos by viewModels() para inicializar el ViewModel
    private val viewModel: HoroscopeViewModel by viewModels()

    // Variables para el estado de la UI
    private var currentHoroscopoId: Int = -1
    private var currentHoroscopoName: String? = null
    private var scrollAnimator: ValueAnimator? = null
    private var currentSelectedButton: TextView? = null

    // Nombres de las claves para pasar datos entre Activities
    companion object {
        const val EXTRA_HOROSCOPE_SIGN_NAME = "extra_horoscope_sign_name"
        const val EXTRA_HOROSCOPE_SIGN_ID = "extra_horoscope_sign_id"
        const val EXTRA_HOROSCOPE_NOMBRE_ID = "extra_horoscope_nombre_id"
        const val EXTRA_HOROSCOPE_FECHAS_ID = "extra_horoscope_fechas_id"
        const val EXTRA_HOROSCOPE_IMAGEN_ID = "extra_horoscope_imagen_id"

        // Clave para guardar el estado de favoritos
        private const val PREFS_NAME = "HoroscopoPrefs"
        private const val FAVORITES_KEY = "FavoriteSigns"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleHoroscopoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        // 1. Obtener datos del Intent
        val extras = intent.extras ?: run { finish(); return }
        currentHoroscopoId = extras.getInt(EXTRA_HOROSCOPE_SIGN_ID)
        currentHoroscopoName = extras.getString(EXTRA_HOROSCOPE_SIGN_NAME)
        val nombreId = extras.getInt(EXTRA_HOROSCOPE_NOMBRE_ID)
        val fechasId = extras.getInt(EXTRA_HOROSCOPE_FECHAS_ID)
        val imagenId = extras.getInt(EXTRA_HOROSCOPE_IMAGEN_ID)

        // 2. Configurar la UI base del signo
        setupSignUI(nombreId, fechasId, imagenId)
        setupListeners()
        setupObservers()
        setupFavoriteButton()

        // 3. Inicializar el botón "Día" como seleccionado por defecto y cargar el horóscopo diario
        selectButton(binding.btnDia)
        if (currentHoroscopoId != -1) {
            viewModel.fetchHoroscope(this, currentHoroscopoId, "daily")
        }
    }

    // ---------------------------------------------------------------------------------------------
    // CONFIGURACIÓN INICIAL
    // ---------------------------------------------------------------------------------------------

    private fun setupSignUI(nombreId: Int, fechasId: Int, imagenId: Int) {
        binding.tvTituloSigno.text = getString(nombreId)
        binding.tvFechas.text = getString(fechasId)
        binding.ivSignIcon.setImageResource(imagenId)
    }

    private fun setupListeners() {
        binding.btnDia.setOnClickListener {
            loadHoroscope("daily", binding.btnDia)
        }
        binding.btnSemana.setOnClickListener {
            loadHoroscope("weekly", binding.btnSemana)
        }
        binding.btnMes.setOnClickListener {
            loadHoroscope("monthly", binding.btnMes)
        }
        binding.btnFavorito.setOnClickListener {
            toggleFavorite()
        }
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            // Recoger el estado de la UI de forma segura
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.tvMensajeHoroscopo.text = "Cargando..."
                            binding.tvMensajeHoroscopo.visibility = TextView.VISIBLE
                            scrollAnimator?.cancel() // Detener scroll
                        }
                        is UiState.Success -> {
                            // Mostrar el mensaje y activar el scroll
                            binding.tvMensajeHoroscopo.text = state.apiData.data.description
                            binding.tvMensajeHoroscopo.visibility = TextView.VISIBLE
                            startAutoScroll()
                        }
                        is UiState.Error -> {
                            binding.tvMensajeHoroscopo.text = "Error: ${state.message}"
                            binding.tvMensajeHoroscopo.visibility = TextView.VISIBLE
                            scrollAnimator?.cancel()
                        }
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // GESTIÓN DE TIEMPO Y VISTAS
    // ---------------------------------------------------------------------------------------------

    private fun loadHoroscope(timeframe: String, button: TextView) {
        scrollAnimator?.cancel() // Detener cualquier scroll previo
        binding.tvMensajeHoroscopo.visibility = TextView.INVISIBLE // Ocultar el texto inmediatamente
        selectButton(button) // Marcar el nuevo botón

        // Llamar al ViewModel
        if (currentHoroscopoId != -1) {
            viewModel.fetchHoroscope(this, currentHoroscopoId, timeframe)
        }
    }

    private fun selectButton(button: TextView) {
        currentSelectedButton?.isSelected = false
        button.isSelected = true
        currentSelectedButton = button
    }

    // ---------------------------------------------------------------------------------------------
    // ANIMACIÓN
    // ---------------------------------------------------------------------------------------------

    /**
     * Implementación del Auto-Scroll en Bucle con Fade-In
     */
    private fun startAutoScroll() {
        scrollAnimator?.cancel() // Cancelar scroll anterior

        val scrollView = binding.mensajeScrollView
        val textView = binding.tvMensajeHoroscopo

        // Asegúrate de que el texto esté visible antes de medir
        if (textView.height == 0) {
            scrollView.post { startAutoScroll() } // Reintenta después del layout
            return
        }

        // Calcular el desplazamiento total
        val scrollRange = textView.height - scrollView.height
        if (scrollRange <= 0) {
            // No hay suficiente contenido para hacer scroll
            return
        }

        // Crear animador de valor para el scroll
        scrollAnimator = ValueAnimator.ofInt(0, scrollRange).apply {
            duration = 20000 // Duración del scroll (20 segundos)
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()

            addUpdateListener { animator ->
                scrollView.scrollTo(0, animator.animatedValue as Int)
            }
            start()
        }
    }

    override fun onPause() {
        super.onPause()
        scrollAnimator?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        scrollAnimator?.cancel()
    }

    // ---------------------------------------------------------------------------------------------
    // GESTIÓN DE FAVORITOS
    // ---------------------------------------------------------------------------------------------

    private fun setupFavoriteButton() {
        updateFavoriteIcon(isFavorite(currentHoroscopoId))
    }

    private fun isFavorite(id: Int): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val favorites = prefs.getStringSet(FAVORITES_KEY, emptySet()) ?: emptySet()
        return favorites.contains(id.toString())
    }

    private fun toggleFavorite() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val favorites = prefs.getStringSet(FAVORITES_KEY, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val idString = currentHoroscopoId.toString()

        val isFav = isFavorite(currentHoroscopoId)
        if (isFav) {
            favorites.remove(idString)
        } else {
            favorites.add(idString)
        }

        editor.putStringSet(FAVORITES_KEY, favorites)
        editor.apply()
        updateFavoriteIcon(!isFav)
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        val icon = if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        binding.btnFavorito.setImageResource(icon)
    }
}
