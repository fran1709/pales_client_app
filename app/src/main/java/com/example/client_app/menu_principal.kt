package com.example.client_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
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

        // Cambiar el título del Toolbar
        val titleTextView: TextView = findViewById(R.id.textView)
        titleTextView.text = "Pale's Cachas Sintéticas"
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
                }
            }
        })
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