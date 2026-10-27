package com.example.agroalerta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.agroalerta.data.local.AgroDatabaseHelper.SessionManager
import com.example.agroalerta.data.repository.AgroRepository
import com.example.agroalerta.ui.screens.ActuadoresScreen
import com.example.agroalerta.ui.screens.AlertsScreen
import com.example.agroalerta.ui.screens.CuartelDetailScreen
import com.example.agroalerta.ui.screens.HomeContent
import com.example.agroalerta.ui.screens.HomeScreen
import com.example.agroalerta.ui.screens.LoginScreen
import com.example.agroalerta.ui.screens.SettingsScreen
import com.example.agroalerta.ui.screens.VisitasHistorialScreen
import com.example.agroalerta.ui.theme.AgroAlertaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AgroRepository.init(applicationContext)

        enableEdgeToEdge()
        setContent {
            AgroAlertaTheme {
                val sesionActiva = remember { SessionManager.esSesionValida(applicationContext) }

                var currentScreen by remember {
                    mutableStateOf(if (sesionActiva) "home" else "login")
                }
                var selectedCuartel by remember { mutableStateOf("Cuartel 1") }

                var usuarioActualNombre by remember {
                    mutableStateOf(
                        if (sesionActiva) SessionManager.obtenerUsuarioNombre(applicationContext)
                        else "Administrador General"
                    )
                }
                var usuarioActualRol by remember {
                    mutableStateOf(
                        if (sesionActiva) SessionManager.obtenerUsuarioRol(applicationContext)
                        else "Usuario Administrador"
                    )
                }

                when (currentScreen) {
                    "login" -> {
                        LoginScreen(
                            onLoginSuccess = { nombre, rol ->
                                SessionManager.guardarSesion(applicationContext, nombre, rol)
                                usuarioActualNombre = nombre
                                usuarioActualRol = rol
                                currentScreen = "home"
                            }
                        )
                    }

                    "home", "alerts", "settings" -> {
                        val activeTab = when (currentScreen) {
                            "alerts" -> 1
                            "settings" -> 2
                            else -> 0
                        }

                        HomeScreen(
                            selectedTab = activeTab,
                            usuarioRol = usuarioActualRol,
                            onNavigateToInicio = { currentScreen = "home" },
                            onNavigateToAlertas = { currentScreen = "alerts" },
                            onNavigateToAjustes = { currentScreen = "settings" }
                        ) { paddingValues ->
                            when (currentScreen) {
                                "home" -> HomeContent(
                                    paddingValues = paddingValues,
                                    usuarioRol = usuarioActualRol,
                                    onCuartelSelected = { name ->
                                        selectedCuartel = name
                                        currentScreen = "detail"
                                    }
                                )

                                "alerts" -> Box(modifier = Modifier.padding(paddingValues)) {
                                    AlertsScreen()
                                }

                                "settings" -> Box(modifier = Modifier.padding(paddingValues)) {
                                    SettingsScreen(
                                        usuarioNombre = usuarioActualNombre,
                                        usuarioRol = usuarioActualRol,
                                        onLogoutClick = {
                                            SessionManager.cerrarSesion(applicationContext)
                                            currentScreen = "login"
                                        }
                                    )
                                }
                            }
                        }
                    }

                    "detail" -> {
                        CuartelDetailScreen(
                            cuartelName = selectedCuartel,
                            usuarioRol = usuarioActualRol,
                            onBackClick = { currentScreen = "home" },
                            onNavigateToActuadores = { currentScreen = "actuadores" },
                            onNavigateToHistorial = { currentScreen = "historial" }
                        )
                    }

                    "actuadores" -> {
                        ActuadoresScreen(
                            cuartelName = selectedCuartel,
                            usuarioRol = usuarioActualRol,
                            onBackClick = { currentScreen = "detail" }
                        )
                    }

                    "historial" -> {
                        VisitasHistorialScreen(
                            cuartelFilter = selectedCuartel,
                            onBackClick = { currentScreen = "detail" }
                        )
                    }
                }
            }
        }
    }
}