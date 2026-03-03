package com.paulosd.neblina.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.paulosd.neblina.ui.main.NeblinaApp
import com.paulosd.neblina.ui.theme.NeblinaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NeblinaTheme {
                NeblinaApp()
            }

        }
    }
}