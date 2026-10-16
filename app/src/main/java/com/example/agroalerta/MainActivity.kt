package com.example.agroalerta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.agroalerta.ui.theme.AgroAlertaTheme
import com.example.agroalerta.ui.theme.AlertsScreen
import com.example.agroalerta.ui.theme.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgroAlertaTheme {
                var currentScreen by remember { mutableStateOf("home") }
                var selectedCuartel by remember { mutableStateOf("Cuartel 1") }

                when (currentScreen) {
                    "home", "alerts", "settings" -> {
                        val activeTab = when (currentScreen) {
                            "alerts" -> 1
                            "settings" -> 2
                            else -> 0
                        }

                        HomeScreen(
                            selectedTab = activeTab,
                            onNavigateToInicio = { currentScreen = "home" },
                            onNavigateToAlertas = { currentScreen = "alerts" },
                            onNavigateToAjustes = { currentScreen = "settings" }
                        ) { paddingValues ->
                            when (currentScreen) {
                                "home" -> HomeContent(
                                    paddingValues = paddingValues,
                                    onCuartelSelected = { name ->
                                        selectedCuartel = name
                                        currentScreen = "detail"
                                    }
                                )
                                "alerts" -> Box(modifier = Modifier.padding(paddingValues)) {
                                    AlertsScreen()
                                }
                                "settings" -> Box(modifier = Modifier.padding(paddingValues)) {
                                    SettingsScreen()
                                }
                            }
                        }
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
                            cuartelName = selectedCuartel,
                            onBackClick = { currentScreen = "detail" }
                        )
                    }
                }
            }
        }
    }
}