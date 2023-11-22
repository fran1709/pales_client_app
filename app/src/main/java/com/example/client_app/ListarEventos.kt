package com.example.client_app

import android.content.Context
import android.content.Intent
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date

data class Evento(val id: String, val descripcion: String, val estado: Boolean, val fecha: String, val imagen_url: String, val nombre: String, var visto: Boolean)

private lateinit var db: FirebaseFirestore
private lateinit var startForResult: ActivityResultLauncher<Intent>

class ListarEventos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_eventos)
        db = FirebaseFirestore.getInstance()

        // Recuperar el estado de "leído" desde SharedPreferences
        val sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val eventosLeidos = sharedPreferences.getStringSet("eventosLeidos", setOf()) ?: setOf()

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

        val backProfile: ImageButton = findViewById(R.id.backButton)
        backProfile.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun eventosListData() {
        val eventosCollection = db.collection("evento_especial")
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")

        // Recuperar el estado de "leído" desde SharedPreferences
        val sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val eventosLeidos = sharedPreferences.getStringSet("eventosLeidos", setOf()) ?: setOf()

        eventosCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val eventosList = mutableListOf<Evento>()

                for (document in querySnapshot) {
                    val id = document.id
                    val nombre = document.getString("nombre") ?: ""
                    val descripcion = document.getString("descripcion") ?: ""
                    val estado = document.getBoolean("estado") ?: false
                    val fecha = document.getDate("fecha") ?: Date()
                    val imagenUrl = document.getString("imagen_url") ?: ""
                    val visto = id in eventosLeidos // Verifica si el evento ya fue leído
                    val fechaFormateada = dateFormat.format(fecha)

                    val evento = Evento(
                        id = id,
                        descripcion = descripcion,
                        estado = estado,
                        fecha = fechaFormateada,
                        imagen_url = imagenUrl,
                        nombre = nombre,
                        visto = visto
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
                itemView = LayoutInflater.from(context).inflate(R.layout.item_evento, parent, false)
            }

            val evento = eventos[position]
            val textView = itemView?.findViewById<TextView>(R.id.eventoTextView)

            // Modificar el color de fondo según el estado "visto" del evento
            if (evento.visto) {
                textView?.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
            } else {
                textView?.setBackgroundColor(ContextCompat.getColor(context, R.color.light_grey))
            }

            textView?.text = "Nombre: ${evento.nombre}\nFecha: ${evento.fecha}"

            // Agregar la animación al hacer clic en el evento
            itemView?.setOnClickListener {
                it.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                    it.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()

                    // Verificar si el evento ya ha sido marcado como leído
                    if (!evento.visto) {
                        // Al abrir los detalles del evento, actualiza el estado "visto" a true
                        evento.visto = true

                        // Guardar el estado de "leído" en SharedPreferences
                        val sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        val eventosLeidos = sharedPreferences.getStringSet("eventosLeidos", setOf()) ?: setOf()
                        val nuevosEventosLeidos = eventosLeidos.toMutableSet()
                        nuevosEventosLeidos.add(evento.id)
                        editor.putStringSet("eventosLeidos", nuevosEventosLeidos)
                        editor.apply()

                        notifyDataSetChanged()
                    }

                    // Abrir los detalles del evento
                    val intent = Intent(context, DetalleEvento::class.java)
                    intent.putExtra("nombre", evento.nombre)
                    intent.putExtra("descripcion", evento.descripcion)
                    intent.putExtra("estado", evento.estado)
                    intent.putExtra("fecha", evento.fecha)
                    intent.putExtra("imagen_url", evento.imagen_url)
                    context.startActivity(intent)
                }
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
