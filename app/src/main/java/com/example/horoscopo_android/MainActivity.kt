package com.example.horoscopo_android

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast // ⬅️ IMPORTAR TOAST
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.example.horoscopo_android.databinding.ActivityMainBinding
import com.example.horoscopo_android.ui.HoroscopeViewModel
import com.example.horoscopo_android.ui.UiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

// La clase Horoscopo debe estar en su propio archivo (Horoscopo.kt) para evitar errores.
// Asumimos que la clase Horoscopo existe y está accesible.

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // DECLARADAS con lateinit var para inicializarlas en onCreate()
    private lateinit var baseHoroscopoList: List<Horoscopo>
    private lateinit var horoscopoList: List<Horoscopo>

    private lateinit var horoscopoAdapter: HoroscopoAdapter
    private lateinit var layoutManager: LinearLayoutManager

    // Inyección del ViewModel para la lógica de API
    private val viewModel: HoroscopeViewModel by viewModels()

    private val PREFS_NAME = "HoroscopoPrefs"
    private val FAVORITES_KEY = "FavoriteSigns"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ---------------------------------------------------------------------
        // ✅ SOLUCIÓN: Inicialización movida a onCreate, donde el Context está disponible
        baseHoroscopoList = getBaseHoroscopoList()
        horoscopoList = getSortedHoroscopoList()
        // ---------------------------------------------------------------------

        setupRecyclerView()
        setupSearchView()
        setupUIColors()
        setupSearchButton() // Se mantiene la lógica del botón para abrir el teclado

        // 4. Observar el estado de la API para navegar
        collectUiState()
    }

    // --- SINCRONIZACIÓN DE FAVORITOS ---
    override fun onResume() {
        super.onResume()
        // Cuando regresamos a la Activity, reordenamos la lista y la actualizamos.
        horoscopoList = getSortedHoroscopoList()
        horoscopoAdapter.updateList(horoscopoList)
    }
    // -----------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------
    // GESTIÓN DE FAVORITOS (SharedPreferences)
    // ---------------------------------------------------------------------------------------------

    /**
     * Obtiene la instancia de SharedPreferences.
     */
    private fun getSharedPreferences() = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Comprueba si un Horoscopo (identificado por su ID) está marcado como favorito.
     */
    private fun isHoroscopoFavorite(horoscopo: Horoscopo): Boolean {
        // Se llama de forma segura dentro de setupRecyclerView
        val prefs = getSharedPreferences()
        // Usamos el ID del signo como clave única para SharedPreferences.
        val favorites = prefs.getStringSet(FAVORITES_KEY, emptySet()) ?: emptySet()
        return favorites.contains(horoscopo.id.toString())
    }

    /**
     * Alterna el estado de favorito de un Horoscopo.
     */
    private fun toggleHoroscopoFavorite(horoscopo: Horoscopo) {
        val prefs = getSharedPreferences()
        val editor = prefs.edit()

        val currentFavorites = prefs.getStringSet(FAVORITES_KEY, emptySet())?.toMutableSet() ?: mutableSetOf()
        // Usamos el ID del signo para la clave de SharedPreferences.
        val signId = horoscopo.id.toString()

        if (currentFavorites.contains(signId)) {
            currentFavorites.remove(signId) // Quitar de favoritos
        } else {
            currentFavorites.add(signId) // Añadir a favoritos
        }

        editor.putStringSet(FAVORITES_KEY, currentFavorites)
        editor.apply()

        // 1. Reordenar la lista inmediatamente para que el signo se mueva al inicio/al final
        horoscopoList = getSortedHoroscopoList()

        // 2. Notificar al adaptador con la nueva lista ordenada
        horoscopoAdapter.updateList(horoscopoList)
    }

    /**
     * Lógica para ordenar la lista: Favoritos primero, el resto después.
     */
    private fun getSortedHoroscopoList(): List<Horoscopo> {
        // La lista base es la que no cambia, usamos la lista BASE para calcular favoritos.
        // baseHoroscopoList ya está inicializada en onCreate()
        val favoriteSigns = baseHoroscopoList.filter { isHoroscopoFavorite(it) }
        val nonFavoriteSigns = baseHoroscopoList.filterNot { isHoroscopoFavorite(it) }

        // Retorna la lista con los favoritos primero
        return favoriteSigns + nonFavoriteSigns
    }


    // ---------------------------------------------------------------------------------------------
    // GESTIÓN DEL VIEWMODEL Y ESTADO (API)
    // ---------------------------------------------------------------------------------------------

    /**
     * Recolecta el estado del ViewModel para manejar la navegación a la actividad de detalle.
     */
    private fun collectUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    when (state) {
                        is UiState.Loading -> {
                            Log.d("API_CALL", "Cargando horóscopo...")
                            // Aquí podrías mostrar un ProgressBar global o bloquear la UI
                        }
                        is UiState.Success -> {
                            // Navegar a la pantalla de detalle con los datos finales
                            val intent = Intent(this@MainActivity, DetalleHoroscopoActivity::class.java).apply {
                                // Datos del signo (Resource IDs)
                                putExtra("horoscopo_nombre_id", state.horoscopo.nombreId)
                                putExtra("horoscopo_fechas_id", state.horoscopo.fechasId)
                                putExtra("horoscopo_imagen_id", state.horoscopo.imagenId)

                                // Datos de la API (Texto del horóscopo y proveedor)
                                putExtra("horoscopo_api_data", state.apiData.horoscope)
                                putExtra("horoscopo_api_provider", state.apiData.provider)
                            }
                            startActivity(intent)

                            // 3. Resetear el estado después de la navegación
                            viewModel.resetState()
                        }
                        is UiState.Error -> {
                            // Manejo de errores
                            Log.e("API_CALL", "Error final al cargar horóscopo: ${state.message}")
                            // ⭐ MOSTRAR TOAST: Informa al usuario que la API falló y por eso no hay navegación.
                            Toast.makeText(this@MainActivity, "Error al cargar el horóscopo: ${state.message}", Toast.LENGTH_LONG).show()

                            viewModel.resetState()
                        }
                        // Usamos 'else' para cubrir el estado 'Idle'
                        else -> { /* No hacer nada o manejar estado inicial/desconocido */ }
                    }
                }
            }
        }
    }


    // ---------------------------------------------------------------------------------------------
    // MANEJO DE VISTA Y EVENTOS
    // ---------------------------------------------------------------------------------------------

    private fun setupSearchButton() {
        // Lógica del botón para abrir el teclado
        binding.btnAbrirTeclado.setOnClickListener {
            binding.searchView.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchView.findFocus(), 0)

            val query = binding.searchView.query?.toString()
            if (query != null && query.isNotEmpty()) {
                searchAndScroll(query)
            }
        }

        // El botón btnToggleFavorite se oculta o se usa dentro del RecyclerView si es necesario
        binding.btnToggleFavorite.visibility = View.GONE
    }

    private fun setupUIColors() {
        val searchSrcTextId = resources.getIdentifier("android:id/search_src_text", null, null)
        val hintTextView = binding.searchView.findViewById<TextView>(searchSrcTextId)

        hintTextView?.setTextColor(Color.BLACK)
        hintTextView?.setHintTextColor(Color.BLACK)
        hintTextView?.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 19f)
        hintTextView?.setTypeface(null, Typeface.ITALIC)

        val searchMagIconId = resources.getIdentifier("android:id/search_mag_icon", null, null)
        val searchIcon = binding.searchView.findViewById<ImageView>(searchMagIconId)

        searchIcon?.setColorFilter(Color.BLACK)
    }

    private fun setupRecyclerView() {
        // La lista inicial ya está ordenada por favoritos
        horoscopoAdapter = HoroscopoAdapter(
            horoscopoList = horoscopoList,
            onClick = { horoscopo ->
                // ⭐ AÑADIDO: Muestra un Toast inmediatamente para confirmar el clic y el inicio de la carga.
                Toast.makeText(this, "Cargando horóscopo para ${getString(horoscopo.nombreId)}...", Toast.LENGTH_SHORT).show()

                // 1. Iniciar la llamada a la API a través del ViewModel
                viewModel.fetchHoroscope(horoscopo)
            },
            // Se pasa la función de la Activity para comprobar el estado de favorito
            isFavoriteChecker = { horoscopo -> isHoroscopoFavorite(horoscopo) },
            // Se pasa la función de la Activity para alternar el estado de favorito
            onFavoriteClick = { horoscopo -> toggleHoroscopoFavorite(horoscopo) }
        )

        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewHoroscopos.adapter = horoscopoAdapter
        binding.recyclerViewHoroscopos.layoutManager = layoutManager

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerViewHoroscopos)
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null && newText.length > 0) {
                    searchAndScrollByName(newText)
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    searchAndScroll(query)
                }
                return true
            }
        })
    }

    /**
     * Busca el horóscopo por nombre o fecha y desplaza el RecyclerView.
     */
    private fun searchAndScroll(query: String) {
        val lowerCaseQuery = query.lowercase(Locale.getDefault())
        val nameMap = mapSignosForSearch()

        // Buscamos en la lista ACTUALMENTE ORDENADA (horoscopoList)
        val foundHoroscopo = horoscopoList.firstOrNull { horoscopo ->
            val signoEspanol = getString(horoscopo.nombreId).lowercase(Locale.getDefault())
            // Usamos el nombre del signo en español para el mapeo, luego verificamos el inglés
            val signoIngles = nameMap[signoEspanol] ?: ""
            val fechasEspanol = getString(horoscopo.fechasId).lowercase(Locale.getDefault())

            signoEspanol.startsWith(lowerCaseQuery) ||
                    signoIngles.startsWith(lowerCaseQuery) ||
                    fechasEspanol.contains(lowerCaseQuery)
        }

        if (foundHoroscopo != null) {
            scrollToHoroscopo(foundHoroscopo)
        } else {
            val horoscopoByDate = findHoroscopoByDate(query)
            if (horoscopoByDate != null) {
                scrollToHoroscopo(horoscopoByDate)
            }
        }
    }

    /**
     * Busca el horóscopo por nombre y desplaza el RecyclerView (tiempo real).
     */
    private fun searchAndScrollByName(query: String) {
        val lowerCaseQuery = query.lowercase(Locale.getDefault())
        val nameMap = mapSignosForSearch()

        // Buscamos en la lista ACTUALMENTE ORDENADA (horoscopoList)
        val foundHoroscopo = horoscopoList.firstOrNull { horoscopo ->
            val signoEspanol = getString(horoscopo.nombreId).lowercase(Locale.getDefault())
            val signoIngles = nameMap[signoEspanol] ?: ""

            signoEspanol.startsWith(lowerCaseQuery) ||
                    signoIngles.startsWith(lowerCaseQuery)
        }

        if (foundHoroscopo != null) {
            scrollToHoroscopo(foundHoroscopo)
        }
    }


    /**
     * Mapea el nombre del signo en español al nombre en inglés para facilitar la búsqueda.
     * Los nombres deben ser en minúsculas.
     */
    private fun mapSignosForSearch(): Map<String, String> {
        return mapOf(
            getString(R.string.aries_nombre).lowercase(Locale.getDefault()) to "aries",
            getString(R.string.tauro_nombre).lowercase(Locale.getDefault()) to "taurus",
            getString(R.string.geminis_nombre).lowercase(Locale.getDefault()) to "gemini",
            getString(R.string.cancer_nombre).lowercase(Locale.getDefault()) to "cancer",
            getString(R.string.leo_nombre).lowercase(Locale.getDefault()) to "leo",
            getString(R.string.virgo_nombre).lowercase(Locale.getDefault()) to "virgo",
            getString(R.string.libra_nombre).lowercase(Locale.getDefault()) to "libra",
            getString(R.string.escorpio_nombre).lowercase(Locale.getDefault()) to "scorpio",
            getString(R.string.sagitario_nombre).lowercase(Locale.getDefault()) to "sagittarius",
            getString(R.string.capricornio_nombre).lowercase(Locale.getDefault()) to "capricorn",
            getString(R.string.acuario_nombre).lowercase(Locale.getDefault()) to "aquarius",
            getString(R.string.piscis_nombre).lowercase(Locale.getDefault()) to "pisces"
        )
    }

    /**
     * Realiza el scroll hasta la posición del horóscopo.
     */
    private fun scrollToHoroscopo(horoscopo: Horoscopo) {
        // Buscamos la posición en la lista ACTUALMENTE ORDENADA
        val position = horoscopoList.indexOf(horoscopo)
        // Usamos la posición para el RecyclerView
        binding.recyclerViewHoroscopos.smoothScrollToPosition(position)
    }

    /**
     * Determina el horóscopo basado en una fecha dada.
     */
    private fun findHoroscopoByDate(query: String): Horoscopo? {
        val regex = "(\\d{1,2})\\s*(?:de\\s*)?(\\p{L}+)".toRegex()
        val matchResult = regex.find(query.lowercase(Locale.getDefault()))
        if (matchResult == null) {
            return null
        }

        val (dayString, monthString) = matchResult.destructured
        val day = dayString.toIntOrNull() ?: return null
        val month = mapMonthNames(monthString) ?: return null

        // Usamos baseHoroscopoList (orden cronológico) para índices fijos:
        return when {
            // Capricornio (22 Dic - 19 Ene) -> Índice 9
            (month == 12 && day >= 22) || (month == 1 && day <= 19) -> baseHoroscopoList[9]
            // Acuario (20 Ene - 18 Feb) -> Índice 10
            (month == 1 && day >= 20) || (month == 2 && day <= 18) -> baseHoroscopoList[10]
            // Piscis (19 Feb - 20 Mar) -> Índice 11
            (month == 2 && day >= 19) || (month == 3 && day <= 20) -> baseHoroscopoList[11]
            // Aries (21 Mar - 19 Abr) -> Índice 0
            (month == 3 && day >= 21) || (month == 4 && day <= 19) -> baseHoroscopoList[0]
            // Tauro (20 Abr - 20 May) -> Índice 1
            (month == 4 && day >= 20) || (month == 5 && day <= 20) -> baseHoroscopoList[1]
            // Géminis (21 May - 20 Jun) -> Índice 2
            (month == 5 && day >= 21) || (month == 6 && day <= 20) -> baseHoroscopoList[2]
            // Cáncer (21 Jun - 22 Jul) -> Índice 3
            (month == 6 && day >= 21) || (month == 7 && day <= 22) -> baseHoroscopoList[3]
            // Leo (23 Jul - 22 Ago) -> Índice 4
            (month == 7 && day >= 23) || (month == 8 && day <= 22) -> baseHoroscopoList[4]
            // Virgo (23 Ago - 22 Sep) -> Índice 5
            (month == 8 && day >= 23) || (month == 9 && day <= 22) -> baseHoroscopoList[5]
            // Libra (23 Sep - 22 Oct) -> Índice 6
            (month == 9 && day >= 23) || (month == 10 && day <= 22) -> baseHoroscopoList[6]
            // Escorpio (23 Oct - 21 Nov) -> Índice 7
            (month == 10 && day >= 23) || (month == 11 && day <= 21) -> baseHoroscopoList[7]
            // Sagitario (22 Nov - 21 Dic) -> Índice 8
            (month == 11 && day >= 22) || (month == 12 && day <= 21) -> baseHoroscopoList[8]
            else -> null
        }
    }

    /**
     * Convierte el nombre de un mes a su número.
     */
    private fun mapMonthNames(monthName: String): Int? {
        return when (monthName) {
            "enero", "january" -> 1
            "febrero", "february" -> 2
            "marzo", "march" -> 3
            "abril", "april" -> 4
            "mayo", "may" -> 5
            "junio", "june" -> 6
            "julio", "july" -> 7
            "agosto", "august" -> 8
            "septiembre", "september" -> 9
            "octubre", "october" -> 10
            "noviembre", "november" -> 11
            "diciembre", "december" -> 12
            else -> null
        }
    }

    /**
     * Crea la lista fija original de objetos Horoscopo, incluyendo el ID único (0-11).
     * NOTA: Esta función asume que tienes definidos los R.string y R.drawable correspondientes.
     * Esta función depende de getString(), por lo que debe llamarse después de onCreate().
     */
    private fun getBaseHoroscopoList(): List<Horoscopo> {
        return listOf(
            Horoscopo(0, R.string.aries_nombre, R.string.aries_fechas, R.drawable.ic_aries),
            Horoscopo(1, R.string.tauro_nombre, R.string.tauro_fechas, R.drawable.ic_tauro),
            Horoscopo(2, R.string.geminis_nombre, R.string.geminis_fechas, R.drawable.ic_geminis),
            Horoscopo(3, R.string.cancer_nombre, R.string.cancer_fechas, R.drawable.ic_cancer),
            Horoscopo(4, R.string.leo_nombre, R.string.leo_fechas, R.drawable.ic_leo),
            Horoscopo(5, R.string.virgo_nombre, R.string.virgo_fechas, R.drawable.ic_virgo),
            Horoscopo(6, R.string.libra_nombre, R.string.libra_fechas, R.drawable.ic_libra),
            Horoscopo(7, R.string.escorpio_nombre, R.string.escorpio_fechas, R.drawable.ic_escorpio),
            Horoscopo(8, R.string.sagitario_nombre, R.string.sagitario_fechas, R.drawable.ic_sagitario),
            Horoscopo(9, R.string.capricornio_nombre, R.string.capricornio_fechas, R.drawable.ic_capricornio),
            Horoscopo(10, R.string.acuario_nombre, R.string.acuario_fechas, R.drawable.ic_acuario),
            Horoscopo(11, R.string.piscis_nombre, R.string.piscis_fechas, R.drawable.ic_piscis)
        )
    }
}
