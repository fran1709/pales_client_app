package com.example.client_app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.MultiAutoCompleteTextView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Horario(
    val fecha: String,
    val reservado: Boolean
)

private lateinit var db: FirebaseFirestore
class CrearReserva : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var autoCompleteHorario: AutoCompleteTextView
    private lateinit var horarios: List<Horario>
    private lateinit var jugadores: List<JugadorReserva>
    override fun onCreate(savedInstanceState: Bundle?) {
        cargarHorariosFirebase()
        cargarJugadoresFirebase()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_reserva)

        val backButton: ImageButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun cargarHorariosFirebase() {
        val eventosCollection = db.collection("horario")
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        eventosCollection
            .whereEqualTo("reservado", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val horariosList = mutableListOf<Horario>()
                val fechaActual = Date()

                for (document in querySnapshot) {
                    val fecha = document.getDate("fecha") ?: Date()

                    // Verificar si la fecha es después de la fecha actual
                    if (fecha.after(fechaActual)) {
                        val reservado = document.getBoolean("reservado") ?: false
                        val fechaFormateada = dateFormat.format(fecha)
                        val horario = Horario(fechaFormateada, reservado)
                        horariosList.add(horario)
                    }
                }

                horarios = horariosList
                configurarSpinner()
            }
            .addOnFailureListener { exception ->
                // Handle failures
            }
    }

    private fun cargarJugadoresFirebase() {
        val jugadoresCollection = db.collection("jugadores")

        jugadoresCollection
            .get()
            .addOnSuccessListener { querySnapshot ->
                val jugadoresList = mutableListOf<JugadorReserva>()

                for (document in querySnapshot) {
                    val posiciones = document.get("posiciones") as? List<String> ?: emptyList()

                    val apodo = document.getString("apodo") ?: document.getString("nombre") ?: ""

                    val uidJugador = document.id
                    val aceptado = false

                    val clasificacionOriginal =
                        posiciones.firstOrNull { it.equals("regular", ignoreCase = true) || it.equals("bueno", ignoreCase = true) || it.equals("malo", ignoreCase = true) } ?: ""
                    val clasificacionNormalizada = normalizarClasificacion(clasificacionOriginal)

                    val retador = JugadorReserva(aceptado, clasificacionNormalizada, posiciones, apodo, uidJugador)
                    jugadoresList.add(retador)
                }
                jugadores = jugadoresList
                configurarMultiAutoComplete()
            }
            .addOnFailureListener { exception ->
                // Manejar errores
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

    private fun configurarSpinner() {
        val spinnerHorario: Spinner = findViewById(R.id.spinnerHorario)

        // Extraer las fechas de la lista de horarios
        val fechas = horarios.map { it.fecha }.toTypedArray()

        // Configurar el adaptador del Spinner
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, fechas)
        spinnerHorario.adapter = adapter
    }

    private fun configurarMultiAutoComplete() {
        val multiAutoCompleteJugadores: MultiAutoCompleteTextView = findViewById(R.id.multiAutoCompleteJugadores)

        // Extraer los apodos de la lista de jugadores
        val apodos = jugadores.map { it.apodo }.toTypedArray()

        // Configurar el adaptador del MultiAutoCompleteTextView
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, apodos)
        multiAutoCompleteJugadores.setAdapter(adapter)

        // Configurar el tokenizador para manejar la entrada del usuario
        multiAutoCompleteJugadores.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())

        multiAutoCompleteJugadores.setValidator(object : AutoCompleteTextView.Validator {
            override fun fixText(invalidText: CharSequence?): CharSequence {
                return invalidText ?: ""
            }

            override fun isValid(text: CharSequence?): Boolean {
                val selectedItems = text?.split(",")?.map { it.trim() } ?: emptyList()
                return selectedItems.size <= 5
            }
        })
    }

}