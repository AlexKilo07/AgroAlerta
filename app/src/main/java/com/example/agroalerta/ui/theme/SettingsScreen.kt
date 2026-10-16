package com.example.agroalerta.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agroalerta.AgroRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    // Estados temporales para los switches e interacción gráfica
    var pushNotificationsEnabled by remember { mutableStateOf(true) }

    // Estados para la sección de simulación
    var cuartelSeleccionado by remember { mutableStateOf("Cuartel 1") }
    val cuarteles = listOf("Cuartel 1", "Cuartel 2", "Cuartel 3", "Cuartel 4")
    var expandedMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título Principal
        Text(
            text = "Ajustes",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F382C),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 1. Sección Perfil
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil",
                        tint = Color(0xFF0F382C),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Perfil",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F382C)
                    )
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))

                Text(
                    text = "Juan Pérez",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )
                Text(
                    text = "Supervisor de Campo",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        // 2. NUEVA SECCIÓN: Simulador de Telemetría
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = "Simulación",
                        tint = Color(0xFF0F382C),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simulador de Telemetría",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F382C)
                    )
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))

                Text(
                    text = "Selecciona un cuartel para cambiar su clima en vivo:",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                // Desplegable de selección de Cuartel
                ExposedDropdownMenuBox(
                    expanded = expandedMenu,
                    onExpandedChange = { expandedMenu = !expandedMenu }
                ) {
                    OutlinedTextField(
                        value = cuartelSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cuartel Objetivo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMenu) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        cuarteles.forEach { cuartel ->
                            DropdownMenuItem(
                                text = { Text(cuartel) },
                                onClick = {
                                    cuartelSeleccionado = cuartel
                                    expandedMenu = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón Forzar Helada (-2.1 °C)
                    Button(
                        onClick = {
                            AgroRepository.simularHelada(cuartelSeleccionado)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9383A))
                    ) {
                        Icon(Icons.Default.AcUnit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simular Helada", fontSize = 12.sp)
                    }

                    // Botón Restablecer Normalidad
                    Button(
                        onClick = {
                            AgroRepository.restaurarEstadoNormal(cuartelSeleccionado)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F382C))
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restablecer", fontSize = 12.sp)
                    }
                }
            }
        }

        // 3. Sección Notificaciones
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notificaciones",
                        tint = Color(0xFF0F382C),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Notificaciones",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F382C)
                    )
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alertas Críticas (Push)",
                        fontSize = 14.sp,
                        color = Color(0xFF0F382C),
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = pushNotificationsEnabled,
                        onCheckedChange = { pushNotificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0F382C)
                        )
                    )
                }
            }
        }

        // 4. Sección Sincronización
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sincronización",
                        tint = Color(0xFF0F382C),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sincronización",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F382C)
                    )
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))

                Button(
                    onClick = { /* Acción futura de sincronizar datos */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F382C)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sincronizar datos",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // 5. Sección Información del Sistema
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Información",
                        tint = Color(0xFF0F382C),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Acerca de la App",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F382C)
                    )
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Versión", fontSize = 13.sp, color = Color.Gray)
                    Text("v1.0.0", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F382C))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Estado Servidor", fontSize = 13.sp, color = Color.Gray)
                    Text("Conectado", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón de Cerrar Sesión
        OutlinedButton(
            onClick = { /* Acción futura de logout */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD9383A)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
        }
    }
}