package com.example.client_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class JugadorBan(
    val documentID: String,
    val nombre: String,
    val estado: Boolean
)

class AdapterBanear(
    private var jugadoresList: List<JugadorBan>,
    private val onBanearDesbanearClick: (JugadorBan) -> Unit
) : RecyclerView.Adapter<AdapterBanear.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreTextView: TextView = itemView.findViewById(R.id.nombreUsuarioBAN)
        val estadoTextView: TextView = itemView.findViewById(R.id.estadoUsuarioBAN)
        val btnBanearDesbanear: Button = itemView.findViewById(R.id.banearDesbanearButton)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val jugador = jugadoresList[position]

        holder.nombreTextView.text = jugador.nombre
        holder.estadoTextView.text = if (jugador.estado) "La cuenta se encuentra activa."  else "La cuenta se encuentra desactivada."
        holder.btnBanearDesbanear.text = if (jugador.estado) "Desactivar cuenta"  else "Activar Cuenta"

        holder.btnBanearDesbanear.setOnClickListener {
            onBanearDesbanearClick(jugador)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_banear_persona, parent, false)
        return ViewHolder(itemView)
    }

    fun updateData(newData: List<JugadorBan>) {
        jugadoresList = newData
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return jugadoresList.size
    }
}

