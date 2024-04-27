package com.example.androidsmartdevice

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import kotlinx.coroutines.delay


@SuppressLint("UnrememberedMutableState")
@Composable
fun DeviceDetail(
    deviceActivity: DeviceActivity,
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
        DisplayRealTimeSpeed(deviceInteraction) // Affiche la vitesse en temps réel
        DisplayAverageSpeed(deviceActivity, deviceInteraction)
        //Stopwatch() // Affiche un chronomètre
        TestChart() // Assurez-vous que cette ligne est présente pour afficher le deuxième graphique
        MyComposable(deviceInteraction.value) // Affiche un graphique
    }
}

@Composable
fun DisplayAverageSpeed(deviceActivity: DeviceActivity, deviceInteraction: MutableState<DeviceComposableInteraction>) {
    var averageSpeed by remember { mutableStateOf(0f) }


    LaunchedEffect(key1 = true) {
        while (true) {
            averageSpeed = deviceActivity.calculateAverageSpeed()
            delay(5000L) // delay for 5 seconds
        }
    }

    Text("Vitesse moyenne : ${averageSpeed.toInt()}")
    Log.e("Average Speed", "Average Speed: $averageSpeed")
}

@Composable
fun DisplayRealTimeSpeed(deviceInteraction: MutableState<DeviceComposableInteraction>) {
    val speed = deviceInteraction.value.realTimeSpeed.value
    LaunchedEffect(speed) {
        // This block will be recomposed whenever speed changes
    }
    Text("Vitesse en temps réel : ${speed.toInt()}")
}

fun createLineChart(accelerometerData: MutableState<List<Int>>): @Composable () -> Unit {
    return {
        val modelProducer = remember { CartesianChartModelProducer.build() }
        val listSize = remember { mutableStateOf(accelerometerData.value.size) }

        LaunchedEffect(listSize.value) {
            modelProducer.tryRunTransaction {
                lineSeries {
                    series(accelerometerData.value)
                }
            }
        }

        // Update listSize whenever accelerometerData changes
        LaunchedEffect(accelerometerData.value) {
            listSize.value = accelerometerData.value.size
        }

        Column {
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                ),
                modelProducer
            )

            // Your legend here
           CustomLegend(
    colors = listOf(androidx.compose.ui.graphics.Color.Red, androidx.compose.ui.graphics.Color(android.graphics.Color.GREEN), androidx.compose.ui.graphics.Color.Blue),
    labels = listOf("Label 1", "Label 2", "Label 3")
)

        }
    }
}


@Composable
fun CustomLegend(colors: List<androidx.compose.ui.graphics.Color>, labels: List<String>) {
    Row {
        colors.zip(labels).forEach { (color, label) ->
            LegendItem(color = color, text = label)
        }
    }
}



@Composable
fun LegendItem(color: androidx.compose.ui.graphics.Color, text: String) {
    Row(Modifier.padding(horizontal = 8.dp)) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(color = color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text)
    }
}


@SuppressLint("UnrememberedMutableState")
@Composable
fun MyComposable(deviceInteraction: DeviceComposableInteraction) {
    Log.d("MyComposable", "Called with realTimeSpeed: ${deviceInteraction.realTimeSpeed}") // Log when MyComposable is called
    val speedValues = if (deviceInteraction.speedValues.value.isEmpty()) {
        mutableStateOf(listOf(0)) // Use a list with a single zero if speedValues is empty
    } else {
        deviceInteraction.speedValues // Use speedValues if it's not empty
    }
    val lineChart = createLineChart(speedValues) // Use speedValues for the chart data

    lineChart()
}

// Create a test chart with static data
@SuppressLint("UnrememberedMutableState")
@Composable
fun TestChart() {
    val testData = mutableStateOf(listOf(1, 2, 3, 4, 5))
    val lineChart = createLineChart(testData)
    lineChart()
}


@Composable
fun Stopwatch() {
    var time by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            time++
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Time: $time", modifier = Modifier.padding(16.dp))
        Button(onClick = { isRunning = !isRunning }) {
            Text(if (isRunning) "Stop" else "Start")
        }
    }
}
class DeviceComposableInteraction(
    var IsConnected: Boolean = false,
    var deviceTitle: String = "",
    var realTimeSpeed: MutableState<Float> = mutableStateOf(0f),
    var speedValues: MutableState<List<Int>> = mutableStateOf(listOf()) // List to store all speed values as Int
)