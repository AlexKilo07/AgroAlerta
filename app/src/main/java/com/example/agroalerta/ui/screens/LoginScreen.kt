package com.example.agroalerta.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agroalerta.ui.theme.AgroBackground
import com.example.agroalerta.ui.theme.AgroGreenPrimary
import com.example.agroalerta.ui.theme.AgroTextDark
import com.example.agroalerta.ui.theme.AgroTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (nombre: String, rol: String) -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Estados para gestión de errores de validación
    var usuarioError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Base de datos simulada de usuarios válidos
    val usuariosValidos = mapOf(
        "admin" to Pair("1234", Pair("Administrador General", "Usuario Administrador")),
        "operador" to Pair("4321", Pair("Juan Pérez", "Operador de Terreno"))
    )

    fun validarYIngresar() {
        val userTrim = usuario.trim()
        val passTrim = password.trim()

        // 1. Validar campos vacíos
        when {
            userTrim.isEmpty() && passTrim.isEmpty() -> {
                usuarioError = true
                passwordError = true
                errorMessage = "Por favor ingresa tu usuario y contraseña."
                return
            }
            userTrim.isEmpty() -> {
                usuarioError = true
                passwordError = false
                errorMessage = "El campo usuario no puede estar vacío."
                return
            }
            passTrim.isEmpty() -> {
                usuarioError = false
                passwordError = true
                errorMessage = "Por favor ingresa tu contraseña."
                return
            }
        }

        // 2. Validar credenciales
        val credencial = usuariosValidos[userTrim.lowercase()]
        if (credencial == null) {
            usuarioError = true
            passwordError = false
            errorMessage = "El usuario '$userTrim' no existe en el sistema."
        } else if (credencial.first != passTrim) {
            usuarioError = false
            passwordError = true
            errorMessage = "Contraseña incorrecta. Inténtalo de nuevo."
        } else {
            // Éxito en la validación
            usuarioError = false
            passwordError = false
            errorMessage = ""
            val (nombre, rol) = credencial.second
            onLoginSuccess(nombre, rol)
        }
    }

    Scaffold(
        containerColor = AgroBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "AgroAlerta",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = AgroGreenPrimary
            )

            Text(
                text = "Sistema de Monitoreo y Control",
                fontSize = 14.sp,
                color = AgroTextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgroTextDark
                    )

                    // Campo Usuario
                    OutlinedTextField(
                        value = usuario,
                        onValueChange = {
                            usuario = it
                            usuarioError = false
                            errorMessage = ""
                        },
                        label = { Text("Usuario") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AgroGreenPrimary) },
                        singleLine = true,
                        isError = usuarioError,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Campo Contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = false
                            errorMessage = ""
                        },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AgroGreenPrimary) },
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = null)
                            }
                        },
                        singleLine = true,
                        isError = passwordError,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Mensaje de Error
                    if (errorMessage.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF0F0),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage,
                                color = Color(0xFFD9383A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Button(
                        onClick = { validarYIngresar() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary)
                    ) {
                        Text(
                            text = "Ingresar al Sistema",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}