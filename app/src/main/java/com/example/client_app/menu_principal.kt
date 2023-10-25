package com.example.client_app

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.tabs.TabLayout

private lateinit var startForResult: ActivityResultLauncher<Intent>

class menu_principal : AppCompatActivity() {
    private lateinit var tabLayout:TabLayout;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_principal)

        tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        toolbar_navigator()

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == androidx.appcompat.app.AppCompatActivity.RESULT_OK) {
                val data: android.content.Intent? = result.data

            }
        }
    }

    fun toolbar_navigator(){
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        val intent = Intent(this@menu_principal, InformationActivity::class.java)
                        startActivity(intent)
                    }
                    1 -> {
                        val intent = Intent(this@menu_principal, ReseniasActivity::class.java)
                        startActivity(intent)
                    }
                    2 -> {
                        val intent = Intent(this@menu_principal, ListarUsuarios::class.java)
                        startForResult.launch(intent)
                    }
                    3 -> {
                        val intent = Intent(this@menu_principal, MiPerfil::class.java)
                        //startActivity(intent)
                        startForResult.launch(intent)
                    }
                    4 -> {
                        val intent = Intent(this@menu_principal, ListarPromociones::class.java)
                        //startActivity(intent)
                        startForResult.launch(intent)
                    }
                    5 -> {
                        val intent = Intent(this@menu_principal, ListarEventos::class.java)
                        //startActivity(intent)
                        startForResult.launch(intent)
                    }
                    // Agrega más casos según tus necesidades
                }
            }


            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // No es necesario implementar nada aquí de momento
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Si la pestaña está seleccionada, también realiza la acción correspondiente
                when (tab?.position) {
                    0 -> {
                        call_info_lugar_activity()
                    }
                    1 -> {
                        callActivityResenias()
                    }
                    2 -> {
                        activitylistarusuarios()
                    }
                    3 -> {
                        activityperfil()
                    }
                    4 -> {
                        activityPromociones()
                    }
                    5 -> {
                        activityEventos()
                    }
                    // Agrega más casos según tus necesidades
                }
            }
        })
    }

    fun callActivityResenias(){
        // Crear un Intent para iniciar la Activity2
        val intent = Intent(this, ReseniasActivity::class.java)

        // Iniciar la Activity2 utilizando el Intent
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
    fun activityPromociones(){
        val intent = Intent(this, ListarPromociones::class.java)
        startActivity(intent)
    }
    fun activityEventos(){
        val intent = Intent(this, ListarEventos::class.java)
        startActivity(intent)
    }
}