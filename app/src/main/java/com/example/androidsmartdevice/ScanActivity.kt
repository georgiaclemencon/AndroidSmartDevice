// ScanActivity.kt
package com.example.androidsmartdevice

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView

class ScanActivity : ComponentActivity() {
    // Variable pour suivre l'état actuel de l'icône
    private var isSquareIcon = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        val title: TextView = findViewById(R.id.title)
        val scanIcon: ImageView = findViewById(R.id.scan_icon)
        val deviceList: ListView = findViewById(R.id.device_list)

        // Ajoutez un écouteur de clic à l'icône de scan
        scanIcon.setOnClickListener {
            // Changez l'icône à "square" ou "play arrow" lorsque l'utilisateur clique dessus
            if (isSquareIcon) {
                scanIcon.setImageResource(R.drawable.baseline_play_arrow_24)
            } else {
                scanIcon.setImageResource(R.drawable.baseline_square_24)
            }

            // Mettez à jour l'état de l'icône
            isSquareIcon = !isSquareIcon
        }

    }
}