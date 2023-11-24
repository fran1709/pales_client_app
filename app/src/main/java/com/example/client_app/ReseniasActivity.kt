package com.example.client_app

import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ListenerRegistration

data class Resenia(
    val docuemntID: String,
    val comentario: String,
    val estado: Boolean,
    val fecha: Timestamp,
    val jugador: String
)

class ReseniasActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private val reseniasList = ArrayList<Resenia>()
    private val reseniasLiveData = MutableLiveData<List<Resenia>>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReseniasAdapter
    private var commentsListener: ListenerRegistration? = null
    private var isLoading = true
    private lateinit var userID: String
    private var userNombre = ""
    private val commentClickListener = object : OnCommentClickListener {
        override fun onCommentClick(resenia: Resenia) {
            // Lógica para editar el comentario al hacer clic en él
            showEditDialog(resenia)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resenias)

        // Obtener el userID o asignarlo según corresponda
        userID = FirebaseAuth.getInstance().currentUser?.uid.toString()

        initializeUI()

        reseniasLiveData.observe(this, Observer { comentarios ->
            adapter.updateData(comentarios)
        })

        val fabAgregarComentario = findViewById<FloatingActionButton>(R.id.fabAgregarComentario)
        fabAgregarComentario.setOnClickListener {
            showComentarioDialog()
        }

        val fabFiltrar = findViewById<FloatingActionButton>(R.id.fabFiltrar)
        fabFiltrar.setOnClickListener {
            showSearchDialog()
        }

        // Oculta el RecyclerView y la vista de espera al principio
        recyclerView.visibility = View.GONE
        val progressBar = findViewById<ProgressBar>(R.id.waitingProgressBar)
        progressBar.visibility = View.VISIBLE

        // Agrega el SnapshotListener para escuchar cambios en la colección "resena"
        commentsListener = db.collection("resena")
            .whereEqualTo("estado", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val newComments = snapshot.documents.map { document ->
                        val data = document.data
                        val comentario = data?.get("comentario") as? String ?: ""
                        val estado = data?.get("estado") as? Boolean ?: false
                        val fechaTimestamp = data?.get("fecha") as? Timestamp
                        val fecha = fechaTimestamp ?: Timestamp.now()
                        val jugador_id = data?.get("jugador") as? String ?: ""
                        Resenia(document.id, comentario, estado, fecha, jugador_id)
                    }

                    reseniasList.clear()
                    reseniasList.addAll(newComments)
                    // Llama a la función para cargar jugadores
                    loadJugadores(reseniasList)
                } else {
                    Log.d(TAG, "Current data: null")
                }
            }

        loadResenias()
    }


    @Suppress("DEPRECATION")
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menucito, menu)
        return true
    }

    /**
     * @author Francisco Ovares Rojas
     * Método encargado de crear el comentario y cargarlo en la BD.
     * @param estado Boolean que representa el estado del comentario (false)=no se muestra, (true)=se muestra
     * @param comentario String que representa la cadena de texto obtenida del usuario.
     */
    private fun createComentario(comentario: String, estado: Boolean) {
        val mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        val userID = user?.uid

        if (userID != null) {
            // Obtener el nombre del jugador
            db.collection("jugadores")
                .document(userID)
                .get()
                .addOnSuccessListener { documentSnapshot ->

                    val nuevoComentario = hashMapOf(
                        "comentario" to comentario,
                        "estado" to estado,
                        "jugador" to userID,
                        "fecha" to FieldValue.serverTimestamp()
                    )

                    // Muestra la vista de espera
                    val progressBar = findViewById<ProgressBar>(R.id.waitingProgressBar)
                    progressBar.visibility = View.VISIBLE
                    // Oculta el RecyclerView
                    recyclerView.visibility = View.GONE

                    db.collection("resena")
                        .add(nuevoComentario)
                        .addOnSuccessListener { documentReference ->
                            Toast.makeText(
                                this@ReseniasActivity,
                                "¡Comentario creado con éxito!",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Cargar los comentarios nuevamente después de un breve retraso
                            loadReseniasWithDelay()
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error al crear el comentario", e)
                            Toast.makeText(
                                this@ReseniasActivity,
                                "Error al crear el comentario",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Oculta la vista de espera y muestra el RecyclerView
                            progressBar.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                        }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error al obtener el nombre del jugador", e)
                }
        }
    }

    /**
     * @author Francisco Ovares Rojas
     * Método encargado de mostrar el Dialog para filtrar las reseñas.
     */
    private fun showSearchDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Buscar por nombre")

        val input = EditText(this)
        input.hint = "Ingrese un nombre"
        builder.setView(input)

        builder.setPositiveButton("Buscar") { dialog: DialogInterface, _ ->
            val nombre = input.text.toString()
            if (nombre.isNotEmpty()) {
                // Lógica para buscar el nombre ingresado
                buscarPorNombre(nombre)
            } else {
                // Manejar el caso cuando el campo está vacío
                // Aquí podrías mostrar un mensaje de error, por ejemplo
                Toast.makeText(this, "Ingrese un nombre para buscar", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog: DialogInterface, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    /**
     * @author Francisco Ovares Rojas
     * Método encargado de filtrar por nombre los comentarios.
     * @param nombre String que recibe el nombre a buscar.
     */
    private fun buscarPorNombre(nombre: String) {
        reseniasLiveData.value?.let { comentarios ->
            val comentariosFiltrados = comentarios.filter { resenia ->
                resenia.jugador.contains(nombre, ignoreCase = true)
            }

            if (comentariosFiltrados.isEmpty()) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("No se encontraron comentarios")
                builder.setMessage("¿Mostrar todos los comentarios?")
                builder.setPositiveButton("Sí") { dialog, _ ->
                    loadResenias() // Mostrar todos los comentarios
                    dialog.dismiss()
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                val dialog = builder.create()
                dialog.show()
            } else {
                reseniasLiveData.value = comentariosFiltrados // Mostrar comentarios filtrados
            }
        }
    }


    @Suppress("DEPRECATION")
    private fun loadReseniasWithDelay() {
        // Agrega un retraso antes de cargar los comentarios nuevamente
        Handler().postDelayed({
            loadResenias()
        }, 2000) // 2000 milisegundos (2 segundos) de retraso
    }


    override fun onDestroy() {
        super.onDestroy()
        commentsListener?.remove()
    }

    /**
     * @author Francisco Ovares Rojas
     * Método encargado de mostrar el Dialog para crear un comentario.
     */
    private fun showComentarioDialog() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Agregar Comentario")

        val input = EditText(this)
        input.hint = "Escribe tu comentario aquí"
        builder.setView(input)

        builder.setPositiveButton("Aceptar") { dialog: DialogInterface, _ ->
            val comentario = input.text.toString()
            if (comentario.isNotEmpty()) {
                // TODO: ACA SE PONDRIA ESTADO EN FALSO PARA EL QUE ADMINISTRADOR LE DE ACEPTAR O RECHAZAR.
                createComentario(comentario, false)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog: DialogInterface, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
    // Función para mostrar el diálogo de edición
    private fun showEditDialog(resenia: Resenia) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar Comentario")

        val input = EditText(this)
        input.setText(resenia.comentario)
        builder.setView(input)

        builder.setPositiveButton("Aceptar") { dialog: DialogInterface, _ ->
            val editedComment = input.text.toString()
            if (editedComment.isNotEmpty()) {
                updateComentario(resenia, editedComment)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog: DialogInterface, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    // Función para actualizar el comentario editado en Firebase
    private fun updateComentario(resenia: Resenia, editedComment: String) {
        val comentarioDocument = db.collection("resena").document(resenia.docuemntID)

        // Crear un HashMap con los campos a actualizar
        val updatedData = hashMapOf(
            "comentario" to editedComment,
            "estado" to false, // se coloca como falso para que aparezca en la vista del admin para volver a aprobar/rechazar
            "jugador" to userID,
            "fecha" to FieldValue.serverTimestamp()
        )

        comentarioDocument.update(updatedData)
            .addOnSuccessListener {
                // Notificar al usuario sobre la actualización exitosa
                Toast.makeText(this, "Comentario actualizado exitosamente", Toast.LENGTH_SHORT).show()
                loadResenias()
            }
            .addOnFailureListener { e ->
                // Manejar el error y notificar al usuario
                Log.w(TAG, "Error al actualizar el comentario", e)
                Toast.makeText(this, "Error al actualizar el comentario", Toast.LENGTH_SHORT).show()
            }
    }

    private fun obtenerNombreUsuario(userID: String, onCompletion: (String) -> Unit) {
        db.collection("jugadores")
            .get()
            .addOnSuccessListener { result ->
                var userNombre = ""

                for (document in result) {
                    val id = document.data["UID"].toString()
                    val nombre = document.data["nombre"] as? String ?: ""

                    if (id == userID) {
                        userNombre = nombre
                    }
                }

                onCompletion(userNombre) // Llamada de devolución para retornar el nombre del usuario
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error obteniendo documentos", exception)
                onCompletion("") // Llamada de devolución con nombre vacío en caso de error
            }
    }



    private fun initializeUI() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Pale's Reseñas"

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        obtenerNombreUsuario(userID) { userNombre ->
            adapter = ReseniasAdapter(reseniasList, commentClickListener, userNombre, this::showEditDialog)
            recyclerView.adapter = adapter
        }

        adapter = ReseniasAdapter(reseniasList, commentClickListener, userNombre, this::showEditDialog)
        recyclerView.adapter = adapter

        reseniasLiveData.value = reseniasList

        // Set the loading state to true
        isLoading = true
    }

    /**
     * @author Francisco Ovares Rojas
     * Método encargado de cargar las reseñas de la base de datos. Método principal.
     */
    private fun loadResenias() {
        db.collection("resena")
            .whereEqualTo("estado", true) // Obtener solo comentarios con estado=true
            .get()
            .addOnSuccessListener { result ->
                val resenias = result.documents.map { document ->
                    val data = document.data
                    val comentario = data?.get("comentario") as? String ?: ""
                    val estado = data?.get("estado") as? Boolean ?: false
                    val fecha = data?.get("fecha") as? Timestamp ?: Timestamp.now()
                    val jugador_id = data?.get("jugador") as? String ?: ""
                    Resenia(document.id,comentario, estado, fecha, jugador_id)
                }
                loadJugadores(resenias)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

    }
    /**
     * @author Francisco Ovares Rojas
     * Método encargado de cargar los jugadores de la base de datos. Método secundario.
     */
    private fun loadJugadores(resenias: List<Resenia>) {
        db.collection("jugadores")
            .get()
            .addOnSuccessListener { result ->
                val jugadores = result.documents.associate { document ->
                    val id = document.data?.get("UID").toString()
                    val nombre = document.data?.get("nombre") as? String ?: ""
                    id to nombre
                }
                combineReseniasAndJugadores(resenias, jugadores)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    /**
     * @author Francisco Ovares Rojas
     * Método encargado de cargar los reseniasLiveData con la información  actualizada (nombre, comentario).
     */
    private fun combineReseniasAndJugadores(resenias: List<Resenia>, jugadores: Map<String, String>) {
        val reseniasConNombres = resenias.mapNotNull { resenia ->
            val jugadorNombre = jugadores[resenia.jugador]
            jugadorNombre?.let { Resenia(resenia.docuemntID,resenia.comentario, resenia.estado, resenia.fecha, it) }
        }

        // Update the LiveData with the new data
        reseniasLiveData.value = reseniasConNombres

        // Oculta la vista de espera
        val progressBar = findViewById<ProgressBar>(R.id.waitingProgressBar)
        progressBar.visibility = View.GONE

        // Muestra el RecyclerView con los comentarios actualizados
        recyclerView.visibility = View.VISIBLE
    }

}