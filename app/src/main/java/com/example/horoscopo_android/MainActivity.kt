package com.example.horoscopo_android

import android.content.Context // NECESARIO para SharedPreferences y getSystemService
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.inputmethod.InputMethodManager // NECESARIO para manejar el teclado
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.example.horoscopo_android.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val horoscopoList = getHoroscopoList()
    private lateinit var horoscopoAdapter: HoroscopoAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private val PREFS_NAME = "HoroscopoPrefs"
    private val FAVORITES_KEY = "FavoriteSigns"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchView()
        setupUIColors()
        setupSearchButton()
    }

    // --- SINCRONIZACIÓN DE FAVORITOS ---
    override fun onResume() {
        super.onResume()
        // Cuando el usuario regresa a la lista, forzamos la actualización para mostrar el estado de favoritos.
        horoscopoAdapter.notifyDataSetChanged()
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
     * Comprueba si un Horoscopo (identificado por su ID de nombre) está marcado como favorito.
     */
    private fun isHoroscopoFavorite(horoscopo: Horoscopo): Boolean {
        val prefs = getSharedPreferences()
        // El ID del recurso (Int) lo convertimos a String para guardarlo en el Set
        val favorites = prefs.getStringSet(FAVORITES_KEY, emptySet()) ?: emptySet()
        return favorites.contains(horoscopo.nombreId.toString())
    }

    /**
     * Alterna el estado de favorito de un Horoscopo.
     */
    private fun toggleHoroscopoFavorite(horoscopo: Horoscopo) {
        val prefs = getSharedPreferences()
        val editor = prefs.edit()

        // 1. Obtener el conjunto actual de favoritos (como MutableSet)
        val currentFavorites = prefs.getStringSet(FAVORITES_KEY, emptySet())?.toMutableSet() ?: mutableSetOf()
        val signId = horoscopo.nombreId.toString()

        // 2. Alternar el estado
        if (currentFavorites.contains(signId)) {
            currentFavorites.remove(signId) // Quitar de favoritos
        } else {
            currentFavorites.add(signId) // Añadir a favoritos
        }

        // 3. Guardar el nuevo conjunto
        editor.putStringSet(FAVORITES_KEY, currentFavorites)
        editor.apply()

        // 4. Notificar al adaptador para que actualice el icono inmediatamente
        horoscopoAdapter.notifyDataSetChanged()
    }


    // ---------------------------------------------------------------------------------------------
    // MANEJO DE VISTA Y EVENTOS
    // ---------------------------------------------------------------------------------------------

    /**
     * Configura el botón dedicado para abrir el teclado y ejecutar la búsqueda (simulando Enter).
     */
    private fun setupSearchButton() {
        binding.btnAbrirTeclado.setOnClickListener {
            binding.searchView.requestFocus()

            // 1. Abrir el teclado
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchView.findFocus(), 0)

            // 2. Ejecutar la búsqueda como si se hubiera presionado ENTER
            val query = binding.searchView.query?.toString()
            if (query != null && query.isNotEmpty()) {
                searchAndScroll(query) // Llama a la búsqueda de fecha/nombre completo
            }
        }
    }

    /**
     * Configura los colores y estilos del componente SearchView (barra de búsqueda).
     */
    private fun setupUIColors() {
        // ... (Tu lógica de configuración de UI existente)
        val searchSrcTextId = resources.getIdentifier("android:id/search_src_text", null, null)
        val hintTextView = binding.searchView.findViewById<TextView>(searchSrcTextId)

        hintTextView?.setTextColor(Color.BLACK)
        hintTextView?.setHintTextColor(Color.BLACK)
        hintTextView?.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 19f)
        hintTextView?.setTypeface(null, Typeface.ITALIC)

        val searchMagIconId = resources.getIdentifier("android:id/search_mag_icon", null, null)
        val searchIcon = binding.searchView.findViewById<ImageView>(searchMagIconId)

        // Color del icono de la lupa -> Negro (o #444444 si lo cambiaste)
        searchIcon?.setColorFilter(Color.BLACK)
    }

    private fun setupRecyclerView() {
        // FIX CRÍTICO: Se pasan los 4 parámetros al adaptador
        horoscopoAdapter = HoroscopoAdapter(
            horoscopoList = horoscopoList,
            onClick = { horoscopo ->
                val intent = Intent(this, DetalleHoroscopoActivity::class.java).apply {
                    putExtra("horoscopo_nombre_id", horoscopo.nombreId)
                    putExtra("horoscopo_fechas_id", horoscopo.fechasId)
                    putExtra("horoscopo_imagen_id", horoscopo.imagenId)
                }
                startActivity(intent)
            },
            // FUNCIÓN 3: Comprueba si es favorito
            isFavoriteChecker = { horoscopo -> isHoroscopoFavorite(horoscopo) },
            // FUNCIÓN 4: Alterna el estado de favorito
            onFavoriteClick = { horoscopo -> toggleHoroscopoFavorite(horoscopo) }
        )

        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewHoroscopos.adapter = horoscopoAdapter
        binding.recyclerViewHoroscopos.layoutManager = layoutManager

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerViewHoroscopos)
    }

    /**
     * Configuración del buscador con comportamiento híbrido.
     */
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            // --- BÚSQUEDA EN TIEMPO REAL (SOLO POR NOMBRE) ---
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null && newText.length > 0) {
                    // Solo busca y desplaza por coincidencia parcial de nombre
                    searchAndScrollByName(newText)
                }
                return true
            }

            // --- BÚSQUEDA FINAL (AL PRESIONAR ENTER) ---
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    // Intenta buscar por nombre y luego por fecha
                    searchAndScroll(query)
                }
                return true
            }
        })
    }

    /**
     * Busca el horóscopo por nombre o fecha y desplaza el RecyclerView.
     * Se usa al presionar ENTER o al pulsar el botón de búsqueda.
     */
    private fun searchAndScroll(query: String) {
        val lowerCaseQuery = query.lowercase(Locale.getDefault())

        val nameMap = mapSignosForSearch()

        // 1. Intenta buscar por coincidencia de nombre completo/parcial o rango de fechas
        val foundHoroscopo = horoscopoList.firstOrNull { horoscopo ->
            val signoEspanol = getString(horoscopo.nombreId).lowercase(Locale.getDefault())
            val signoIngles = nameMap[signoEspanol] ?: ""
            val fechasEspanol = getString(horoscopo.fechasId).lowercase(Locale.getDefault())

            // Búsqueda por nombre o rango de fechas (texto)
            signoEspanol.startsWith(lowerCaseQuery) ||
                    signoIngles.startsWith(lowerCaseQuery) ||
                    fechasEspanol.contains(lowerCaseQuery)
        }

        if (foundHoroscopo != null) {
            scrollToHoroscopo(foundHoroscopo)
        } else {
            // 2. Si no lo encuentra por texto, intenta la búsqueda por fecha exacta
            val horoscopoByDate = findHoroscopoByDate(query)
            if (horoscopoByDate != null) {
                scrollToHoroscopo(horoscopoByDate)
            }
        }
    }

    /**
     * Busca el horóscopo por nombre y desplaza el RecyclerView.
     * Este método se usa en TIEMPO REAL (onQueryTextChange).
     */
    private fun searchAndScrollByName(query: String) {
        val lowerCaseQuery = query.lowercase(Locale.getDefault())
        val nameMap = mapSignosForSearch()

        // Solo busca coincidencias parciales al inicio del nombre.
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
     */
    private fun mapSignosForSearch(): Map<String, String> {
        return mapOf(
            "aries" to "aries",
            "tauro" to "taurus",
            "géminis" to "gemini",
            "cáncer" to "cancer",
            "leo" to "leo",
            "virgo" to "virgo",
            "libra" to "libra",
            "escorpio" to "scorpio",
            "sagitario" to "sagittarius",
            "capricornio" to "capricorn",
            "acuario" to "aquarius",
            "piscis" to "pisces"
        )
    }

    /**
     * Realiza el scroll hasta la posición del horóscopo.
     */
    private fun scrollToHoroscopo(horoscopo: Horoscopo) {
        val position = horoscopoList.indexOf(horoscopo)
        val targetPosition = (horoscopoList.size * 1) + position
        binding.recyclerViewHoroscopos.smoothScrollToPosition(targetPosition)
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

        return when {
            // Capricornio (22 Dic - 19 Ene)
            (month == 12 && day >= 22) || (month == 1 && day <= 19) -> horoscopoList[9]
            // Acuario (20 Ene - 18 Feb)
            (month == 1 && day >= 20) || (month == 2 && day <= 18) -> horoscopoList[10]
            // Piscis (19 Feb - 20 Mar)
            (month == 2 && day >= 19) || (month == 3 && day <= 20) -> horoscopoList[11]
            // Aries (21 Mar - 19 Abr)
            (month == 3 && day >= 21) || (month == 4 && day <= 19) -> horoscopoList[0]
            // Tauro (20 Abr - 20 May)
            (month == 4 && day >= 20) || (month == 5 && day <= 20) -> horoscopoList[1]
            // Géminis (21 May - 20 Jun)
            (month == 5 && day >= 21) || (month == 6 && day <= 20) -> horoscopoList[2]
            // Cáncer (21 Jun - 22 Jul)
            (month == 6 && day >= 21) || (month == 7 && day <= 22) -> horoscopoList[3]
            // Leo (23 Jul - 22 Ago)
            (month == 7 && day >= 23) || (month == 8 && day <= 22) -> horoscopoList[4]
            // Virgo (23 Ago - 22 Sep)
            (month == 8 && day >= 23) || (month == 9 && day <= 22) -> horoscopoList[5]
            // Libra (23 Sep - 22 Oct)
            (month == 9 && day >= 23) || (month == 10 && day <= 22) -> horoscopoList[6]
            // Escorpio (23 Oct - 21 Nov)
            (month == 10 && day >= 23) || (month == 11 && day <= 21) -> horoscopoList[7]
            // Sagitario (22 Nov - 21 Dic)
            (month == 11 && day >= 22) || (month == 12 && day <= 21) -> horoscopoList[8]
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
     * Crea la lista fija de objetos Horoscopo.
     */
    private fun getHoroscopoList(): List<Horoscopo> {
        return listOf(
            Horoscopo(R.string.aries_nombre, R.string.aries_fechas, R.drawable.ic_aries),
            Horoscopo(R.string.tauro_nombre, R.string.tauro_fechas, R.drawable.ic_tauro),
            Horoscopo(R.string.geminis_nombre, R.string.geminis_fechas, R.drawable.ic_geminis),
            Horoscopo(R.string.cancer_nombre, R.string.cancer_fechas, R.drawable.ic_cancer),
            Horoscopo(R.string.leo_nombre, R.string.leo_fechas, R.drawable.ic_leo),
            Horoscopo(R.string.virgo_nombre, R.string.virgo_fechas, R.drawable.ic_virgo),
            Horoscopo(R.string.libra_nombre, R.string.libra_fechas, R.drawable.ic_libra),
            Horoscopo(R.string.escorpio_nombre, R.string.escorpio_fechas, R.drawable.ic_escorpio),
            Horoscopo(R.string.sagitario_nombre, R.string.sagitario_fechas, R.drawable.ic_sagitario),
            Horoscopo(R.string.capricornio_nombre, R.string.capricornio_fechas, R.drawable.ic_capricornio),
            Horoscopo(R.string.acuario_nombre, R.string.acuario_fechas, R.drawable.ic_acuario),
            Horoscopo(R.string.piscis_nombre, R.string.piscis_fechas, R.drawable.ic_piscis)
        )
    }
}