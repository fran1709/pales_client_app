package com.example.client_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilJugador : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private val listaBloqueos = mutableListOf<String>()
    private var jugadorUID: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_jugador)
        // Inicializar Firebase
        db = FirebaseFirestore.getInstance()

        val backButton: ImageButton = findViewById(R.id.backButton)
        val bloquearButton: Button = findViewById(R.id.BloquearButton)
        val desbloquearButton: Button = findViewById(R.id.DesloquearButton)

        val uid = intent.getStringExtra("UID")
        if (uid != null) {
            jugadorUID = uid
        }
        val nombre = intent.getStringExtra("nombre")
        val apodo = intent.getStringExtra("apodo")
        val posiciones = intent.getStringArrayListExtra("posiciones")

        val userName: TextView = findViewById(R.id.tvUsers)
        val nickname: TextView = findViewById(R.id.nicknameText)
        val position: TextView = findViewById(R.id.posicionText)

        userName.text = "$nombre"
        nickname.text = "$apodo"
        position.text = "${posiciones?.joinToString(", ")}"

        displayBloqueos()

        backButton.setOnClickListener {
            setResult(RESULT_CANCELED);
            finish();
        }

        bloquearButton.setOnClickListener {
            bloquearJugador()
            setResult(RESULT_CANCELED);
            finish();
        }

        desbloquearButton.setOnClickListener {
            desbloquearJugador()
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private fun bloquearJugador() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val usuarioConectado = user.uid
            val idDelJugadorBloqueado = jugadorUID

            val jugadoresCollection = db.collection("jugadores")

            jugadoresCollection.whereEqualTo("UID", usuarioConectado)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        val bloqueados = document.get("bloqueos") as? MutableList<String> ?: mutableListOf()
                        if (!bloqueados.contains(idDelJugadorBloqueado)) {
                            bloqueados.add(idDelJugadorBloqueado)

                            jugadoresCollection.document(document.id)
                                .update("bloqueos", bloqueados)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Jugador bloqueado con éxito", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error al bloquear jugador", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al buscar al jugador", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun desbloquearJugador() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val usuarioConectado = user.uid
            val idDelJugadorDesbloqueado = jugadorUID

            val jugadoresCollection = db.collection("jugadores")

            jugadoresCollection.whereEqualTo("UID", usuarioConectado)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        val bloqueados = document.get("bloqueos") as? MutableList<String> ?: mutableListOf()

                        bloqueados.removeAll { it == idDelJugadorDesbloqueado }

                        jugadoresCollection.document(document.id)
                            .update("bloqueos", bloqueados)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Jugador desbloqueado con éxito", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al desbloquear jugador", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al buscar al jugador", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun displayBloqueos() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val usuarioConectado = user.uid
            val idDelJugadorBloqueado = jugadorUID

            val jugadoresCollection = db.collection("jugadores")

            jugadoresCollection.whereEqualTo("UID", usuarioConectado)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        val bloqueados = document.get("bloqueos") as? List<String> ?: emptyList()
                        listaBloqueos.clear()
                        listaBloqueos.addAll(bloqueados)

                        val idDelJugadorBloqueado = idDelJugadorBloqueado

                        val bloqueado = listaBloqueos.contains(idDelJugadorBloqueado)

                        actualizarBotones(bloqueado)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al buscar los bloqueos", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun actualizarBotones(bloqueado: Boolean) {
        val bloquearButton: Button = findViewById(R.id.BloquearButton)
        val desbloquearButton: Button = findViewById(R.id.DesloquearButton)

        if (bloqueado) {
            bloquearButton.visibility = View.GONE
            bloquearButton.isEnabled = false
            desbloquearButton.visibility = View.VISIBLE
            desbloquearButton.isEnabled = true
        } else {
            bloquearButton.visibility = View.VISIBLE
            bloquearButton.isEnabled = true
            desbloquearButton.visibility = View.GONE
            desbloquearButton.isEnabled = false
        }
    }

}