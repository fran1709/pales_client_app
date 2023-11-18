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


class AprobarResenia : AppCompatActivity() {

    private val db = Firebase.firestore
    private val reseniasList = ArrayList<Resenia>()
    private val reseniasLiveData = MutableLiveData<List<Resenia>>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AprobarAdapter
    private var commentsListener: ListenerRegistration? = null
    private var isLoading = true
    private lateinit var userID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aprobar_resenia)

        // Obtener el userID o asignarlo según corresponda
        userID = FirebaseAuth.getInstance().currentUser?.uid.toString()

        initializeUI()

        reseniasLiveData.observe(this, Observer { comentarios ->
            adapter.updateData(comentarios)
        })

        // Oculta el RecyclerView y la vista de espera al principio
        recyclerView.visibility = View.GONE
        val progressBar = findViewById<ProgressBar>(R.id.waitingProgressBarTV)
        progressBar.visibility = View.VISIBLE

        // Agrega el SnapshotListener para escuchar cambios en la colección "resena"
        commentsListener = db.collection("resena")
            .whereEqualTo("estado", false)
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


    override fun onDestroy() {
        super.onDestroy()
        commentsListener?.remove()
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

    private fun actualizarEstadoResenia(resenia: Resenia, nuevoEstado: Boolean) {
        // Actualizar el estado en la base de datos
        val comentarioDocument = db.collection("resena").document(resenia.docuemntID)

        // Crear un HashMap con los campos a actualizar
        val updatedData = hashMapOf(
            "estado" to nuevoEstado
        )

        comentarioDocument.update(updatedData as Map<String, Any>)
            .addOnSuccessListener {
                // Éxito al actualizar
                Toast.makeText(this, "Estado actualizado correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Error al actualizar
                Log.e(TAG, "Error al actualizar el estado de la Reseña", e)
                Toast.makeText(this, "Error al actualizar el estado", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarResenia(documentID: String) {
        // Referencia al documento a eliminar
        val reseniaDocument = db.collection("resena").document(documentID)

        // Elimina el documento
        reseniaDocument.delete()
            .addOnSuccessListener {
                // Éxito al eliminar
                Toast.makeText(this, "Reseña eliminada correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Error al eliminar
                Log.e(TAG, "Error al eliminar la Reseña", e)
                Toast.makeText(this, "Error al eliminar la Reseña", Toast.LENGTH_SHORT).show()
            }
    }
    private fun initializeUI() {
        val toolbar: Toolbar = findViewById(R.id.toolbarTV)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Pale's Reseñas por Aprobar"

        recyclerView = findViewById(R.id.recyclerViewTV)
        recyclerView.layoutManager = LinearLayoutManager(this)


        obtenerNombreUsuario(userID) { userNombre ->
            adapter = AprobarAdapter(reseniasList,
                onAprobarClick = { resenia ->
                    // Lógica para aprobar la Resenia
                    Toast.makeText(this, "Resenia Aprobada: ${resenia.comentario}", Toast.LENGTH_SHORT).show()
                    actualizarEstadoResenia(resenia,true)
                },
                onRechazarClick = { resenia ->
                    // Lógica para rechazar la Resenia
                    Toast.makeText(this, "Resenia Rechazada: ${resenia.comentario}", Toast.LENGTH_SHORT).show()
                    eliminarResenia(resenia.docuemntID)
                }
            )
            recyclerView.adapter = adapter
        }

        adapter = AprobarAdapter(reseniasList,
            onAprobarClick = { resenia ->
                // Lógica para aprobar la Resenia
                Toast.makeText(this, "Resenia Aprobada: ${resenia.comentario}", Toast.LENGTH_SHORT).show()
                actualizarEstadoResenia(resenia,true)
            },
            onRechazarClick = { resenia ->
                // Lógica para rechazar la Resenia
                Toast.makeText(this, "Resenia Rechazada: ${resenia.comentario}", Toast.LENGTH_SHORT).show()
                eliminarResenia(resenia.docuemntID)
            }
        )
        recyclerView.adapter = adapter

        // Set the loading state to true
        isLoading = true
    }

    /**
     * @author Francisco Ovares Rojas
     * Método encargado de cargar las reseñas de la base de datos. Método principal.
     */
    private fun loadResenias() {
        db.collection("resena")
            .whereEqualTo("estado", false) // Obtener solo comentarios con estado=false
            .get()
            .addOnSuccessListener { result ->
                val resenias = result.documents.map { document ->
                    val data = document.data
                    val comentario = data?.get("comentario") as? String ?: ""
                    val estado = data?.get("estado") as? Boolean ?: false
                    val fecha = data?.get("fecha") as? Timestamp ?: Timestamp.now()
                    val jugador_id = data?.get("jugador") as? String ?: ""
                    Resenia(document.id, comentario, estado, fecha, jugador_id)
                }

                if (resenias.isEmpty()) {
                    // No hay comentarios con estado=false, muestra el Toast
                    Toast.makeText(this, "No hay comentarios pendientes de aprobación", Toast.LENGTH_SHORT).show()

                    // Limpia la lista de resenias y actualiza el LiveData
                    reseniasList.clear()
                    reseniasLiveData.value = emptyList()
                } else {
                    loadJugadores(resenias)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }


    @Suppress("DEPRECATION")
    private fun loadReseniasWithDelay() {
        // Agrega un retraso antes de cargar los comentarios nuevamente
        Handler().postDelayed({
            loadResenias()
        }, 2000) // 2000 milisegundos (2 segundos) de retraso
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
        val progressBar = findViewById<ProgressBar>(R.id.waitingProgressBarTV)
        progressBar.visibility = View.GONE

        // Muestra el RecyclerView con los comentarios actualizados
        recyclerView.visibility = View.VISIBLE
    }

}