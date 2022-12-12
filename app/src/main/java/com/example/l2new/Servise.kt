package com.example.l2new

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.util.*

const val ACTION_STOP = "1"
class Servise :Service() {
    val ACTION_STOP_FOREGROUND = 1
    private var iconNotification: Bitmap? = null
    private var notification: Notification? = null
    var mNotificationManager: NotificationManager? = null
    private lateinit var pendingIntentService: PendingIntent
    private lateinit var alarmManager: AlarmManager
    private val mNotificationId = 123

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action != null && intent.action!!.equals(
                ACTION_STOP_FOREGROUND)) {
            stopForeground(true)
            stopSelf()
        }
        generateForegroundNotification()

        val intentService = Intent(this, UpdateReciever::class.java)

        pendingIntentService = PendingIntent.getBroadcast(this, 1, intentService,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            1000,
            pendingIntentService
        )



        return START_STICKY


    }

    private fun generateForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intentMainLanding = Intent(this, MainActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(this, 0, intentMainLanding, FLAG_IMMUTABLE)
            iconNotification = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            if (mNotificationManager == null) {
                mNotificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mNotificationManager?.createNotificationChannelGroup(
                    NotificationChannelGroup("chats_group", "Chats")
                )
                val notificationChannel =
                    NotificationChannel("service_channel", "Service Notifications",
                        NotificationManager.IMPORTANCE_MIN)
                notificationChannel.enableLights(false)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
                mNotificationManager?.createNotificationChannel(notificationChannel)
            }
            val builder = NotificationCompat.Builder(this, "service_channel")

            builder.setContentTitle(StringBuilder(resources.getString(R.string.app_name)).append(" service is running").toString())
                .setTicker(StringBuilder(resources.getString(R.string.app_name)).append("service is running").toString())
                .setContentText("Touch to open") //                    , swipe down for more options.
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setWhen(0)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
            if (iconNotification != null) {
                builder.setLargeIcon(Bitmap.createScaledBitmap(iconNotification!!, 128, 128, false))
            }
            builder.color = resources.getColor(R.color.purple_200)
            notification = builder.build()
            startForeground(mNotificationId, notification)
        }

    }
}

class UpdateReciever: BroadcastReceiver(), SensorEventListener  {
    private var s: SensorManager? = null
    private lateinit var c: Context
    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.d("АХТУНГ", "ЗДЕСЬ Я ЕЩЕ РАБОТАЮ")
        c = p0!!
        s = p0.getSystemService(SENSOR_SERVICE) as SensorManager
        val sensor = s?.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (sensor != null) {
            s?.registerListener(this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(p0, "No light sensor!", Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(p0: SensorEvent?) {
        Log.d("АХТУНГ", "ПОЛУЧИЛ ДАННЫЕ С ДАТЧИКА")
        val lux = p0!!.values[0]
        val pendingMainActivityIntent: PendingIntent =
            Intent(c, MainActivity::class.java).let { notificationIntent: Intent ->
                PendingIntent.getActivity(c, 0, notificationIntent,
                    FLAG_IMMUTABLE)
            }

        val mNotificationManager = c.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

        val notification: Notification = Notification.Builder(c,"service_channel")
            .setContentTitle("Измерение")
            .setContentText("Уровень освещенности: $lux")
            .setSmallIcon(androidx.constraintlayout.widget.R.drawable.notification_template_icon_bg)
            .setContentIntent(pendingMainActivityIntent)
            .setTicker("1")
            .build()

        mNotificationManager.notify(Random(System.currentTimeMillis()).nextInt(),notification)
        s?.unregisterListener(this)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

}
