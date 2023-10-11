package com.example.client_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore

class PerfilJugador : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_jugador)
        // Inicializar Firebase
        db = FirebaseFirestore.getInstance()

        val nombre = intent.getStringExtra("nombre")
        val apodo = intent.getStringExtra("apodo")
        val posiciones = intent.getStringArrayListExtra("posiciones")
        val fechaNacimiento = intent.getStringExtra("fecha_nacimiento")
        val telefono = intent.getStringExtra("telefono")

        val userName: TextView = findViewById(R.id.user_name)
        val nickname: TextView = findViewById(R.id.nickname)
        val position: TextView = findViewById(R.id.position)
        val age: TextView = findViewById(R.id.age)
        val phone: TextView = findViewById(R.id.phone)

        userName.text = "Nombre: $nombre"
        nickname.text = "Apodo: $apodo"
        position.text = "Posiciones: ${posiciones?.joinToString(", ")}"
        age.text = "Fecha de Nacimiento: $fechaNacimiento"
        phone.text = "Teléfono: $telefono"

    }
}