package com.example.agroalerta.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agroalerta.AgroViewModel
import com.example.agroalerta.AlertaItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: AgroViewModel = viewModel()
) {
    val alertasActivas by viewModel.alertasActivas.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Activas, 1: Historial

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Centro de Alertas",
                        fontWeight = FontWeight.Bold,
                        color = AgroGreenPrimary
                    )
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
        ) {
            // Pestañas (Tabs) trabajadas por tu compañero
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = AgroGreenPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Alertas Activas (${alertasActivas.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Activas") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Historial", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (selectedTab == 0) {
                    // Vista 1: Alertas Activas
                    if (alertasActivas.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Sin alertas",
                                    tint = AgroGreenPrimary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No hay alertas activas en el predio.",
                                    color = AgroTextMuted,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(alertasActivas) { alerta ->
                                AlertaCardItem(alerta = alerta, esHistorial = false)
                            }
                        }
                    }
                } else {
                    // Vista 2: Historial de Alertas Atendidas / Pasadas
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Registro de Eventos Pasados",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AgroTextMuted,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        // Muestra las alertas registradas como historial
                        items(alertasActivas) { alerta ->
                            AlertaCardItem(alerta = alerta, esHistorial = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlertaCardItem(alerta: AlertaItem, esHistorial: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (esHistorial) Color(0xFFF9F9F9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (esHistorial) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (esHistorial) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = "Estado Alerta",
                tint = if (esHistorial) AgroTextMuted else if (alerta.esCritica) Color(0xFFD9383A) else Color(0xFFFF9800),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = alerta.cuartel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AgroTextDark
                    )
                    Text(
                        text = alerta.fechaHora,
                        fontSize = 12.sp,
                        color = AgroTextMuted
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alerta.titulo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (esHistorial) AgroTextMuted else if (alerta.esCritica) Color(0xFFD9383A) else Color(0xFFFF9800)
                )
            }
        }
    }
}