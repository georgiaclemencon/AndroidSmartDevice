package com.example.androidsmartdevice

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
        TestChart()
        MyComposable(deviceInteraction.value.realTimeSpeed) // Affiche un graphique
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
        LaunchedEffect(accelerometerData.value) {
            modelProducer.tryRunTransaction {
                lineSeries {
                    series(accelerometerData.value)
                }
            }
        }
        CartesianChartHost(
            rememberCartesianChart(
                rememberLineCartesianLayer(),
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
            ),
            modelProducer,
        )
    }
}


// Modifier MyComposable pour prendre realTimeSpeed comme argument
@SuppressLint("UnrememberedMutableState")
@Composable
fun MyComposable(realTimeSpeed: MutableState<Float>) {
    Log.d("MyComposable", "Called with realTimeSpeed: ${realTimeSpeed.value}") // Log when MyComposable is called
    val lineChart = createLineChart(mutableStateOf(listOf(realTimeSpeed.value.toInt())))
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
    var realTimeSpeed: MutableState<Float> = mutableStateOf(0f), // Change val to var
    val speedValues: MutableList<Float> = mutableListOf() // List to store all speed values
)