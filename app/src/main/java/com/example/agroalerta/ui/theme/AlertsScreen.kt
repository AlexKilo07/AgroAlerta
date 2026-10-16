package com.example.agroalerta.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AlertModel(
    val id: Int,
    val type: String,
    val isWarning: Boolean
)

data class HistoryEventModel(
    val id: Int,
    val date: String,
    val title: String,
    val status: String,
    val icon: ImageVector,
    val iconTint: Color
)

@Composable
fun AlertsScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    // Lista mutable interactiva
    val activeAlerts = remember {
        mutableStateListOf(
            AlertModel(1, "Riesgo de Helada", isWarning = true),
            AlertModel(2, "Riesgo de Helada", isWarning = true),
            AlertModel(3, "Fuga en Cañería Principal", isWarning = false),
            AlertModel(4, "Bajo Nivel de Estanque", isWarning = false)
        )
    }

    val historyEvents = remember {
        listOf(
            HistoryEventModel(
                id = 1,
                date = "16/10 14:20",
                title = "Alerta de Helada en Cuartel 2",
                status = "Atendida.",
                icon = Icons.Default.WarningAmber,
                iconTint = Color(0xFFD9383A)
            ),
            HistoryEventModel(
                id = 2,
                date = "15/10 09:15",
                title = "Bajo nivel de estanque",
                status = "Resuelto.",
                icon = Icons.Default.WaterDrop,
                iconTint = AgroGreenPrimary
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AgroBackground)
            .padding(16.dp)
    ) {
        Text(
            text = if (selectedTab == 0) "Alertas Activas" else "Historial",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = AgroGreenPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F4F1), shape = RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Button(
                onClick = { selectedTab = 0 },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 0) AgroGreenPrimary else Color.Transparent,
                    contentColor = if (selectedTab == 0) Color.White else AgroGreenPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = null
            ) {
                Text("Alertas Activas", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { selectedTab = 1 },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 1) AgroGreenPrimary else Color.Transparent,
                    contentColor = if (selectedTab == 1) Color.White else AgroGreenPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = null
            ) {
                Text("Historial de Eventos", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            if (activeAlerts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay alertas activas en este momento",
                        color = AgroTextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(activeAlerts, key = { it.id }) { alert ->
                        AlertCardItem(
                            alert = alert,
                            onDismiss = { activeAlerts.remove(alert) }
                        )
                    }
                }
            }
        } else {
            Column {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterSelector(
                            icon = Icons.Default.DateRange,
                            label = "Fecha",
                            options = listOf("Hoy", "Última semana", "Último mes"),
                            modifier = Modifier.weight(1f)
                        )

                        FilterSelector(
                            icon = Icons.Default.Spa,
                            label = "Tipo de evento",
                            options = listOf("Todos", "Helada", "Fuga", "Riego"),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(historyEvents) { event ->
                        TimelineEventItem(event = event)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSelector(
    icon: ImageVector,
    label: String,
    options: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(options.firstOrNull() ?: "") }

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = AgroGreenPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AgroGreenPrimary)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF2F6F3), shape = RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE0EAE3), shape = RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selectedOption, fontSize = 12.sp, color = AgroTextDark)
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expandir",
                    tint = AgroGreenPrimary
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedOption = option
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineEventItem(event: HistoryEventModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(AgroGreenPrimary)
                    .border(3.dp, Color(0xFFE0EAE3), CircleShape)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .weight(1f)
                    .background(Color(0xFFC8D8CE))
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF2F6F3)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = event.icon,
                        contentDescription = null,
                        tint = event.iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "[${event.date}] – ${event.title}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AgroGreenPrimary,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEAF2EB)
                ) {
                    Text(
                        text = event.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgroGreenPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AlertCardItem(alert: AlertModel, onDismiss: () -> Unit) {
    val cardBgColor = if (alert.isWarning) Color(0xFFFFF0F0) else Color(0xFFF2F8F4)
    val mainColor = if (alert.isWarning) Color(0xFFD9383A) else AgroGreenPrimary

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tipo de alerta",
                    fontSize = 12.sp,
                    color = mainColor,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = alert.type,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AgroGreenPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text("Cuartel afectado", fontSize = 12.sp, color = Color.DarkGray)
            }

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = mainColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("Marcar como vista", color = Color.White, fontSize = 11.sp)
            }
        }
    }
}