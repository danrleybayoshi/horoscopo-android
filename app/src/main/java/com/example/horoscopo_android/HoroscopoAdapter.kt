package com.example.horoscopo_android

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat // Necesario para getColorFilter

class HoroscopoAdapter(
    // CAMBIO CLAVE: Cambiamos 'val' por 'var' para que la lista pueda ser reasignada
    private var horoscopoList: List<Horoscopo>,
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
            // Llama al callback en MainActivity, que guarda el estado y reordena la lista.
            onFavoriteClick(horoscopo)

            // La llamada a updateFavoriteIcon aquí solo proporciona una retroalimentación visual
            // instantánea para el item que se acaba de tocar.
            updateFavoriteIcon(holder.btnFavorito, horoscopo)
        }
    }

    /**
     * MÉTODO CLAVE: Permite a MainActivity pasar la nueva lista reordenada
     * y forzar el refresco del RecyclerView.
     */
    fun updateList(newList: List<Horoscopo>) {
        this.horoscopoList = newList
        notifyDataSetChanged()
    }

    /**
     * Helper para actualizar el icono del botón de favorito y aplicar el color.
     */
    private fun updateFavoriteIcon(button: ImageButton, horoscopo: Horoscopo) {
        val context = button.context
        if (isFavoriteChecker(horoscopo)) {
            button.setImageResource(R.drawable.ic_favorite_filled)
            // Usamos el color dorado que definiste en R.color.gold_accent
            button.setColorFilter(ContextCompat.getColor(context, R.color.gold_accent))
        } else {
            button.setImageResource(R.drawable.ic_favorite_border)
            // Quitamos el tinte si no es favorito
            button.colorFilter = null
        }
    }


    override fun getItemCount(): Int = horoscopoList.size * REPEAT_COUNT
}
