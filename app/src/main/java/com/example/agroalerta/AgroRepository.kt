package com.example.agroalerta

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

// Modelo de estado para Actuadores (Bombas y Válvulas)
data class ActuadorState(
    val cuartelId: String,
    val bombaEncendida: Boolean = false,
    val valvulaAbierta: Boolean = false
)

object AgroRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var dbHelper: AgroDatabaseHelper? = null

    // Estados reactivos (StateFlow)
    private val _telemetriaMap = MutableStateFlow<Map<String, TelemetriaCuartel>>(emptyMap())
    val telemetriaMap: StateFlow<Map<String, TelemetriaCuartel>> = _telemetriaMap.asStateFlow()

    private val _actuadoresMap = MutableStateFlow<Map<String, ActuadorState>>(emptyMap())
    val actuadoresMap: StateFlow<Map<String, ActuadorState>> = _actuadoresMap.asStateFlow()

    private val _visitasGps = MutableStateFlow<List<VisitaGps>>(emptyList())
    val visitasGps: StateFlow<List<VisitaGps>> = _visitasGps.asStateFlow()

    // Indicador de Conectividad (Online vs Offline)
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    fun init(context: Context) {
        if (dbHelper == null) {
            dbHelper = AgroDatabaseHelper(context)

            // Cargar visitas guardadas en SQLite
            scope.launch {
                val guardadas = dbHelper?.obtenerVisitas() ?: emptyList()
                _visitasGps.value = guardadas
            }

            // Inicializar estado por defecto de actuadores
            _actuadoresMap.value = mapOf(
                "Cuartel 1" to ActuadorState("Cuartel 1", bombaEncendida = true, valvulaAbierta = true),
                "Cuartel 2" to ActuadorState("Cuartel 2", bombaEncendida = false, valvulaAbierta = false),
                "Cuartel 3" to ActuadorState("Cuartel 3", bombaEncendida = true, valvulaAbierta = true),
                "Cuartel 4" to ActuadorState("Cuartel 4", bombaEncendida = false, valvulaAbierta = false)
            )

            // Primera sincronización al arrancar
            sincronizarTelemetria()
        }
    }

    // Lógica de Sincronización Online/Offline
    fun sincronizarTelemetria(simularErrorRed: Boolean = false) {
        scope.launch {
            try {
                if (simularErrorRed) {
                    throw Exception("Error de red simulado para probar Modo Offline")
                }

                // 1. SIMULACIÓN / PETICIÓN REST (Genera datos dinámicos con fluctuación aleatoria)
                val nuevosDatos = generarTelemetriaDinamica()

                // 2. Guardar inmediatamente en Caché Local SQLite
                dbHelper?.guardarTelemetriaLocal(nuevosDatos)

                // 3. Actualizar la UI y marcar como ONLINE
                _telemetriaMap.value = nuevosDatos.associateBy { it.cuartelId }
                _isOffline.value = false

            } catch (e: Exception) {
                // 4. Si la red falla (MODO OFFLINE), cargar desde SQLite
                val datosOffline = dbHelper?.obtenerTelemetriaLocal() ?: emptyList()

                if (datosOffline.isNotEmpty()) {
                    _telemetriaMap.value = datosOffline.associateBy { it.cuartelId }
                }
                _isOffline.value = true
            }
        }
    }

    // Genera mediciones con pequeñas variaciones aleatorias para simular el servidor REST
    private fun generarTelemetriaDinamica(): List<TelemetriaCuartel> {
        val nombresCuarteles = listOf("Cuartel 1", "Cuartel 2", "Cuartel 3", "Cuartel 4")
        return nombresCuarteles.map { nombre ->
            val tempVal = Random.nextDouble(14.0, 26.0)
            val humSueloVal = Random.nextInt(40, 85)
            val caudalVal = Random.nextInt(20, 60)
            val radVal = Random.nextInt(400, 800)
            val humRelVal = Random.nextInt(50, 90)

            val tieneRiesgo = tempVal < 2.0 || humSueloVal < 45
            val estadoGlobal = when {
                tempVal < 2.0 -> "Riesgo Helada"
                humSueloVal < 45 -> "Falta de Riego"
                else -> "Estado OK"
            }

            TelemetriaCuartel(
                cuartelId = nombre,
                temperatura = String.format(Locale.US, "%.1f °C", tempVal),
                humedadSuelo = "$humSueloVal %",
                caudal = "$caudalVal L/min",
                radiacionSolar = "$radVal W/m²",
                humedadRelativa = "$humRelVal %",
                tieneRiesgo = tieneRiesgo,
                estadoGlobal = estadoGlobal
            )
        }
    }

    // Desactivar la generación automática al cambiar de vista
    fun actualizarLecturasAleatorias(cuartelId: String) {
        // Se deja vacío intencionalmente para preservar la estabilidad de los datos
        // durante la navegación entre pantallas.
    }

    // Registro GPS en SQLite
    fun registrarVisitaTerreno(cuartel: String, lat: Double, lng: Double) {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fecha = formato.format(Date())

        scope.launch {
            dbHelper?.insertarVisita(cuartel, lat, lng, fecha)
            val actualizadas = dbHelper?.obtenerVisitas() ?: emptyList()
            _visitasGps.value = actualizadas
        }
    }

    // Conmutadores de Actuadores
    fun toggleBomba(cuartelId: String) {
        val actual = _actuadoresMap.value[cuartelId] ?: ActuadorState(cuartelId)
        val nuevoMap = _actuadoresMap.value.toMutableMap()
        nuevoMap[cuartelId] = actual.copy(bombaEncendida = !actual.bombaEncendida)
        _actuadoresMap.value = nuevoMap
    }

    fun toggleValvula(cuartelId: String) {
        val actual = _actuadoresMap.value[cuartelId] ?: ActuadorState(cuartelId)
        val nuevoMap = _actuadoresMap.value.toMutableMap()
        nuevoMap[cuartelId] = actual.copy(valvulaAbierta = !actual.valvulaAbierta)
        _actuadoresMap.value = nuevoMap
    }
}