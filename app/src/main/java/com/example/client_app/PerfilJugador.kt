package com.example.client_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore

class PerfilJugador : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_jugador)
        // Inicializar Firebase
        db = FirebaseFirestore.getInstance()

        val backButton: ImageButton = findViewById(R.id.backButton)

        val nombre = intent.getStringExtra("nombre")
        val apodo = intent.getStringExtra("apodo")
        val posiciones = intent.getStringArrayListExtra("posiciones")
        val fechaNacimiento = intent.getStringExtra("fecha_nacimiento")
        val telefono = intent.getStringExtra("telefono")
        val clasificaciontext = intent.getStringExtra("clasificacion")

        val userName: TextView = findViewById(R.id.tvUsers)
        val nickname: TextView = findViewById(R.id.nicknameText)
        val position: TextView = findViewById(R.id.posicionText)
        val age: TextView = findViewById(R.id.edadText)
        val phone: TextView = findViewById(R.id.telefonoText)
        val clasificacion: TextView = findViewById(R.id.clasificacionText)

        userName.text = "$nombre"
        nickname.text = "$apodo"
        position.text = "${posiciones?.joinToString(", ")}"
        age.text = "$fechaNacimiento"
        phone.text = "$telefono"
        clasificacion.text = "$clasificaciontext"


        backButton.setOnClickListener {
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}