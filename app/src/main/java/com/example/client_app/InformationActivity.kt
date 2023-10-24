package com.example.client_app
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.example.pales.ImagePagerAdapter
import com.google.firebase.firestore.core.View

class InformationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informacion)

        val imageResources = listOf(
            R.drawable.cancha1,
            R.drawable.cancha2
        )

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val imagePagerAdapter = ImagePagerAdapter(this, imageResources)
        viewPager.adapter = imagePagerAdapter
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