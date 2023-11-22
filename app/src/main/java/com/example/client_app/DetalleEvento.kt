package com.example.client_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import com.squareup.picasso.Picasso
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetalleEvento : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        val imageView: ImageView = findViewById(R.id.imagenView)
        Picasso.get().load(imagenUrl).placeholder(R.drawable.cancha1).into(imageView)

        val tvPromotionName: TextView = findViewById(R.id.tvEventoName)

        // Establecer los valores en los TextViews
        tvPromotionName.text = nombre
        descripcionText.text = descripcion
        fechaText.text = fecha

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