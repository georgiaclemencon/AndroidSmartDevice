package com.example.androidsmartdevice

import DistanceIndicator
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme
class DeviceActivity : ComponentActivity() {
    private lateinit var btManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var bluetoothGatt: BluetoothGatt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deviceAddress = intent.getStringExtra("device_address")
        val deviceName = intent.getStringExtra("device_name")
        val deviceRssi = intent.getIntExtra("device_rssi", 0)

        btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = btManager.adapter
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress)

        connectToDevice()

        setContent {
            AndroidSmartDeviceTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DeviceDetail(deviceName, deviceAddress, deviceRssi) {
                        connectToDevice()
                    }
                }
            }
        }
    }

   @SuppressLint("MissingPermission")
private fun connectToDevice() {
    bluetoothGatt = bluetoothDevice.connectGatt(this, false, object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i("BluetoothGatt", "Connected to GATT server.")
                    Log.i("BluetoothGatt", "Attempting to start service discovery: " +
                            gatt.discoverServices())
                    runOnUiThread {
                        Toast.makeText(this@DeviceActivity, "VOUS ETES CONNECTE", Toast.LENGTH_SHORT).show()
                        Toast.makeText(this@DeviceActivity, "Test Toast", Toast.LENGTH_SHORT).show()

                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i("BluetoothGatt", "Disconnected from GATT server.")
                    runOnUiThread {
                        Toast.makeText(this@DeviceActivity, "Disconnected from GATT server", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    })
}

}

