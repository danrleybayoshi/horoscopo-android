package com.example.horoscopo_android

import com.example.horoscopo_android.R // Importación necesaria para acceder a los recursos

/**
 * Objeto que contiene la lista estática de los 12 signos del zodiaco.
 */
object HoroscopoData {

    /**
     * Lista base de los 12 signos del zodiaco usando Resource IDs.
     */
    val horoscopoList: List<Horoscopo> = listOf(
        // Asegúrate de que tus archivos R.string y R.drawable coincidan con estos nombres
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

    /**
     * Mapea el ID del signo a su objeto Horoscopo.
     */
    fun getHoroscopoById(id: Int): Horoscopo? {
        return horoscopoList.find { it.id == id }
    }

    /**
     * Mapea el ID del signo al nombre que espera la API (ej: 'aries').
     */
    fun getSignNameById(context: Context, id: Int): String? {
        val horoscopo = getHoroscopoById(id)
        return horoscopo?.let {
            context.getString(it.nombreId).lowercase()
        }
    }
}
