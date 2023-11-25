package com.example.client_app

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarioActivity : AppCompatActivity() {
    lateinit var textViewFecha: TextView
    lateinit var fechaCovertida: String
    var idTanda: String = ""
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendario)

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Reservas"


        textViewFecha = findViewById(R.id.fechaCal)

        textViewFecha.setOnClickListener {
            var calendar: Calendar = Calendar.getInstance()
            var year = calendar.get(Calendar.YEAR)
            var month = calendar.get(Calendar.MONTH)
            var day = calendar.get(Calendar.DAY_OF_MONTH)

            // Crea una instancia del DatePickerDialog y muestra el selector de fecha
            val datePickerDialog = DatePickerDialog(
                this, android.R.style.Theme_Material_Dialog,
                { view, year, month, dayOfMonth ->
                    // Crea una instancia de Calendar y configura la fecha seleccionada
                    val calendario = Calendar.getInstance()
                    calendario.set(Calendar.YEAR, year)
                    calendario.set(Calendar.MONTH, month) // Ten en cuenta que los meses comienzan desde 0 en Calendar
                    calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    // Formatea la fecha seleccionada en el formato deseado
                    val formatoFecha = SimpleDateFormat("dd 'de' MMMM 'de' yyyy, 00:00:00 'UTC-6'", Locale.getDefault())
                    val fechaFormateada = formatoFecha.format(calendario.time)

                    // Muestra la fecha formateada en un TextView (si es necesario)
                    val fecha: TextView = findViewById(R.id.fechaActual)
                    fecha.text = fechaFormateada
                    fechaCovertida = fechaFormateada
                    // Convierte la fecha formateada a un timestamp en milisegundos
                    val timestamp = formatoFecha.parse(fechaFormateada)?.time

                    // Ahora "timestamp" contiene el timestamp en milisegundos
                    if (timestamp != null) {
                        // Aquí puedes hacer algo con el timestamp, por ejemplo, guardarlo en Firebase
                        // o compararlo con otros timestamps
                        println("Timestamp: $timestamp")
                    } else {
                        println("Error al convertir la fecha a timestamp")
                    }
                },
                year, month, day
            )

            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
            datePickerDialog.show()
        }

        val btnBuscar : Button = findViewById(R.id.btnBuscar)

        btnBuscar.setOnClickListener {
            if (fechaCovertida.isNotEmpty()){
                limpiarTabla()
                buscarTanda(fechaCovertida)
                buscarReservas()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                // Acción para el elemento de búsqueda
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun buscarTanda(fechaSelect: String){
        val formatoFecha = SimpleDateFormat("dd 'de' MMMM 'de' yyyy, 00:00:00 'UTC-6'", Locale.getDefault())
        val fecha = formatoFecha.parse(fechaSelect)?.time

        // Muestra las horas en los TextField
        val textFieldInicio: TextView = findViewById(R.id.fechaIni)
        val textFieldFin: TextView = findViewById(R.id.fechaFin)

        db.collection("horario")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val fechaHorario = document.getTimestamp("fecha")
                    Log.d("Mitag", "Fecha Horario: " + fechaHorario?.seconds?.times(1000).toString())
                    Log.d("Mitag", "Fecha : " + fecha?.toString())
                    if ((fechaHorario?.seconds?.times(1000)).toString() == fecha?.toString()){
                        val tandas = document.get("tanda") as List<Timestamp>?

                        idTanda = document.id

                        // Verifica si tandas no es nulo y tiene al menos dos elementos
                        if (tandas != null && tandas.size >= 2) {
                            // Obtiene los timestamps de inicio y fin
                            val timestampInicio = tandas[0].toDate()
                            val timestampFin = tandas[1].toDate()

                            // Convierte los timestamps a formato de hora deseado
                            val formatoHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

                            val horaInicio = formatoHora.format(timestampInicio)
                            val horaFin = formatoHora.format(timestampFin)

                            textFieldInicio.setText(horaInicio)
                            textFieldFin.setText(horaFin)
                        }
                    }
                }
                if (idTanda.isEmpty()) {
                    textFieldInicio.setText("No asignado")
                    textFieldFin.setText("No asignado")
                }
            }
            .addOnFailureListener { e ->

            }
    }

    fun buscarReservas(){
        val tabla: TableLayout = findViewById(R.id.tablaReservas)
        //idTanda = "KYMtYhYUINlSTxiIgBUH"
        Log.d("MiTag", "Id tienda: " + idTanda)
        if (idTanda.isNotEmpty()){
            db.collection("reservas")
                .whereEqualTo("horario", idTanda)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val tipoReserva = document.getString("tipo")
                        val fechaReserva = document.getTimestamp("fecha")

                        // Convierte los timestamps a formato de hora deseado
                        val formatoHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

                        val horaInicio = formatoHora.format(fechaReserva?.toDate())

                        llenarTabla(tabla, horaInicio, tipoReserva)
                    }
                }
                .addOnFailureListener { e ->
                    // Handle the error
                }
        }
    }

    private fun llenarTabla(tableLayout: TableLayout, hora : String, tipo : String?) {
        val tableRow = TableRow(this)
        tableRow.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )

        // Crea un TextView para la hora
        val textViewHora = TextView(this)
        textViewHora.text = hora
        textViewHora.gravity = Gravity.CENTER
        textViewHora.setPadding(8, 8, 8, 8)
        tableRow.addView(textViewHora)

        // Crea un TextView para el tipo
        val textViewTipo = TextView(this)
        textViewTipo.text = tipo
        textViewTipo.gravity = Gravity.CENTER
        textViewTipo.setPadding(8, 8, 8, 8)
        tableRow.addView(textViewTipo)

        // Crea un botón para el estado
        val buttonEstado = Button(this)
        buttonEstado.text = "Reservar"
        buttonEstado.gravity = Gravity.CENTER
        buttonEstado.setPadding(8, 8, 8, 8)
        buttonEstado.setBackgroundColor(ContextCompat.getColor(this, R.color.principal))
        buttonEstado.isEnabled = (tipo == "Publica") // Habilita solo si el tipo es "Publica"

        // Agrega un listener al botón para cambiar de actividad
        buttonEstado.setOnClickListener {
            if (tipo == "Publica") {
                // Lógica para cambiar de actividad cuando el tipo es "Publica"
                cambiarDeActividad()
            }
        }
        tableRow.addView(buttonEstado)
        // Agrega la fila a la tabla
        tableLayout.addView(tableRow)
    }

    private fun limpiarTabla(){
        val tableLayout = findViewById<TableLayout>(R.id.tablaReservas)

        // Obtener el número total de filas
        val rowCount = tableLayout.childCount

        // Iterar sobre las filas en orden inverso para evitar problemas con la eliminación
        for (i in rowCount - 1 downTo 0) {
            val row = tableLayout.getChildAt(i)

            // Verificar si el nombre de la fila no es "Encabezado"
            if (row.tag != "Encabezado") {
                // Eliminar la fila que no es "Encabezado"
                tableLayout.removeViewAt(i)
            }
        }

    }

    private fun cambiarDeActividad() {
        // Lógica para cambiar de actividad
        Toast.makeText(this, "Hola", Toast.LENGTH_SHORT).show()
    }
}