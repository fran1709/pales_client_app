package com.example.client_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun callLogin(view: View) {
        // Crear un Intent para iniciar la Activity2
        val intent = Intent(this, LoginActivity::class.java)
        // Iniciar la Activity2 utilizando el Intent
        startActivity(intent)
    }
    fun callRegister(view: View) {
        // Crear un Intent para iniciar la Activity2
        val intent = Intent(this, RegisterActivity::class.java)
        // Iniciar la Activity2 utilizando el Intent
        startActivity(intent)
    }
}