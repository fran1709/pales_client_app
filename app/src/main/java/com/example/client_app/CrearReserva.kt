package com.example.client_app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import com.google.firebase.Timestamp
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Horario(
    val id: String,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val reservado: Boolean
)

private lateinit var db: FirebaseFirestore
class CrearReserva : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var horarios: List<Horario>
    override fun onCreate(savedInstanceState: Bundle?) {
        cargarHorariosFirebase()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_reserva)

        val spinnerDificultad: Spinner = findViewById(R.id.spinnerDificultad)
        val backButton: ImageButton = findViewById(R.id.backButton)
        ArrayAdapter.createFromResource(this, R.array.dificultad_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerDificultad.adapter = adapter
        }

        backButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        val crearReservaButton: Button = findViewById(R.id.crearReservaButton)
        crearReservaButton.setOnClickListener {
            crearReserva()
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun cargarHorariosFirebase() {
        val horarioCollection = db.collection("horario")
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        horarioCollection
            .whereEqualTo("reservado", false)
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
                        val infoHorario = "$fechaFormateada \nInicio: $horaInicioFormateada Fin: $horaFinFormateada"
                        val horario = Horario(id, infoHorario, horaInicioFormateada , horaFinFormateada, reservado)
                        horariosList.add(horario)
                    }
                }

                horarios = horariosList
                configurarSpinnerHorario()
            }
            .addOnFailureListener { exception ->
                // Handle failures
            }
    }

    private fun configurarSpinnerHorario() {
        val spinnerHorario: Spinner = findViewById(R.id.spinnerHorario)

        // Extraer las fechas de la lista de horarios
        val fechas = horarios.map { it.fecha }.toTypedArray()

        // Configurar el adaptador del Spinner
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, fechas)
        spinnerHorario.adapter = adapter
    }

    private fun crearReserva() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val encargado = user.uid
            val horarioID = "VbyPLIr78sJ56DGZSPyb"
            val tipo = "Publica"
            val clasificacion = "Regular"

            val retadoresReferences = listOf(db.document("jugadores/VsAShRzpcYOjyIybrFY3"))

            val reservasCollection = db.collection("reservas")
            val nuevaReserva = hashMapOf(
                    "encargado" to encargado,
                    "horarioID" to horarioID,
                    "estado" to true,
                    "tipo" to tipo,
                    "clasificacion" to clasificacion,
                    "retadores" to retadoresReferences
            )

            reservasCollection
                .add(nuevaReserva)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this, " Debe cancelar un 25% de la reserva para poder reservarla, esto por sinpe al....", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al crear reserva", Toast.LENGTH_SHORT).show()
                }
        }
    }
}