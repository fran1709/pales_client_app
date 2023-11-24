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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private lateinit var db: FirebaseFirestore
private lateinit var startForResult: ActivityResultLauncher<Intent>
private lateinit var etDateStart: EditText
private lateinit var etDateEnd: EditText
private var selectedStartDate: String = ""
private var selectedEndDate: String = ""


class BuscarPromocion : AppCompatActivity() {
    private val promocionesList = mutableListOf<Promocion>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscar_promocion)
        etDateStart = findViewById(R.id.etDateStart)
        etDateStart.setOnClickListener{ showDatePickerDialogStart() }
        etDateEnd = findViewById(R.id.etDateEnd)
        etDateEnd.setOnClickListener{ showDatePickerDialogEnd() }

        db = FirebaseFirestore.getInstance()

        val backButton: ImageButton = findViewById(R.id.backButton)
        val searchPromocionButton: ImageButton = findViewById(R.id.searchPromocion)
        val searchInput: EditText = findViewById(R.id.etBuscarPromocion)
        val lv1: ListView = findViewById(R.id.lv1)

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data
            }
        }

        val adapter = PromocionAdapter(this, mutableListOf())
        lv1.adapter = adapter

        searchPromocionButton.setOnClickListener {
            val searchText = searchInput.text.toString().toLowerCase(Locale.getDefault())
            val hasTextFilter = searchText.isNotEmpty()

            val startDateFilter: Date? = if (selectedStartDate.isNotEmpty()) {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(selectedStartDate)
            } else {
                null
            }

            val endDateFilter: Date? = if (selectedEndDate.isNotEmpty()) {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(selectedEndDate)
            } else {
                null
            }

            val filteredPromociones = promocionesList.filter { promocion ->
                val fechaInicio = SimpleDateFormat("dd/MM/yyyy").parse(promocion.fecha_inicio)
                val fechaFinal = SimpleDateFormat("dd/MM/yyyy").parse(promocion.fecha_final)

                val textMatch = !hasTextFilter || promocion.nombre.toLowerCase(Locale.getDefault()).contains(searchText)
                val startDateMatch = startDateFilter == null || fechaInicio >= startDateFilter
                val endDateMatch = endDateFilter == null || fechaFinal <= endDateFilter

                textMatch && startDateMatch && endDateMatch
            }

            adapter.updateData(filteredPromociones)
        }


        backButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        loadPromocionesData()
    }

    private fun loadPromocionesData() {
        val lv1: ListView = findViewById(R.id.lv1)
        val promocionesCollection = db.collection("promocion")
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")

        promocionesCollection.whereEqualTo("estado", true)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val id = document.id
                    val nombre = document.getString("nombre") ?: ""
                    val descripcion = document.getString("descripcion") ?: ""
                    val estado = document.getBoolean("estado") ?: false
                    val visto = false

                    if (estado) {
                        val fechaInicioRaw = document.getDate("fecha_inicio")?.time ?: 0
                        val fechaFinalRaw = document.getDate("fecha_final")?.time ?: 0
                        val imagenUrl = document.getString("imagen_url") ?: ""

                        // Formatear las fechas para mostrar en la interfaz
                        val fechaInicioFormateada = dateFormat.format(Date(fechaInicioRaw))
                        val fechaFinalFormateada = dateFormat.format(Date(fechaFinalRaw))

                        val promocion = Promocion(
                            id = id,
                            descripcion = descripcion,
                            estado = estado,
                            fecha_inicio = fechaInicioFormateada,
                            fecha_final = fechaFinalFormateada,
                            imagen_url = imagenUrl,
                            nombre = nombre,
                            visto = visto
                        )
                        promocionesList.add(promocion)
                    }
                }

                val adapter = lv1.adapter as PromocionAdapter
                adapter.updateData(promocionesList)
            }
            .addOnFailureListener { exception ->
                println("Ocurrió un error: ${exception.message}")
            }
    }


    class PromocionAdapter(context: Context, private val promociones: MutableList<Promocion>) : ArrayAdapter<Promocion>(context, 0, promociones) {
        fun updateData(newData: List<Promocion>) {
            promociones.clear()
            promociones.addAll(newData)
            notifyDataSetChanged()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var itemView = convertView
            if (itemView == null) {
                itemView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
            }

            val promocion = promociones[position]
            itemView?.findViewById<TextView>(android.R.id.text1)?.text = "Nombre: ${promocion.nombre}\nFecha de inicio: ${promocion.fecha_inicio}\nFecha de fin: ${promocion.fecha_final}"

            itemView?.setOnClickListener {
                db.collection("promocion")
                    .whereEqualTo("nombre", promocion.nombre)
                    .whereEqualTo("estado", true)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        for (document in documentSnapshot) {
                            val descripcion = document.getString("descripcion")
                            val estado = document.getBoolean("estado")
                            val fechaInicio = document.getTimestamp("fecha_inicio")?.toDate()?.time
                            val fechaFinal = document.getTimestamp("fecha_final")?.toDate()?.time
                            val imagenUrl = document.getString("imagen_url")
                            val nombre = document.getString("nombre")

                            val intent = Intent(context, DetallePromocion::class.java)
                            intent.putExtra("descripcion", descripcion)
                            intent.putExtra("estado", estado)
                            intent.putExtra("fechaInicio", fechaInicio)
                            intent.putExtra("fechaFinal", fechaFinal)
                            intent.putExtra("imagenUrl", imagenUrl)
                            intent.putExtra("nombre", nombre)
                            context.startActivity(intent)
                        }
                    }
            }
            return itemView!!
        }
    }

    fun activityDetallePromocion(view: View) {
        val intent = Intent(this, DetallePromocion::class.java)
        startForResult.launch(intent)
    }

    private fun showDatePickerDialogStart(){
        val datePicker = DatePickerFragment({day, month, year , -> onDateSelectedStart(day, month, year)})
        datePicker.show(supportFragmentManager, "datePicker")
    }

    fun onDateSelectedStart(day: Int, month: Int, year: Int) {
        val formattedDay = String.format("%02d", day)
        val formattedMonth = String.format("%02d", month + 1)
        selectedStartDate = "$formattedDay/$formattedMonth/$year"
        etDateStart.setText(selectedStartDate)
    }

    private fun showDatePickerDialogEnd(){
        val datePicker = DatePickerFragment({day, month, year , -> onDateSelectedEnd(day, month, year)})
        datePicker.show(supportFragmentManager, "datePicker")
    }

    fun onDateSelectedEnd(day: Int, month: Int, year: Int) {
        val formattedDay = String.format("%02d", day)
        val formattedMonth = String.format("%02d", month + 1)
        selectedEndDate = "$formattedDay/$formattedMonth/$year"
        etDateEnd.setText(selectedEndDate)
    }
}
