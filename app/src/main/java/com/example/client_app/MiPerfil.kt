package com.example.client_app


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase

private lateinit var startForResult: ActivityResultLauncher<Intent>

class MiPerfil : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mi_perfil)
        // Inicializar Firebase
        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth

        val editProfile: ImageButton = findViewById(R.id.editProfile)

        val id = auth.currentUser?.uid

        userData(id)

        editProfile.setOnClickListener {
            callActivityEditar(id)
        }

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == androidx.appcompat.app.AppCompatActivity.RESULT_OK) {
                val data: android.content.Intent? = result.data
                val nombre1 = data?.getStringExtra("nombre")
                val apodo1 = data?.getStringExtra("apodo")
                //val posicion1 = data?.getStringExtra("posicion")
                val age1 = data?.getStringExtra("age")
                val phone1 = data?.getStringExtra("phone")

                val collection = db.collection("jugadores")
                val document = collection.document("fI66gzILBfx48DrIpSQ2")

                val nuevosDatos = mapOf(
                    "nombre" to nombre1,
                    "apodo" to apodo1,
                    //"posicion" to posicion1,
                    "fecha_nacimiento" to age1,
                    "telefono" to phone1
                    // Agrega otros campos y sus nuevos valores aquí
                )


                document.update("nombre", nombre1.toString())
                    .addOnSuccessListener {
                        // Éxito, los datos se han actualizado correctamente
                        userData(id)
                    }
                    .addOnFailureListener { exception ->
                        // Maneja cualquier error
                    }
                document.update("apodo", apodo1.toString())
                    .addOnSuccessListener {
                        // Éxito, los datos se han actualizado correctamente
                        userData(id)
                    }
                    .addOnFailureListener { exception ->
                        // Maneja cualquier error
                    }
                document.update("fecha_nacimiento", age1.toString())
                    .addOnSuccessListener {
                        // Éxito, los datos se han actualizado correctamente
                        userData(id)
                    }
                    .addOnFailureListener { exception ->
                        // Maneja cualquier error
                    }
                document.update("telefono", phone1.toString())
                    .addOnSuccessListener {
                        // Éxito, los datos se han actualizado correctamente
                        userData(id)
                    }
                    .addOnFailureListener { exception ->
                        // Maneja cualquier error
                    }
            }
        }
    }

    private fun userData(id : String?) {
        val userName: TextView = findViewById(R.id.tvUsers)
        val nickname: TextView = findViewById(R.id.nicknameText)
        val correoText: TextView = findViewById(R.id.correoText)
        val position: TextView = findViewById(R.id.posicionText)
        val age: TextView = findViewById(R.id.edadText)
        val phone: TextView = findViewById(R.id.telefonoText)
        val clasificacion: TextView = findViewById(R.id.clasificacionText)


        if (id != null) {
            db.collection("jugadores")
                .whereEqualTo("UID", id)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    for (document in documentSnapshot) {
                        // Retrieve data from the document
                        val playerName = document.getString("nombre")
                        val playerNickname = document.getString("apodo")
                        val playerPositions = document.get("posiciones") as List<String>?
                        val playerBirthday = document.getString("fecha_nacimiento")
                        val playerPhone = document.getString("telefono")
                        val playerClasificacion = document.getString("clasificacion")
                        val playerCorreo = document.getString("correo")

                        // Update the TextViews with the retrieved data
                        userName.text = "$playerName"
                        nickname.text = "$playerNickname"
                        position.text = "${playerPositions?.joinToString(", ")}"
                        age.text = "$playerBirthday"
                        phone.text = "$playerPhone"
                        clasificacion.text = "$playerClasificacion"
                        correoText.text = "$playerCorreo"
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle failures
                }
        }

    }
    fun callActivityEditar(id : String?){
        // Crear un Intent para iniciar la Activity
        val intent = Intent(this, EditarPerfil::class.java)
        intent.putExtra("idUser",id)
        // Iniciar la Activity2 utilizando el Intent
        startForResult.launch(intent)
    }

}