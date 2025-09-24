package com.example.horoscopo_android

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HoroscopoAdapter(
    private val horoscopoList: List<Horoscopo>,
    private val onClick: (Horoscopo) -> Unit
) : RecyclerView.Adapter<HoroscopoAdapter.HoroscopoViewHolder>() {

    private val REPEAT_COUNT = 1000

    class HoroscopoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivSigno: ImageView = view.findViewById(R.id.ivSigno)
        val tvNombreSigno: TextView = view.findViewById(R.id.tvNombreSigno)
        val tvFechas: TextView = view.findViewById(R.id.tvFechas)
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

        holder.itemView.setOnClickListener {
            onClick(horoscopo)
        }
    }

    override fun getItemCount(): Int = horoscopoList.size * REPEAT_COUNT
}