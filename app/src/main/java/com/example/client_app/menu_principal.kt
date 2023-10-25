package com.example.client_app

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class menu_principal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_principal)
    }

    fun callActivityResenias(view: View){
        // Crear un Intent para iniciar la Activity2
        val intent = Intent(this, ReseniasActivity::class.java)

        // Iniciar la Activity2 utilizando el Intent
        startActivity(intent)
    }

    fun activityperfil(view: View){
        val intent = Intent(this, MiPerfil::class.java)
        startActivity(intent)
    }
    fun activitylistarusuarios(view: View){
        val intent = Intent(this, ListarUsuarios::class.java)
        startActivity(intent)
    }
    fun call_info_lugar_activity(view: View){
        val intent = Intent(this, InformationActivity::class.java)
        startActivity(intent)
    }
    fun activityPromociones(view: View){
        val intent = Intent(this, ListarPromociones::class.java)
        startActivity(intent)
    }
    fun activityEventos(view: View){
        val intent = Intent(this, ListarEventos::class.java)
        startActivity(intent)
    }
}