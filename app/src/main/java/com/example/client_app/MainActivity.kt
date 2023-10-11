package com.example.client_app

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Configura la Toolbar como ActionBar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Pale's"
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.it1 -> {
                // Acción para editar
                Toast.makeText(this, "et1", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.it2 -> {
                // Acción para eliminar
                Toast.makeText(this, "et2", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }
    fun activityperfil(view: View){
        val intent = Intent(this, MiPerfil::class.java)
        startActivity(intent)
    }
    fun activitylistarusuarios(view: View){
        val intent = Intent(this, ListarUsuarios::class.java)
        startActivity(intent)
    }
}