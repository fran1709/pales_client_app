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

    // Clase interna que representa un elemento en el RecyclerView
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jugadorTextView: TextView = itemView.findViewById(R.id.jugadorTV)
        val comentarioTextView: TextView = itemView.findViewById(R.id.comentarioTV)
        val btnAprobar: Button = itemView.findViewById(R.id.aprobarButtonTV)
        val btnRechazar: Button = itemView.findViewById(R.id.rechazarButtonTV)
    }

    // Este método se llama cuando se necesita mostrar datos en una posición específica.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resenia = reseniasList[position]

        // Configura los valores de los elementos de la vista con los datos de la reseña
        holder.jugadorTextView.text = resenia.jugador
        holder.comentarioTextView.text = resenia.comentario

        // Establece los manejadores de clic para los botones de aprobar y rechazar
        holder.btnAprobar.setOnClickListener {
            onAprobarClick(resenia)
        }
        holder.btnRechazar.setOnClickListener {
            onRechazarClick(resenia)
        }
    }

    // Este método se llama cuando se necesita crear una nueva instancia de ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.resenias_item, parent, false)
        return ViewHolder(itemView)
    }
    // Método para actualizar la lista de reseñas con nuevos datos y notificar al RecyclerView.
    fun updateData(newData: List<Resenia>) {
        reseniasList = newData
        notifyDataSetChanged() // Notifica al RecyclerView sobre los cambios en los datos
    }
    // Retorna la cantidad total de elementos en la lista de reseñas.
    override fun getItemCount(): Int {
        return reseniasList.size
    }

}
