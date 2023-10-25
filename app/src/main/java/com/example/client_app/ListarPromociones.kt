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

data class Promocion(val descripcion: String, val estado: Boolean, val fecha_final: String, val fecha_inicio: String, val imagen_url: String, val nombre: String)

private lateinit var db: FirebaseFirestore
private lateinit var startForResult: ActivityResultLauncher<Intent>

class ListarPromociones : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_promociones)
        db = FirebaseFirestore.getInstance()

        promocionesListData()

        val searchPromotion: ImageButton = findViewById(R.id.searchPromocion)
        searchPromotion.setOnClickListener {
            activitybuscarPromocion()
        }

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data
                // Handle the result if needed
            }
        }
    }

    private fun promocionesListData() {
        val promocionesCollection = db.collection("promocion")
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")

        promocionesCollection.whereEqualTo("estado", true)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val promocionesList = mutableListOf<Promocion>()

                for (document in querySnapshot) {
                    val nombre = document.getString("nombre") ?: ""
                    val descripcion = document.getString("descripcion") ?: ""
                    val estado = document.getBoolean("estado") ?: false
                    val fechaInicio = document.getDate("fecha_inicio") ?: Date()
                    val fechaFinal = document.getDate("fecha_final") ?: Date()
                    val imagenUrl = document.getString("imagen_url") ?: ""

                    // Formatear las fechas antes de imprimir
                    val fechaInicioFormateada = dateFormat.format(fechaInicio)
                    val fechaFinalFormateada = dateFormat.format(fechaFinal)

                    println("Fecha de inicio: $fechaInicioFormateada")
                    println("Fecha final: $fechaFinalFormateada")

                    val promocion = Promocion(
                        descripcion = descripcion,
                        estado = estado,
                        fecha_inicio = fechaInicioFormateada,
                        fecha_final = fechaFinalFormateada,
                        imagen_url = imagenUrl,
                        nombre = nombre
                    )
                    promocionesList.add(promocion)
                }

                displayPromociones(promocionesList)
            }
            .addOnFailureListener { exception ->
                // Handle failures
            }
    }

    private fun displayPromociones(promocionesList: List<Promocion>) {
        val listView: ListView = findViewById(R.id.lv1)
        val adapter = PromocionAdapter(this, promocionesList)
        listView.adapter = adapter
    }

    class PromocionAdapter(context: Context, private val promociones: List<Promocion>) : ArrayAdapter<Promocion>(context, 0, promociones) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var itemView = convertView
            if (itemView == null) {
                itemView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
            }

            val promocion = promociones[position]
            itemView?.findViewById<TextView>(android.R.id.text1)?.text = "Nombre: ${promocion.nombre}\nFecha de inicio: ${promocion.fecha_inicio}\nFecha de fin: ${promocion.fecha_final}"
            //itemView?.findViewById<TextView>(android.R.id.text1)?.text = "Nombre: ${promocion.nombre}  Fecha: ${promocion.fechaEjemplo}"

            itemView?.setOnClickListener {
                // Aquí debes acceder a los campos específicos de la promoción
                val intent = Intent(context, DetallePromocion::class.java)
                intent.putExtra("nombre", promocion.nombre)
                intent.putExtra("descripcion", promocion.descripcion)
                intent.putExtra("estado", promocion.estado)
                intent.putExtra("fecha_final", promocion.fecha_final)
                intent.putExtra("fecha_inicio", promocion.fecha_inicio)
                intent.putExtra("imagen_url", promocion.imagen_url)
                // Añadir otros campos según sea necesario
                context.startActivity(intent)
            }
            return itemView!!
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun activitybuscarPromocion() {
        val intent = Intent(this, BuscarPromocion::class.java)
        startForResult.launch(intent)
    }
}
