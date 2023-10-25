package com.example.client_app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

private lateinit var db: FirebaseFirestore
private lateinit var startForResult: ActivityResultLauncher<Intent>
data class Jugador1(val id: String?, val nombre: String, val apodo: String, val posiciones: List<String>)

class BuscarJugador : AppCompatActivity() {
    private val jugadoresList = mutableListOf<Jugador1>()
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscar_jugador)

        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        val id = auth.currentUser?.uid

        val backButton: ImageButton = findViewById(R.id.backButton)
        val searchPlayerButton: ImageButton = findViewById(R.id.searchPlayer)
        val searchInput: EditText = findViewById(R.id.etBuscarJugador)
        val lv1: ListView = findViewById(R.id.lv1)

        //usersListData()

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == androidx.appcompat.app.AppCompatActivity.RESULT_OK) {
                val data: android.content.Intent? = result.data
            }
        }
        // Configura un adaptador vacío para el ListView
        val adapter = JugadorAdapter(this, mutableListOf())
        lv1.adapter = adapter

        // Maneja el clic en el botón de búsqueda
        searchPlayerButton.setOnClickListener {
            val searchText = searchInput.text.toString().toLowerCase(Locale.getDefault())
            // Realiza la búsqueda de jugadores que coincidan con el texto ingresado
            val filteredJugadores = jugadoresList.filter {
                it.nombre.toLowerCase(Locale.getDefault()).contains(searchText) ||
                        it.apodo.toLowerCase(Locale.getDefault()).contains(searchText)
            }

            // Actualiza el adaptador del ListView con los resultados de la búsqueda
            adapter.updateData(filteredJugadores)
        }
        backButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
        // Cargar los datos de la colección "jugadores"
        loadJugadoresData(id)
    }
    private fun loadJugadoresData(id1 : String?) {
        val lv1: ListView = findViewById(R.id.lv1)
        db.collection("jugadores")
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val id = document.getString("UID")
                    if(id != id1) {
                        val nombre = document.getString("nombre") ?: ""
                        val apodo = document.getString("apodo") ?: ""
                        val posiciones = document.get("posiciones") as? List<String> ?: emptyList()
                        val jugador = Jugador1(id, nombre, apodo, posiciones)
                        jugadoresList.add(jugador)
                    }
                }

                // Notificar al adaptador para que actualice la lista en el ListView
                val adapter = lv1.adapter as JugadorAdapter
                adapter.updateData(jugadoresList)
            }
            .addOnFailureListener { exception ->
                println("Ocurrió un error: ${exception.message}")
            }
    }
//    private fun usersListData() {
//        val jugadoresCollection = db.collection("jugadores")
//
//        jugadoresCollection.get()
//            .addOnSuccessListener { querySnapshot ->
//                val jugadoresList = mutableListOf<Jugador1>()
//
//                for (document in querySnapshot) {
//                    val nombre = document.getString("nombre") ?: ""
//                    val apodo = document.getString("apodo") ?: ""
//                    val posiciones = document.get("posiciones") as? List<String> ?: emptyList()
//                    val jugador = Jugador1(nombre, apodo, posiciones) //AGREGAR ID
//                    jugadoresList.add(jugador)
//                }
//
//                // Mostrar los datos en el ListView
//                displayJugadores(jugadoresList)
//            }
//            .addOnFailureListener { exception ->
//                // Handle failures
//            }
//    }
//    private fun displayJugadores(jugadoresList: MutableList<Jugador1>) {
//        val listView: ListView = findViewById(R.id.lv1)
//        val adapter = JugadorAdapter(this, jugadoresList)
//        listView.adapter = adapter
//    }
    class JugadorAdapter(context: Context, private val jugadores: MutableList<Jugador1>) : ArrayAdapter<Jugador1>(context, 0, jugadores) {
        fun updateData(newData: List<Jugador1>) {
            jugadores.clear()
            jugadores.addAll(newData)
            notifyDataSetChanged()
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var itemView = convertView
            if (itemView == null) {
                itemView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
            }

            val jugador = jugadores[position]
            itemView?.findViewById<TextView>(android.R.id.text1)?.text = "Nombre: ${jugador.nombre}  Posición: ${jugador.posiciones.joinToString(", ")}"

            itemView?.setOnClickListener {
                db.collection("jugadores")
                    .whereEqualTo("nombre", jugador.nombre) //VERIFICAR POR ID NO POR NOMBRE
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        for (document in documentSnapshot) {0
                            // Retrieve data from the document
                            val playerName = document.getString("nombre")
                            val playerNickname = document.getString("apodo")
                            val playerPositions = document.get("posiciones") as List<String>?
                            val playerBirthday = document.getString("fecha_nacimiento")
                            val playerPhone = document.getString("telefono")
                            val playerClasificacion = document.getString("clasificacion")

                            // Cuando se hace clic en un elemento, abrir la actividad PerfilJugador
                            val intent = Intent(context, PerfilJugador::class.java)
                            intent.putExtra("nombre", playerName)
                            intent.putExtra("apodo", playerNickname)
                            intent.putStringArrayListExtra("posiciones", ArrayList(jugador.posiciones))
                            intent.putExtra("fecha_nacimiento", playerBirthday)
                            intent.putExtra("telefono", playerPhone)
                            intent.putExtra("clasificacion", playerClasificacion)
                            context.startActivity(intent)
                        }
                    }

            }

            return itemView!!
        }
    }
    fun activityperfiljugador(view: View){
        val intent = Intent(this, PerfilJugador::class.java)
        startForResult.launch(intent)
    }
}