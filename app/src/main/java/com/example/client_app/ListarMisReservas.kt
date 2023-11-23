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
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date

private lateinit var db: FirebaseFirestore
private lateinit var startForResult: ActivityResultLauncher<Intent>
private val reservaList = mutableListOf<Reserva>()
private val jugadorList = mutableListOf<Jugador>()

class ListarMisReservas : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_mis_reservas)
        db = FirebaseFirestore.getInstance()
        jugadoresListData {
            reservasListData {
                mapJugadoresToReservas()
                displayReservas(reservaList)
            }
        }

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data
            }
        }

        /*
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonIndividual -> {
                    val reservasFiltradas = reservaList.filter { it.equipo.not()  }
                    displayReservas(reservasFiltradas)
                }
                R.id.radioButtonGrupal -> {
                    val reservasFiltradas = reservaList.filter { it.equipo}
                    displayReservas(reservasFiltradas)
                }
            }
        }
        */

        val backProfile: ImageButton = findViewById(R.id.backButton)
        backProfile.setOnClickListener {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private fun reservasListData(onComplete: () -> Unit) {
        val reservasCollection = db.collection("reservas")
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val userId = user.uid

            reservasCollection
                .get()
                .addOnSuccessListener { querySnapshot ->
                    reservaList.clear()

                    for (document in querySnapshot) {
                        val reserva = mapFirebaseDocumentToReservaMisReservas(document)
                        if (reserva.encargado == userId || userId in reserva.retadores) {
                            reserva.documentId = document.id
                            reservaList.add(reserva)
                        }
                    }

                    onComplete()
                }
                .addOnFailureListener { exception ->
                    onComplete()
                }
        }
    }

    private fun jugadoresListData(onComplete: () -> Unit) {
        val reservasCollection = db.collection("jugadores")
        reservasCollection.get()
            .addOnSuccessListener { querySnapshot ->
                jugadorList.clear()

                for (document in querySnapshot) {
                    val jugador = mapFirebaseDocumentToJugadorReserva(document)
                    jugadorList.add(jugador)
                }
                onComplete()
            }
            .addOnFailureListener { exception -> }
    }

    // Función para convertir un documento de Firebase a un objeto Reserva
    fun mapFirebaseDocumentToReservaMisReservas(document: DocumentSnapshot): Reserva {
        val encargadoUID = document.getString("encargado") ?: ""
        val estado = false
        val equipo = document.getBoolean("equipo") ?: false
        val horario = document.getString("horario") ?: ""
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        val fecha = document.getDate("fecha") ?: Date()
        val fechaFormateada = dateFormat.format(fecha)
        val retadoresList = mutableListOf<String>()
        val retadoresField = document.get("retadores") as? List<String>
        if (retadoresField != null) {
            for (retadorId in retadoresField) {
                retadoresList.add(retadorId)
            }
        }
        val tipo = document.getString("tipo") ?: ""
        var apodoEncargado = ""

        return Reserva(encargadoUID, estado, equipo, horario, retadoresList, tipo, apodoEncargado, fechaFormateada)
    }

    // Función para convertir un documento de Firebase a un objeto Jugador
    fun mapFirebaseDocumentToJugadorReserva(document: DocumentSnapshot): Jugador {
        val apodo = document.getString("apodo") ?: ""
        val uidJugador = document.getString("UID") ?: ""
        val posiciones = document.get("posiciones") as? List<String> ?: emptyList()

        return Jugador(uidJugador , apodo, posiciones )
    }

    private fun mapJugadoresToReservas() {
        for (reserva in reservaList) {
            val encargadoUID = reserva.encargado
            val jugadorEncargado = jugadorList.find { it.id == encargadoUID }
            jugadorEncargado?.let {
                reserva.apodoEncargado = it.nombre
            }
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
            itemView?.findViewById<TextView>(android.R.id.text1)?.apply {
                text = " "
                if (reserva.tipo == "Publica") {
                    append("Reto")
                } else {
                    append("Reserva")
                }
                if (reserva.equipo) {
                    append(" Grupal")
                } else {
                    append(" Individual")
                }
                append("\n      Reserva de ${reserva.apodoEncargado}")
                append("\n      Fecha del reto: ${reserva.fecha}")
            }
            itemView?.setOnClickListener {
                val intent = Intent(context, DetalleMiReserva::class.java)
                intent.putExtra("encargado", reserva.encargado)
                intent.putExtra("estado", reserva.estado)
                intent.putExtra("equipo", reserva.equipo)
                intent.putExtra("horario", reserva.horario)
                intent.putExtra("retadores", ArrayList(reserva.retadores))
                intent.putExtra("tipo", reserva.tipo)
                intent.putExtra("apodoEncargado", reserva.apodoEncargado)
                intent.putExtra("fecha", reserva.fecha)
                intent.putExtra("documentId", reserva.documentId)

                context.startActivity(intent)
            }
            return itemView!!
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
