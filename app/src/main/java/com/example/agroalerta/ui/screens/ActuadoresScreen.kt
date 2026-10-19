package com.example.agroalerta.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agroalerta.ui.viewmodel.AgroViewModel
import com.example.agroalerta.data.repository.ActuadorState
import com.example.agroalerta.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActuadoresScreen(
    cuartelName: String = "Cuartel 1",
    onBackClick: () -> Unit = {},
    viewModel: AgroViewModel = viewModel()
) {
    // Escuchar el estado de actuadores desde el ViewModel
    val actuadoresMap by viewModel.actuadoresMap.collectAsState()
    val estadoActual = actuadoresMap[cuartelName] ?: ActuadorState(cuartelName)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Actuadores - $cuartelName",
                        fontWeight = FontWeight.Bold,
                        color = AgroGreenPrimary
                    )
                },
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
            Text(
                text = "Control de Riego y Mitigación",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AgroGreenPrimary
                )
            )

            // 1. Control de Bomba Principal
            ActuadorCard(
                title = "Bomba Principal de Agua",
                subtitle = if (estadoActual.bombaEncendida) "Estado: Encendida (Bombeando)" else "Estado: Apagada",
                isActive = estadoActual.bombaEncendida,
                onCheckedChange = { viewModel.actualizarBomba(cuartelName) }
            )

            // 2. Control de Válvula Solenoide de Riego
            ActuadorCard(
                title = "Válvula Solenoide de Riego",
                subtitle = if (estadoActual.valvulaAbierta) "Estado: Abierta (Flujo activo)" else "Estado: Cerrada",
                isActive = estadoActual.valvulaAbierta,
                onCheckedChange = { viewModel.actualizarValvula(cuartelName) }
            )

            // 3. Sistema de Mitigación de Heladas (Control asistido)
            ActuadorCard(
                title = "Sistema Anti-Heladas (Asistido)",
                subtitle = if (estadoActual.bombaEncendida) "Activado mediante Bomba" else "Inactivo",
                isActive = estadoActual.bombaEncendida,
                onCheckedChange = { viewModel.actualizarAntiHeladas(cuartelName) }
            )
        }
    }
}

@Composable
fun ActuadorCard(
    title: String,
    subtitle: String,
    isActive: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AgroTextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = if (isActive) AgroGreenPrimary else AgroTextMuted
                )
            }
            Switch(
                checked = isActive,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AgroGreenPrimary
                )
            )
        }
    }
}