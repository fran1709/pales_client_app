package com.example.client_app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date

data class Evento(val descripcion: String, val estado: Boolean, val fecha: String,  val imagen_url: String, val nombre: String)

private lateinit var db: FirebaseFirestore
private lateinit var startForResult: ActivityResultLauncher<Intent>

class ListarEventos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_eventos)
        db = FirebaseFirestore.getInstance()

        eventosListData()

        val searchEvento: ImageButton = findViewById(R.id.searchEventos)
        searchEvento.setOnClickListener {
            activitybuscarEvento()
        }

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data
                // Handle the result if needed
            }
        }
    }

    private fun eventosListData() {
        val eventosCollection = db.collection("evento_especial")
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")

        eventosCollection.whereEqualTo("estado", true)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val eventosList = mutableListOf<Evento>()

                for (document in querySnapshot) {
                    val nombre = document.getString("nombre") ?: ""
                    val descripcion = document.getString("descripcion") ?: ""
                    val estado = document.getBoolean("estado") ?: false
                    val fecha = document.getDate("fecha") ?: Date()
                    val imagenUrl = document.getString("imagen_url") ?: ""
                    val fechaFormateada = dateFormat.format(fecha)

                    val evento = Evento(
                        descripcion = descripcion,
                        estado = estado,
                        fecha = fechaFormateada,
                        imagen_url = imagenUrl,
                        nombre = nombre
                    )
                    eventosList.add(evento)
                }
                displayEventos(eventosList)
            }
            .addOnFailureListener { exception ->
                // Handle failures
            }
    }

    private fun displayEventos(eventosList: List<Evento>) {
        val listView: ListView = findViewById(R.id.lv1)
        val adapter = EventoAdapter(this, eventosList)
        listView.adapter = adapter
    }

    class EventoAdapter(context: Context, private val eventos: List<Evento>) : ArrayAdapter<Evento>(context, 0, eventos) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var itemView = convertView
            if (itemView == null) {
                itemView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
            }

            val evento = eventos[position]
            itemView?.findViewById<TextView>(android.R.id.text1)?.text = "Nombre: ${evento.nombre}\nFecha: ${evento.fecha}"

            itemView?.setOnClickListener {
                val intent = Intent(context, DetalleEvento::class.java)
                intent.putExtra("nombre", evento.nombre)
                intent.putExtra("descripcion", evento.descripcion)
                intent.putExtra("estado", evento.estado)
                intent.putExtra("fecha", evento.fecha)
                intent.putExtra("imagen_url", evento.imagen_url)
                context.startActivity(intent)
            }
            return itemView!!
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun activitybuscarEvento() {
        val intent = Intent(this, BuscarEvento::class.java)
        startForResult.launch(intent)
    }
}
