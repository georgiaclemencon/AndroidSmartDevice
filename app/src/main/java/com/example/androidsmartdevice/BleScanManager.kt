package com.example.androidsmartdevice


import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.os.Handler
import android.os.Looper
import android.bluetooth.le.BluetoothLeScanner
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class BleScanManager(
    btManager: BluetoothManager,
    private val scanPeriod: Long = DEFAULT_SCAN_PERIOD,
    private val scanCallback: BleScanCallback = BleScanCallback()
) {
    private val btAdapter = btManager.adapter
    private val bleScanner = btAdapter.bluetoothLeScanner


    /** True when the manager is performing the scan */
    private var scanning = false

    private val handler = Handler(Looper.getMainLooper())

    /**
     * Scans for Bluetooth LE devices and stops the scan after [scanPeriod] seconds.
     * Does not checks the required permissions are granted, check must be done beforehand.
     */
    @SuppressLint("MissingPermission")
    fun scanBleDevices() {
        fun stopScan() {
            scanning = false
            bleScanner.stopScan(scanCallback)

        }

        // scans for bluetooth LE devices
        if (scanning) {
            stopScan()
        } else {
            // stops scanning after scanPeriod millis
            handler.postDelayed({ stopScan() }, scanPeriod)


            // starts scanning
            scanning = true
            bleScanner.startScan(scanCallback)
        }
    }
    @SuppressLint("MissingPermission")
    fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
    }

    companion object {
        const val DEFAULT_SCAN_PERIOD: Long = 10000


    }


}