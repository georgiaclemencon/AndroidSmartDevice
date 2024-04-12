// ScanActivity.kt
package com.example.androidsmartdevice

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
                ScanActivityContent()

            }
        }
    }


}
// Composable function to display the content of the Scan Activity

@Composable
fun ScanActivityContent() {
    val isSquareIcon = remember { mutableStateOf(false) }
    val isScanning = remember { mutableStateOf(false) }
    ScanActivityUI(isSquareIcon, isScanning, ::toggleButtonPlayScan)
}

// Function to toggle the play/scan button
private fun toggleButtonPlayScan(
    isSquareIcon: MutableState<Boolean>,
    isScanning: MutableState<Boolean>
) {
    isSquareIcon.value = !isSquareIcon.value
    isScanning.value = !isScanning.value
}



// Function to check if all the permissions are granted
fun areAllPermissionsGranted(context: Context): Boolean {
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

@Composable
fun checkBluetoothStatus(
    context: Context, requestMultiplePermissionsLauncher: ActivityResultLauncher<Array<String>>,
): Boolean {
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    if (bluetoothAdapter == null) {
        // Device doesn't support Bluetooth
        Toast.makeText(context, "Votre appareil ne supporte pas le Bluetooth", Toast.LENGTH_SHORT)
            .show()
        BluetoothNotSupportedScreen()
        return false
    } else {
        if (!bluetoothAdapter.isEnabled) {
            // Bluetooth is not enabled
            Toast.makeText(context, "Bluetooth désactivé", Toast.LENGTH_SHORT).show()
            BluetoothDisabledScreen()
            // Check if the permission is granted
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is granted, open Bluetooth settings
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                context.startActivity(enableBtIntent)
            } else {
                // Permission is not granted, request the permission
                requestMultiplePermissionsLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH))
            }
            return false
        } else {
            // Bluetooth is enabled
            Toast.makeText(context, "Bluetooth activé", Toast.LENGTH_SHORT).show()
            // Check if all permissions are granted
            val allPermissionsGranted = areAllPermissionsGranted(context)
            if (!allPermissionsGranted) {
                // Not all permissions are granted, request the permissions
                requestMultiplePermissionsLauncher.launch(arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }
            return allPermissionsGranted
        }
    }
}

// Display the list of connected devices
@Composable
fun DisplayDevices(isScanning: MutableState<Boolean>, devices: List<String>) {
    if (isScanning.value) {
        devices.forEach { device ->
            Text(text = device, modifier = Modifier.padding(16.dp))
        }
    }
}

// UI of the Scan Activity
@Composable
fun ScanActivityUI(
    isSquareIcon: MutableState<Boolean>,
    isScanning: MutableState<Boolean>,
    onIconClick: (MutableState<Boolean>, MutableState<Boolean>) -> Unit
) {
    val devices =
        listOf("Device 1", "Device 2", "Device 3") // This is the fictive list of connected devices

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Scan Activity", modifier = Modifier.padding(16.dp))
        Image(
            painter = painterResource(
                id = if (isSquareIcon.value) R.drawable.baseline_square_24 else R.drawable.baseline_play_arrow_24
            ),
            contentDescription = "Scan Icon",
            modifier = Modifier
                .size(150.dp)
                .padding(16.dp)
                .clickable {
                    onIconClick(isSquareIcon, isScanning)
                }
        )
        Text(text = "Lancer le scan", modifier = Modifier.padding(16.dp))
        if (isScanning.value) {
            LinearProgressIndicator(modifier = Modifier.padding(16.dp))
        }
        DisplayDevices(isScanning, devices) // Pass the devices list here
    }
}

@Composable
fun BluetoothDisabledScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.baseline_bluetooth_disabled_24),
            contentDescription = "Bluetooth Disabled Logo",
            modifier = Modifier.size(150.dp)
        )
        Text(text = "Votre Bluetooth n'est pas activé", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun BluetoothNotSupportedScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.baseline_bluetooth_disabled_24),
            contentDescription = "Bluetooth Not Supported Logo",
            modifier = Modifier.size(150.dp)
        )
        Text(
            text = "Votre appareil ne supporte pas le Bluetooth",
            modifier = Modifier.padding(16.dp)
        )
    }
}