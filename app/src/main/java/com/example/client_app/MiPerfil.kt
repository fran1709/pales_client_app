package com.example.client_app


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore

class MiPerfil : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mi_perfil)
        // Inicializar Firebase
        db = FirebaseFirestore.getInstance()

        userData()

    }
    private fun userData() {
        val userName: TextView = findViewById(R.id.user_name)
        val nickname: TextView = findViewById(R.id.nickname)
        val position: TextView = findViewById(R.id.position)
        val age: TextView = findViewById(R.id.age)
        val phone: TextView = findViewById(R.id.phone)

        val nombreUsuario = "Diego Carrillo"

       db.collection("jugadores")
            .whereEqualTo("nombre", nombreUsuario)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                for (document in documentSnapshot) {
                    // Retrieve data from the document
                    val playerName = document.getString("nombre")
                    val playerNickname = document.getString("apodo")
                    val playerPositions = document.get("posiciones") as List<String>?
                    val playerBirthday = document.getString("fecha_nacimiento")
                    val playerPhone = document.getString("teléfono")

                    // Update the TextViews with the retrieved data
                    userName.text = "Nombre de Usuario: $playerName"
                    nickname.text = "Apodo: $playerNickname"
                    position.text = "Posiciones:" + playerPositions?.joinToString(", ") ?: ""
                    age.text = "Fecha de Nacimiento: $playerBirthday"
                    phone.text = "Teléfono: $playerPhone"
                }
            }
            .addOnFailureListener { exception ->
                // Handle failures
            }
    }

}