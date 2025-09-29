package com.example.horoscopo_android

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton // IMPORTACIÓN NECESARIA para manejar el botón de favoritos
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HoroscopoAdapter(
    private val horoscopoList: List<Horoscopo>,
    private val onClick: (Horoscopo) -> Unit,
    // Parámetros para la funcionalidad de favoritos
    private val isFavoriteChecker: (Horoscopo) -> Boolean,
    private val onFavoriteClick: (Horoscopo) -> Unit
) : RecyclerView.Adapter<HoroscopoAdapter.HoroscopoViewHolder>() {

    private val REPEAT_COUNT = 1000

    class HoroscopoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivSigno: ImageView = view.findViewById(R.id.ivSigno)
        val tvNombreSigno: TextView = view.findViewById(R.id.tvNombreSigno)
        val tvFechas: TextView = view.findViewById(R.id.tvFechas)
        // FIX: Se agrega la referencia a btnFavorito para resolver el error
        val btnFavorito: ImageButton = view.findViewById(R.id.btnFavorito)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoroscopoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_horoscopo, parent, false)
        return HoroscopoViewHolder(view)
    }

    override fun onBindViewHolder(holder: HoroscopoViewHolder, position: Int) {
        val realPosition = position % horoscopoList.size
        val horoscopo = horoscopoList[realPosition]

        holder.ivSigno.setImageResource(horoscopo.imagenId)
        holder.tvNombreSigno.setText(horoscopo.nombreId)
        holder.tvFechas.setText(horoscopo.fechasId)

        // 1. Lógica de Favoritos (Inicialización)
        updateFavoriteIcon(holder.btnFavorito, horoscopo)

        // 2. Click Listener del Ítem Completo (Navegación)
        holder.itemView.setOnClickListener {
            onClick(horoscopo)
        }

        // 3. Click Listener del Botón de Favoritos (Toggle)
        holder.btnFavorito.setOnClickListener {
            // Llama al callback en MainActivity para guardar el estado
            onFavoriteClick(horoscopo)
            // Actualiza el icono inmediatamente
            updateFavoriteIcon(holder.btnFavorito, horoscopo)
        }
    }

    /**
     * Helper para actualizar el icono del botón de favorito.
     */
    private fun updateFavoriteIcon(button: ImageButton, horoscopo: Horoscopo) {
        if (isFavoriteChecker(horoscopo)) {
            button.setImageResource(R.drawable.ic_favorite_filled)
        } else {
            button.setImageResource(R.drawable.ic_favorite_border)
        }
    }


    override fun getItemCount(): Int = horoscopoList.size * REPEAT_COUNT
}
