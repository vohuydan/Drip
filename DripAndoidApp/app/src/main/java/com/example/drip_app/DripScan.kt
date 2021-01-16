 package com.example.drip_app

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_drip_scan.*


 private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
 private const val LOCATION_PERMISSION_REQUEST_CODE = 2

class DripScan : AppCompatActivity() {




    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    private val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)


    private fun Context.hasPermission(permissionType: String): Boolean {
        Log.i("LOACATION PERMISSION","Inside Context.hasPermission")
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }
    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result
                scanResultAdapter.notifyItemChanged(indexQuery)
            } else {
                with(result.device) {
                    Log.i("ScanCallBack", "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                }
                scanResults.add(result)
                scanResultAdapter.notifyItemInserted(scanResults.size - 1)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("ScanCallBack", "onScanFailed: code $errorCode")
        }
    }

    private var isScanning = false
        set(value) {
            field = value
            runOnUiThread { button.text = if (value) "Stop Scan" else "Start Scan" }
        }

    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }

    private fun startBleScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2)
        }
        else {
            scanResults.clear()
            scanResultAdapter.notifyDataSetChanged()
            bleScanner.startScan(null, scanSettings, scanCallback)
            isScanning = true
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drip_scan)

        button.setOnClickListener {
            if (isScanning) {
                stopBleScan()
            } else {
                startBleScan()
            }
        }

        setupRecyclerView()


    }

    private val scanResults = mutableListOf<ScanResult>()
    private val scanResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(scanResults) { result ->
            // User tapped on a scan result
            if (isScanning) {
                stopBleScan()
            }
            with(result.device) {
                Log.w("ScanResultAdapter", "Connecting to $address")
                //connectGatt(this@MainActivity, false)

                val intent = Intent(this@DripScan, inble::class.java)
                intent.putExtra(BluetoothDevice.EXTRA_DEVICE, result.device)
                startActivity(intent)
                finish()
            }
        }
    }


    private fun setupRecyclerView() {
        scan_results_recycler_view.apply {
            adapter = scanResultAdapter
            layoutManager = LinearLayoutManager(
                this@DripScan,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        val animator = scan_results_recycler_view.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("device","Inside OnResume")
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth is enabled", Toast.LENGTH_SHORT).show()
                }else {
                    Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show()
                    promptEnableBluetooth()
                }
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "After first denied request", Toast.LENGTH_SHORT).show()
                    Log.i("LOACATION PERMISSION","Inside onRequestPermissionsResult")
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2)
                } else {
                    startBleScan()
                }
            }
        }
    }


}


