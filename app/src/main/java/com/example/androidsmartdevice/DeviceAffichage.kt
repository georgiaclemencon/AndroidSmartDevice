package com.example.androidsmartdevice

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import java.util.UUID

@Composable
fun DeviceDetail(
    deviceInteraction: MutableState<DeviceComposableInteraction>,
    onConnectClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text("Nom du périphérique : ${deviceInteraction.value.deviceTitle}")
        Button(onClick = onConnectClick) {
            Text("Se connecter")
        }

        DeviceActions(
            deviceInteraction
        ) { index -> deviceInteraction.value.LedArray[index].switchLed(index) }
    }
}

@Composable
fun DeviceActions(
    deviceInteraction: MutableState<DeviceComposableInteraction>,
    onLedToggle: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Row { // Utiliser Row pour afficher les ampoules en ligne
            deviceInteraction.value.LedArray.forEachIndexed { index, led ->
                DisplayBulbLogo(
                    onClick = { led.switchLed(index) },
                    onNotificationSubscribe = deviceInteraction.value.onNotificationSubscribe,
                    isOn = mutableStateOf(led.isOn)
                )
            }
        }
        DisplaySubscriptionPrompt(
            deviceInteraction = deviceInteraction,
            notificationCounter = deviceInteraction.value.notificationCounter,
            onNotificationSubscribe = deviceInteraction.value.onNotificationSubscribe
        )
    }
}


@Composable
fun DisplaySubscriptionPrompt(
    deviceInteraction: MutableState<DeviceComposableInteraction>,
    notificationCounter: String,
    onNotificationSubscribe: () -> Unit
) {
    // Create a state for the checkbox
    val checkedState = remember { mutableStateOf(false) }
    val ledControlState = remember { mutableStateOf(false) } // State for the new checkbox

    Column(modifier = Modifier.fillMaxSize()) { // Use fillMaxSize on the main container
        // Display the text
        Text("Abonnez-vous pour voir le nombre d'incrémentation")

        // Display the checkbox
        Checkbox(
            checked = checkedState.value,
            onCheckedChange = {
                checkedState.value = it
                if (it) onNotificationSubscribe()
            }
        )

        // Display the new text
        Text("Abonnez-vous pour piloter les leds")

        // Display the new checkbox
        Checkbox(
            checked = ledControlState.value,
            onCheckedChange = { ledControlState.value = it }
        )

        // Display the services and characteristics
        DisplayServicesAndCharacteristics(
            services = deviceInteraction.value.Services,
            onSubscribeClick = { _, _ -> },
            onWriteToService = { }
        )
    }
}





@Composable
fun DisplayBulbLogo(
    onClick: () -> Unit,
    onNotificationSubscribe: () -> Unit,
    isOn: MutableState<Boolean>
) {
    val imageResource =
        if (isOn.value) R.drawable.baseline_lightbulb_24 else R.drawable.ampoule_vide

    Image(
        painter = painterResource(id = imageResource),
        contentDescription = "Logo Ampoule",
        modifier = Modifier
            .size(100.dp)
            .clickable(onClick = {
                isOn.value = !isOn.value
                onClick()
                onNotificationSubscribe()
            })
    )
}

@Composable
fun DisplayServicesAndCharacteristics(
    services: List<BluetoothGattService>,
    onSubscribeClick: (BluetoothGattService, BluetoothGattCharacteristic) -> Unit,
    onWriteToService: (BluetoothGattService) -> Unit
) {
    if (services.isEmpty()) {
        Text("Aucun service disponible", color = Color.Red)
    } else {
        LazyColumn {
            items(services) { service ->
                val checkedState = remember { mutableStateOf(false) }
                Row {
                    Checkbox(
                        checked = checkedState.value,
                        onCheckedChange = { checked ->
                            checkedState.value = checked
                            if (checked) {
                                service.characteristics.forEach { characteristic ->
                                    onSubscribeClick(service, characteristic)
                                }
                                // Check if the service is the service 2
                                if (service.uuid == UUID.fromString("your-service-2-uuid")) {
                                    onWriteToService(service)
                                }
                            }
                        }
                    )
                    Text("Service UUID: ${service.uuid}", color = Color.Red)
                }
                service.characteristics.forEach { characteristic ->
                    Row {
                        Text("Characteristic UUID: ${characteristic.uuid}", color = Color.DarkGray)
                    }
                }
            }
        }
    }
}


class DeviceComposableInteraction(
    var IsConnected: Boolean = false,
    var deviceTitle: String = "",
    val LedArray: List<Led>,
    var notificationCounter: String = "",
    val onNotificationSubscribe: () -> Unit,
    val Services: MutableList<BluetoothGattService>,
    val serviceWithCharacteristics: MutableState<HashMap<UUID, List<UUID>> > = mutableStateOf(hashMapOf())
)


data class Led(var isOn: Boolean, val switchLed: (index: Int) -> Unit)