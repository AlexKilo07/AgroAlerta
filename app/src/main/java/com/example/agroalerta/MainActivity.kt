package com.example.agroalerta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.agroalerta.ui.theme.AgroAlertaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgroAlertaTheme {
                var currentScreen by remember { mutableStateOf("home") }
                var selectedCuartel by remember { mutableStateOf("Cuartel 1") }

                when (currentScreen) {
                    "home" -> {
                        HomeScreen(
                            onCuartelSelected = { name ->
                                selectedCuartel = name
                                currentScreen = "detail"
                            }
                        )
                    }
                    "detail" -> {
                        CuartelDetailScreen(
                            cuartelName = selectedCuartel,
                            onBackClick = { currentScreen = "home" },
                            onNavigateToActuadores = { currentScreen = "actuadores" }
                        )
                    }
                    "actuadores" -> {
                        ActuadoresScreen(
                            onBackClick = { currentScreen = "detail" }
                        )
                    }
                }
            }
        }
    }
}