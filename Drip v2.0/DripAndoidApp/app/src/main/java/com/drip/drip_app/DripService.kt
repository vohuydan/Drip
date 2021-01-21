package com.drip.drip_app

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Intent

import android.graphics.BitmapFactory
import android.os.IBinder
import androidx.core.app.NotificationCompat
//import com.drip.drip_app.App.CHANNEL_ID

class DripService:Service() {
    private lateinit var device: BluetoothDevice
    private val CHANNEL_ID = "ChannelID"
    private val NEWIDE= "NEWIDE"
    override fun onCreate() {
        super.onCreate()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }
        createNotification()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun createNotification(){
        val intent = Intent(this, inble::class.java)
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device)
        //intent.flags = FLAG_ACTIVITY_SINGLE_TOP
        //intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this,0,intent,
            FLAG_UPDATE_CURRENT)
        val bitmap = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.originalwaterdrop)

        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_notification)
            .setLargeIcon(bitmap)
            .setContentTitle("Drip is still running")
            .setContentText("Tap for more information or to stop app")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel((false))

            .setShowWhen(false)

        startForeground(1,builder.build())

    }
    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}