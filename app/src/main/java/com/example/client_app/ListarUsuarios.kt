package com.example.client_app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore

data class Jugador(val nombre: String, val posiciones: List<String>)
private lateinit var db: FirebaseFirestore
class ListarUsuarios : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_usuarios)
        // Inicializar Firebase

        db = FirebaseFirestore.getInstance()
        usersListData()
    }
    private fun usersListData() {
        val jugadoresCollection = db.collection("jugadores")

        jugadoresCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val jugadoresList = mutableListOf<Jugador>()

                for (document in querySnapshot) {
                    val nombre = document.getString("nombre") ?: ""
                    val posiciones = document.get("posiciones") as? List<String> ?: emptyList()
                    val jugador = Jugador(nombre, posiciones)
                    jugadoresList.add(jugador)
                }

                // Mostrar los datos en el ListView
                displayJugadores(jugadoresList)
            }
            .addOnFailureListener { exception ->
                // Handle failures
            }
    }
    private fun displayJugadores(jugadoresList: List<Jugador>) {
        val listView: ListView = findViewById(R.id.lv1)
        val adapter = JugadorAdapter(this, jugadoresList)
        listView.adapter = adapter
    }
    class JugadorAdapter(context: Context, private val jugadores: List<Jugador>) : ArrayAdapter<Jugador>(context, 0, jugadores) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var itemView = convertView
            if (itemView == null) {
                itemView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
            }

            val jugador = jugadores[position]
            itemView?.findViewById<TextView>(android.R.id.text1)?.text = "${jugador.nombre} - ${jugador.posiciones.joinToString(", ")}"

            itemView?.setOnClickListener {
                db.collection("jugadores")
                    .whereEqualTo("nombre", jugador.nombre)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        for (document in documentSnapshot) {
                            // Retrieve data from the document
                            val playerName = document.getString("nombre")
                            val playerNickname = document.getString("apodo")
                            val playerPositions = document.get("posiciones") as List<String>?
                            val playerBirthday = document.getString("fecha_nacimiento")
                            val playerPhone = document.getString("teléfono")

                            // Cuando se hace clic en un elemento, abrir la actividad PerfilJugador
                            val intent = Intent(context, PerfilJugador::class.java)
                            intent.putExtra("nombre", playerName)
                            intent.putExtra("apodo", playerNickname)
                            intent.putStringArrayListExtra("posiciones", ArrayList(jugador.posiciones))
                            intent.putExtra("fecha_nacimiento", playerBirthday)
                            intent.putExtra("telefono", playerPhone)
                            context.startActivity(intent)
                        }
                    }

            }

            return itemView!!
        }
    }

    fun activityperfiljugador(view: View){
        val intent = Intent(this, PerfilJugador::class.java)
        startActivity(intent)
    }
}