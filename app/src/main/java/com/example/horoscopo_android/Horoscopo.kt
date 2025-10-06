package com.example.horoscopo_android

import java.io.Serializable

/**
 * Modelo de datos para un signo del zodiaco en la lista principal.
 * Usa IDs de recursos (Int) para nombres e imágenes, facilitando la localización.
 * Implementa Serializable para ser pasado fácilmente entre Activities (requerido por el Intent).
 *
 * @param id ID interno único del signo (0-11). Se usa para la API y favoritos.
 * @param nombreId Resource ID (R.string.x) del nombre del signo.
 * @param fechasId Resource ID (R.string.x) de las fechas del signo.
 * @param imagenId Resource ID (R.drawable.ic_x) del icono.
 */
data class Horoscopo(
    val id: Int,
    val nombreId: Int,
    val fechasId: Int,
    val imagenId: Int
) : Serializable
