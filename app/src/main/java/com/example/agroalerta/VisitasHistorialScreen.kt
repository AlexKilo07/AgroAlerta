package com.example.agroalerta

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agroalerta.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitasHistorialScreen(
    cuartelFilter: String? = null,
    onBackClick: () -> Unit = {}
) {
    val visitasGps by AgroRepository.visitasGps.collectAsState()

    // Filtrar si se desea ver sólo las del cuartel actual o todas
    val visitasFiltradas = remember(visitasGps, cuartelFilter) {
        if (cuartelFilter != null) {
            visitasGps.filter { it.cuartel == cuartelFilter }
        } else {
            visitasGps
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (cuartelFilter != null) "Historial - $cuartelFilter" else "Historial de Inspecciones GPS",
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
                .padding(16.dp)
        ) {
            if (visitasFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay inspecciones GPS registradas.",
                        color = AgroTextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(visitasFiltradas) { visita ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "GPS",
                                    tint = AgroGreenPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = visita.cuartel,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = AgroTextDark
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Lat: ${String.format("%.4f", visita.latitud)}, Lng: ${String.format("%.4f", visita.longitud)}",
                                        fontSize = 12.sp,
                                        color = AgroTextMuted
                                    )
                                }
                                Text(
                                    text = visita.fechaHora,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AgroGreenPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}