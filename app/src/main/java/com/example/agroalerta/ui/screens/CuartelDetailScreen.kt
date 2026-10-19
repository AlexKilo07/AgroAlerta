package com.example.agroalerta.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.agroalerta.data.model.TelemetriaCuartel
import com.example.agroalerta.data.repository.AgroRepository
import com.example.agroalerta.ui.theme.*
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuartelDetailScreen(
    cuartelName: String = "Cuartel 1",
    onBackClick: () -> Unit = {},
    onNavigateToActuadores: () -> Unit = {},
    onNavigateToHistorial: () -> Unit = {}
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // NOTA: Se eliminó el LaunchedEffect que regeneraba la telemetría al entrar.

    // 1. Escuchar la telemetría en tiempo real desde el Repositorio
    val telemetriaMap by AgroRepository.telemetriaMap.collectAsState()

    // 2. Obtener los datos específicos del cuartel seleccionado
    val cuartelInfo = telemetriaMap[cuartelName] ?: TelemetriaCuartel(
        cuartelId = cuartelName,
        temperatura = "-- °C",
        humedadSuelo = "-- %",
        caudal = "-- L/min",
        radiacionSolar = "-- W/m²",
        humedadRelativa = "-- %"
    )

    // Obtención de coordenadas GPS reales, guardado en SQLite y navegación al Historial
    @SuppressLint("MissingPermission")
    fun obtenerUbicacionYRegistrar() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lng = location.longitude

                AgroRepository.registrarVisitaTerreno(cuartelName, lat, lng)

                Toast.makeText(
                    context,
                    "Visita registrada en $cuartelName",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val defaultLat = -33.7251
                val defaultLng = -70.7320
                AgroRepository.registrarVisitaTerreno(cuartelName, defaultLat, defaultLng)
                Toast.makeText(
                    context,
                    "Visita registrada (Predio Buin)",
                    Toast.LENGTH_SHORT
                ).show()
            }
            // Navegar directamente a la pantalla de Historial GPS tras registrar
            onNavigateToHistorial()
        }.addOnFailureListener {
            Toast.makeText(context, "Error al obtener señal GPS", Toast.LENGTH_SHORT).show()
        }
    }

    // Gestor de permisos para pedir acceso al GPS en tiempo de ejecución
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            obtenerUbicacionYRegistrar()
        } else {
            Toast.makeText(
                context,
                "Se requiere permiso GPS para registrar la visita",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cuartelName, fontWeight = FontWeight.Bold, color = AgroGreenPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = AgroGreenPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AgroBackground)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AgroBackground)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Botón de Acción Principal: GPS
            Button(
                onClick = {
                    val tienePermiso = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (tienePermiso) {
                        obtenerUbicacionYRegistrar()
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "GPS",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Registrar visita en terreno (GPS)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                text = "Telemetría en Tiempo Real",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AgroGreenPrimary
                )
            )

            // Grilla de Tarjetas de Sensores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SensorCard(
                    icon = Icons.Default.Thermostat,
                    iconTint = Color(0xFFE57373),
                    title = "Temperatura",
                    value = cuartelInfo.temperatura,
                    modifier = Modifier.weight(1f)
                )
                SensorCard(
                    icon = Icons.Default.WaterDrop,
                    iconTint = Color(0xFF64B5F6),
                    title = "Humedad Suelo",
                    value = cuartelInfo.humedadSuelo,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SensorCard(
                    icon = Icons.Default.Speed,
                    iconTint = Color(0xFF81C784),
                    title = "Caudal",
                    value = cuartelInfo.caudal,
                    modifier = Modifier.weight(1f)
                )
                SensorCard(
                    icon = Icons.Default.WbSunny,
                    iconTint = Color(0xFFFFB74D),
                    title = "Radiación Solar",
                    value = cuartelInfo.radiacionSolar,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SensorCard(
                    icon = Icons.Default.WaterDrop,
                    iconTint = Color(0xFF4DD0E1),
                    title = "Humedad Relativa",
                    value = cuartelInfo.humedadRelativa,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            // Acceso a Actuadores
            OutlinedButton(
                onClick = onNavigateToActuadores,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AgroGreenPrimary)
            ) {
                Text("Ver Estado de Actuadores (Bombas / Riego)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SensorCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                color = AgroTextMuted,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AgroTextDark
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CuartelDetailScreenPreview() {
    AgroAlertaTheme {
        CuartelDetailScreen()
    }
}