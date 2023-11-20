package com.example.client_app

import android.app.ActivityManager
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
import com.google.android.material.tabs.TabLayout

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

data class Jugador(val id: String?, val nombre: String, val posiciones: List<String>)

private lateinit var db: FirebaseFirestore
private lateinit var startForResult: ActivityResultLauncher<Intent>
private lateinit var recyclerView2: RecyclerView
class ListarUsuarios : AppCompatActivity() {
    private lateinit var tabLayout: TabLayout;
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

        tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        toolbar_navigator()

        // Obtener la posición de la pestaña seleccionada del Intent
        val selectedTabPosition = intent.getIntExtra("selectedTab", -1)
        if (selectedTabPosition != -1) {
            // Establecer la pestaña seleccionada en el TabLayout
            tabLayout.getTabAt(selectedTabPosition)?.select()
        }

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

    fun activityperfiljugador(view: View){
        val intent = Intent(this, PerfilJugador::class.java)
        startForResult.launch(intent)
    }
    fun activitybuscarJugador(){
        val intent = Intent(this, BuscarJugador::class.java)
        startForResult.launch(intent)
    }

    fun toolbar_navigator(){
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        if (!isCurrentActivity(InformationActivity::class.java)) {
                            val intent = Intent(this@ListarUsuarios, InformationActivity::class.java)
                            intent.putExtra("selectedTab", 0) // Agregar posición como extra
                            call_info_lugar_activity()
                        }
                    }
                    1 -> {
                        if (!isCurrentActivity(ReseniasActivity::class.java)) {
                            val intent = Intent(this@ListarUsuarios, ReseniasActivity::class.java)
                            intent.putExtra("selectedTab", 1) // Agregar posición como extra
                            callActivityResenias()
                        }
                    }
                    2 -> {
                        if (!isCurrentActivity(ListarUsuarios::class.java)) {
                            val intent = Intent(this@ListarUsuarios, ListarUsuarios::class.java)
                            intent.putExtra("selectedTab", 2) // Agregar posición como extra
                            activitylistarusuarios()
                        }
                    }
                    3 -> {
                        if (!isCurrentActivity(MiPerfil::class.java)) {
                            val intent = Intent(this@ListarUsuarios, MiPerfil::class.java)
                            intent.putExtra("selectedTab", 2) // Agregar posición como extra
                            activityperfil()
                        }
                    }
                }
            }


            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // No es necesario implementar nada aquí de momento
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Si la pestaña está seleccionada, también realiza la acción correspondiente
                when (tab?.position) {
                    0 -> {
                        if (!isCurrentActivity(InformationActivity::class.java)) {
                            call_info_lugar_activity()
                        }
                    }
                    1 -> {
                        if (!isCurrentActivity(ReseniasActivity::class.java)) {
                            callActivityResenias()
                        }
                    }
                    2 -> {
                        if (!isCurrentActivity(ListarUsuarios::class.java)) {
                            activitylistarusuarios()
                        }
                    }
                    3 -> {
                        if (!isCurrentActivity(MiPerfil::class.java)) {
                            activityperfil()
                        }
                    }
                }
            }
        })
    }
    // Función para verificar si la actividad actual es la especificada
    fun isCurrentActivity(activityClass: Class<*>): Boolean {
        val manager = (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        val runningTaskInfoList = manager.getRunningTasks(1)
        if (runningTaskInfoList.isNotEmpty()) {
            val topActivity = runningTaskInfoList[0].topActivity
            return topActivity?.className == activityClass.name
        }
        return false
    }
    fun callActivityResenias(){
        val intent = Intent(this, ReseniasActivity::class.java)
        startActivity(intent)
    }

    fun activityperfil(){
        val intent = Intent(this, MiPerfil::class.java)
        startActivity(intent)
    }
    fun activitylistarusuarios(){
        val intent = Intent(this, ListarUsuarios::class.java)
        startActivity(intent)
    }
    fun call_info_lugar_activity(){
        val intent = Intent(this, InformationActivity::class.java)
        startActivity(intent)
    }
}
