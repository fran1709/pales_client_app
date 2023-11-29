package com.example.client_app

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance()

        // Recordar al usuario
        loadPreferences()
    }

    fun logIn(view: View){
        val email: EditText = findViewById(R.id.editTextEmail)
        val password: EditText = findViewById(R.id.editTextPassword)

        if (email.text.isNotEmpty() && password.text.isNotEmpty()){
            showProgressBar()
            auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = auth.currentUser
                        if (user != null) {
                            verification(user.uid) { isVerified ->
                                handleVerificationResult(isVerified)
                            }
                        }
                        rememberUser(email.text.toString(), password.text.toString())
                    } else {
                        // If sign in fails, display a message to the user.
                        hideProgressBar()
                        Toast.makeText(
                            baseContext,
                            "No se pudo iniciar sesión.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        } else if (email.text.isEmpty()){
            hideProgressBar()
            Toast.makeText(
                baseContext,
                "Por favor ingrese un correo",
                Toast.LENGTH_SHORT,
            ).show()
        } else if (password.text.isEmpty()){
            hideProgressBar()
            Toast.makeText(
                baseContext,
                "Por favor ingrese una contraseña",
                Toast.LENGTH_SHORT,
            ).show()
        }

    }
    private fun showProgressBar() {
        // Muestra el ProgressBar
        val progressBar = findViewById<ProgressBar>(R.id.loginProgressBar)
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        // Oculta el ProgressBar
        val progressBar = findViewById<ProgressBar>(R.id.loginProgressBar)
        progressBar.visibility = View.INVISIBLE
    }
    private fun handleVerificationResult(isVerified: Boolean) {
        hideProgressBar()
        if (isVerified) {
            Toast.makeText(
                baseContext,
                "Bienvenido",
                Toast.LENGTH_SHORT,
            ).show()
            call_activity_menu_principal()
        } else {
            Toast.makeText(
                baseContext,
                "La verificación de usuario está pendiente",
                Toast.LENGTH_LONG,
            ).show()
            auth.signOut()
        }
    }
    fun verification(userID: String, callback: (Boolean) -> Unit) {
        db.collection("jugadores")
            .whereEqualTo("UID", userID)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val document = documentSnapshot.documents[0]
                val isVerified = document.getBoolean("estado") == true
                callback(isVerified)
            }
            .addOnFailureListener { exception ->
                // Handle failures
                callback(false)
            }
    }

    // Función para recuperar contraseña
    fun resetPassword(view: View) {
        val email: EditText = findViewById(R.id.editTextEmail)

        val emailAddress = email.text.toString()

        if (emailAddress.isNotEmpty()) {
            auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Se ha enviado un correo para restablecer la contraseña.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error al enviar el correo de restablecimiento de contraseña.", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Ingrese un correo electrónico válido.", Toast.LENGTH_SHORT).show()
        }
    }

    fun call_activity_menu_principal(){
        // Crear un Intent para iniciar la Activity2
        val intent = Intent(this, menu_principal::class.java)
        startActivity(intent)
    }

    fun rememberUser(correo : String, password : String){
        val checkBoxRemember = findViewById<CheckBox>(R.id.checkRemember)
        val isChecked = checkBoxRemember.isChecked

        if (isChecked){
            savePreferences(correo, password, isChecked)
        }
    }

    fun savePreferences(correo : String, password : String, estado : Boolean){
        // Obtener una referencia al objeto SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("RememberUser", Context.MODE_PRIVATE)

        // Editor para realizar cambios en SharedPreferences
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        // Almacenar un valor en SharedPreferences
        editor.putString("correo", correo)
        editor.putString("password", password)
        editor.putBoolean("recordar", estado)
        editor.apply() // Guardar los cambios
    }

    fun loadPreferences(){
        // Obtener una referencia al objeto SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("RememberUser", Context.MODE_PRIVATE)

        // Recuperar valores de SharedPreferences
        val correo: String? = sharedPreferences.getString("correo", "")
        val contrasenia: String? = sharedPreferences.getString("password", "")
        val estado : Boolean = sharedPreferences.getBoolean("recordar", false)

        if (estado){
            val email: EditText = findViewById(R.id.editTextEmail)
            val password: EditText = findViewById(R.id.editTextPassword)
            val checkBoxRemember = findViewById<CheckBox>(R.id.checkRemember)

            email.setText(correo)
            password.setText(contrasenia)
            checkBoxRemember.isChecked = estado
        }
    }
    fun callRegister(view: View) {
        // Crear un Intent para iniciar la Activity2
        val intent = Intent(this, RegisterActivity::class.java)

        // Iniciar la Activity2 utilizando el Intent
        startActivity(intent)
    }



}