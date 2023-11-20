package com.example.client_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AprobarAdapter(
    private var reseniasList: List<Resenia>,
    private val onAprobarClick: (Resenia) -> Unit,
    private val onRechazarClick: (Resenia) -> Unit
) : RecyclerView.Adapter<AprobarAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jugadorTextView: TextView = itemView.findViewById(R.id.jugadorTV)
        val comentarioTextView: TextView = itemView.findViewById(R.id.comentarioTV)
        val btnAprobar: Button = itemView.findViewById(R.id.aprobarButtonTV)
        val btnRechazar: Button = itemView.findViewById(R.id.rechazarButtonTV)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resenia = reseniasList[position]

        holder.jugadorTextView.text = resenia.jugador
        holder.comentarioTextView.text = resenia.comentario

        holder.btnAprobar.setOnClickListener {
            onAprobarClick(resenia)
        }

        holder.btnRechazar.setOnClickListener {
            onRechazarClick(resenia)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.resenias_item, parent, false)
        return ViewHolder(itemView)
    }

    fun updateData(newData: List<Resenia>) {
        reseniasList = newData
        notifyDataSetChanged() // Notifica al RecyclerView sobre los cambios en los datos
    }
    override fun getItemCount(): Int {
        return reseniasList.size
    }

}
