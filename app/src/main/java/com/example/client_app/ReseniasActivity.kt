package com.example.client_app

import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
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
    private var filtro: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resenias)

        initializeUI()

        reseniasLiveData.observe(this, Observer { comentarios ->
            adapter.updateData(comentarios)
        })

        val fabAgregarComentario = findViewById<FloatingActionButton>(R.id.fabAgregarComentario)
        fabAgregarComentario.setOnClickListener {
            showComentarioDialog()
        }

        // Oculta el RecyclerView y la vista de espera al principio
        recyclerView.visibility = View.GONE
        val progressBar = findViewById<ProgressBar>(R.id.waitingProgressBar)
        progressBar.visibility = View.VISIBLE

        // Agrega el SnapshotListener para escuchar cambios en la colección "resena"
        commentsListener = db.collection("resena")
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
                        Resenia(comentario, estado, fecha, jugador_id)
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



    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menucito, menu)
        return true
    }

    //funcion para filtrar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return false;
    }

    fun createComentario(comentario: String, estado: Boolean, jugador: String) {
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
                            Toast.makeText(this@ReseniasActivity, "¡Comentario creado con éxito!", Toast.LENGTH_SHORT).show()
                            // Cargar los comentarios nuevamente después de un breve retraso
                            loadReseniasWithDelay()
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error al crear el comentario", e)
                            Toast.makeText(this@ReseniasActivity, "Error al crear el comentario", Toast.LENGTH_SHORT).show()

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

    fun showComentarioDialog() {
        val mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        val userID = user?.uid

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Agregar Comentario")

        val input = EditText(this)
        input.hint = "Escribe tu comentario aquí"
        builder.setView(input)

        builder.setPositiveButton("Aceptar") { dialog: DialogInterface, _ ->
            val comentario = input.text.toString()
            if (comentario.isNotEmpty()) {
                createComentario(comentario, true, userID.toString())
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog: DialogInterface, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun initializeUI() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Pale's Reseñas"

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ReseniasAdapter(reseniasList)
        recyclerView.adapter = adapter

        reseniasLiveData.value = reseniasList

        // Set the loading state to true
        isLoading = true
    }

    private fun loadResenias() {
        db.collection("resena")
            .get()
            .addOnSuccessListener { result ->
                val resenias = result.documents.map { document ->
                    val data = document.data
                    val comentario = data?.get("comentario") as? String ?: ""
                    val estado = data?.get("estado") as? Boolean ?: false
                    val fecha = data?.get("fecha") as? Timestamp ?: Timestamp.now()
                    val jugador_id = data?.get("jugador") as? String ?: ""
                    Resenia(comentario, estado, fecha, jugador_id)
                }
                loadJugadores(resenias)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

    }

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

    private fun combineReseniasAndJugadores(resenias: List<Resenia>, jugadores: Map<String, String>) {
        val reseniasConNombres = resenias.mapNotNull { resenia ->
            val jugadorNombre = jugadores[resenia.jugador]
            jugadorNombre?.let { Resenia(resenia.comentario, resenia.estado, resenia.fecha, it) }
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