package com.example.agroalerta.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agroalerta.data.repository.AgroRepository
import com.example.agroalerta.ui.theme.*
import java.util.Locale

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    selectedTab: Int = 0,
    onCuartelSelected: (String) -> Unit = {},
    onNavigateToInicio: () -> Unit = {},
    onNavigateToAlertas: () -> Unit = {},
    onNavigateToAjustes: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(
                containerColor = AgroGreenLight,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { onNavigateToInicio() },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { onNavigateToAlertas() },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Alertas") },
                    label = { Text("Alertas") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { onNavigateToAjustes() },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                    label = { Text("Ajustes") }
                )
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Composable
fun HomeContent(
    paddingValues: PaddingValues,
    onCuartelSelected: (String) -> Unit
) {
    val telemetriaMap by AgroRepository.telemetriaMap.collectAsState()
    val actuadoresMap by AgroRepository.actuadoresMap.collectAsState()
    val visitasGps by AgroRepository.visitasGps.collectAsState()

    // Cálculo dinámico ajustado estrictamente a 4 cuarteles
    val totalCuarteles = 4
    val cuartelesEnRiesgo = telemetriaMap.values.count { it.tieneRiesgo }
    val bombasEncendidas = actuadoresMap.values.count { it.bombaEncendida }

    val ultimaVisita = visitasGps.firstOrNull()?.let {
        "${it.cuartel} (${it.fechaHora})"
    } ?: "Sin registros hoy"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(AgroBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AgroAlerta",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = AgroGreenPrimary
                )
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "En línea",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Sincronizado",
                        style = MaterialTheme.typography.labelSmall.copy(color = AgroTextMuted)
                    )
                }
            }
        }

        // Estado Global
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (cuartelesEnRiesgo > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = "Estado Global",
                    tint = if (cuartelesEnRiesgo > 0) Color(0xFFD9383A) else AgroGreenPrimary,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Estado global del predio",
                        style = MaterialTheme.typography.titleMedium.copy(color = AgroTextMuted)
                    )
                    Text(
                        text = if (cuartelesEnRiesgo > 0) "$cuartelesEnRiesgo Zonas en Riesgo" else "Normal (Óptimo)",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (cuartelesEnRiesgo > 0) Color(0xFFD9383A) else AgroGreenPrimary,
                            fontSize = 20.sp
                        )
                    )
                }
            }
        }

        // Selección de Cuarteles
        Text(
            text = "Seleccionar Cuartel",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = AgroGreenPrimary
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val c1 = telemetriaMap["Cuartel 1"]
            CuartelCard(
                title = "Cuartel 1",
                status = c1?.estadoGlobal ?: "Estado OK",
                modifier = Modifier.weight(1f),
                onClick = { onCuartelSelected("Cuartel 1") }
            )

            val c2 = telemetriaMap["Cuartel 2"]
            CuartelCard(
                title = "Cuartel 2",
                status = c2?.estadoGlobal ?: "Estado OK",
                modifier = Modifier.weight(1f),
                onClick = { onCuartelSelected("Cuartel 2") }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val c3 = telemetriaMap["Cuartel 3"]
            CuartelCard(
                title = "Cuartel 3",
                status = c3?.estadoGlobal ?: "Estado OK",
                modifier = Modifier.weight(1f),
                onClick = { onCuartelSelected("Cuartel 3") }
            )

            val c4 = telemetriaMap["Cuartel 4"]
            CuartelCard(
                title = "Cuartel 4",
                status = c4?.estadoGlobal ?: "Estado OK",
                modifier = Modifier.weight(1f),
                onClick = { onCuartelSelected("Cuartel 4") }
            )
        }

        // Resumen Promedio
        val cuartelesList = telemetriaMap.values.toList()
        fun extraerNumero(texto: String): Double = texto.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0

        val tempProm = if (cuartelesList.isNotEmpty()) String.format(Locale.US, "%.1f °C", cuartelesList.sumOf { extraerNumero(it.temperatura) } / cuartelesList.size) else "18.5 °C"
        val humProm = if (cuartelesList.isNotEmpty()) String.format(Locale.US, "%.0f %%", cuartelesList.sumOf { extraerNumero(it.humedadSuelo) } / cuartelesList.size) else "68 %"
        val caudalProm = if (cuartelesList.isNotEmpty()) String.format(Locale.US, "%.0f L/min", cuartelesList.sumOf { extraerNumero(it.caudal) } / cuartelesList.size) else "45 L/min"
        val radProm = if (cuartelesList.isNotEmpty()) String.format(Locale.US, "%.0f W/m²", cuartelesList.sumOf { extraerNumero(it.radiacionSolar) } / cuartelesList.size) else "650 W/m²"

        Text(
            text = "Resumen General del Predio (Promedio)",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = AgroGreenPrimary
            )
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricItem(
                        icon = Icons.Default.Thermostat,
                        iconTint = Color(0xFFE57373),
                        label = "Temp. Promedio",
                        value = tempProm,
                        modifier = Modifier.weight(1f)
                    )
                    MetricItem(
                        icon = Icons.Default.WaterDrop,
                        iconTint = Color(0xFF64B5F6),
                        label = "Humedad Suelo",
                        value = humProm,
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricItem(
                        icon = Icons.Default.Speed,
                        iconTint = Color(0xFF81C784),
                        label = "Caudal Riego",
                        value = caudalProm,
                        modifier = Modifier.weight(1f)
                    )
                    MetricItem(
                        icon = Icons.Default.WbSunny,
                        iconTint = Color(0xFFFFB74D),
                        label = "Radiación Solar",
                        value = radProm,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Centro de Control Operativo
        Text(
            text = "Centro de Control Operativo",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = AgroGreenPrimary
            )
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricItem(
                        icon = Icons.Default.Power,
                        iconTint = Color(0xFF4CAF50),
                        label = "Bombas Activas",
                        value = "$bombasEncendidas / $totalCuarteles encendidas",
                        modifier = Modifier.weight(1f)
                    )
                    MetricItem(
                        icon = Icons.Default.Shield,
                        iconTint = Color(0xFF2196F3),
                        label = "Zonas Protegidas",
                        value = "${totalCuarteles - cuartelesEnRiesgo} / $totalCuarteles en OK",
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricItem(
                        icon = Icons.Default.Warning,
                        iconTint = Color(0xFFFF9800),
                        label = "Alertas Activas",
                        value = "$cuartelesEnRiesgo pendientes",
                        modifier = Modifier.weight(1f)
                    )
                    MetricItem(
                        icon = Icons.Default.LocationOn,
                        iconTint = Color(0xFF9C27B0),
                        label = "Último Registro GPS",
                        value = ultimaVisita,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun MetricItem(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 11.sp, color = AgroTextMuted)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AgroTextDark)
        }
    }
}

@Composable
fun CuartelCard(
    title: String,
    status: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (status == "Estado OK") AgroStatusOk else Color(0xFFD9383A)
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AgroAlertaTheme {
        HomeScreen { padding ->
            HomeContent(paddingValues = padding, onCuartelSelected = {})
        }
    }
}