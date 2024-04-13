import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
                .clickable(onClick = scanInteraction::toggleButtonPlayScan)
        )

        Text(
            text = if (scanInteraction.isScanning.value) "Scanning..." else "Click the icon to start scanning",
            modifier = Modifier.padding(16.dp)
        )
        if (scanInteraction.isScanning.value) {
            LinearProgressIndicator(modifier = Modifier.padding(16.dp))
            DisplayDevices(scanInteraction.isScanning, scanInteraction.deviceResults.value)
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun DisplayBluetoothStatus(context: Context, status: String) {
    when (status) {
        "notSupported" -> BluetoothNotSupportedScreen()
        "disabled" -> BluetoothDisabledScreen()
        "enabled" -> {
            val scanInteraction = ScanComposableInteraction(
                mutableStateOf(false),
                mutableStateOf(false),
                mutableStateOf(emptyList()),
                mutableStateOf("")
            ) {}
            ScanActivityUI(scanInteraction)
        }
    }
}

// Display the list of connected devices
@Composable
fun DisplayDevices(isScanning: Boolean, devices: List<String>) {
    if (isScanning) {
        devices.forEach { device ->
            Text(text = device, modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun ScanActivityContent(scanInteraction: ScanComposableInteraction) {
    ScanActivityUI(scanInteraction)
}

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


data class Device(val name: String, val macAddress: String, val distance: Int)

val fakeDevices = listOf(
    Device("Device 1", "00:11:22:33:44:55", 10),
    Device("Device 2", "66:77:88:99:AA:BB", 20),
    Device("Device 3", "CC:DD:EE:FF:00:11", 30),
    Device("Device 4", "22:33:44:55:66:77", 40),
)

class ScanComposableInteraction(
    var isScanning: MutableState<Boolean>,
    var isSquareIcon: MutableState<Boolean>,
    var deviceResults: MutableState<List<Device>>, // Changed from List<String> to List<Device>
    val hasBLEIssue: MutableState<String>,
    val onIconClick: (ScanResult) -> Unit
) {
    fun toggleButtonPlayScan() {
        isSquareIcon.value = !isSquareIcon.value
        isScanning.value = !isScanning.value

        // If scanning is enabled, display the devices
        if (isScanning.value) {
            deviceResults.value = fakeDevices
        } else {
            deviceResults.value = emptyList()
        }
    }
}

@Composable
fun DisplayDevices(isScanning: MutableState<Boolean>, devices: List<Device>) {
    if (isScanning.value) {
        LazyColumn {
            items(devices) { device ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = device.name)
                        Text(text = "MAC Address: ${device.macAddress}")
                        DistanceIndicator(distance = device.distance)
                    }
                }
            }
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


