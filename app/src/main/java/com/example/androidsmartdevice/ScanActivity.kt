// ScanActivity.kt
package com.example.androidsmartdevice

import DisplayBluetoothStatus
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
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
                    // Permission was denied. You can notify the user that the operation cannot be performed.
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isBluetoothEnabled = checkBluetoothStatus(this, requestMultiplePermissionsLauncher)
            if (isBluetoothEnabled) {
                // Initialiser la liste des appareils et les interactions avec le bouton si le Bluetooth est activé

            }
        }
    }


    @Composable
    private fun checkAndRequestPermissions(
        context: Context,
        requestMultiplePermissionsLauncher: ActivityResultLauncher<Array<String>>
    ): Boolean {
        val allPermissionsGranted = areAllPermissionsGranted(context)
        if (!allPermissionsGranted) {
            requestMultiplePermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        return allPermissionsGranted
    }

    @Composable
    private fun checkBluetoothStatus(
        context: Context,
        requestMultiplePermissionsLauncher: ActivityResultLauncher<Array<String>>
    ): Boolean {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            DisplayBluetoothStatus(context, "notSupported")
            return false
        } else {
            if (!bluetoothAdapter.isEnabled) {
                DisplayBluetoothStatus(context, "disabled")
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    context.startActivity(enableBtIntent)
                } else {
                    requestMultiplePermissionsLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH))
                }

                return false
            } else {
                DisplayBluetoothStatus(context, "enabled")
                return checkAndRequestPermissions(context, requestMultiplePermissionsLauncher)
            }
        }
    }


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

