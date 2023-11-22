package com.example.client_app

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class JugadorReserva(
    val uidJugador: String,
    val apodo: String,
    val bloqueos: List<String>,
    val clasificacion: List<String>,
    val posiciones: List<String>
)

private val retadoresList = mutableListOf<JugadorReserva>()

class DetalleReserva : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var horarios: List<Horario>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_reserva)

        db = FirebaseFirestore.getInstance()

        val documentId = intent.getStringExtra("documentId")
        val horario = intent.getStringExtra("horario")
        val encargado = intent.getStringExtra("encargado")
        val estado = intent.getBooleanExtra("estado", false)
        val equipo = intent.getBooleanExtra("equipo", false)
        val retadores = intent.getStringArrayListExtra("retadores") ?: ArrayList()
        val tipo = intent.getStringExtra("tipo")
        val apodoEncargado = intent.getStringExtra("apodoEncargado")
        val fecha = intent.getStringExtra("fecha")
        val portero = intent.getBooleanExtra("portero", false)

        val retadoresLabel: TextView = findViewById(R.id.retadores)
        val retadoresListView: ListView = findViewById(R.id.lv1)
        val crearReservaButton: Button = findViewById(R.id.UnirmeReservaButton)

        if (equipo) {
            retadoresLabel.visibility = View.GONE
            retadoresListView.visibility = View.GONE
        }

        if (tipo.equals("Privada", ignoreCase = true) || tipo.equals("privada", ignoreCase = true) || !estado) {
            crearReservaButton.visibility = View.GONE
        }

        val titulo: TextView = findViewById(R.id.tituloReserva)
        val fechaText: TextView = findViewById(R.id.fecha)
        val privacidadText: TextView = findViewById(R.id.privacidad)
        val horaInicioText: TextView = findViewById(R.id.horaInicio)
        val horaFinText: TextView = findViewById(R.id.horaFin)
        titulo.text = ("Reserva de ${apodoEncargado}")
        fechaText.text = ("${fecha}")
        privacidadText.text = ("${tipo}")

        jugadoresListData(retadores) {
            if (horario != null) {
                horarioListData(horario) {
                    if (horarios.isNotEmpty()) {
                        horaInicioText.text = ("${horarios[0].horaInicio}")
                        horaFinText.text = ("${horarios[0].horaFin}")
                    }
                    displayRetadores(retadoresList)
                }
            }
        }

        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        crearReservaButton.setOnClickListener {
            if (documentId != null) {
                unirseReserva(documentId)
            }
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun unirseReserva(documentId: String) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val encargado = user.uid

            val reservasCollection = db.collection("reservas")

            reservasCollection.document(documentId)
                .update("retadores", FieldValue.arrayUnion(encargado))
                .addOnSuccessListener {
                    Toast.makeText(this, "Te has unido a la reserva con éxito", Toast.LENGTH_SHORT).show()

                    reservasCollection.document(documentId).get()
                        .addOnSuccessListener { documentSnapshot ->
                            val retadores = documentSnapshot.get("retadores") as? List<String>
                            if (retadores != null && retadores.size == 6) {
                                reservasCollection.document(documentId)
                                    .update("tipo", "Privada", "equipo", true)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "La reserva ahora es privada", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Error al cambiar el tipo de reserva", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al unirse a la reserva", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun jugadoresListData(retadores: List<String>, onComplete: () -> Unit) {
        val reservasCollection = db.collection("jugadores")
        reservasCollection.get()
            .addOnSuccessListener { querySnapshot ->
                retadoresList.clear()

                for (document in querySnapshot) {

                    val jugador = mapFirebaseJugador(document)
                    if (jugador.uidJugador in retadores) {
                        retadoresList.add(jugador)
                    }

                }
                onComplete()
            }
            .addOnFailureListener { exception -> }
    }

    private fun horarioListData(horarioID: String, onComplete: () -> Unit) {
        val horarioCollection = db.collection("horario")
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        horarioCollection.whereEqualTo("id", horarioID)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val horariosList = mutableListOf<Horario>()
                val fechaActual = Date()

                for (document in querySnapshot) {
                    var fecha = document.getDate("fecha") ?: Date()

                    // Verificar si la fecha es después de la fecha actual
                    if (fecha.after(fechaActual)) {
                        val id = document.getString("id") ?: ""
                        val reservado = document.getBoolean("reservado") ?: false
                        val tanda = document.get("tanda") as? ArrayList<Timestamp>
                        val formatoHoraMinutos = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val fechaFormateada = dateFormat.format(fecha)
                        var horaInicioFormateada = "00:00"
                        var horaFinFormateada = "00:00"
                        if (tanda != null && tanda.size >= 2) {
                            val horaInicio = tanda[0].toDate()
                            val horaFin = tanda[1].toDate()
                            horaInicioFormateada = formatoHoraMinutos.format(horaInicio)
                            horaFinFormateada = formatoHoraMinutos.format(horaFin)
                        }
                        val horario = Horario(id, fechaFormateada, horaInicioFormateada , horaFinFormateada, reservado)
                        horariosList.add(horario)
                    }
                }
                horarios = horariosList
                onComplete()
            }
            .addOnFailureListener { exception -> }
    }

    fun mapFirebaseJugador(document: DocumentSnapshot): JugadorReserva {
        val uidJugador = document.getString("UID") ?: ""
        val apodo = document.getString("apodo") ?: ""
        val bloqueos = document.get("posiciones") as? List<String> ?: emptyList()
        val clasificacion = document.get("posiciones") as? List<String> ?: emptyList()
        val posiciones = document.get("posiciones") as? List<String> ?: emptyList()

        return JugadorReserva(uidJugador , apodo, bloqueos, clasificacion , posiciones )
    }

    private fun displayRetadores(reservasList: List<JugadorReserva>) {
        val listView: ListView = findViewById(R.id.lv1)
        val adapter = RetadoresAdapter(this, retadoresList)
        listView.adapter = adapter
    }

    class RetadoresAdapter(context: Context, private val retadores: List<JugadorReserva>) : ArrayAdapter<JugadorReserva>(context, 0, retadores) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var itemView = convertView
            if (itemView == null) {
                itemView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
            }

            val retador = retadores[position]
            itemView?.findViewById<TextView>(android.R.id.text1)?.text = "   Apodo: ${retador.apodo}\n"

            return itemView!!
        }
    }
}