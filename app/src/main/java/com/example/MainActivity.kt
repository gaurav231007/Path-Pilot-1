package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.ui.screens.PathPilotNativeApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PathPilotViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: PathPilotViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PathPilotNativeApp(viewModel)
            }
        }
    }
}
