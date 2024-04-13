// ScanActivity.kt
package com.example.androidsmartdevice

import BluetoothDisabledScreen
import BluetoothNotSupportedScreen
import Device
import DisplayBluetoothStatus
import ScanActivityUI
import ScanComposableInteraction
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

    private lateinit var scanInteraction: ScanComposableInteraction

    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {

            val isScanning by remember { mutableStateOf(false) }
            val hasBLEIssue = remember { mutableStateOf("") }
            val scanResult = remember { mutableListOf<String>() }
            val devices = remember { mutableStateOf(listOf<Device>()) }
            val playPause = { /* define your function here */ }
            val onIconClick: (ScanResult) -> Unit = { /* define your function here */ }

            val isSquareIcon = remember { mutableStateOf(false) }

            scanInteraction = ScanComposableInteraction(
                isScanning = mutableStateOf(isScanning),
                isSquareIcon = isSquareIcon,
                deviceResults = devices,
                hasBLEIssue = hasBLEIssue,
                onIconClick = onIconClick
            )

            checkBluetoothStatus(this, requestMultiplePermissionsLauncher, scanInteraction)

            // Use scanInteraction in your composable functions
            // For example:
            ScanActivityUI(scanInteraction)
        }
    }

    private fun isBluetoothSupported(): BluetoothAdapter? {
        return BluetoothAdapter.getDefaultAdapter()
    }

    private fun isBluetoothEnabled(bluetoothAdapter: BluetoothAdapter?): Boolean {
        return bluetoothAdapter?.isEnabled ?: false
    }

    private fun requestBluetoothPermission(
        context: Context,
        requestMultiplePermissionsLauncher: ActivityResultLauncher<Array<String>>
    ) {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        requestMultiplePermissionsLauncher.launch(permissions)
    }


    @Composable
    private fun checkBluetoothStatus(
        context: Context, requestMultiplePermissionsLauncher: ActivityResultLauncher<Array<String>>,
        scanComposableInteraction: ScanComposableInteraction
    ): Boolean {
        val bluetoothAdapter = isBluetoothSupported()
        if (bluetoothAdapter == null) {
            scanComposableInteraction.hasBLEIssue.value =
                "not_supported" // error. Bluetooth not supported
            BluetoothNotSupportedScreen()
            return false
        } else {
            if (!isBluetoothEnabled(bluetoothAdapter)) {
                scanComposableInteraction.hasBLEIssue.value = "disabled"
                BluetoothDisabledScreen()
                requestBluetoothPermission(context, requestMultiplePermissionsLauncher)
                return false
            } else {
                scanComposableInteraction.hasBLEIssue.value = "enabled" // Bluetooth is enabled
                val allPermissionsGranted = areAllPermissionsGranted(context)
                if (!allPermissionsGranted) {
                    requestBluetoothPermission(context, requestMultiplePermissionsLauncher)
                }
                return allPermissionsGranted
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
