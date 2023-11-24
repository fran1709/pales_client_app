package com.example.client_app

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import com.google.firebase.Timestamp
import android.widget.Spinner
import android.widget.TextView
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
    private var equipo: Boolean = false
    private var tipo: String = "Publica"
    private var horarioSeleccionadoID: String = ""
    private var dificultadSeleccionada: String = ""
    private var fecha: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        cargarHorariosFirebase()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_reserva)

        val individualCheck: CheckBox = findViewById(R.id.individualCheck)
        val grupoCheck: CheckBox = findViewById(R.id.grupoCheck)
        val privadaCheck: CheckBox = findViewById(R.id.privadaCheck)
        val publicaCheck: CheckBox = findViewById(R.id.publicaCheck)
        val backButton: ImageButton = findViewById(R.id.backButton)
        val spinnerDificultad: Spinner = findViewById(R.id.spinnerDificultad)
        ArrayAdapter.createFromResource(this, R.array.dificultad_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerDificultad.apply {
                this.adapter = adapter

                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                        dificultadSeleccionada = parentView.getItemAtPosition(position).toString()
                    }

                    override fun onNothingSelected(parentView: AdapterView<*>) {}
                }
            }
        }

        backButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        individualCheck.setOnCheckedChangeListener { _, isChecked ->
            equipo = !isChecked
        }

        grupoCheck.setOnCheckedChangeListener { _, isChecked ->
            equipo = isChecked
        }

        privadaCheck.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tipo = "Privada"
            }
        }

        publicaCheck.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tipo = "Publica"
            }
        }

        val crearReservaButton: Button = findViewById(R.id.crearReservaButton)
        crearReservaButton.setOnClickListener {
            crearReserva()
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
                    val id = document.id
                    var fecha = document.getDate("fecha") ?: Date()

                    // Verificar si la fecha es después de la fecha actual
                    if (fecha.after(fechaActual)) {
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
                configurarSpinnerHorario()
            }
            .addOnFailureListener { exception ->
                // Handle failures
            }
    }

    private fun configurarSpinnerHorario() {
        val spinnerHorario: Spinner = findViewById(R.id.spinnerHorario)
        val adapter = object : ArrayAdapter<Horario>(this, android.R.layout.simple_spinner_dropdown_item, horarios) {
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val horario = getItem(position)
                view.findViewById<TextView>(android.R.id.text1).text = "${horario?.fecha} Inicio: ${horario?.horaInicio} Fin: ${horario?.horaFin}"
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val horario = getItem(position)
                view.findViewById<TextView>(android.R.id.text1).text = "${horario?.fecha} Inicio: ${horario?.horaInicio} Fin: ${horario?.horaFin}"
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerHorario.adapter = adapter
        configurarListenerSpinner()
    }

    private fun configurarListenerSpinner() {
        val spinnerHorario: Spinner = findViewById(R.id.spinnerHorario)

        spinnerHorario.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                val horarioSeleccionado = horarios[position]
                horarioSeleccionadoID = horarioSeleccionado.id
                fecha = horarioSeleccionado.fecha
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
            }
        }
    }


    private fun crearReserva() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val encargado = user.uid

            if (horarioSeleccionadoID.isBlank()) {
                Toast.makeText(this, "Por favor, seleccione un horario", Toast.LENGTH_SHORT).show()
                return
            }

            if (dificultadSeleccionada.isBlank()) {
                Toast.makeText(this, "Por favor, seleccione una dificultad", Toast.LENGTH_SHORT).show()
                return
            }

            if (tipo != "Publica" && tipo != "Privada") {
                Toast.makeText(this, "Por favor, seleccione un tipo válido", Toast.LENGTH_SHORT).show()
                return
            }

            if (equipo != true && equipo != false) {
                Toast.makeText(this, "Por favor, seleccione un valor válido para equipo", Toast.LENGTH_SHORT).show()
                return
            }

            val jugadores: List<String> = emptyList()
            val reservasCollection = db.collection("reservas")
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaTimestamp = Timestamp(dateFormat.parse(fecha)!!)

            val nuevaReserva = hashMapOf(
                "encargado" to encargado,
                "horario" to horarioSeleccionadoID,
                "fecha" to fechaTimestamp,
                "estado" to true,
                "tipo" to tipo,
                "equipo" to equipo,
                "clasificacion" to dificultadSeleccionada,
                "jugadores" to jugadores,
                "retadores" to jugadores,
                "portero" to false
            )

            reservasCollection
                .add(nuevaReserva)
                .addOnSuccessListener { documentReference ->
                    val horarioCollection = db.collection("horario")
                    horarioCollection.document(horarioSeleccionadoID)
                        .update("reservado", true)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Debe cancelar un 25% de la reserva por sinpe", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al actualizar el horario", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al crear reserva", Toast.LENGTH_SHORT).show()
                }
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}