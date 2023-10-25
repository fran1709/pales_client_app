package com.example.client_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetallePromocion : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_promocion)

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance()

        val backButton: ImageButton = findViewById(R.id.backButton)

        val descripcion = intent.getStringExtra("descripcion")
        val estado = intent.getBooleanExtra("estado", false)
        val fechaInicio = intent.getStringExtra("fecha_inicio")
        val fechaFinal = intent.getStringExtra("fecha_final")
        val imagenUrl = intent.getStringExtra("imagen_url")
        val nombre = intent.getStringExtra("nombre")

        val descripcionText: TextView = findViewById(R.id.descripcionText)
        val estadoText: TextView = findViewById(R.id.estadoText)
        val fechaInicioText: TextView = findViewById(R.id.fecha_inicioText)
        val fechaFinalText: TextView = findViewById(R.id.fecha_finalText)
        val imagenUrlText: TextView = findViewById(R.id.imagen_urlText)
        val tvPromotionName: TextView = findViewById(R.id.tvPromotionName)

        // Establecer los valores en los TextViews
        tvPromotionName.text = nombre
        descripcionText.text = descripcion
        estadoText.text = if (estado) "Activa" else "Inactiva"
        //fechaInicioText.text = convertTimestampToString(fechaInicio)
        //fechaFinalText.text = convertTimestampToString(fechaFinal)
        fechaInicioText.text = fechaInicio
        fechaFinalText.text = fechaFinal
        imagenUrlText.text = imagenUrl

        backButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    // Función para convertir una marca de tiempo en milisegundos a una cadena de fecha legible
    private fun convertTimestampToString(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = Date(timestamp)
        return dateFormat.format(date)
    }

}