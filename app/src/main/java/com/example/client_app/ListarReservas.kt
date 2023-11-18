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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Reserva(
    val encargado: String,
    val nombreEncargado: String,
    val estado: Boolean,
    val horario: String,
    val hora: String,
    val clasificacion: String,
    val jugadores: List<JugadorReserva>,
    val retadores: List<JugadorReserva>,
    val tipo: String
)

data class JugadorReserva(
    val aceptado: Boolean,
    val clasificacion: String,
    val posicion: List<String>,
    val apodo: String,
    val uidJugador: String
)

private lateinit var db: FirebaseFirestore
private lateinit var startForResult: ActivityResultLauncher<Intent>
private val reservaList = mutableListOf<Reserva>()

class ListarReservas : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_reservas)
        db = FirebaseFirestore.getInstance()

        reservasListData()
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data
            }
        }

        val searchPromotion: ImageButton = findViewById(R.id.crearReservas)
        searchPromotion.setOnClickListener {
            activityCrearReserva()
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonIndividual -> {
                    val reservasFiltradas = reservaList.filter { it.retadores.size < 6 }
                    displayReservas(reservasFiltradas)
                }
                R.id.radioButtonGrupal -> {
                    val reservasFiltradas = reservaList.filter { it.retadores.isEmpty() }
                    displayReservas(reservasFiltradas)
                }
                else -> {
                    displayReservas(reservaList)
                }
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
            //TODO borrar esta línea-------------------------------------------------------------------------------------------
            //.whereNotEqualTo("tipo", "privada")
            //TODO borrar esta línea-------------------------------------------------------------------------------------------
            .get()
            .addOnSuccessListener { querySnapshot ->
                reservaList.clear()

                for (document in querySnapshot) {
                    val reserva = mapFirebaseDocumentToReserva(document)
                    reservaList.add(reserva)
                }
                displayReservas(reservaList)
            }
            .addOnFailureListener { exception ->  }
    }

    // Función para convertir un documento de Firebase a un objeto Reserva
    fun mapFirebaseDocumentToReserva(document: DocumentSnapshot): Reserva {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        val encargado = document.getString("encargado") ?: ""
        val nombreEncargado = document.getString("nombreEncargado") ?: ""
        val estado = document.getBoolean("estado") ?: false
        val horario = document.getString("horario") ?: ""
        val clasificacion = document.getString("clasificacion") ?: ""
        val hora = document.getDate("hora") ?: Date()
        val horaFormateada = dateFormat.format(hora)
        val jugadoresList = mapFirebaseJugadoresList(document.get("jugadores"))
        val retadoresList = mapFirebaseJugadoresList(document.get("retadores"))
        val tipo = document.getString("tipo") ?: ""

        return Reserva(encargado, nombreEncargado , estado, horario, horaFormateada, clasificacion ,jugadoresList ,retadoresList, tipo)
    }

    // Mapper de los jugadores
    fun mapFirebaseJugadoresList(jugadores: Any?): List<JugadorReserva> {
        val retadoresList = mutableListOf<JugadorReserva>()

        if (jugadores is List<*>) {
            for (jugadorItem in jugadores) {
                if (jugadorItem is Map<*, *>) {
                    val aceptado = jugadorItem["aceptado"] as? Boolean ?: false
                    val clasificacionOriginal = jugadorItem["clasificacion"] as? String ?: ""
                    val clasificacionNormalizada = normalizarClasificacion(clasificacionOriginal)
                    val posicion = jugadorItem["posicion"] as? List<*> ?: emptyList<Any>()
                    val apodo = jugadorItem["apodo"] as? String ?: ""
                    val uidJugador = jugadorItem["uidJugador"] as? String ?: ""

                    val jugador = JugadorReserva(aceptado, clasificacionNormalizada, posicion.map { it.toString() }, apodo, uidJugador)
                    retadoresList.add(jugador)
                }
            }
        }

        return retadoresList
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
            itemView?.findViewById<TextView>(android.R.id.text1)?.text = "Reserva de ${reserva.nombreEncargado}\n${reserva.hora}"

            itemView?.setOnClickListener {
                val intent = Intent(context, DetalleReserva::class.java)
                //intent.putExtra("nombre", evento.nombre)
                context.startActivity(intent)
            }
            return itemView!!
        }
    }

    fun normalizarClasificacion(clasificacionActual: String): String {
        val clasificacionLower = clasificacionActual.toLowerCase()
        return when {
            clasificacionLower == "regular" -> "regular"
            clasificacionLower == "bueno" || clasificacionLower == "good" -> "bueno"
            clasificacionLower == "malo" || clasificacionLower == "bad" -> "malo"
            else -> clasificacionLower
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
}
