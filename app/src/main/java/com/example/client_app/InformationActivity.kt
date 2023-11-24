package com.example.client_app
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.example.pales.ImagePagerAdapter
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.tabs.TabLayout
import android.os.Handler
import android.widget.ImageView

class InformationActivity : AppCompatActivity() {
    private lateinit var tabLayout:TabLayout;
    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private val imageResources = listOf(
        R.drawable.cancha1,
        R.drawable.cancha2,
        R.drawable.cancha3,
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

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == androidx.appcompat.app.AppCompatActivity.RESULT_OK) {
                val data: android.content.Intent? = result.data

            }
        }

        tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        toolbar_navigator()

        // Cambiar el título del Toolbar
        val titleTextView: TextView = findViewById(R.id.textView)
        titleTextView.text = "Pale's Cachas Sintéticas"

        // Obtener la posición de la pestaña seleccionada del Intent
        val selectedTabPosition = intent.getIntExtra("selectedTab", -1)
        if (selectedTabPosition != -1) {
            // Establecer la pestaña seleccionada en el TabLayout
            tabLayout.getTabAt(selectedTabPosition)?.select()
        }

        val whatsAppIcon = findViewById<ImageView>(R.id.whatsAppIcon)
        whatsAppIcon.setOnClickListener {
            // Número de teléfono al que deseas enviar el mensaje (debe incluir el código de país)
            val phoneNumber = "+50684092161"

            // Crea un Intent con la acción ACTION_VIEW y el URI del número de teléfono en el formato de WhatsApp
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phoneNumber"))

            // Inicia la actividad con el Intent
            startActivity(intent)
        }

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

    private fun toolbar_navigator(){
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        if (!isCurrentActivity(InformationActivity::class.java)) {
                            val intent = Intent(this@InformationActivity, InformationActivity::class.java)
                            intent.putExtra("selectedTab", 0) // Agregar posición como extra
                            call_info_lugar_activity()
                        }
                    }
                    1 -> {
                        if (!isCurrentActivity(ReseniasActivity::class.java)) {
                            val intent = Intent(this@InformationActivity, ReseniasActivity::class.java)
                            intent.putExtra("selectedTab", 1) // Agregar posición como extra
                            callActivityResenias()
                        }
                    }
                    2 -> {
                        if (!isCurrentActivity(ListarUsuarios::class.java)) {
                            val intent = Intent(this@InformationActivity, ListarUsuarios::class.java)
                            intent.putExtra("selectedTab", 2) // Agregar posición como extra
                            activitylistarusuarios()
                        }
                    }
                    3 -> {
                        if (!isCurrentActivity(MiPerfil::class.java)) {
                            val intent = Intent(this@InformationActivity, MiPerfil::class.java)
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