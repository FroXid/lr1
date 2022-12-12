package com.example.l2new

import android.app.ActivityManager
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btn_start)?.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, Servise::class.java))
                Log.d("SERVICE", "CLICK")
            }
            updateTextStatus()
        }
        findViewById<View>(R.id.btn_stop)?.setOnClickListener {
            stopService(Intent(this, Servise::class.java))
            Handler().postDelayed({
                updateTextStatus()
            },100)
        }
        updateTextStatus()
    }

    private fun updateTextStatus() {
        if(isMyServiceRunning(Servise::class.java)){
            findViewById<TextView>(R.id.txt_service_status)?.text = "Service is Running"
        }else{
            findViewById<TextView>(R.id.txt_service_status)?.text = "Service is NOT Running"
        }
    }


    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        try {
            val manager =
                getSystemService(ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(
                Int.MAX_VALUE
            )) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }

    companion object{
        const val  ACTION_STOP = "${BuildConfig.APPLICATION_ID}.stop"
    }

}