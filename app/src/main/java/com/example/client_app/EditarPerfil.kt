package com.example.client_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.text.Editable
import android.widget.EditText
import android.widget.ImageButton
import com.google.firebase.firestore.FirebaseFirestore

class EditarPerfil : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        db = FirebaseFirestore.getInstance()

        userData()

    }
    private fun userData() {
        val userName: EditText = findViewById(R.id.etName)
        val nickname: EditText = findViewById(R.id.etnickname)
        val position: EditText = findViewById(R.id.etnPosicion)
        val age: EditText = findViewById(R.id.etAge)
        val phone: EditText = findViewById(R.id.etphone)

        val saveProfile: ImageButton = findViewById(R.id.saveProfile)
        val backProfile: ImageButton = findViewById(R.id.backProfile)

        val id = "h5GdAbTqbDOT8fbqX6GwIGGFokw1"

        db.collection("jugadores")
            .whereEqualTo("UID", id)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                for (document in documentSnapshot) {
                    // Retrieve data from the document
                    val playerName = document.getString("nombre")
                    val playerNickname = document.getString("apodo")
                    //val playerPositions = document.get("posiciones") as List<String>?
                    val playerBirthday = document.getString("fecha_nacimiento")
                    val playerPhone = document.getString("telefono")

                    // Update the TextViews with the retrieved data
                    userName.setText(playerName)
                    nickname.setText(playerNickname)
                    //position.text = playerToEditable(playerPositions?.joinToString(", "))
                    age.setText(playerBirthday)
                    phone.setText(playerPhone)
                }
            }

            .addOnFailureListener { exception ->
                // Handle failures
            }
        saveProfile.setOnClickListener {
            callActivity1(userName.text.toString(), nickname.text.toString(), position.text.toString(), age.text.toString(), phone.text.toString())
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
        //intent.putExtra("posicion", posicion)
        intent.putExtra("age", age)
        intent.putExtra("phone", phone)
        setResult(RESULT_OK, intent)
        finish()
        // Iniciar la Activity1 utilizando el Intent
        //startActivity(intent)
    }
}