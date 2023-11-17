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

data class Reserva(val encargado: String, val equipo: Boolean, val estado: Boolean, val horario: String, val retadores: List<String>, val tipo: String)

private lateinit var db: FirebaseFirestore
private lateinit var startForResult: ActivityResultLauncher<Intent>

class ListarReservas : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_reservas)
        db = FirebaseFirestore.getInstance()

        reservasListData()

        val searchReserva: ImageButton = findViewById(R.id.searchReservas)
        searchReserva.setOnClickListener {
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
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private fun reservasListData() {
        val reservasCollection = db.collection("reservas")

        reservasCollection.whereEqualTo("estado", true)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val reservasList = mutableListOf<Reserva>()

                for (document in querySnapshot) {
                    val encargado = document.getString("encargado") ?: ""
                    val equipo = document.getBoolean("equipo") ?: false
                    val estado = document.getBoolean("estado") ?: false
                    val horario = document.getString("horario") ?: ""
                    val retadores = document.get("retadores") as? List<String> ?: emptyList()
                    val tipo = document.getString("tipo") ?: ""

                    val reservas = Reserva(
                        encargado = encargado,
                        equipo = equipo,
                        estado = estado,
                        horario = horario,
                        retadores = retadores,
                        tipo = tipo
                    )
                    reservasList.add(reservas)
                }
                displayReservas(reservasList)
            }
            .addOnFailureListener { exception ->
                // Handle failures
            }
    }

    private fun displayReservas(reservasList: List<Reserva>) {
        val listView: ListView = findViewById(R.id.lv1)
        val adapter = ReservasAdapter(this, reservasList)
        listView.adapter = adapter
    }

    class ReservasAdapter(context: Context, private val reservas: List<Reserva>) : ArrayAdapter<Reserva>(context, 0, reservas) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var itemView = convertView
            if (itemView == null) {
                itemView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
            }

            val reserva = reservas[position]
            //itemView?.findViewById<TextView>(android.R.id.text1)?.text = "Reserva: ${reserva.nombre}"
            itemView?.findViewById<TextView>(android.R.id.text1)?.text = "Reserva: "

            itemView?.setOnClickListener {
                val intent = Intent(context, DetalleReserva::class.java)
                //intent.putExtra("nombre", evento.nombre)
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
