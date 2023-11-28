package com.example.client_app

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.Date
import java.util.Locale

class EditarPerfil : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    lateinit var textViewFechanacimiento: TextView
    lateinit var fechaCovertida: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth

        val id = auth.currentUser?.uid

        userData(id)

    }
    private fun userData(id : String?) {
        val userName: EditText = findViewById(R.id.etName)
        val nickname: EditText = findViewById(R.id.etnickname)
        val phone: EditText = findViewById(R.id.etphone)
        val porteroCheck1 = findViewById<CheckBox>(R.id.porteroCheck1)
        val defensaCheck1 = findViewById<CheckBox>(R.id.defensaCheck1)
        val medioCheck1 = findViewById<CheckBox>(R.id.medioCheck1)
        val delanteroCheck1 = findViewById<CheckBox>(R.id.delanteroCheck1)
        val latIzqCheck1 = findViewById<CheckBox>(R.id.latIzqCheck1)
        val latDerCheck1 = findViewById<CheckBox>(R.id.latDerCheck1)

        textViewFechanacimiento = findViewById(R.id.fechaNacimientoEdit)

        val saveProfile: ImageButton = findViewById(R.id.saveProfile)
        val backProfile: ImageButton = findViewById(R.id.backProfile)

        val formatoDeseado = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        db.collection("jugadores")
            .whereEqualTo("UID", id)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                for (document in documentSnapshot) {
                    // Retrieve data from the document
                    val playerName = document.getString("nombre")
                    val playerNickname = document.getString("apodo")
                    val playerBirthday = document.getString("fecha_nacimiento")
                    val fechaFormateada = try {
                        val playerBirthdayMillis = playerBirthday?.toLongOrNull()
                        if (playerBirthdayMillis != null) {
                            val fechaNacimiento = Date(playerBirthdayMillis)

                            formatoDeseado.format(fechaNacimiento)
                        } else {
                            "Fecha no disponible"
                        }
                    } catch (e: Exception) {
                        "Fecha no disponible"
                    }
                    textViewFechanacimiento.text = fechaFormateada
                    fechaCovertida = playerBirthday ?: ""

                    val playerPhone = document.getString("telefono")
                    val playerPositions = document.get("posiciones") as List<String>?
                    playerPositions?.let {
                        for (position in it) {
                            val trimmedPosition = position.trim()
                            when (trimmedPosition) {
                                "Portero" -> porteroCheck1.isChecked = true
                                "Defensa" -> defensaCheck1.isChecked = true
                                "Mediocampista" -> medioCheck1.isChecked = true
                                "Delantero" -> delanteroCheck1.isChecked = true
                                "Lateral Izquierdo" -> latIzqCheck1.isChecked = true
                                "Lateral Derecho" -> latDerCheck1.isChecked = true
                            }
                        }
                    }

                    // Update the TextViews with the retrieved data
                    userName.setText(playerName)
                    nickname.setText(playerNickname)
                    phone.setText(playerPhone)
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
            }

            .addOnFailureListener { exception ->
                // Handle failures
            }
        saveProfile.setOnClickListener {
            val selectedPositions = mutableListOf<String>()
            if (porteroCheck1.isChecked) selectedPositions.add("Portero")
            if (defensaCheck1.isChecked) selectedPositions.add("Defensa")
            if (medioCheck1.isChecked) selectedPositions.add("Mediocampista")
            if (delanteroCheck1.isChecked) selectedPositions.add("Delantero")
            if (latIzqCheck1.isChecked) selectedPositions.add("Lateral Izquierdo")
            if (latDerCheck1.isChecked) selectedPositions.add("Lateral Derecho")

            callActivity1(userName.text.toString(), nickname.text.toString(), selectedPositions.joinToString(", "), fechaCovertida, phone.text.toString())
        }
        backProfile.setOnClickListener {
            setResult(RESULT_CANCELED);
            finish();
        }
    }
    fun callActivity1(nombre: String?, apodo: String?, posicion: String?, age: String?, phone: String?){
        // Crear un Intent para iniciar la Activity
        val intent = Intent(this, MiPerfil::class.java)
        intent.putExtra("nombre", nombre)
        intent.putExtra("apodo", apodo)
        intent.putExtra("posicion", posicion)
        intent.putExtra("age", age)
        intent.putExtra("phone", phone)
        setResult(RESULT_OK, intent)
        finish()
        // Iniciar la Activity1 utilizando el Intent
        //startActivity(intent)
    }
}