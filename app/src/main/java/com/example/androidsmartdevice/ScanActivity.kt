// ScanActivity.kt
package com.example.androidsmartdevice

//import Device
import BluetoothDisabledScreen
import BluetoothNotSupportedScreen
import ScanActivityUI
import ScanComposableInteraction
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.Serializable

import android.provider.Settings
import android.widget.Toast


class ScanActivity : ComponentActivity() {

    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val isGranted = it.value
                if (!isGranted) {
//                    Toast.makeText(this, "Permissions not granted by the user. " + it.key, Toast.LENGTH_SHORT).show()
                    // ask for permission
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(it.key),
                        REQUEST_CODE_BLUETOOTH_PERMISSIONS
                    )
                }
            }
        }
    // VOIR ET COMPARER AVEC LA FUN DU PROF

    private lateinit var btManager: BluetoothManager
    private lateinit var bleScanManager: BleScanManager
    private val scanResults = mutableStateListOf<ScanResult>()


    private lateinit var scanInteraction: ScanComposableInteraction


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        btManager = getSystemService(BluetoothManager::class.java)
        bleScanManager = BleScanManager(btManager, 10000, scanCallback = BleScanCallback({ result ->
            if (result != null) {
                Log.d("BleScanner", "Nouveau résultat du scan : $result")
                scanResults.add(result)
            }
        }))

        setContent {

            val isScanning = remember { mutableStateOf(false) }
            val hasBLEIssue = remember { mutableStateOf("") }
//            val scanResult = remember { mutableListOf<String>() }
//            val devices = remember { mutableStateOf(listOf<ScanResult>()) }
            val playPause = { /* define your function here */ }
            val onIconClick: (ScanResult) -> Unit = { /* define your function here */ }

            val isSquareIcon = remember { mutableStateOf(false) }

            scanInteraction = ScanComposableInteraction(
                isScanning = isScanning,
                isSquareIcon = isSquareIcon,
                deviceResults = this.scanResults,
                hasBLEIssue = hasBLEIssue,
                bleScanManager = bleScanManager,
            )




            checkBluetoothStatus(this, requestMultiplePermissionsLauncher, scanInteraction)
//            checkAndRequestLocation(this) // Add this line
            scanLeDeviceswithPermissions() // Add this line
            getRequiredPermissions() // Add this line
            DisplayBluetoothStatus(scanInteraction)

        }
    }




    private fun checkBluetoothStatus(
        context: Context,
        requestMultiplePermissionsLauncher: ActivityResultLauncher<Array<String>>,
        scanComposableInteraction: ScanComposableInteraction
    ): Serializable {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            scanComposableInteraction.hasBLEIssue.value = "notSupported"
            return false
        } else {
            if (!bluetoothAdapter.isEnabled) {
                scanComposableInteraction.hasBLEIssue.value = "disabled"
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    context.startActivity(enableBtIntent)
                } else {
                    requestMultiplePermissionsLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION))
                }
                return false
            } else {
                scanComposableInteraction.hasBLEIssue.value = "enabled"
                return areAllPermissionsGranted()
            }
        }
    }
    @SuppressLint("UnrememberedMutableState")
    @Composable
    fun DisplayBluetoothStatus(scanComposableInteraction: ScanComposableInteraction) {
        when (scanComposableInteraction.hasBLEIssue.value) {
            "not_supported" -> BluetoothNotSupportedScreen()
            "disabled" -> BluetoothDisabledScreen()
            "enabled" -> {
                ScanActivityUI(scanComposableInteraction)
            }
        }
    }

    private fun getRequiredPermissions(): Array<String> {
        var permissions = arrayOf(
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions = permissions.plus(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADMIN
                ))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions = permissions.plus(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
        return permissions
    }

    private fun scanLeDeviceswithPermissions() {
        if (areAllPermissionsGranted()) {
            bleScanManager.scanBleDevices()
        } else {
            requestMultiplePermissionsLauncher.launch(getRequiredPermissions())
        }
    }



    private fun areAllPermissionsGranted(): Boolean {
        val permissionsList = getRequiredPermissions()
        val retVal = permissionsList.all { permission ->
            ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
        Log.d("Permissions", "All permissions granted: $retVal")
        return retVal
    }

//    private fun isLocationEnabled(context: Context): Boolean {
//        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
//            LocationManager.NETWORK_PROVIDER
//        )
//    }
//
//    private fun checkAndRequestLocation(context: Context) {
//        if (!isLocationEnabled(context)) {
//            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//            context.startActivity(intent)
//        }
//    }




//fun bleScannerResults(results: MutableList<ScanResult>) {
//    if (!areAllPermissionsGranted()) {
//        // Request the missing permissions
//        ActivityCompat.requestPermissions(
//            this,
//            getRequiredPermissions(),
//            REQUEST_CODE_BLUETOOTH_PERMISSIONS
//        )
//    }
//}

    // Define a constant for the request code
    val REQUEST_CODE_BLUETOOTH_PERMISSIONS = 1001

}

// REVENIR SUR LES PERMISSIONS