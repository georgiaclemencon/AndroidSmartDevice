package com.example.androidsmartdevice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun DeviceDetail(deviceName: String?, deviceAddress: String?, deviceRssi: Int, onConnectClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text("Nom du périphérique : ${deviceName ?: "Inconnu"}")
        Text("Adresse MAC : $deviceAddress")
        Text("Force du signal (RSSI) : $deviceRssi dBm")
        Button(onClick = onConnectClick) {
            Text("Se connecter")
        }
    }
}





