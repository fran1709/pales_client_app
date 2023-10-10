package com.example.client_app

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

data class Resenia(
    val comentario: String,
    val estado: Boolean,
    val fecha: Timestamp,
    val jugador: String
)
class ReseniasActivity : AppCompatActivity() {

    val db = Firebase.firestore
    val reseniasList = ArrayList<Resenia>()
    private lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resenias)

        //INICIALIZACION DEL TOOLLBAR
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Pale's Reseñas"

        // Inicializar el RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        getResenias()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menucito, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                // Acción para el elemento de búsqueda
                Toast.makeText(this, "Elemento de búsqueda seleccionado", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_settings -> {
                // Acción para el elemento de configuración
                Toast.makeText(this, "Elemento de configuración seleccionado", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    fun getResenias() {
        db.collection("resena")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val data = document.data
                    val comentario = data["comentario"] as String
                    val estado = data["estado"] as Boolean
                    val fecha = data["fecha"] as Timestamp
                    val jugador = data["jugador"] as String

                    val resenia = Resenia(comentario, estado, fecha, jugador)
                    reseniasList.add(resenia)
                }
                if (reseniasList.isNotEmpty()) {

                    Toast.makeText(this@ReseniasActivity, "Sí hay comentarios disponibles", Toast.LENGTH_SHORT).show()

                    // Convierte la lista de resenias a JSON
                    val gson = Gson()
                    val jsonData = gson.toJson(reseniasList)
                    // Imprime el JSON en el logcat
                    Log.d("JSON", jsonData) // "JSON" es un identificador para encontrar más fácilmente el registro en logcat

                    // La lista reseniasList se llena con comentarios
                    //chargeListViewCompleja()
                    //chargeListViewSimple()

                    // Agregar adaptador al RecyclerView
                    val adapter = ReseniasAdapter(reseniasList)
                    recyclerView.adapter = adapter

                } else {
                    // La lista está vacía, lo que significa que no se recuperaron comentarios
                    Toast.makeText(this@ReseniasActivity, "No hay comentarios disponibles", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }
    /*
    fun chargeListViewSimple() {
        val listView: ListView = findViewById(R.id.listVIew)

        // Crear un ArrayAdapter personalizado para mostrar los comentarios en el ListView
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, reseniasList.map { it.comentario })

        // Asociar el ArrayAdapter con el ListView
        listView.adapter = adapter

        // Configurar un escuchador para el clic en los elementos del ListView
        listView.setOnItemClickListener { _, _, position, _ ->
            val comentarioSeleccionado = reseniasList[position]
            // TODO: Hacer algo con el comentario(reseña) seleccionado(a)
            Toast.makeText(this@ReseniasActivity, "Comentario seleccionado: ${comentarioSeleccionado.comentario}", Toast.LENGTH_SHORT).show()
        }
    }

    fun chargeListViewCompleja() {
        val listView: ListView = findViewById(R.id.listVIew)

        // Crear un ArrayAdapter personalizado para mostrar jugador y comentario en el ListView
        val adapter = ArrayAdapter(this, R.layout.list_item_layout, reseniasList)

        // Asocia el ArrayAdapter con el ListView
        listView.adapter = adapter

        // Configura un escuchador para el clic en los elementos del ListView
        listView.setOnItemClickListener { _, _, position, _ ->
            val reseniaSeleccionada = reseniasList[position]
            // Accediendo a reseniaSeleccionada.jugador y reseniaSeleccionada.comentario
            // TODO: Hacer algo con el comentario(reseña) seleccionado(a)
            Toast.makeText(this@ReseniasActivity, "Jugador: ${reseniaSeleccionada.jugador}\nComentario: ${reseniaSeleccionada.comentario}", Toast.LENGTH_SHORT).show()
        }
    }*/

}
