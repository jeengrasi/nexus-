
package com.nexus.driverpro

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.util.regex.Pattern

class NexusNotificationService : NotificationListenerService() {
    companion object {
        val PAY_REGEX = Pattern.compile("\\\$(\\d+\\.\\d{2})")
        val MILES_REGEX = Pattern.compile("(\\d+\\.?\\d*)\\s*mi")
        val MIN_REGEX = Pattern.compile("(\\d+)\\s*min")
        var instance: NexusNotificationService? = null
    }
    override fun onCreate() { super.onCreate(); instance = this }
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        if (pkg !in listOf("com.doordash.driverapp","com.ubercab.eats.driver","com.amazon.flex.rabbit")) return
        val text = sbn.notification.extras.getString("android.text") ?: "" + sbn.notification.extras.getString("android.title")
        // Regex en milisegundos, 0% bateria
        val pay = extract(PAY_REGEX, text)?.toDoubleOrNull() ?: return
        val miles = extract(MILES_REGEX, text)?.toDoubleOrNull() ?: 0.0
        val mins = extract(MIN_REGEX, text)?.toDoubleOrNull() ?: 15.0

        // Motor Financiero
        val gasPrice = Prefs.getGasPrice(this) // 3.40
        val mpg = Prefs.getRealMpg(this) // 28 real
        val wear = 0.35
        val maintenance = 0.05
        val costPerMile = gasPrice/mpg + wear + maintenance
        val totalCost = miles * costPerMile
        // Deadhead: millas de retorno a zona caliente
        val deadhead = Prefs.getDeadhead(this, miles)
        val totalCostWithDeadhead = (miles+deadhead)*costPerMile
        val net = pay - totalCostWithDeadhead
        val perHour = net / (mins/60.0)

        // Blacklist check
        val isBlacklisted = Prefs.isBlacklisted(this, text)
        // Hidden tip
        val isHiddenTip = (pay==6.5 || pay==8.0) && miles < 4.0

        // Veredicto Multi-IA
        val iaFinanciera = perHour >= Prefs.getMeta(this)
        val iaPropina = if(isHiddenTip) true else perHour > 0
        val iaRiesgo = !isBlacklisted && deadhead < 5

        val green = listOf(iaFinanciera, iaPropina, iaRiesgo).count{it} >=2

        Log.d("Nexus", "Oferta $pay $miles mi verde=$green")

        // Mostrar burbuja flotante
        OverlayService.show(this, pay, miles, mins, net, perHour, green, isBlacklisted, isHiddenTip, deadhead, pkg)
    }
    private fun extract(p: Pattern, t: String): String? {
        val m = p.matcher(t); return if(m.find()) m.group(1) else null
    }
}
