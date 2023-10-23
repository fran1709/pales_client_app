package com.example.client_app

import android.annotation.SuppressLint
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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

        val fabAgregarComentario = findViewById<FloatingActionButton>(R.id.fabAgregarComentario)
        fabAgregarComentario.setOnClickListener {
            // Maneja el clic en el botón flotante (por ejemplo, muestra el cuadro de diálogo para agregar un comentario)
            showComentarioDialog()
        }
        fabAgregarComentario.setColorFilter(ContextCompat.getColor(this, R.color.white))

        //getResenias()
        cargarResenias()
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

    @SuppressLint("NotifyDataSetChanged")
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

    // Método para cargar la lista de reseñas
    private fun cargarResenias() {
        db.collection("resena")
            .get()
            .addOnSuccessListener { result ->
                val reseñas = ArrayList<Resenia>()
                for (document in result) {
                    val data = document.data
                    val comentario = data["comentario"] as String
                    val estado = data["estado"] as Boolean
                    val fecha = data["fecha"] as Timestamp
                    val jugador_id = data["jugador"] as String
                    reseñas.add(Resenia(comentario, estado, fecha, jugador_id))
                }
                cargarJugadores(reseñas)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }
    // Método para cargar la lista de jugadores
    private fun cargarJugadores(reseñas: List<Resenia>) {
        db.collection("jugadores")
            .get()
            .addOnSuccessListener { result ->
                val jugadores = HashMap<String, String>()
                for (document in result) {
                    val data = document.data
                    val id = document.id
                    val nombre = data["nombre"] as String
                    jugadores[id] = nombre
                }
                combinarReseniasYJugadores(reseñas, jugadores)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }
    // Método para combinar reseñas y jugadores y cargar el RecyclerView
    private fun combinarReseniasYJugadores(reseñas: List<Resenia>, jugadores: Map<String, String>) {
        val reseniasList = ArrayList<Resenia>()
        for (resenia in reseñas) {
            val jugadorNombre = jugadores[resenia.jugador]
            if (jugadorNombre != null) {
                val reseniaConNombre = Resenia(resenia.comentario, resenia.estado, resenia.fecha, jugadorNombre)
                reseniasList.add(reseniaConNombre)
            }
        }

        if (reseniasList.isNotEmpty()) {
            Toast.makeText(this@ReseniasActivity, "Sí hay comentarios disponibles", Toast.LENGTH_SHORT).show()
            adapter = ReseniasAdapter(reseniasList)
            recyclerView.adapter = adapter
        } else {
            Toast.makeText(this@ReseniasActivity, "No hay comentarios disponibles", Toast.LENGTH_SHORT).show()
        }
    }
/*
    fun getNombreJugador(idJugador: String, callback: (String) -> Unit) {
        db.collection("jugadores").get()
            .addOnSuccessListener { result ->
                var nombreJugador = "" // Inicializa con un valor vacío
                for (document in result) {
                    if (idJugador == document.id) {
                        val data = document.data
                        nombreJugador = data["nombre"] as String
                        break // Se encontró el jugador, puedes salir del bucle
                    }
                }
                callback(nombreJugador) // Llama a la devolución de llamada con el nombre del jugador
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error obteniendo el nombre del jugador", exception)
                callback("") // En caso de error, llama a la devolución de llamada con un nombre vacío
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
                    val jugador_id = data["jugador"] as String
                    // se obtiene el nombre del jugador
                    // Llama a la función para obtener el nombre del jugador de manera asíncrona
                    getNombreJugador(jugador_id) { nombreJugador ->
                        if (nombreJugador.isNotEmpty()) {
                            val resenia = Resenia(comentario, estado, fecha, nombreJugador)
                            reseniasList.add(resenia)// Notifica al adaptador sobre el cambio de datos
                        }
                    }

                }
                if (reseniasList.isNotEmpty()) {

                    Toast.makeText(this@ReseniasActivity, "Sí hay comentarios disponibles", Toast.LENGTH_SHORT).show()

                    // Agregar adaptador al RecyclerView
                    this.adapter = ReseniasAdapter(reseniasList)
                    recyclerView.adapter = adapter

                } else {
                    Toast.makeText(this@ReseniasActivity, reseniasList.size.toString(), Toast.LENGTH_SHORT).show()
                    // La lista está vacía, lo que significa que no se recuperaron comentarios
                    //Toast.makeText(this@ReseniasActivity, "No hay comentarios disponibles", Toast.LENGTH_SHORT).show()

                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }*/

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