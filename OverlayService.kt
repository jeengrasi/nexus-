
package com.nexus.driverpro
import android.app.*
import android.content.*
import android.graphics.PixelFormat
import android.view.*
import android.widget.*
import android.os.*

class OverlayService : Service() {
    companion object {
        fun show(ctx: Context, pay: Double, miles: Double, mins: Double, net: Double, perHour: Double, green: Boolean, black: Boolean, hidden: Boolean, deadhead: Double, pkg: String){
            val i = Intent(ctx, OverlayService::class.java)
            i.putExtra("pay", pay); i.putExtra("miles", miles); i.putExtra("perHour", perHour); i.putExtra("green", green)
            i.putExtra("black", black); i.putExtra("hidden", hidden); i.putExtra("deadhead", deadhead); i.putExtra("pkg", pkg)
            ctx.startForegroundService(i)
        }
    }
    private var windowManager: WindowManager? = null
    private var view: View? = null
    override fun onBind(intent: Intent?) = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent==null) return START_NOT_STICKY
        val pay = intent.getDoubleExtra("pay",0.0)
        val perHour = intent.getDoubleExtra("perHour",0.0)
        val green = intent.getBooleanExtra("green",false)
        val black = intent.getBooleanExtra("black",false)
        val hidden = intent.getBooleanExtra("hidden",false)
        val pkg = intent.getStringExtra("pkg")?:""
        showOverlay(pay, perHour, green, black, hidden, pkg)
        return START_NOT_STICKY
    }
    private fun showOverlay(pay: Double, perHour: Double, green: Boolean, black: Boolean, hidden: Boolean, pkg: String){
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val layout = LinearLayout(this).apply{
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(if(green) 0xFF2E7D32.toInt() else 0xFFC62828.toInt())
            setPadding(30,30,30,30)
        }
        val title = TextView(this).apply{
            text = (if(green) "🟢 APROBADO" else "🔴 RECHAZAR") + " \$${String.format("%.2f", pay)} | \$${String.format("%.2f", perHour)}/h"
            textSize = 18f; setTextColor(0xFFFFFFFF.toInt())
        }
        val warn = TextView(this).apply{
            val t = StringBuilder()
            if(black) t.append("⚠️ LISTA NEGRA \n")
            if(hidden) t.append("⭐ POSIBLE PROPINA OCULTA \n")
            text = t.toString(); setTextColor(0xFFFFFF00.toInt())
        }
        val btnTake = Button(this).apply{
            text = "ABRIR PARA TOMAR"
            setOnClickListener{
                // Abre la app oficial, NO auto-click (evita baneo)
                val launch = packageManager.getLaunchIntentForPackage(pkg)
                startActivity(launch?.apply{ addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                hide()
            }
        }
        val btnPause = Button(this).apply{
            text = "PAUSAR OTRAS APPS 30 MIN"
            setOnClickListener{ 
                // Guarda pausa en prefs
                getSharedPreferences("nexus_v43", MODE_PRIVATE).edit().putLong("pause_until", System.currentTimeMillis()+30*60*1000).apply()
                hide()
            }
        }
        layout.addView(title); layout.addView(warn); layout.addView(btnTake); layout.addView(btnPause)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT)
        params.gravity = android.view.Gravity.TOP
        try{ windowManager?.addView(layout, params); view = layout } catch(e: Exception){}
        Handler(Looper.getMainLooper()).postDelayed({ hide() }, 15000)
    }
    private fun hide(){ view?.let{ windowManager?.removeView(it) }; stopSelf() }
}
