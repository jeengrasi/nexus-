
package com.nexus.driverpro
import android.content.Context
object Prefs {
    private fun sp(c: Context) = c.getSharedPreferences("nexus_v43", Context.MODE_PRIVATE)
    fun getGasPrice(c: Context) = sp(c).getFloat("gas", 3.40f).toDouble()
    fun getRealMpg(c: Context) = sp(c).getFloat("mpg", 28f).toDouble()
    fun getMeta(c: Context) = sp(c).getFloat("meta", 22f).toDouble()
    fun getDeadhead(c: Context, miles: Double): Double {
        // Si entrega >6mi fuera zona, asume 70% retorno
        return if(miles>6) miles*0.7 else 0.0
    }
    fun isBlacklisted(c: Context, text: String): Boolean {
        val list = sp(c).getStringSet("blacklist", setOf()) ?: setOf()
        return list.any{ text.contains(it, true) }
    }
}
