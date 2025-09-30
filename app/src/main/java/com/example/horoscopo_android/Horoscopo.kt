package com.example.horoscopo_android

/**
 * Clase de datos que representa un signo del horóscopo.
 * Contiene referencias a recursos (R.string, R.drawable) para la UI.
 *
 * @param id Identificador único (0-11) usado para ordenar y guardar favoritos.
 * @param nombreId Resource ID del nombre del signo (ej: R.string.aries_nombre).
 * @param fechasId Resource ID de las fechas del signo (ej: R.string.aries_fechas).
 * @param imagenId Resource ID de la imagen/icono del signo (ej: R.drawable.ic_aries).
 */
data class Horoscopo(
    val id: Int,
    val nombreId: Int,
    val fechasId: Int,
    val imagenId: Int
) {
    /**
     * Mapea el ID interno del Horóscopo a un nombre de signo en minúsculas para la API.
     * Esta función es crucial para formar la URL de consulta de la API.
     */
    fun getApiSignName(): String {
        return when (id) {
            0 -> "aries"
            1 -> "tauro"
            2 -> "geminis"
            3 -> "cancer"
            4 -> "leo"
            5 -> "virgo"
            6 -> "libra"
            7 -> "escorpio"
            8 -> "sagitario"
            9 -> "capricornio"
            10 -> "acuario"
            11 -> "piscis"
            else -> throw IllegalArgumentException("ID de horóscopo no válido: $id")
        }
    }
}
