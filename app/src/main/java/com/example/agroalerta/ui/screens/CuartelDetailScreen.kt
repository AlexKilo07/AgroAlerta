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
import androidx.compose.material.icons.filled.Power
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
    usuarioRol: String = "Usuario Administrador",
    onBackClick: () -> Unit = {},
    onNavigateToActuadores: () -> Unit = {},
    onNavigateToHistorial: () -> Unit = {}
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val esTecnico = usuarioRol == "Técnico en Mantenimiento"

    val telemetriaMap by AgroRepository.telemetriaMap.collectAsState()
    val cuartelInfo = telemetriaMap[cuartelName] ?: TelemetriaCuartel(
        cuartelId = cuartelName,
        temperatura = "-- °C",
        humedadSuelo = "-- %",
        caudal = "-- L/min",
        radiacionSolar = "-- W/m²",
        humedadRelativa = "-- %"
    )

    fun estadoSensor(cuartel: TelemetriaCuartel, tipo: String): Pair<String, Color> {
        val tieneFalla = when (tipo) {
            "Temp" -> cuartel.estadoGlobal.contains("Helada")
            "Hum" -> cuartel.estadoGlobal.contains("Humedad")
            "Caudal" -> cuartel.estadoGlobal.contains("Estanque")
            else -> false
        }
        return if (tieneFalla) "Revisión Requerida" to Color(0xFFD9383A) else "Buen Estado" to AgroGreenPrimary
    }

    @SuppressLint("MissingPermission")
    fun obtenerUbicacionYRegistrar() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                AgroRepository.registrarVisitaTerreno(cuartelName, location.latitude, location.longitude)
                Toast.makeText(context, "Visita registrada en $cuartelName", Toast.LENGTH_SHORT).show()
            } else {
                AgroRepository.registrarVisitaTerreno(cuartelName, -33.7251, -70.7320)
                Toast.makeText(context, "Visita registrada (Predio Buin)", Toast.LENGTH_SHORT).show()
            }
            onNavigateToHistorial()
        }.addOnFailureListener {
            Toast.makeText(context, "Error al obtener señal GPS", Toast.LENGTH_SHORT).show()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            obtenerUbicacionYRegistrar()
        } else {
            Toast.makeText(context, "Se requiere permiso GPS para registrar la visita", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cuartelName, fontWeight = FontWeight.Bold, color = AgroGreenPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver", tint = AgroGreenPrimary)
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
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = "GPS", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrar visita en terreno (GPS)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Text(
                text = if (esTecnico) "Estado de Sensores" else "Telemetría en Tiempo Real",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = AgroGreenPrimary)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val (estadoTemp, colorTemp) = estadoSensor(cuartelInfo, "Temp")
                SensorCard(
                    icon = Icons.Default.Thermostat,
                    iconTint = Color(0xFFE57373),
                    title = "Sensor Temperatura",
                    value = if (esTecnico) estadoTemp else cuartelInfo.temperatura,
                    valueColor = if (esTecnico) colorTemp else AgroTextDark,
                    modifier = Modifier.weight(1f)
                )

                val (estadoHum, colorHum) = estadoSensor(cuartelInfo, "Hum")
                SensorCard(
                    icon = Icons.Default.WaterDrop,
                    iconTint = Color(0xFF64B5F6),
                    title = "Sensor Humedad Suelo",
                    value = if (esTecnico) estadoHum else cuartelInfo.humedadSuelo,
                    valueColor = if (esTecnico) colorHum else AgroTextDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val (estadoCaudal, colorCaudal) = estadoSensor(cuartelInfo, "Caudal")
                SensorCard(
                    icon = Icons.Default.Speed,
                    iconTint = Color(0xFF81C784),
                    title = "Sensor Caudal",
                    value = if (esTecnico) estadoCaudal else cuartelInfo.caudal,
                    valueColor = if (esTecnico) colorCaudal else AgroTextDark,
                    modifier = Modifier.weight(1f)
                )

                SensorCard(
                    icon = Icons.Default.WbSunny,
                    iconTint = Color(0xFFFFB74D),
                    title = "Sensor Radiación",
                    value = if (esTecnico) "Buen Estado" else cuartelInfo.radiacionSolar,
                    valueColor = if (esTecnico) AgroGreenPrimary else AgroTextDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = onNavigateToActuadores,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary)
            ) {
                Icon(imageVector = Icons.Default.Power, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (esTecnico) "Estado de Actuadores" else "Panel de Actuadores",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
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
    valueColor: Color = AgroTextDark,
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
                Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontSize = 12.sp, color = AgroTextMuted, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}