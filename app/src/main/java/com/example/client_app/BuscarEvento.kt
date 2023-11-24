package com.example.client_app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.client_app.utils.DatePickerFragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private lateinit var db: FirebaseFirestore
private lateinit var startForResult: ActivityResultLauncher<Intent>
private lateinit var etDate: EditText
private var selectedDate: String = ""

class BuscarEvento : AppCompatActivity() {
    private val eventosList = mutableListOf<Evento>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscar_evento)
        etDate = findViewById(R.id.etDate)
        etDate.setOnClickListener{ showDatePickerDialog() }

        db = FirebaseFirestore.getInstance()

        val backButton: ImageButton = findViewById(R.id.backButton)
        val searchEventoButton: ImageButton = findViewById(R.id.searchEvento)
        val searchInput: EditText = findViewById(R.id.etBuscarEvento)
        val lv1: ListView = findViewById(R.id.lv1)

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data
            }
        }

        val adapter = EventoAdapter(this, mutableListOf())
        lv1.adapter = adapter

        searchEventoButton.setOnClickListener {
            val searchText = searchInput.text.toString().toLowerCase(Locale.getDefault())
            val filteredEventos = eventosList.filter {
                it.nombre.toLowerCase(Locale.getDefault()).contains(searchText) ||
                        it.fecha.contains(selectedDate)
            }
            adapter.updateData(filteredEventos)
        }

        backButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        loadEventosData()
    }

    private fun loadEventosData() {
        val lv1: ListView = findViewById(R.id.lv1)
        val eventosCollection = db.collection("evento_especial")
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")

        eventosCollection.whereEqualTo("estado", true)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val id = document.id
                    val nombre = document.getString("nombre") ?: ""
                    val descripcion = document.getString("descripcion") ?: ""
                    val estado = document.getBoolean("estado") ?: false
                    val fecha = document.getDate("fecha") ?: Date()
                    val imagenUrl = document.getString("imagen_url") ?: ""
                    val fechaFormateada = dateFormat.format(fecha)
                    val visto = false

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

                val adapter = lv1.adapter as EventoAdapter
                adapter.updateData(eventosList)
            }
            .addOnFailureListener { exception ->
                println("Ocurrió un error: ${exception.message}")
            }
    }

    class EventoAdapter(context: Context, private val eventos: MutableList<Evento>) : ArrayAdapter<Evento>(context, 0, eventos) {
        fun updateData(newData: List<Evento>) {
            eventos.clear()
            eventos.addAll(newData)
            notifyDataSetChanged()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var itemView = convertView
            if (itemView == null) {
                itemView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
            }

            val evento = eventos[position]
            itemView?.findViewById<TextView>(android.R.id.text1)?.text = "Nombre: ${evento.nombre}\nFecha: ${evento.fecha}"

            itemView?.setOnClickListener {
                val intent = Intent(context, DetalleEvento::class.java)
                intent.putExtra("id", evento.id)
                intent.putExtra("nombre", evento.nombre)
                intent.putExtra("descripcion", evento.descripcion)
                intent.putExtra("estado", evento.estado)
                intent.putExtra("fecha", evento.fecha)
                intent.putExtra("imagen_url", evento.imagen_url)
                intent.putExtra("visto", evento.visto)
                context.startActivity(intent)
            }
            return itemView!!
        }
    }

    private fun showDatePickerDialog(){
        val datePicker = DatePickerFragment({day, month, year , -> onDateSelected(day, month, year)})
        datePicker.show(supportFragmentManager, "datePicker")
    }

    fun onDateSelected(day: Int, month: Int, year: Int) {
        val formattedDay = String.format("%02d", day)
        val formattedMonth = String.format("%02d", month + 1)
        selectedDate = "$formattedDay/$formattedMonth/$year"
        etDate.setText(selectedDate)
    }

}