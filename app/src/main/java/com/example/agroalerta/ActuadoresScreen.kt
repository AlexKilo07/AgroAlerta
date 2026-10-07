package com.example.agroalerta

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDamage
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
import com.example.agroalerta.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActuadoresScreen(
    onBackClick: () -> Unit = {}
) {
    var bombaEncendida by remember { mutableStateOf(true) }
    var valvulaAbierta by remember { mutableStateOf(true) }
    var antiHeladasActivo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Estado de Actuadores",
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
            // 1. Bomba de Impulsión
            ActuadorCard(
                icon = Icons.Default.WaterDamage,
                iconTint = Color(0xFF2E7D32),
                title = "Bomba de Impulsión:",
                option1Label = "Encendida",
                option2Label = "Apagada",
                isOption1Selected = bombaEncendida,
                onOptionSelected = { bombaEncendida = it }
            )

            // 2. Válvula de Riego
            ActuadorCard(
                icon = Icons.Default.Water,
                iconTint = Color(0xFF0288D1),
                title = "Válvula de Riego:",
                option1Label = "Abierta",
                option2Label = "Cerrada",
                isOption1Selected = valvulaAbierta,
                onOptionSelected = { valvulaAbierta = it }
            )

            // 3. Sistema Anti-heladas
            ActuadorCard(
                icon = Icons.Default.AcUnit,
                iconTint = Color(0xFF00ACC1),
                title = "Sistema Anti-heladas:",
                option1Label = "Activo",
                option2Label = "Inactivo",
                isOption1Selected = antiHeladasActivo,
                onOptionSelected = { antiHeladasActivo = it }
            )
        }
    }
}

@Composable
fun ActuadorCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    option1Label: String,
    option2Label: String,
    isOption1Selected: Boolean,
    onOptionSelected: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Fila 1: Ícono + Título completo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AgroTextDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(8.dp))

            // Fila 2: Selectores/RadioButtons distribuidos equitativamente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    RadioButton(
                        selected = isOption1Selected,
                        onClick = { onOptionSelected(true) },
                        colors = RadioButtonDefaults.colors(selectedColor = AgroGreenPrimary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = option1Label,
                        fontSize = 14.sp,
                        fontWeight = if (isOption1Selected) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = !isOption1Selected,
                        onClick = { onOptionSelected(false) },
                        colors = RadioButtonDefaults.colors(selectedColor = AgroGreenPrimary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = option2Label,
                        fontSize = 14.sp,
                        fontWeight = if (!isOption1Selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActuadoresScreenPreview() {
    AgroAlertaTheme {
        ActuadoresScreen()
    }
}

