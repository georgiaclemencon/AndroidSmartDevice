package com.example.androidsmartdevice

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
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
    private val CCC_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb"


    @SuppressLint("UnrememberedMutableState")
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
                    val counter = remember { mutableStateOf(0) }

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
                        Services = mutableListOf(),
                        counter = mutableStateOf("0"),
                        subscribeToCounter = {
                            subscribeToCounter()
                        },
                        unsubscribeToCounter = {
                            unsubscribeToCounter()
                        }
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

            @Deprecated("Deprecated for Android 13+")
            @Suppress("DEPRECATION")
            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                with(characteristic) {
                    when (status) {
                        BluetoothGatt.GATT_SUCCESS -> {
                            Log.i(
                                "BluetoothGattCallback",
                                "Read characteristic $uuid:\n${value.toHexString()}"
                            )
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

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
                status: Int
            ) {
                val uuid = characteristic.uuid
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i(
                            "BluetoothGattCallback",
                            "Read characteristic $uuid:\n${value.toHexString()}"
                        )
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


            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val gattTable = gatt.getGattTable()
                    gattTable.forEach { (serviceUuid, characteristicUuids) ->
                        Log.i("BluetoothGatt", "Service UUID: $serviceUuid")
                        deviceInteraction.serviceWithCharacteristics.value[serviceUuid] =
                            characteristicUuids
                        Log.i(
                            "BluetoothGatt",
                            "deviceInteraction: ${deviceInteraction.serviceWithCharacteristics}"
                        )
                    }
                    // Add the discovered services to deviceInteraction.Services
                    deviceInteraction.Services.addAll(gatt.services)
                } else {
                    Log.w("BluetoothGatt", "onServicesDiscovered received: $status")
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                with(characteristic) {
                    Log.i(
                        "BluetoothGattCallback",
                        "Characteristic $uuid changed | value: ${value.toHexString()}"
                    )
                    deviceInteraction.counter.value = value.toHexString()
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                val newValueHex = value.toHexString()
                with(characteristic) {
                    Log.i(
                        "BluetoothGattCallback",
                        "Characteristic $uuid changed | value: $newValueHex"
                    )

                    deviceInteraction.counter.value = value.toHexString()
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
            readCounterCharacteristic()
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


    fun ByteArray.toHexString(): String =
        joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }


    @SuppressLint("MissingPermission")
    private fun writeToLEDCharacteristic(ledState: LEDStateEnum) {
        val serviceUUID = UUID.fromString("0000feed-cc7a-482a-984a-7f2ed5b3e58f")
        val service: BluetoothGattService? = bluetoothGatt.getService(serviceUUID)
        val characteristic: BluetoothGattCharacteristic? =
            service?.characteristics?.get(0) // Obtenez la première caractéristique

        characteristic?.let {
            it.value = ledState.hex
            val isWriteInitiated = bluetoothGatt.writeCharacteristic(it)
            if (isWriteInitiated) {
                onCharacteristicWrite(bluetoothGatt, it, BluetoothGatt.GATT_SUCCESS)
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
                    if (value != null) {
                        Log.i(
                            "BluetoothGattCallback",
                            "Wrote to characteristic $uuid | value: ${value.toHexString()}"
                        )
                    } else {
                        Log.i("BluetoothGattCallback", "Wrote to characteristic $uuid")
                    }
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


    @SuppressLint("MissingPermission")
    private fun readCounterCharacteristic() {
        // Check if the Bluetooth connection is established
        if (bluetoothGatt != null) {
            // LED S:2 C:0
            // Counter S:2 C:1
            // Get the service at index 2
            val service = bluetoothGatt.services.getOrNull(2)
            if (service != null) {
                // Get the characteristic at index 3
                val counterCharacteristic = service.characteristics.getOrNull(1)
                if (counterCharacteristic != null) {
                    enableNotifications(
                        counterCharacteristic
                    )

                } else {
                    Log.e("DeviceActivity", "Counter characteristic is null")
                }
            } else {
                Log.e("DeviceActivity", "Service is null")
            }
//
//            var services = bluetoothGatt.services
//            services.forEach { service ->
//                Log.e("DeviceActivity", "Service[${services.indexOf(service)}]: ${service.uuid}")
//                var characteristics = service.characteristics
//                characteristics.forEach { characteristic ->
//                    Log.e(
//                        "DeviceActivity",
//                        "Characteristic[${characteristics.indexOf(characteristic)}]: ${characteristic.uuid}"
//                    )
//                    if (characteristic.isReadable()) { // Vérifiez si la caractéristique est lisible
//                        if (service.characteristics.contains(characteristic)) { // Vérifiez si la caractéristique appartient à ce service
//                            enableNotifications(
//                                characteristic,
//                                characteristic.uuid.toString()
//                            )
//                            val isReadInitiated = bluetoothGatt.readCharacteristic(characteristic)
//                            if (isReadInitiated) { // Vérifiez si la lecture de la caractéristique a été initiée avec succès
//                                Log.e(
//                                    "DeviceActivity",
//                                    "read : $isReadInitiated"
//                                )
//                                enableNotifications(
//                                    characteristic,
//                                    characteristic.uuid.toString()
//                                )
//                            } else {
//                                Log.e("DeviceActivity", "Failed to initiate read operation")
//                            }
//
//
//                        } else {
//                            Log.e(
//                                "DeviceActivity",
//                                "Characteristic does not belong to this service"
//                            )
//                        }
//                    } else {
//                        Log.e("DeviceActivity", "Characteristic is not readable")
//                    }
//                }
//            }
        } else {
            Log.e("DeviceActivity", "BluetoothGatt connection is not established")
        }

    }

    fun BluetoothGattCharacteristic.isReadable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

    fun BluetoothGattCharacteristic.isWritable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)


//SABONNER

    fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

    fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

    fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean =
        properties and property != 0


    @SuppressLint("MissingPermission")
    fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray) {
        bluetoothGatt.let { gatt ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(descriptor, payload)
            } else {
                // Fall back to deprecated version of writeDescriptor for Android <13
                gatt.legacyDescriptorWrite(descriptor, payload)
            }
        }
    }

    @SuppressLint("MissingPermission")
    @TargetApi(Build.VERSION_CODES.S)
    @Suppress("DEPRECATION")
    private fun BluetoothGatt.legacyDescriptorWrite(
        descriptor: BluetoothGattDescriptor,
        value: ByteArray
    ) {
        descriptor.value = value
        writeDescriptor(descriptor)
    }

    @SuppressLint("MissingPermission")
    fun enableNotifications(characteristic: BluetoothGattCharacteristic) {
        val cccdUuid = UUID.fromString(CCC_DESCRIPTOR_UUID)
        val payload = when {
            characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> {
                Log.e(
                    "ConnectionManager",
                    "${characteristic.uuid} doesn't support notifications/indications"
                )
                return
            }
        }

        characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
            if (bluetoothGatt.setCharacteristicNotification(characteristic, true) == false) {
                Log.e(
                    "ConnectionManager",
                    "setCharacteristicNotification failed for ${characteristic.uuid}"
                )
                return
            }
            writeDescriptor(cccDescriptor, payload)
        } ?: Log.e(
            "ConnectionManager",
            "${characteristic.uuid} doesn't contain the CCC descriptor!"
        )
    }

    fun subscribeToCounter() {
        val serviceUUID = UUID.fromString("0000feed-cc7a-482a-984a-7f2ed5b3e58f")
        val service: BluetoothGattService? = bluetoothGatt.getService(serviceUUID)
        val characteristic: BluetoothGattCharacteristic? =
            service?.characteristics?.get(1) // Obtenez la première caractéristique

        characteristic?.let {
            enableNotifications(it)
        }
    }

    fun unsubscribeToCounter() {
        val serviceUUID = UUID.fromString("0000feed-cc7a-482a-984a-7f2ed5b3e58f")
        val service: BluetoothGattService? = bluetoothGatt.getService(serviceUUID)
        val characteristic: BluetoothGattCharacteristic? =
            service?.characteristics?.get(1) // Obtenez la première caractéristique

        characteristic?.let {
            disableNotifications(it)
        }
    }
    @SuppressLint("MissingPermission")
    fun disableNotifications(characteristic: BluetoothGattCharacteristic) {
        if (!characteristic.isNotifiable() && !characteristic.isIndicatable()) {
            Log.e("ConnectionManager", "${characteristic.uuid} doesn't support indications/notifications")
            return
        }

        val cccdUuid = UUID.fromString(CCC_DESCRIPTOR_UUID)
        characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
            if (bluetoothGatt?.setCharacteristicNotification(characteristic, false) == false) {
                Log.e("ConnectionManager", "setCharacteristicNotification failed for ${characteristic.uuid}")
                return
            }
            writeDescriptor(cccDescriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
        } ?: Log.e("ConnectionManager", "${characteristic.uuid} doesn't contain the CCC descriptor!")
    }

}


