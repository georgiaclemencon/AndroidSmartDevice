package com.example.androidsmartdevice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme
import java.util.UUID

class DeviceActivity : ComponentActivity() {
    private lateinit var btManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var bluetoothGatt: BluetoothGatt
    private var ledBluetoothGattCharacteristic: BluetoothGattCharacteristic? = null
    private var counterBluetoothGattCharacteristic: BluetoothGattCharacteristic? = null
    private var controlBluetoothGattCharacteristic: BluetoothGattCharacteristic? = null
    private lateinit var deviceInteraction: DeviceComposableInteraction


    private var currentLEDStateEnum = LEDStateEnum.NONE


//    private val counterNotificationUUID =
//        UUID.fromString("Your-UUID-Here") // Replace with your UUID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deviceAddress = intent.getStringExtra("device_address")
        val deviceName = intent.getStringExtra("device_name")
        val deviceRssi = intent.getIntExtra("device_rssi", 0)

        btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = btManager.adapter
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress)

        setContent {


            AndroidSmartDeviceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val notificationCounter = remember { mutableStateOf("0") }
                    val isStateConnected by remember { mutableStateOf(false) }
                    val ledState1 = remember { mutableStateOf(false) }
                    val ledState2 = remember { mutableStateOf(false) }
                    val ledState3 = remember { mutableStateOf(false) }
                    val ledStates = listOf(ledState1, ledState2, ledState3)

                    val ledArray = listOf(
                        Led(ledState1.value) { turnOnLight(0, LEDStateEnum.LED_1, ledStates) },
                        Led(ledState2.value) { turnOnLight(1, LEDStateEnum.LED_2, ledStates) },
                        Led(ledState3.value) { turnOnLight(2, LEDStateEnum.LED_3, ledStates) }
                    )

                    deviceInteraction = DeviceComposableInteraction(
                        IsConnected = isStateConnected,
                        deviceTitle = deviceName ?: "",
                        LedArray = ledArray,
                        notificationCounter = notificationCounter.value,
                        onNotificationSubscribe = {
                            // Ajoutez ici le code pour gérer l'abonnement aux notifications
                            // Supposons que vous recevez une notification ici
                            notificationCounter.value =
                                (notificationCounter.value.toInt() + 1).toString()
                        }
                    )


                    DeviceDetail(deviceInteraction) {
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
                        Log.i(
                            "BluetoothGatt", "Attempting to start service discovery: " +
                                    gatt.discoverServices()
                        )
                        connectionStateChange(gatt, newState)


                        runOnUiThread {
                            Toast.makeText(
                                this@DeviceActivity,
                                "VOUS ETES CONNECTE",
                                Toast.LENGTH_SHORT
                            ).show()
                            Toast.makeText(this@DeviceActivity, "Test Toast", Toast.LENGTH_SHORT)
                                .show()

                        }
                    }

                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.i("BluetoothGatt", "Disconnected from GATT server.")
                        runOnUiThread {
                            Toast.makeText(
                                this@DeviceActivity,
                                "Disconnected from GATT server",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            private val gattCallback = object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    val deviceAddress = gatt.device.address
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                            // TODO: Store a reference to BluetoothGatt
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                            gatt.close()
                        }
                    } else {
                        Log.w("BluetoothGattCallback", "Error $status encountered for $deviceAddress! Disconnecting...")
                        gatt.close()
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (Looper.myLooper() == null) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        val gattTable = gatt.getGattTable()
                        gattTable.forEach { (serviceUuid, characteristicUuids) ->
                            Log.i("BluetoothGatt", "Service UUID: $serviceUuid")
                            characteristicUuids.forEach { characteristicUuid ->
                                Log.i("BluetoothGatt", "Characteristic UUID: $characteristicUuid")
                            }
                        }
                    } else {
                        Log.w("BluetoothGatt", "onServicesDiscovered received: $status")
                    }
                }
            }


        })
    }

    private fun BluetoothGatt.getGattTable(): Map<UUID, List<UUID>> {
        val gattTable = mutableMapOf<UUID, List<UUID>>()

        if (services.isEmpty()) {
            Log.i(
                "getGattTable",
                "No service and characteristic available, call discoverServices() first?"
            )
            return gattTable
        }

        services.forEach { service ->
            val characteristics = service.characteristics.map { it.uuid }
            gattTable[service.uuid] = characteristics
        }

        return gattTable
    }


    @SuppressLint("MissingPermission")
    private fun connectionStateChange(gatt: BluetoothGatt?, newState: Int) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            gatt?.discoverServices()
            runOnUiThread {
                deviceInteraction.IsConnected = newState == BluetoothProfile.STATE_CONNECTED
            }
        }
    }


    private fun turnOnLight(
        index: Int,
        newLedState: LEDStateEnum,
        ledStates: List<MutableState<Boolean>>
    ) {
        if (currentLEDStateEnum != newLedState) {
            ledStates.forEach { it.value = false }
            ledStates[index].value = !ledStates[index].value
            currentLEDStateEnum = newLedState
        } else {
            ledStates.forEach { it.value = false }
            currentLEDStateEnum = LEDStateEnum.NONE
        }
        //writeToLEDCharacteristic(currentLEDStateEnum)
    }

    override fun onStop() {
        super.onStop()
        closeBluetoothGatt()
    }

    @SuppressLint("MissingPermission")
    private fun closeBluetoothGatt() {
        bluetoothGatt.close()
    }


}