package com.example.client_app


import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase

private lateinit var startForResult: ActivityResultLauncher<Intent>

class MiPerfil : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var tabLayout:TabLayout;
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


                val nuevosDatos = mapOf(
                    "nombre" to nombre1,
                    "apodo" to apodo1,
                    //"posicion" to posicion1,
                    "fecha_nacimiento" to age1,
                    "telefono" to phone1
                    // Agrega otros campos y sus nuevos valores aquí
                )
                db.collection("jugadores")
                    .whereEqualTo("UID", id)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            // Update the document with the new data
                            db.collection("jugadores").document(document.id)
                                .update("nombre", nombre1.toString())
                                .addOnSuccessListener {
                                    // Update was successful
                                    userData(id)
                                }
                                .addOnFailureListener { e ->
                                    // Handle the error
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle the error
                    }
                db.collection("jugadores")
                    .whereEqualTo("UID", id)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            // Update the document with the new data
                            db.collection("jugadores").document(document.id)
                                .update("apodo", apodo1.toString())
                                .addOnSuccessListener {
                                    // Update was successful
                                    userData(id)
                                }
                                .addOnFailureListener { e ->
                                    // Handle the error
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle the error
                    }
                db.collection("jugadores")
                    .whereEqualTo("UID", id)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            // Update the document with the new data
                            db.collection("jugadores").document(document.id)
                                .update("fecha_nacimiento", age1.toString())
                                .addOnSuccessListener {
                                    // Update was successful
                                    userData(id)
                                }
                                .addOnFailureListener { e ->
                                    // Handle the error
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle the error
                    }
                db.collection("jugadores")
                    .whereEqualTo("UID", id)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            // Update the document with the new data
                            db.collection("jugadores").document(document.id)
                                .update("telefono", phone1.toString())
                                .addOnSuccessListener {
                                    // Update was successful
                                    userData(id)
                                }
                                .addOnFailureListener { e ->
                                    // Handle the error
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle the error
                    }
            }
        }

        tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        toolbar_navigator()

        // Obtener la posición de la pestaña seleccionada del Intent
        val selectedTabPosition = intent.getIntExtra("selectedTab", -1)
        if (selectedTabPosition != -1) {
            // Establecer la pestaña seleccionada en el TabLayout
            tabLayout.getTabAt(selectedTabPosition)?.select()
        }
    }

    private fun userData(id : String?) {
        val userName: TextView = findViewById(R.id.tvUsers)
        val nickname: TextView = findViewById(R.id.nicknameText)
        val correoText: TextView = findViewById(R.id.correoText)
        val position: TextView = findViewById(R.id.posicionText)
        val age: TextView = findViewById(R.id.edadText)
        val phone: TextView = findViewById(R.id.telefonoText)

        val backProfile: ImageButton = findViewById(R.id.backButton)

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
                    val playerCorreo = document.getString("correo")

                    // Update the TextViews with the retrieved data
                    userName.text = "$playerName"
                    nickname.text = "$playerNickname"
                    position.text = "${playerPositions?.joinToString(", ")}"
                    age.text = "$playerBirthday"
                    phone.text = "$playerPhone"
                    correoText.text = "$playerCorreo"
                }
            }
            .addOnFailureListener { exception ->
                // Handle failures
            }
        backProfile.setOnClickListener {
            setResult(RESULT_CANCELED);
            finish();
        }
    }
    fun callActivityEditar(id : String?){
        // Crear un Intent para iniciar la Activity
        val intent = Intent(this, EditarPerfil::class.java)
        intent.putExtra("idUser",id)
        // Iniciar la Activity2 utilizando el Intent
        startForResult.launch(intent)
    }
    fun callActivity1(){
        // Crear un Intent para iniciar la Activity
        val intent = Intent(this, menu_principal::class.java)
        setResult(RESULT_OK, intent)
        finish()
        // Iniciar la Activity1 utilizando el Intent
        //startActivity(intent)
    }

    fun toolbar_navigator(){
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        if (!isCurrentActivity(InformationActivity::class.java)) {
                            val intent = Intent(this@MiPerfil, InformationActivity::class.java)
                            intent.putExtra("selectedTab", 0) // Agregar posición como extra
                            call_info_lugar_activity()
                        }
                    }
                    1 -> {
                        if (!isCurrentActivity(ReseniasActivity::class.java)) {
                            val intent = Intent(this@MiPerfil, ReseniasActivity::class.java)
                            intent.putExtra("selectedTab", 1) // Agregar posición como extra
                            callActivityResenias()
                        }
                    }
                    2 -> {
                        if (!isCurrentActivity(ListarUsuarios::class.java)) {
                            val intent = Intent(this@MiPerfil, ListarUsuarios::class.java)
                            intent.putExtra("selectedTab", 2) // Agregar posición como extra
                            activitylistarusuarios()
                        }
                    }
                    3 -> {
                        if (!isCurrentActivity(MiPerfil::class.java)) {
                            val intent = Intent(this@MiPerfil, MiPerfil::class.java)
                            intent.putExtra("selectedTab", 2) // Agregar posición como extra
                            activityperfil()
                        }
                    }
                }
            }


            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // No es necesario implementar nada aquí de momento
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Si la pestaña está seleccionada, también realiza la acción correspondiente
                when (tab?.position) {
                    0 -> {
                        if (!isCurrentActivity(InformationActivity::class.java)) {
                            call_info_lugar_activity()
                        }
                    }
                    1 -> {
                        if (!isCurrentActivity(ReseniasActivity::class.java)) {
                            callActivityResenias()
                        }
                    }
                    2 -> {
                        if (!isCurrentActivity(ListarUsuarios::class.java)) {
                            activitylistarusuarios()
                        }
                    }
                    3 -> {
                        if (!isCurrentActivity(MiPerfil::class.java)) {
                            activityperfil()
                        }
                    }
                }
            }
        })
    }
    // Función para verificar si la actividad actual es la especificada
    fun isCurrentActivity(activityClass: Class<*>): Boolean {
        val manager = (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        val runningTaskInfoList = manager.getRunningTasks(1)
        if (runningTaskInfoList.isNotEmpty()) {
            val topActivity = runningTaskInfoList[0].topActivity
            return topActivity?.className == activityClass.name
        }
        return false
    }
    fun callActivityResenias(){
        val intent = Intent(this, ReseniasActivity::class.java)
        startActivity(intent)
    }

    fun activityperfil(){
        val intent = Intent(this, MiPerfil::class.java)
        startActivity(intent)
    }
    fun activitylistarusuarios(){
        val intent = Intent(this, ListarUsuarios::class.java)
        startActivity(intent)
    }
    fun call_info_lugar_activity(){
        val intent = Intent(this, InformationActivity::class.java)
        startActivity(intent)
    }
}