package com.example.androidsmartdevice

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun DeviceDetail(deviceInteraction: DeviceComposableInteraction, onConnectClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text("Nom du périphérique : ${deviceInteraction.deviceTitle}")
        Button(onClick = onConnectClick) {
            Text("Se connecter")
        }
        DeviceActions(
            deviceInteraction,
            onLedToggle = { index -> deviceInteraction.LedArray[index].switchLed(index) })
    }
}

@Composable
fun DeviceActions(deviceInteraction: DeviceComposableInteraction, onLedToggle: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Row { // Utiliser Row pour afficher les ampoules en ligne
            deviceInteraction.LedArray.forEachIndexed { index, led ->
                DisplayBulbLogo(onClick = { led.switchLed(index) }, onNotificationSubscribe = deviceInteraction.onNotificationSubscribe, isOn = mutableStateOf(led.isOn))
            }
        }
        DisplaySubscriptionPrompt(
            deviceInteraction.notificationCounter,
            deviceInteraction.onNotificationSubscribe
        )
    }
}


@Composable
fun DisplaySubscriptionPrompt(notificationCounter: String, onNotificationSubscribe: () -> Unit) {
    // Create a state for the checkbox
    val checkedState = remember { mutableStateOf(false) }

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

    // Display the counter
    if (checkedState.value) {
        Text("Nombre d'incrémentation : $notificationCounter")
    }
}

@Composable
fun DisplayBulbLogo(onClick: () -> Unit, onNotificationSubscribe: () -> Unit, isOn: MutableState<Boolean>) {
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



class DeviceComposableInteraction(
    var IsConnected: Boolean = false,
    var deviceTitle: String = "",
    val LedArray: List<Led>,
    var notificationCounter: String = "",
    val onNotificationSubscribe: () -> Unit
)

data class Led(var isOn: Boolean, val switchLed: (index: Int) -> Unit)