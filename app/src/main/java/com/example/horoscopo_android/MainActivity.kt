package com.example.horoscopo_android

import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
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
        setupTranslateButton()
        setupSearchView()
    }

    private fun setupRecyclerView() {
        horoscopoAdapter = HoroscopoAdapter(horoscopoList) { horoscopo ->
            val intent = Intent(this, DetalleHoroscopoActivity::class.java)
            intent.putExtra("horoscopo_nombre_id", horoscopo.nombreId)
            intent.putExtra("horoscopo_fechas_id", horoscopo.fechasId)
            intent.putExtra("horoscopo_imagen_id", horoscopo.imagenId)
            // 💡 CORRECCIÓN 1: Eliminada la línea de putExtra, ya que el mensaje
            // se carga mediante la API dentro de DetalleHoroscopoActivity.kt
            startActivity(intent)
        }

        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewHoroscopos.adapter = horoscopoAdapter
        binding.recyclerViewHoroscopos.layoutManager = layoutManager

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerViewHoroscopos)
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    searchAndScroll(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun searchAndScroll(query: String) {
        val lowerCaseQuery = query.lowercase(Locale.getDefault())

        val foundHoroscopo = horoscopoList.firstOrNull {
            getString(it.nombreId).lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                    getString(it.fechasId).lowercase(Locale.getDefault()).contains(lowerCaseQuery)
        }

        if (foundHoroscopo != null) {
            val position = horoscopoList.indexOf(foundHoroscopo)
            val targetPosition = (horoscopoList.size * 1) + position
            binding.recyclerViewHoroscopos.smoothScrollToPosition(targetPosition)
        } else {
            // Lógica avanzada: si no se encuentra por nombre, busca por fecha
            val horoscopoByDate = findHoroscopoByDate(query)
            if (horoscopoByDate != null) {
                val position = horoscopoList.indexOf(horoscopoByDate)
                val targetPosition = (horoscopoList.size * 1) + position
                binding.recyclerViewHoroscopos.smoothScrollToPosition(targetPosition)
            }
        }
    }

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

    private fun getHoroscopoList(): List<Horoscopo> {
        // 💡 CORRECCIÓN 2: Eliminado el cuarto parámetro (R.array.xxx_mensaje) de todos los constructores
        // para coincidir con la nueva estructura de la clase Horoscopo (que solo necesita tres parámetros).
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

    private fun setupTranslateButton() {
        binding.botonTraducir.setOnClickListener {
            val currentLocale = resources.configuration.locales[0]
            val newLocale = if (currentLocale.language == "es") Locale("en") else Locale("es")

            val config = resources.configuration
            config.setLocale(newLocale)
            resources.updateConfiguration(config, resources.displayMetrics)

            recreate()
        }
    }
}
