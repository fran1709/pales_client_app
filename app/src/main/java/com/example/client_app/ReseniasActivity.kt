package com.example.client_app

import android.content.ContentValues.TAG
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import com.google.firebase.firestore.FieldValue
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.util.Calendar

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
    private lateinit var adapter: ReseniasAdapter
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

    // Funcion para que funcione el retroceso (flechita para atrás xde)
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menucito, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nuevoComentario -> {
                // Acción para el elemento de configuración
                Toast.makeText(this, "Agregar nuevo comentario seleccionado", Toast.LENGTH_SHORT).show()
                showComentarioDialog();
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun createComentario(comentario: String, estado: Boolean, jugador: String) {
        // Crear un nuevo documento en la colección "resena"
        val nuevoComentario = hashMapOf(
            "comentario" to comentario,
            "estado" to estado,
            "jugador" to jugador,
            "fecha" to FieldValue.serverTimestamp() // Agregar la fecha actual del servidor
        )

        db.collection("resena")
            .add(nuevoComentario)
            .addOnSuccessListener { documentReference ->
                // El comentario se creó con éxito, puedes mostrar un mensaje o realizar otras acciones
                Toast.makeText(this@ReseniasActivity, "Comentario creado con éxito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Error al crear el comentario, muestra un mensaje de error o realiza acciones de manejo de errores
                Log.w(TAG, "Error al crear el comentario", e)
                Toast.makeText(this@ReseniasActivity, "Error al crear el comentario", Toast.LENGTH_SHORT).show()
            }
        // Obtener la fecha actual
        val calendar = Calendar.getInstance()

        // Crear un objeto Timestamp a partir de la fecha actual
        val fechaActual = Timestamp(calendar.time)
        reseniasList.add(Resenia(comentario, estado, fechaActual, jugador));
        adapter.notifyDataSetChanged();
    }

    fun showComentarioDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Agregar Comentario")

        val input = EditText(this)
        input.hint = "Escribe tu comentario aquí"
        builder.setView(input)

        builder.setPositiveButton("Aceptar") { dialog: DialogInterface, _ ->
            val comentario = input.text.toString()
            if (comentario.isNotEmpty()) {
                // TODO: Se debe de obtener el id del jugador para guardarlo en el param @jugador
                createComentario(comentario, true, "Nombre del Jugador")
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog: DialogInterface, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
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
                    //val gson = Gson()
                    //val jsonData = gson.toJson(reseniasList)
                    // Imprime el JSON en el logcat
                    //Log.d("JSON", jsonData) // "JSON" es un identificador para encontrar más fácilmente el registro en logcat

                    // La lista reseniasList se llena con comentarios
                    //chargeListViewCompleja()
                    //chargeListViewSimple()

                    // Agregar adaptador al RecyclerView
                    this.adapter = ReseniasAdapter(reseniasList)
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