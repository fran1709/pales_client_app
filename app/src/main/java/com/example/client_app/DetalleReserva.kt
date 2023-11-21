package com.example.client_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetalleReserva : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
        setContentView(R.layout.activity_detalle_evento)

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance()

        val backButton: ImageButton = findViewById(R.id.backButton)

        val descripcion = intent.getStringExtra("descripcion")
        val fecha = intent.getStringExtra("fecha")
        val imagenUrl = intent.getStringExtra("imagen_url")
        val nombre = intent.getStringExtra("nombre")

        val descripcionText: TextView = findViewById(R.id.descripcionText)
        val fechaText: TextView = findViewById(R.id.fechaText)
        val imagenUrlText: TextView = findViewById(R.id.imagen_urlText)
        val tvPromotionName: TextView = findViewById(R.id.tvEventoName)

        // Establecer los valores en los TextViews
        tvPromotionName.text = nombre
        descripcionText.text = descripcion
        fechaText.text = fecha
        imagenUrlText.text = imagenUrl

        backButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
        */
    }
}