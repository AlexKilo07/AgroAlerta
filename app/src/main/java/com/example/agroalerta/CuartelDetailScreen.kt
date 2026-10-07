package com.example.agroalerta

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import com.example.agroalerta.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuartelDetailScreen(
    cuartelName: String = "Cuartel 1",
    onBackClick: () -> Unit = {},
    onNavigateToActuadores: () -> Unit = {}
) {
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
            // 1. Botón de Acción Principal: Registrar Visita GPS
            Button(
                onClick = { /* Acción GPS */ },
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

            // 2. Grilla de Tarjetas de Sensores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SensorCard(
                    icon = Icons.Default.Thermostat,
                    iconTint = Color(0xFFE57373),
                    title = "Temperatura",
                    value = "19.2 °C",
                    modifier = Modifier.weight(1f)
                )
                SensorCard(
                    icon = Icons.Default.WaterDrop,
                    iconTint = Color(0xFF64B5F6),
                    title = "Humedad Suelo",
                    value = "64 %",
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
                    value = "42 L/min",
                    modifier = Modifier.weight(1f)
                )
                SensorCard(
                    icon = Icons.Default.WbSunny,
                    iconTint = Color(0xFFFFB74D),
                    title = "Radiación Solar",
                    value = "620 W/m²",
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
                    value = "55 %",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(1f)) // Espacio para mantener el equilibrio visual
            }

            // 3. Botón para acceder al estado de los Actuadores
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