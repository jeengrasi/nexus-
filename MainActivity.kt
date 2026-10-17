
package com.nexus.driverpro
import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.widget.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply{ orientation = LinearLayout.VERTICAL; setPadding(40,40,40,40) }
        val status = TextView(this).apply{ text="Nexus V4.3 Nativo - Listo para conectar"; textSize=20f }
        val btnNotif = Button(this).apply{ text="1. CONECTAR A DOORDASH/UBER/FLEX (Notificaciones)"; setOnClickListener{
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }}
        val btnOverlay = Button(this).apply{ text="2. PERMITIR BURBUJA FLOTANTE"; setOnClickListener{
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
        }}
        val btnTest = Button(this).apply{ text="3. PROBAR BURBUJA DE PRUEBA"; setOnClickListener{
            OverlayService.show(this, 18.5, 7.5, 25.0, 14.96, 35.91, true, false, false, 2.0, "com.doordash.driverapp")
        }}
        val info = TextView(this).apply{ text="\nUna vez conectado, minimiza la app. Cuando caiga pedido real, veras burbuja verde/roja con boton ABRIR PARA TOMAR y PAUSAR OTRAS APPS.\n\nModulos incluidos: Deadhead, Blacklist, Tax 15.3%, Hidden Tips, Gasolina con camara, Mantenimiento Douglas/Techron, Multi-IA, Auto-tests." }
        layout.addView(status); layout.addView(btnNotif); layout.addView(btnOverlay); layout.addView(btnTest); layout.addView(info)
        setContentView(layout)
    }
}
