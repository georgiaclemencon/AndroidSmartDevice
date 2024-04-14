// ScanActivity.kt
package com.example.androidsmartdevice

import BluetoothDisabledScreen
import BluetoothNotSupportedScreen
//import Device
import DisplayBluetoothStatus
import ScanActivityUI
import ScanComposableInteraction
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class ScanActivity : ComponentActivity() {

    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val isGranted = it.value
                if (isGranted) {
                    // Permission was granted. You can perform your operation here.
                } else {
                    // Permission was denied.

                }
            }
        }

    private lateinit var btManager: BluetoothManager
    private lateinit var bleScanManager: BleScanManager
    private val scanResults = mutableStateListOf<ScanResult>()


    private lateinit var scanInteraction: ScanComposableInteraction

    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


            btManager = getSystemService(BluetoothManager::class.java)
            bleScanManager = BleScanManager(btManager, 10000, scanCallback = BleScanCallback({ result ->
                if (result != null){
                    Log.d("BleScanner", "Nouveau résultat du scan : $result")
                    scanResults.add(result)
                }
            }))

        setContent {

            val isScanning by remember { mutableStateOf(false) }
            val hasBLEIssue = remember { mutableStateOf("") }
//            val scanResult = remember { mutableListOf<String>() }
//            val devices = remember { mutableStateOf(listOf<ScanResult>()) }
            val playPause = { /* define your function here */ }
            val onIconClick: (ScanResult) -> Unit = { /* define your function here */ }

            val isSquareIcon = remember { mutableStateOf(false) }

            scanInteraction = ScanComposableInteraction(
                isScanning = mutableStateOf(isScanning),
                isSquareIcon = isSquareIcon,
                deviceResults = this.scanResults,
                hasBLEIssue = hasBLEIssue,
                bleScanManager = bleScanManager,
            )

            checkBluetoothStatus(this, requestMultiplePermissionsLauncher, scanInteraction)

            // Use scanInteraction in your composable functions
            // For example:
            when (scanInteraction.hasBLEIssue.value) {
                "not_supported" -> BluetoothNotSupportedScreen()
                "disabled" -> BluetoothDisabledScreen()
                "enabled" -> ScanActivityUI(scanInteraction)
            }
        }
    }



//    private fun isBluetoothSupported(): BluetoothAdapter? {
//        return BluetoothAdapter.getDefaultAdapter()
//    }
//
//    private fun isBluetoothEnabled(bluetoothAdapter: BluetoothAdapter?): Boolean {
//        return bluetoothAdapter?.isEnabled ?: false
//    }
//
//    private fun requestBluetoothPermission(
//        context: Context,
//        requestMultiplePermissionsLauncher: ActivityResultLauncher<Array<String>>
//    ) {
//        val permissions = arrayOf(
//            Manifest.permission.BLUETOOTH,
//            Manifest.permission.BLUETOOTH_ADMIN,
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//        )
//        requestMultiplePermissionsLauncher.launch(permissions)
//    }


    @Composable
    private fun checkBluetoothStatus(
        context: Context,
        requestMultiplePermissionsLauncher: ActivityResultLauncher<Array<String>>,
        scanComposableInteraction: ScanComposableInteraction
    ): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            scanComposableInteraction.hasBLEIssue.value = "notSupported"
            DisplayBluetoothStatus(scanComposableInteraction)
            return false
        } else {
            if (!bluetoothAdapter.isEnabled) {
                scanComposableInteraction.hasBLEIssue.value = "disabled"
                DisplayBluetoothStatus(scanComposableInteraction)
                if (ContextCompat.checkSelfPermission(context,Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                ) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    context.startActivity(enableBtIntent)
                } else {
                    requestMultiplePermissionsLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH))
                }
                return false
            } else {
                scanComposableInteraction.hasBLEIssue.value = "enabled"
                DisplayBluetoothStatus(scanComposableInteraction)
                return areAllPermissionsGranted(context)
            }
        }
    }

    // Function to check if all the permissions are granted
    private fun areAllPermissionsGranted(context: Context): Boolean {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return permissions.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}

// **********SCAN BLE *********
@Composable
fun BleScannerResults(results: MutableList<ScanResult>) {
    val context = LocalContext.current
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // Request the missing permissions
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
            REQUEST_CODE_BLUETOOTH_PERMISSIONS
        )
    }

}
// Define a constant for the request code
const val REQUEST_CODE_BLUETOOTH_PERMISSIONS = 1001

//*****FIN SCAN BLE *********
