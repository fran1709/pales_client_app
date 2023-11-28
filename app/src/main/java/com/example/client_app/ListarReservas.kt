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

data class Reserva(
    val encargado: String,
    val estado: Boolean,
    val equipo: Boolean,
    val horario: String,
    val retadores: List<String>,
    val tipo: String,
    var apodoEncargado: String,
    val fecha: String,
    var documentId: String = ""
)

private lateinit var db: FirebaseFirestore
private lateinit var startForResult: ActivityResultLauncher<Intent>
private val reservaList = mutableListOf<Reserva>()
private val jugadorList = mutableListOf<Jugador>()
private var idUsuarioConectado = ""

class ListarReservas : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_reservas)
        db = FirebaseFirestore.getInstance()
        obtenerBloqueosUsuarioActual {
            jugadoresListData {
                reservasListData {
                    mapJugadoresToReservas()
                    displayReservas(reservaList)
                }
            }
        }
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data
            }
        }

        val verMisReservasBoton: ImageButton = findViewById(R.id.misReservas)
        verMisReservasBoton.setOnClickListener {
            activityListarMisReservas()
        }

        val searchPromotion: ImageButton = findViewById(R.id.crearReservas)
        searchPromotion.setOnClickListener {
            activityCrearReserva()
        }

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

        val backProfile: ImageButton = findViewById(R.id.backButton)
        backProfile.setOnClickListener {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private fun obtenerBloqueosUsuarioActual(onComplete: (List<String>) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uidUsuario = user.uid
            idUsuarioConectado = uidUsuario

            db.collection("jugadores")
                .whereEqualTo("UID", uidUsuario)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val bloqueosList = mutableListOf<String>()

                    for (document in querySnapshot) {
                        val bloqueos = document.get("bloqueos") as? List<String> ?: emptyList()
                        bloqueosList.addAll(bloqueos)
                    }

                    onComplete(bloqueosList)
                }
                .addOnFailureListener { exception ->
                    onComplete(emptyList())
                }
        } else {
            onComplete(emptyList())
        }
    }

    private fun reservasListData(onComplete: () -> Unit) {
        obtenerBloqueosUsuarioActual { bloqueosUsuarioActual ->
            val reservasCollection = db.collection("reservas")
            reservasCollection.whereEqualTo("estado", true)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    reservaList.clear()

                    for (document in querySnapshot) {
                        val reserva = mapFirebaseDocumentToReserva(document)
                        if (reserva.tipo != "privada" &&
                            !reserva.retadores.any { it in bloqueosUsuarioActual } &&
                            reserva.encargado !in bloqueosUsuarioActual) {
                            reserva.documentId = document.id
                            reservaList.add(reserva)
                        }
                    }

                    onComplete()
                }
                .addOnFailureListener { exception -> onComplete() }
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
    fun mapFirebaseDocumentToReserva(document: DocumentSnapshot): Reserva {
        val encargadoUID = document.getString("encargado") ?: ""
        val estado = document.getBoolean("estado") ?: false
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
                val intent = Intent(context, DetalleReserva::class.java)
                intent.putExtra("encargado", reserva.encargado)
                intent.putExtra("idUsuarioConectado", idUsuarioConectado)
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

    fun activityCrearReserva() {
        val intent = Intent(this, CrearReserva::class.java)
        startForResult.launch(intent)
    }

    fun activityListarMisReservas() {
        val intent = Intent(this, ListarMisReservas::class.java)
        startForResult.launch(intent)
    }
}
