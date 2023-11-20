package com.example.client_app

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.example.pales.ImagePagerAdapter
import android.view.View
import android.os.Handler

class InformationActivity : AppCompatActivity() {

    private val imageResources = listOf(
        R.drawable.cancha1,
        R.drawable.cancha1,
        R.drawable.cancha2,
        R.drawable.cancha4
    )

    private val handler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informacion)

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val imagePagerAdapter = ImagePagerAdapter(this, imageResources)
        viewPager.adapter = imagePagerAdapter

        // Configura el cambio automático de imágenes cada 3 segundos
        val handler = android.os.Handler()
        val updateRunnable = Runnable {
            val currentItem = viewPager.currentItem
            val nextItem = (currentItem + 1) % imageResources.size
            viewPager.setCurrentItem(nextItem, true)
        }

        handler.postDelayed(updateRunnable, 3000) // Cambia la imagen cada 3 segundos

        // Agrega un callback para detener la actualización cuando la actividad está en segundo plano
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    handler.postDelayed(updateRunnable, 3000)
                } else {
                    handler.removeCallbacks(updateRunnable)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    fun openGoogleMaps(view: View) {
        val latitude = 10.368946534699061
        val longitude = -84.33685552627428
        val locationName = "Cancha Pale's"

        val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($locationName)")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps") // Abre Google Maps
        startActivity(mapIntent)
    }
}