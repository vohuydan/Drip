package com.drip.drip_app

import android.app.Notification
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.inble_activity.*
import org.jetbrains.anko.notificationManager
import java.nio.charset.Charset
import java.util.*


class inble:AppCompatActivity() {
    private val CHANNEL_ID = "ChannelID"
    private val NEWIDE= "NEWIDE"
    private val pattern = longArrayOf(100,300,300,300)
    var chosenGatt: BluetoothGatt? = null
    private var tx: BluetoothGattCharacteristic? = null
    private var rx: BluetoothGattCharacteristic? = null
    private lateinit var device: BluetoothDevice
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inble_activity)
        Log.d("NOTIFICATION", "Inside inBle Oncreate")

        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

        Log.d("NOTIFICATION", "got device")

        textView3.text = "$device"


        startConnection()
        button3.setOnClickListener{send("red")}
        button4.setOnClickListener{send("off")}
        button5.setOnClickListener{vibrate()}

        toggleButton.setOnCheckedChangeListener{_, isChecked->
            if (isChecked){
                Toast.makeText(this, "Bluetooth Disonnected!", Toast.LENGTH_SHORT).show()
                chosenGatt?.disconnect()
                val serviceIntent  = Intent ( this, DripService::class.java)
                stopService(serviceIntent)
            }else{
                startConnection()
            }

        }
    }


    private fun vibrate(){
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            Log.d("inside vibrate", "SDK greater than 26")
        } else {
            vibrator.vibrate(200)
            Log.d("inside vibrate", "SDK less than 26")
        }
    }
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")

                    chosenGatt = gatt
                    Handler(Looper.getMainLooper()).post {
                        chosenGatt?.discoverServices()

                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt.close()
                }
            } else {
                Log.w(
                    "BluetoothGattCallback",
                    "Error $status encountered for $deviceAddress! Disconnecting..."
                )
                gatt.close()
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) { // BASICALLY BLE NOTIFICATION
            with(characteristic) {
                Log.i("BluetoothGattCallback", "Characteristic $uuid changed | value: ${value.toString(Charsets.UTF_8)}")
                var stringvalue = value.toString(Charsets.UTF_8)
                onReceive(stringvalue)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                Log.w(
                    "BluetoothGattCallback",
                    "Discovered ${services.size} services for ${device.address}"
                )
                //printGattTable() // See implementation just above this section
                // Consider connection setup as complete here
            }
            tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID)
            rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID)
            // Setup notifications on RX characteristic changes (i.e. data received).
            // First call setCharacteristicNotification to enable notification.
            if (!gatt.setCharacteristicNotification(rx, true)) {
                // Stop if the characteristic notification setup failed.
                Log.e("BLE", "characteristic notification setup failed")
                tx = null
                rx = null
                return
            }
            // Next update the RX characteristic's client descriptor to enable notifications.
            val desc = rx!!.getDescriptor(CLIENT_UUID)
            if (desc == null) {
                // Stop if the RX characteristic has no client descriptor.
                Log.e("BLE", "RX characteristic has no client descriptor")
                tx = null
                rx = null
                return
            }
            desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            if (!gatt.writeDescriptor(desc)) {
                // Stop if the client descriptor could not be written.
                Log.e("BLET", "client descriptor could not be written")
                tx = null
                rx = null
                return
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            Log.d("ONREADCHARACTERISTICS", "INSIDE char Read" )
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        val charset = Charsets.UTF_8
                        // value is a bytearray
                        Log.i("BluetoothGattCallback", "Read characteristic $uuid:\n${value.toString(charset)}")
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        Log.e("BluetoothGattCallback", "Read not permitted for $uuid!")
                    }
                    else -> {
                        Log.e(
                            "BluetoothGattCallback",
                            "Characteristic read failed for $uuid, error: $status"
                        )
                    }
                }
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i("BluetoothGattCallback", "Wrote to characteristic $uuid | value: ${value.toString(Charsets.UTF_8)}")
                    }
                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        Log.e("BluetoothGattCallback", "Write exceeded connection ATT MTU!")
                    }
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        Log.e("BluetoothGattCallback", "Write not permitted for $uuid!")
                    }
                    else -> {
                        Log.e("BluetoothGattCallback", "Characteristic write failed for $uuid, error: $status")
                    }
                }
            }
        }

    }


    private fun onReceive(data:String) {
        runOnUiThread {
            if(data =="left"){
                textView3.text = "Left Button Pressed"
                tempNotification(0)
            }
            if(data =="right"){
                textView3.text = "Right Button Pressed"
                tempNotification(0)
            }
            if(data == "float1"){
                tempNotification(1)
            }
            if(data == "float2"){
                tempNotification(2)
            }
        }

    }


    private fun send(data: String) {
        var byteArrayData =data.toByteArray(Charset.forName("UTF-8"))
        if (tx == null || data == null || data.length == 0) {
            // Do nothing if there is no connection or message to send.
            return
        }
        // Update TX characteristic value.  Note the setValue overload that takes a byte array must be used.
        tx!!.value = byteArrayData
        //        writeInProgress = true; // Set the write in progress flag
        Log.d("BLE", "writing")
        chosenGatt!!.writeCharacteristic(tx)
    }

    fun ByteArray.toHexString(): String =
        joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }


    private fun BluetoothGatt.printGattTable() {
        if (services.isEmpty()) {
            Log.i("printGattTable", "No service and characteristic available, call discoverServices() first?")
            return
        }
        services.forEach { service ->
            val characteristicsTable = service.characteristics.joinToString(
                separator = "\n|--",
                prefix = "|--"
            ) { it.uuid.toString() }
            Log.i("printGattTable", "\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable"
            )
        }
    }
    fun BluetoothGattCharacteristic.isReadable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

    fun BluetoothGattCharacteristic.isWritable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

    fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
        return properties and property != 0}

    fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

    fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

    private fun disconnect(){

    }
    override  fun onDestroy(){
        super.onDestroy()

    }
    private fun tempNotification(int: Int){
        val bitmap = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.originalwaterdrop)
        val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val v = longArrayOf(500, 1000)
        var stringTitle = "Button Pressed"
        var stringText = "A button was pressed"
        if (int == 1){
            stringTitle ="Float 1"
            stringText = "RODI Water Full"
        }else if (int == 2){
            stringTitle ="Float 2"
            stringText = "Waste Water Full"
        }
        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_notification)
            .setLargeIcon(bitmap)
            .setContentTitle(stringTitle)
            .setContentText(stringText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel((false))
            .setDefaults(Notification.DEFAULT_ALL)
            .setShowWhen(false)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setVibrate(pattern)
        }
        notificationManager.notify(1234, builder.build())
    }
    private fun startConnection(){
        device.connectGatt(this, false, gattCallback)
        Toast.makeText(this, "Bluetooth Connected!", Toast.LENGTH_SHORT).show()

        val serviceIntent  = Intent( this, DripService::class.java)
        serviceIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, device)
        ContextCompat.startForegroundService(this,serviceIntent)
    }
    companion object{
        // UUIDs for UART service and associated characteristics.
        var UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
        var TX_UUID = UUID.fromString  ("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
        var RX_UUID = UUID.fromString  ("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")

        // UUID for the UART BTLE client characteristic which is necessary for notifications.
        var CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        // UUIDs for the Device Information service and associated characteristics.
        var DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")
        var DIS_MANUF_UUID = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb")
        var DIS_MODEL_UUID = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb")
        var DIS_HWREV_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb")
        var DIS_SWREV_UUID = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb")
    }
}