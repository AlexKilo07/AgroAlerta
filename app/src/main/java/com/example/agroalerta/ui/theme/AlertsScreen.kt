package com.example.agroalerta.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Estructura de datos para representar una alerta
data class AlertModel(
    val id: Int,
    val type: String,
    val isWarning: Boolean // true: Helada (Rojo), false: Fuga (Verde)
)

@Composable
fun AlertsScreen() {
    var selectedTab by remember { mutableStateOf(0) } // 0: Activas, 1: Historial

    // Lista de ejemplo basada en tu diseño
    val activeAlerts = remember {
        listOf(
            AlertModel(1, "Riesgo de Helada", isWarning = true),
            AlertModel(2, "Riesgo de Helada", isWarning = true),
            AlertModel(3, "Fuga", isWarning = false),
            AlertModel(4, "Fuga", isWarning = false)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(16.dp)
    ) {
        // Título Principal
        Text(
            text = "Alertas Activas",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F382C),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Botones de Selector de Pestaña (Tabs)
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
                    containerColor = if (selectedTab == 0) Color(0xFF0F382C) else Color.Transparent,
                    contentColor = if (selectedTab == 0) Color.White else Color(0xFF0F382C)
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = null
            ) {
                Text(
                    text = "Alertas Activas",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { selectedTab = 1 },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 1) Color(0xFF0F382C) else Color.Transparent,
                    contentColor = if (selectedTab == 1) Color.White else Color(0xFF0F382C)
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = null
            ) {
                Text(
                    text = "Historial de Eventos",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contenido según la pestaña seleccionada
        if (selectedTab == 0) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(activeAlerts) { alert ->
                    AlertCardItem(alert = alert)
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay historial de eventos registrado",
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun AlertCardItem(alert: AlertModel) {
    // Definición de colores según el tipo de alerta (Riesgo / Fuga)
    val cardBgColor = if (alert.isWarning) Color(0xFFFFF0F0) else Color(0xFFF2F8F4)
    val mainColor = if (alert.isWarning) Color(0xFFD9383A) else Color(0xFF0F382C)

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
                    color = Color(0xFF0F382C)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Cuartel afectado",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }

            Button(
                onClick = { /* Acción para marcar vista */ },
                colors = ButtonDefaults.buttonColors(containerColor = mainColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Marcar como vista",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }
    }
}