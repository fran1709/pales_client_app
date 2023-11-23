package com.example.client_app

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

interface OnCommentClickListener {
    fun onCommentClick(resenia: Resenia)
}

class ReseniasAdapter(
    private var reseniasList: List<Resenia>,
    private val clickListener: OnCommentClickListener,
    private val userID: String, // Agregar el ID del usuario
    private val onEditClick: (Resenia) -> Unit // Agregar esta función de devolución de llamada
) : RecyclerView.Adapter<ReseniasAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jugadorTextView: TextView = itemView.findViewById(R.id.jugadorTextView)
        val comentarioTextView: TextView = itemView.findViewById(R.id.comentarioTextView)
        val editIcon: ImageView = itemView.findViewById(R.id.editIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resenia = reseniasList[position]
        holder.jugadorTextView.text = resenia.jugador
        holder.comentarioTextView.text = resenia.comentario
        Log.d("Resenia.jugador", resenia.jugador)
        Log.d("User ID", userID)
        // Verifica si el ID del usuario actual coincide con el ID del jugador del comentario
        if (resenia.jugador == userID) {
            holder.itemView.setOnClickListener {
                clickListener.onCommentClick(resenia)
            }
            // Muestra el icono de edición y configura su clic
            holder.editIcon.visibility = View.VISIBLE
            holder.editIcon.setOnClickListener {
                onEditClick(resenia)
            }
        } else {
            // Si los IDs no coinciden, desactiva el click listener y oculta el icono de edición
            holder.itemView.setOnClickListener {
                Toast.makeText(holder.itemView.context, "No puedes editar este comentario", Toast.LENGTH_SHORT).show()
            }
            holder.editIcon.visibility = View.GONE
        }
    }

    fun updateData(newData: List<Resenia>) {
        reseniasList = newData
        notifyDataSetChanged() // Notifica al RecyclerView sobre los cambios en los datos
    }
    override fun getItemCount(): Int {
        return reseniasList.size
    }

}
