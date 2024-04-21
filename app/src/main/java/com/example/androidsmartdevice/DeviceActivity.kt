package com.example.androidsmartdevice

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.os.Bundle
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

                            notificationCounter.value =
                                (notificationCounter.value.toInt() + 1).toString()
                        },
                        Services = mutableListOf()
                    )

                    val deviceInteractionDisplay = remember { mutableStateOf(deviceInteraction) }


                    if (this::deviceInteraction.isInitialized) {
                        DeviceDetail(deviceInteractionDisplay) {
                            connectToDevice()
                        }
                    } else {
                        Log.i("DeviceActivity", "DeviceInteraction not initialized")

                    }

                }


            }
        }
    }

    @SuppressLint("MissingPermission")
    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
        val writeType = when {
            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.isWritableWithoutResponse() -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }

            else -> error("Characteristic ${characteristic.uuid} cannot be written to")
        }
        bluetoothGatt.let { gatt ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeCharacteristic(characteristic, payload, writeType)
            } else {
                // Fall back to deprecated version of writeCharacteristic for Android <13
                gatt.legacyCharacteristicWrite(characteristic, payload, writeType)
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
                override fun onConnectionStateChange(
                    gatt: BluetoothGatt,
                    status: Int,
                    newState: Int
                ) {
                    val deviceAddress = gatt.device.address
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            Log.w(
                                "BluetoothGattCallback",
                                "Successfully connected to $deviceAddress"
                            )
                            // TODO: Store a reference to BluetoothGatt
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            Log.w(
                                "BluetoothGattCallback",
                                "Successfully disconnected from $deviceAddress"
                            )
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
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
    if (status == BluetoothGatt.GATT_SUCCESS) {
        val gattTable = gatt.getGattTable()
        gattTable.forEach { (serviceUuid, characteristicUuids) ->
            Log.i("BluetoothGatt", "Service UUID: $serviceUuid")
            deviceInteraction.serviceWithCharacteristics.value[serviceUuid] = characteristicUuids
            Log.i("BluetoothGatt", "deviceInteraction: ${deviceInteraction.serviceWithCharacteristics}")
        }
        // Add the discovered services to deviceInteraction.Services
        deviceInteraction.Services.addAll(gatt.services)
    } else {
        Log.w("BluetoothGatt", "onServicesDiscovered received: $status")
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
        writeToLEDCharacteristic(currentLEDStateEnum)
        Log.i("DeviceActivity", "Turned on light $index")
    }

    override fun onStop() {
        super.onStop()
        closeBluetoothGatt()
    }

    @SuppressLint("MissingPermission")
    private fun closeBluetoothGatt() {
        bluetoothGatt.close()
    }

    fun BluetoothGattCharacteristic.isReadable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

    fun BluetoothGattCharacteristic.isWritable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

    fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
        return properties and property != 0
    }


    @SuppressLint("MissingPermission")
    private fun readLEDServices() {
        val LEDServiceUuid = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
        val LEDLevelCharUuid = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
        val LEDLevelChar =
            bluetoothGatt.getService(LEDServiceUuid)?.getCharacteristic(LEDLevelCharUuid)

        if (LEDLevelChar?.isReadable() == true) {
            bluetoothGatt.readCharacteristic(LEDLevelChar)
        }

    }

//    @SuppressLint("MissingPermission")
//    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
//        val writeType = when {
//            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
//            characteristic.isWritableWithoutResponse() -> {
//                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
//            }
//
//            else -> error("Characteristic ${characteristic.uuid} cannot be written to")
//        }
//        bluetoothGatt.let { gatt ->
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                gatt.writeCharacteristic(characteristic, payload, writeType)
//            } else {
//                // Fall back to deprecated version of writeCharacteristic for Android <13
//                gatt.legacyCharacteristicWrite(characteristic, payload, writeType)
//            }
//        }
//    }

    @SuppressLint("MissingPermission")
    @TargetApi(Build.VERSION_CODES.S)
    @Suppress("DEPRECATION")
    private fun BluetoothGatt.legacyCharacteristicWrite(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        writeType: Int
    ) {
        characteristic.writeType = writeType
        characteristic.value = value
        writeCharacteristic(characteristic)
    }

    @Deprecated("Deprecated for Android 13+")
    @Suppress("DEPRECATION")
    fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        with(characteristic) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    val readBytes = value // The byte array from the characteristic
                    val readString: String = readBytes.toHexString()

                    // If the byte array has at least 1 byte
                    if (readBytes.isNotEmpty()) {
                        val ledState = when (readBytes[0]) {
                            LEDStateEnum.LED_1.hex[0] -> LEDStateEnum.LED_1
                            LEDStateEnum.LED_2.hex[0] -> LEDStateEnum.LED_2
                            LEDStateEnum.LED_3.hex[0] -> LEDStateEnum.LED_3
                            else -> LEDStateEnum.NONE
                        }

                        Log.i(
                            "BluetoothGattCallback",
                            "Read characteristic $uuid:\n$readString, interpreted as $ledState"
                        )
                    } else {
                        Log.i(
                            "BluetoothGattCallback",
                            "Read characteristic $uuid:\n$readString"
                        )
                    }
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

    fun ByteArray.toHexString(): String =
        joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }


    @SuppressLint("MissingPermission")
    private fun writeToLEDCharacteristic(ledState: LEDStateEnum) {
        ledBluetoothGattCharacteristic?.let { characteristic ->
            characteristic.value = ledState.hex
            val isWriteInitiated = bluetoothGatt.writeCharacteristic(characteristic)
            if (isWriteInitiated) {
                onCharacteristicWrite(bluetoothGatt, characteristic, BluetoothGatt.GATT_SUCCESS)
            }
        }
    }

    fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        with(characteristic) {
            val value = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                characteristic.value
            } else {
                TODO("Get from cache somewhere as getValue is deprecated for Android 13+")
            }
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.i(
                        "BluetoothGattCallback",
                        "Wrote to characteristic $uuid | value: ${value.toHexString()}"
                    )
                }

                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                    Log.e("BluetoothGattCallback", "Write exceeded connection ATT MTU!")
                }

                BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                    Log.e("BluetoothGattCallback", "Write not permitted for $uuid!")
                }

                else -> {
                    Log.e(
                        "BluetoothGattCallback",
                        "Characteristic write failed for $uuid, error: $status"
                    )
                }
            }
        }
    }


}