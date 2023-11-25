package com.example.client_app

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.client_app.utils.CryptograpyPasswordClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    lateinit var editTextNombre: EditText
    lateinit var editTextApodo: EditText
    lateinit var editTextTelefono: EditText
    lateinit var editTextEmail: EditText
    lateinit var editTextPassword: EditText
    lateinit var textViewFechanacimiento: TextView
    lateinit var fechaCovertida: String

    lateinit var btnRegister: Button

    private var posiciones = mutableListOf<String>();
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        // Inicializa los inputs
        editTextNombre = findViewById(R.id.editTextFullName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextApodo = findViewById(R.id.editTextNickname)
        editTextTelefono = findViewById(R.id.editTextPhoneNumber)

        btnRegister = findViewById(R.id.btnRegister)
        // Initialize Firebase Auth
        auth = Firebase.auth

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Registro de Usuario"

        textViewFechanacimiento = findViewById(R.id.fechaNacimientoRegister)

        btnRegister.setOnClickListener { registerButtonClicked() }

        textViewFechanacimiento.setOnClickListener {
            var calendar: Calendar = Calendar.getInstance()
            var year = calendar.get(Calendar.YEAR)
            var month = calendar.get(Calendar.MONTH)
            var day = calendar.get(Calendar.DAY_OF_MONTH)

            // Crea una instancia del DatePickerDialog y muestra el selector de fecha

            val datePickerDialog = DatePickerDialog(
                this, android.R.style.Theme_Material_Dialog,
                { view, year, month, dayOfMonth ->
                    // La fecha seleccionada por el usuario
                    val fechaSeleccionada = "$dayOfMonth/${month + 1}/$year"
                    // Aquí puedes hacer algo con la fecha seleccionada, por ejemplo, mostrarla en un TextView


                    // Crea una instancia de Calendar y configura la fecha seleccionada
                    val calendario = Calendar.getInstance()
                    calendario.set(Calendar.YEAR, year)
                    calendario.set(Calendar.MONTH, month) // Ten en cuenta que los meses comienzan desde 0 en Calendar
                    calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    // Obtiene el Timestamp de la fecha seleccionada
                    textViewFechanacimiento.text = fechaSeleccionada
                    fechaCovertida = calendario.timeInMillis.toString()

                },
                year, month, day
            )
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
            datePickerDialog.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                // Acción para el elemento de búsqueda
                call_main_activity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    fun registerButtonClicked() {
        auth.createUserWithEmailAndPassword(editTextEmail.text.toString(), editTextPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    if (user != null) {
                        saveUserData(editTextEmail.text.toString(), editTextPassword.text.toString(), user.uid)
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
        val fullName = editTextNombre.text.toString()
        val phoneNumber = editTextTelefono.text.toString()
        val nickname = editTextApodo.text.toString()
        val bloqueosList = mutableListOf<String>()
        val cryptClass = CryptograpyPasswordClass()
        val passEncrypt = cryptClass.encrypt(password)

        if (emptyInputsCheck() && positionCheck()){
            val usuario = hashMapOf(
                "correo" to email,
                "contrasena" to passEncrypt,
                "nombre" to fullName.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                "telefono" to phoneNumber,
                "fecha_nacimiento" to fechaCovertida,
                "apodo" to nickname.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()) else it.toString() },
                "posiciones" to posiciones,
                "bloqueos" to bloqueosList,
                "rol" to "Jugador",
                "estado" to false,
                "clasificacion" to "Regular",
                "UID" to id.toString()
            )

            db.collection("jugadores")
                .add(usuario)
                .addOnSuccessListener { documentReference ->
                    println("DocumentSnapshot agregado con ID: ${documentReference.id}")
                    Toast.makeText(
                        baseContext,
                        "Registro Exitoso",
                        Toast.LENGTH_SHORT,
                    ).show()
                    call_main_activity()
                }
                .addOnFailureListener { e ->
                    println("Error al agregar documento: $e")
                    Toast.makeText(
                        baseContext,
                        "Registro Fallido",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
        } else {
            Toast.makeText(
                baseContext,
                "Informacion incompleta",
                Toast.LENGTH_SHORT,
            ).show()
        }

    }

    fun positionCheck(): Boolean {
        val goalkeeper = findViewById<CheckBox>(R.id.porteroCheck)
        val defense = findViewById<CheckBox>(R.id.defensaCheck)
        val latDer = findViewById<CheckBox>(R.id.latDerCheck)
        val latIzq = findViewById<CheckBox>(R.id.latIzqCheck)
        val midfielder = findViewById<CheckBox>(R.id.medioCheck)
        val forward = findViewById<CheckBox>(R.id.delanteroCheck)

        val isGoalKeeper = goalkeeper.isChecked
        val isDefense = defense.isChecked
        val islatDer = latDer.isChecked
        val islatIzq = latIzq.isChecked
        val isMidfielder = midfielder.isChecked
        val isFordward = forward.isChecked

        var totalCheck = false

        if(isGoalKeeper){
            posiciones.add("Portero")
            totalCheck = true
        }
        if (isDefense){
            posiciones.add("Defensa")
            totalCheck = true
        }
        if (islatDer){
            posiciones.add("Lateral Derecho")
            totalCheck = true
        }
        if (islatIzq){
            posiciones.add("Lateral Izquierdo")
            totalCheck = true
        }
        if (isMidfielder){
            posiciones.add("Mediocampista")
            totalCheck = true
        }
        if (isFordward){
            posiciones.add("Delantero")
            totalCheck = true
        }
        return totalCheck
    }

    fun emptyInputsCheck(): Boolean{
        val nombre: String = editTextNombre.text.toString()
        val email: String = editTextEmail.text.toString()
        val password: String = editTextPassword.text.toString()
        var apodo: String = editTextApodo.text.toString()
        val telefono: String = editTextTelefono.text.toString()
        val fechaNac: String = textViewFechanacimiento.text.toString()

        var totalCheck = false

        if(TextUtils.isEmpty(nombre) || TextUtils.isEmpty(email) ||
            TextUtils.isEmpty(password) || TextUtils.isEmpty(telefono)
            || TextUtils.isEmpty(fechaNac)
        ){

            if (TextUtils.isEmpty(nombre)) {
                Toast.makeText(
                    baseContext,
                    "Ingrese su nombre",
                    Toast.LENGTH_SHORT,
                ).show()
            }
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(
                    baseContext,
                    "Ingrese un email",
                    Toast.LENGTH_SHORT,
                ).show()
            }
            if (TextUtils.isEmpty(telefono)) {
                Toast.makeText(
                    baseContext,
                    "Ingrese un telefono",
                    Toast.LENGTH_SHORT,
                ).show()
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(
                    baseContext,
                    "Ingrese una contraseña",
                    Toast.LENGTH_SHORT,
                ).show()
            }
            if (TextUtils.isEmpty(fechaNac)) {
                Toast.makeText(
                    baseContext,
                    "Seleccione una fecha de nacimiento",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        } else {
            totalCheck = true
        }
        if (TextUtils.isEmpty(apodo)) {
            apodo = " "
            apodo = editTextNombre.text.toString()
        }
        return totalCheck
    }
    fun call_main_activity(){
        // Crear un Intent para iniciar la Activity2
        val intent = Intent(this, MainActivity::class.java)

        startActivity(intent)
    }
}