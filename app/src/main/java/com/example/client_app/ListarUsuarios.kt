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
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

data class Jugador(val id: String?, val nombre: String, val posiciones: List<String>)

private lateinit var db: FirebaseFirestore
private lateinit var startForResult: ActivityResultLauncher<Intent>
private lateinit var recyclerView2: RecyclerView
class ListarUsuarios : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_usuarios)
        // Inicializar Firebase

        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        val id = auth.currentUser?.uid

        usersListData(id)
        val backProfile: ImageButton = findViewById(R.id.backButton)

        val searchPlayer: ImageButton = findViewById(R.id.searchPlayer)
        searchPlayer.setOnClickListener {
            activitybuscarJugador()
        }
        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == androidx.appcompat.app.AppCompatActivity.RESULT_OK) {
                val data: android.content.Intent? = result.data

            }
        }
        backProfile.setOnClickListener {
            setResult(RESULT_CANCELED);
            finish();
        }
        recyclerView2 = findViewById(R.id.recyclerView2)
        recyclerView2.layoutManager = LinearLayoutManager(this)
    }
    private fun usersListData(id1 : String?) {
        val jugadoresCollection = db.collection("jugadores")

        jugadoresCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val jugadoresList = mutableListOf<Jugador>()

                for (document in querySnapshot) {
                    val id = document.getString("UID")
                    if(id != id1){
                        val nombre = document.getString("nombre") ?: ""
                        val posiciones = document.get("posiciones") as? List<String> ?: emptyList()
                        val jugador = Jugador(id, nombre, posiciones) //AGREGAR ID
                        jugadoresList.add(jugador)
                    }

                }
                // Mostrar los datos en el ListView
                displayJugadores(jugadoresList)
            }
            .addOnFailureListener { exception ->
                Log.e("ListarUsuarios", "Error al obtener jugadores", exception)
            }
    }

    class JugadorAdapter(private val context: Context, private val jugadores: List<Jugador>) :
        RecyclerView.Adapter<JugadorAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvJugadorInfo: TextView = itemView.findViewById(R.id.tvJugadorInfo)
            val tvPositionInfo: TextView = itemView.findViewById(R.id.tvPositionInfo)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView =
                LayoutInflater.from(context).inflate(R.layout.item_jugador, parent, false)
            return ViewHolder(itemView)
        }
        override fun getItemCount(): Int {
            return jugadores.size

        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val jugador = jugadores[position]

            holder.tvJugadorInfo.text = jugador.nombre
            holder.tvPositionInfo.text = "Posición: ${jugador.posiciones.joinToString(", ")}"


            holder.itemView.setOnClickListener {
                db.collection("jugadores")
                    .whereEqualTo("UID", jugador.id) //VERIFICAR POR ID NO POR NOMBRE
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
        }
    }
    private fun displayJugadores(jugadoresList: List<Jugador>) {
        val adapter = JugadorAdapter(this, jugadoresList)
        recyclerView2.adapter = adapter
    }
    fun activityperfiljugador(view: View){
        val intent = Intent(this, PerfilJugador::class.java)
        startForResult.launch(intent)
    }
    fun activitybuscarJugador(){
        val intent = Intent(this, BuscarJugador::class.java)
        startForResult.launch(intent)
    }
}
    /*class JugadorAdapter(context: Context, private val jugadores: List<Jugador>) : ArrayAdapter<Jugador>(context, 0, jugadores) {

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
    }*/

