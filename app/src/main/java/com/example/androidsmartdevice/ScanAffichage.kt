import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.androidsmartdevice.BleScanManager
import com.example.androidsmartdevice.R

@Composable
fun ScanActivityUI(
    scanInteraction: ScanComposableInteraction
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Scan Activity", modifier = Modifier.padding(16.dp))
        Image(
            painter = painterResource(
                id = if (scanInteraction.isSquareIcon.value) R.drawable.baseline_square_24 else R.drawable.baseline_play_arrow_24
            ),
            contentDescription = "Scan Icon",
            modifier = Modifier
                .size(150.dp)
                .padding(16.dp)
                .clickable(onClick = {
                    scanInteraction.toggleButtonPlayScan(scanInteraction.deviceResults)
                })
        )

        Text(
            text = if (scanInteraction.isScanning.value) "Scanning..." else "Click the icon to start scanning",
            modifier = Modifier.padding(16.dp)
        )
        if (scanInteraction.isScanning.value) {
            LinearProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
        DisplayDevices(scanInteraction.isScanning, scanInteraction.deviceResults)
//        DisplayTempDevice(arrayListOf())
    }
}


//
//@SuppressLint("UnrememberedMutableState")
//@Composable
//fun DisplayBluetoothStatus(scanComposableInteraction: ScanComposableInteraction) {
//    when (scanComposableInteraction.hasBLEIssue.value) {
//        "not_supported" -> BluetoothNotSupportedScreen()
//        "disabled" -> BluetoothDisabledScreen()
//        "enabled" -> {
//            ScanActivityUI(scanComposableInteraction)
//        }
//    }
//}

// Display the list of connected devices
//@Composable
//fun DisplayDevices(isScanning: Boolean, devices: List<String>) {
//    if (isScanning) {
//        devices.forEach { device ->
//            Text(text = device, modifier = Modifier.padding(16.dp))
//        }
//    }
//}

//@Composable
//fun ScanActivityContent(scanInteraction: ScanComposableInteraction) {
//    ScanActivityUI(scanInteraction)
//}

@Composable
fun BluetoothDisabledScreen() {
    val context = LocalContext.current
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
    val context = LocalContext.current
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


//data class Device(val name: String, val macAddress: String, val distance: Int)
//
//val fakeDevices = listOf(
//    Device("Device 1", "00:00:00:00:00:00", 5),
//    Device("Device 2", "00:00:00:00:00:01", 10),
//    Device("Device 3", "00:00:00:00:00:02", 15),
//    Device("Device 4", "00:00:00:00:00:03", 20)
//)

class ScanComposableInteraction(
    var isScanning: MutableState<Boolean>,
    var isSquareIcon: MutableState<Boolean>,
    var deviceResults: MutableList<ScanResult>,
    val hasBLEIssue: MutableState<String>,
//    val onIconClick: Unit,
    val bleScanManager: BleScanManager
) {
    fun toggleButtonPlayScan(scanResults: List<ScanResult>) {
        if (isScanning.value) {
            isScanning.value = false
            stopBleScan()
        } else {
            isScanning.value = true
            startBleScan()
        }
        isSquareIcon.value = !isSquareIcon.value

        // If scanning is enabled, display the devices
//        if (isScanning.value) {
//            deviceResults.addAll(scanResults)
//        } else {
//            deviceResults.clear()
//        }toggleButtonPlayScan
    }

    private fun startBleScan() {
        deviceResults.clear()
        bleScanManager.scanBleDevices()
    }

    private fun stopBleScan() {
        bleScanManager.stopBleScan()
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DisplayTempDevice(device: List<ScanResult>) {
    LazyColumn {
        items(device) { result ->
            Text(text = "Nom du périphérique : ${result.device.name ?: "Inconnu"}")
        }
    }
}


@Composable
@SuppressLint("MissingPermission")
fun DisplayDevices(isScanning: MutableState<Boolean>, results: MutableList<ScanResult>) {
    LazyColumn {
        // item { Text("Résultats du scan BLE :") }
        items(results) { result ->

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text("Nom du périphérique : ${result.device.name ?: "Inconnu"}")
                Text("Adresse MAC : ${result.device.address}")
                Text("Force du signal (RSSI) : ${result.rssi} dBm")
                Text("Services annoncés : ${result.scanRecord?.serviceUuids?.joinToString() ?: "Aucun"}")
                DistanceIndicator(result.rssi)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun DistanceIndicator(distance: Int) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(color = if (distance < 10) Color.Green else Color.Red),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$distance m",
            color = Color.White,

            )
    }
}


