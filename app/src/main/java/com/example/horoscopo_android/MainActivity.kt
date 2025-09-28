package com.example.horoscopo_android

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface // Importación necesaria para Typeface
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchView()
        setupUIColors() // Configura los colores y estilos de la SearchView
    }

    /**
     * Configura los colores y estilos del componente SearchView (barra de búsqueda).
     */
    private fun setupUIColors() {
        // --- 1. CONFIGURACIÓN DEL TEXTVIEW INTERNO (Texto de búsqueda y Pista) ---

        // Obtiene el ID del TextView interno que maneja el texto y el hint
        val searchSrcTextId = resources.getIdentifier("android:id/search_src_text", null, null)
        val hintTextView = binding.searchView.findViewById<TextView>(searchSrcTextId)

        // A. Color del texto que el usuario escribe -> Negro
        hintTextView?.setTextColor(Color.BLACK)

        // B. Color de la pista (hint) -> Negro más oscuro
        hintTextView?.setHintTextColor(Color.BLACK)

        // C. Aumentar el tamaño del texto y ponerlo en cursiva (sin negrita)
        hintTextView?.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 19f)
        // ✅ MODIFICACIÓN: Cambiamos Typeface.BOLD por Typeface.ITALIC
        hintTextView?.setTypeface(null, Typeface.ITALIC)

        // --- 2. CONFIGURACIÓN DEL ICONO DE LA LUPA ---

        // Obtiene el ID del ImageView interno que contiene el icono de la lupa
        val searchMagIconId = resources.getIdentifier("android:id/search_mag_icon", null, null)
        val searchIcon = binding.searchView.findViewById<ImageView>(searchMagIconId)

        // D. Color del icono de la lupa -> Negro
        searchIcon?.setColorFilter(Color.BLACK)
    }

    /**
     * Configura el RecyclerView, el adaptador y el LinearSnapHelper.
     */
    private fun setupRecyclerView() {
        // Inicializa el adaptador con la lista de horóscopos y el listener de clic
        horoscopoAdapter = HoroscopoAdapter(horoscopoList) { horoscopo ->
            val intent = Intent(this, DetalleHoroscopoActivity::class.java).apply {
                putExtra("horoscopo_nombre_id", horoscopo.nombreId)
                putExtra("horoscopo_fechas_id", horoscopo.fechasId)
                putExtra("horoscopo_imagen_id", horoscopo.imagenId)
            }
            startActivity(intent)
        }

        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewHoroscopos.adapter = horoscopoAdapter
        binding.recyclerViewHoroscopos.layoutManager = layoutManager

        // Añade LinearSnapHelper para que el scroll se detenga en el centro de cada elemento
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerViewHoroscopos)
    }

    /**
     * Configura el listener para la barra de búsqueda.
     */
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    searchAndScroll(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // No se realiza ninguna acción en tiempo real
                return false
            }
        })
    }

    /**
     * Busca el horóscopo por nombre o fecha y desplaza el RecyclerView.
     */
    private fun searchAndScroll(query: String) {
        val lowerCaseQuery = query.lowercase(Locale.getDefault())

        // 1. Búsqueda por nombre o rango de fechas (texto)
        val foundHoroscopo = horoscopoList.firstOrNull {
            getString(it.nombreId).lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                    getString(it.fechasId).lowercase(Locale.getDefault()).contains(lowerCaseQuery)
        }

        if (foundHoroscopo != null) {
            scrollToHoroscopo(foundHoroscopo)
        } else {
            // 2. Búsqueda por fecha exacta (ej: "15 de marzo")
            val horoscopoByDate = findHoroscopoByDate(query)
            if (horoscopoByDate != null) {
                scrollToHoroscopo(horoscopoByDate)
            }
        }
    }

    /**
     * Calcula la posición y realiza el scroll.
     */
    private fun scrollToHoroscopo(horoscopo: Horoscopo) {
        val position = horoscopoList.indexOf(horoscopo)
        // Se multiplica por un número grande para simular un carrusel infinito
        val targetPosition = (horoscopoList.size * 1) + position
        binding.recyclerViewHoroscopos.smoothScrollToPosition(targetPosition)
    }

    /**
     * Determina el horóscopo basado en una fecha dada (ej: "15 de marzo").
     */
    private fun findHoroscopoByDate(query: String): Horoscopo? {
        val regex = "(\\d{1,2}) de (\\p{L}+)".toRegex()
        val matchResult = regex.find(query.lowercase(Locale.getDefault()))
        if (matchResult == null) {
            return null
        }

        val (dayString, monthString) = matchResult.destructured
        val day = dayString.toIntOrNull() ?: return null
        val month = getMonthNumber(monthString) ?: return null

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
     * Convierte el nombre de un mes en español (en minúsculas) a su número (1 a 12).
     */
    private fun getMonthNumber(monthName: String): Int? {
        return when (monthName) {
            "enero" -> 1
            "febrero" -> 2
            "marzo" -> 3
            "abril" -> 4
            "mayo" -> 5
            "junio" -> 6
            "julio" -> 7
            "agosto" -> 8
            "septiembre" -> 9
            "octubre" -> 10
            "noviembre" -> 11
            "diciembre" -> 12
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
