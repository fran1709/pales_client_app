package com.example.client_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReseniasAdapter(private val reseniasList: List<Resenia>) : RecyclerView.Adapter<ReseniasAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jugadorTextView: TextView = itemView.findViewById(R.id.jugadorTextView)
        val comentarioTextView: TextView = itemView.findViewById(R.id.comentarioTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resenia = reseniasList[position]
        holder.jugadorTextView.text = resenia.jugador
        holder.comentarioTextView.text = resenia.comentario
    }

    override fun getItemCount(): Int {
        return reseniasList.size
    }
}
