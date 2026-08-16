package com.seasentry.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.seasentry.app.ui.SeaSentryScreen
import com.seasentry.app.ui.theme.SeaSentryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SeaSentryTheme {
                SeaSentryScreen()
            }
        }
    }
}
