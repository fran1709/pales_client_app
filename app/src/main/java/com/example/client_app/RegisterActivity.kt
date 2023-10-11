package com.example.client_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Registro de Usuario"

        // Spinner (Dropdown) para la posición
        val spinner = findViewById<Spinner>(R.id.spinnerPosition)
        val positions = arrayOf("Portero", "Defensa", "Creativo", "Delantero")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, positions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                // Acción para el elemento de búsqueda
                callLogin()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun registerButtonClicked(view: View) {

        val email = findViewById<EditText>(R.id.editTextEmail).text.toString()
        val password = findViewById<EditText>(R.id.editTextPassword).text.toString()


        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    if (user != null) {
                        saveUserData(email, password, user.uid)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    fun saveUserData(email: String, password: String, id: String){
        val fullName = findViewById<EditText>(R.id.editTextFullName).text.toString()
        val nickname = findViewById<EditText>(R.id.editTextNickname).text.toString()
        val phoneNumber = findViewById<EditText>(R.id.editTextPhoneNumber).text.toString()
        val age = findViewById<EditText>(R.id.editTextAge).text.toString()
        val selectedPosition = findViewById<Spinner>(R.id.spinnerPosition).selectedItem.toString()
        val bloqueo = intArrayOf();

        val usuario = hashMapOf(
            "email" to email,
            "contraseña" to password,
            "nombre" to fullName,
            "telefono" to phoneNumber,
            "edad" to age,
            "apodo" to nickname,
            "posiciones" to selectedPosition,
            "bloqueos" to bloqueo,
            "rol" to "Jugador",
            "estado" to true,
            "id" to id
        )

        db.collection("jugadores")
            .add(usuario)
            .addOnSuccessListener { documentReference ->
                println("DocumentSnapshot agregado con ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                println("Error al agregar documento: $e")
            }
    }

    fun callLogin() {
        // Crear un Intent para iniciar la Activity2
        val intent = Intent(this, LoginActivity::class.java)
        // Iniciar la Activity2 utilizando el Intent
        startActivity(intent)
    }
}