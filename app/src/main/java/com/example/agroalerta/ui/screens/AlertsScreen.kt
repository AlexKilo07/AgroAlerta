package com.example.agroalerta.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agroalerta.ui.viewmodel.AgroViewModel
import com.example.agroalerta.data.model.AlertaItem
import com.example.agroalerta.ui.theme.AgroBackground
import com.example.agroalerta.ui.theme.AgroGreenPrimary
import com.example.agroalerta.ui.theme.AgroTextDark
import com.example.agroalerta.ui.theme.AgroTextMuted
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: AgroViewModel = viewModel()
) {
    val alertasActivas by viewModel.alertasActivas.collectAsState()
    val historialAlertas by viewModel.historialAlertas.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Alertas Activas, 1: Historial de Eventos

    // Filtros para la vista de Alertas Activas (Chips)
    var filtroTipoSelected by remember { mutableStateOf("Todas") }

    // Filtros para la vista de Historial
    var fechaSelected by remember { mutableStateOf("Hoy") }
    var tipoEventoSelected by remember { mutableStateOf("Todos") }

    var expandedFecha by remember { mutableStateOf(false) }
    var expandedTipo by remember { mutableStateOf(false) }

    // Función Helper para comprobar si una fecha dada en String entra en el rango del filtro
    fun coincideConFiltroFecha(fechaHoraStr: String, filtro: String): Boolean {
        if (filtro == "Todas") return true

        val sdfVisual = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault())
        val sdfSoloHora = SimpleDateFormat("HH:mm", Locale.getDefault())

        val fechaAlertaCalendar = Calendar.getInstance()
        val fechaAlertaDate = try {
            if (fechaHoraStr.contains("-")) {
                sdfVisual.parse(fechaHoraStr)
            } else {
                // Si solo viene la hora, asumimos que fue ingresada Hoy
                val dateSoloHora = sdfSoloHora.parse(fechaHoraStr)
                if (dateSoloHora != null) {
                    val calHoy = Calendar.getInstance()
                    val calHora = Calendar.getInstance().apply { time = dateSoloHora }
                    calHoy.set(Calendar.HOUR_OF_DAY, calHora.get(Calendar.HOUR_OF_DAY))
                    calHoy.set(Calendar.MINUTE, calHora.get(Calendar.MINUTE))
                    calHoy.time
                } else null
            }
        } catch (e: Exception) {
            null
        } ?: return true

        // Ajustar el año actual para las alertas analizadas
        fechaAlertaCalendar.time = fechaAlertaDate
        val calActual = Calendar.getInstance()
        fechaAlertaCalendar.set(Calendar.YEAR, calActual.get(Calendar.YEAR))

        val hoyInicio = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return when (filtro) {
            "Hoy" -> {
                fechaAlertaCalendar.timeInMillis >= hoyInicio.timeInMillis
            }
            "Ayer" -> {
                val ayerInicio = (hoyInicio.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
                fechaAlertaCalendar.timeInMillis >= ayerInicio.timeInMillis && fechaAlertaCalendar.timeInMillis < hoyInicio.timeInMillis
            }
            "Última semana" -> {
                val semanaAtras = (hoyInicio.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -7) }
                fechaAlertaCalendar.timeInMillis >= semanaAtras.timeInMillis
            }
            "Último mes" -> {
                val mesAtras = (hoyInicio.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                fechaAlertaCalendar.timeInMillis >= mesAtras.timeInMillis
            }
            else -> true
        }
    }

    // Filtrado dinámico según la pestaña activa
    val listaAlertasFiltradas = remember(
        alertasActivas, historialAlertas, selectedTab, filtroTipoSelected, fechaSelected, tipoEventoSelected
    ) {
        if (selectedTab == 0) {
            when (filtroTipoSelected) {
                "Heladas" -> alertasActivas.filter { it.titulo.contains("Helada", ignoreCase = true) }
                "Riego / Humedad" -> alertasActivas.filter {
                    it.titulo.contains("Humedad", ignoreCase = true) || it.titulo.contains("Riego", ignoreCase = true)
                }
                "Estanque / General" -> alertasActivas.filter {
                    it.titulo.contains("Estanque", ignoreCase = true) || it.cuartel.contains("Predio", ignoreCase = true)
                }
                else -> alertasActivas
            }
        } else {
            // Filtrar primero por Tipo y luego por Fecha
            val porTipo = when (tipoEventoSelected) {
                "Heladas" -> historialAlertas.filter { it.titulo.contains("Helada", ignoreCase = true) }
                "Riego / Humedad" -> historialAlertas.filter {
                    it.titulo.contains("Humedad", ignoreCase = true) || it.titulo.contains("Riego", ignoreCase = true)
                }
                "Estanque / General" -> historialAlertas.filter {
                    it.titulo.contains("Estanque", ignoreCase = true) || it.cuartel.contains("Predio", ignoreCase = true)
                }
                else -> historialAlertas
            }

            porTipo.filter { coincideConFiltroFecha(it.fechaHora, fechaSelected) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedTab == 0) "Alertas Activas" else "Historial",
                        fontWeight = FontWeight.Bold,
                        color = AgroGreenPrimary,
                        fontSize = 24.sp
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Selector de Pestañas Estilizado
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEFF4F0)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                ) {
                    Button(
                        onClick = { selectedTab = 0 },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 0) AgroGreenPrimary else Color.Transparent,
                            contentColor = if (selectedTab == 0) Color.White else AgroGreenPrimary
                        ),
                        elevation = null
                    ) {
                        Text(
                            text = "Alertas Activas (${alertasActivas.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { selectedTab = 1 },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 1) AgroGreenPrimary else Color.Transparent,
                            contentColor = if (selectedTab == 1) Color.White else AgroGreenPrimary
                        ),
                        elevation = null
                    ) {
                        Text(
                            text = "Historial de Eventos",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 2. Filtros según la pestaña seleccionada
            if (selectedTab == 0) {
                // Filtro por Chips para Alertas Activas
                val tiposFiltro = listOf("Todas", "Heladas", "Riego / Humedad", "Estanque / General")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(tiposFiltro) { tipo ->
                        FilterChip(
                            selected = filtroTipoSelected == tipo,
                            onClick = { filtroTipoSelected = tipo },
                            label = { Text(tipo, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AgroGreenPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = AgroTextDark
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = filtroTipoSelected == tipo,
                                borderColor = if (filtroTipoSelected == tipo) AgroGreenPrimary else Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            } else {
                // Filtros con Tarjeta Dropdown para Historial
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
                        // Dropdown 1: Fecha
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = AgroGreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Fecha", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AgroGreenPrimary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF2F6F3),
                                    onClick = { expandedFecha = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(fechaSelected, fontSize = 13.sp, color = AgroTextDark)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AgroGreenPrimary)
                                    }
                                }
                                DropdownMenu(
                                    expanded = expandedFecha,
                                    onDismissRequest = { expandedFecha = false }
                                ) {
                                    listOf("Hoy", "Ayer", "Última semana", "Último mes", "Todas").forEach { opcion ->
                                        DropdownMenuItem(
                                            text = { Text(opcion) },
                                            onClick = {
                                                fechaSelected = opcion
                                                expandedFecha = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Dropdown 2: Tipo de evento
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = AgroGreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tipo de evento", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AgroGreenPrimary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF2F6F3),
                                    onClick = { expandedTipo = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(tipoEventoSelected, fontSize = 13.sp, color = AgroTextDark)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AgroGreenPrimary)
                                    }
                                }
                                DropdownMenu(
                                    expanded = expandedTipo,
                                    onDismissRequest = { expandedTipo = false }
                                ) {
                                    listOf("Todos", "Heladas", "Riego / Humedad", "Estanque / General").forEach { opcion ->
                                        DropdownMenuItem(
                                            text = { Text(opcion) },
                                            onClick = {
                                                tipoEventoSelected = opcion
                                                expandedTipo = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Contenido Principal
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp)
            ) {
                if (listaAlertasFiltradas.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedTab == 0) "No hay alertas activas." else "No hay eventos registrados en el historial.",
                            color = AgroTextMuted,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(listaAlertasFiltradas.size) { index ->
                            val alerta = listaAlertasFiltradas[index]
                            if (selectedTab == 0) {
                                TarjetaAlertaActiva(
                                    alerta = alerta,
                                    onMarcarComoVista = { viewModel.marcarAlertaComoVista(alerta) }
                                )
                            } else {
                                TarjetaHistorialTimeline(
                                    alerta = alerta,
                                    isLastItem = index == listaAlertasFiltradas.size - 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Vista 1: Tarjeta para Alertas Activas
@Composable
fun TarjetaAlertaActiva(
    alerta: AlertaItem,
    onMarcarComoVista: () -> Unit
) {
    val isCritica = alerta.esCritica
    val cardBgColor = if (isCritica) Color(0xFFFFF0F0) else Color(0xFFF2F7F3)
    val buttonBgColor = if (isCritica) Color(0xFFD9383A) else AgroGreenPrimary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Tipo de alerta",
                    fontSize = 11.sp,
                    color = if (isCritica) Color(0xFFD9383A) else AgroTextMuted,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = alerta.titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AgroGreenPrimary
                )
                Text(
                    text = "Cuartel: ${alerta.cuartel}",
                    fontSize = 12.sp,
                    color = AgroTextMuted
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onMarcarComoVista,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonBgColor),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Marcar como vista",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// Vista 2: Tarjeta con Línea de Tiempo para Historial de Eventos
@Composable
fun TarjetaHistorialTimeline(
    alerta: AlertaItem,
    isLastItem: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicador de Línea de Tiempo (Timeline)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(AgroGreenPrimary)
            )
            if (!isLastItem) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(64.dp)
                        .background(Color(0xFFC8E6C9))
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Tarjeta principal del evento del historial
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isHelada = alerta.titulo.contains("Helada", ignoreCase = true)
                    val iconBgColor = if (isHelada) Color(0xFFFFF0F0) else Color(0xFFE8F5E9)
                    val iconTint = if (isHelada) Color(0xFFD9383A) else AgroGreenPrimary

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(iconBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isHelada) Icons.Default.Warning else Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "[${alerta.fechaHora}] - ${alerta.titulo}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgroGreenPrimary
                        )
                        if (alerta.cuartel.isNotEmpty()) {
                            Text(
                                text = "Cuartel: ${alerta.cuartel}",
                                fontSize = 11.sp,
                                color = AgroTextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEDF4EE)
                ) {
                    Text(
                        text = if (alerta.esCritica) "Atendida." else "Resuelto.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AgroGreenPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}