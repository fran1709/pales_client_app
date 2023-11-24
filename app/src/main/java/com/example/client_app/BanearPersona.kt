package com.example.client_app

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class BanearPersona : AppCompatActivity() {

    private val db = Firebase.firestore
    private val jugadoresList = ArrayList<JugadorBan>()
    private val jugadoresLiveData = MutableLiveData<List<JugadorBan>>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterBanear
    private var jugadoresListener: ListenerRegistration? = null
    private lateinit var userID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_banear_persona)

        // Obtener el userID o asignarlo según corresponda
        userID = FirebaseAuth.getInstance().currentUser?.uid.toString()

        initializeUI()

        jugadoresLiveData.observe(this, Observer { jugadores ->
            adapter.updateData(jugadores)
        })

        recyclerView.visibility = View.VISIBLE  // Set RecyclerView to visible
        val progressBar = findViewById<ProgressBar>(R.id.waitingProgressBarBAN)
        progressBar.visibility = View.GONE  // Set ProgressBar to gone

        // Agrega el SnapshotListener para escuchar cambios en la colección "jugadores"
        jugadoresListener = db.collection("jugadores")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(ContentValues.TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val newJugadores = snapshot.documents.map { document ->
                        val data = document.data
                        val nombre = data?.get("nombre") as? String ?: ""
                        val estado = data?.get("estado") as? Boolean ?: false
                        JugadorBan(document.id, nombre, estado)
                    }

                    jugadoresList.clear()
                    jugadoresList.addAll(newJugadores)
                    jugadoresLiveData.value = jugadoresList
                } else {
                    Log.d(ContentValues.TAG, "Current data: null")
                }
            }

        // Cargar jugadores al inicio
        loadJugadores()
    }
    @Suppress("DEPRECATION")
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    override fun onDestroy() {
        super.onDestroy()
        jugadoresListener?.remove()
    }

    private fun initializeUI() {
        val toolbar: Toolbar = findViewById(R.id.toolbarBAN)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Sección Baneo/Desbaneo"

        recyclerView = findViewById(R.id.recyclerViewBanearPersona)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AdapterBanear(jugadoresList) { jugador ->
            // Lógica para cambiar el estado (banear/desbanear) del jugador
            updateEstadoJugador(jugador.documentID, !jugador.estado)
        }
        recyclerView.adapter = adapter
    }

    private fun updateEstadoJugador(jugadorID: String, nuevoEstado: Boolean) {
        val jugadorDocument = db.collection("jugadores").document(jugadorID)
        val updatedData = hashMapOf(
            "estado" to nuevoEstado
        )

        jugadorDocument.update(updatedData as Map<String, Any>)
            .addOnFailureListener { e ->
                Log.w("BanearPersonaActivity", "Error al actualizar el estado del jugador", e)
            }
    }

    private fun loadJugadores() {
        db.collection("jugadores")
            .get()
            .addOnSuccessListener { result ->
                val jugadores = result.documents.map { document ->
                    val data = document.data
                    val nombre = data?.get("nombre") as? String ?: ""
                    val estado = data?.get("estado") as? Boolean ?: false
                    JugadorBan(document.id, nombre, estado)
                }
                jugadoresLiveData.value = jugadores
            }
            .addOnFailureListener { exception ->
                Log.w("BanearPersonaActivity", "Error getting documents.", exception)
            }
    }
}
