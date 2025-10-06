package com.example.horoscopo_android.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.horoscopo_android.Horoscopo
import com.example.horoscopo_android.databinding.ItemHoroscopoBinding

class HoroscopeAdapter(
    private var horoscopos: List<Horoscopo>,
    private val onItemSelected: (Horoscopo) -> Unit
) : RecyclerView.Adapter<HoroscopeAdapter.HoroscopeViewHolder>() {

    fun updateList(newList: List<Horoscopo>) {
        horoscopos = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoroscopeViewHolder {
        val binding = ItemHoroscopoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HoroscopeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HoroscopeViewHolder, position: Int) {
        holder.bind(horoscopos[position], onItemSelected)
    }

    override fun getItemCount(): Int = horoscopos.size

    class HoroscopeViewHolder(private val binding: ItemHoroscopoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(horoscopo: Horoscopo, onItemSelected: (Horoscopo) -> Unit) {
            val context = binding.root.context

            // 1. Mostrar nombre, fechas e icono usando los Resource IDs
            binding.tvSignName.text = context.getString(horoscopo.nombreId)
            binding.tvSignDates.text = context.getString(horoscopo.fechasId)
            binding.ivSignIcon.setImageResource(horoscopo.imagenId)

            // 2. Establecer listener para la selección del item
            binding.root.setOnClickListener {
                onItemSelected(horoscopo)
            }
        }
    }
}
