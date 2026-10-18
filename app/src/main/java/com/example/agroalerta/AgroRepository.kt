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

    // Persistencia de Alertas Atendidas (Clave: "Cuartel|Titulo")
    private val _alertasAtendidas = MutableStateFlow<Set<String>>(emptySet())
    val alertasAtendidas: StateFlow<Set<String>> = _alertasAtendidas.asStateFlow()

    // Indicador de Conectividad (Online vs Offline)
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    fun init(context: Context) {
        if (dbHelper == null) {
            dbHelper = AgroDatabaseHelper(context)

            // Cargar visitas y alertas atendidas desde SQLite
            scope.launch {
                val guardadas = dbHelper?.obtenerVisitas() ?: emptyList()
                _visitasGps.value = guardadas

                val atendidas = dbHelper?.obtenerAlertasAtendidas() ?: emptySet()
                _alertasAtendidas.value = atendidas
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

                // 1. SIMULACIÓN / PETICIÓN REST (Genera datos dinámicos)
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

    // Genera mediciones aleatorias garantizando los 3 tipos de alerta para pruebas
    private fun generarTelemetriaDinamica(): List<TelemetriaCuartel> {
        val nombresCuarteles = listOf("Cuartel 1", "Cuartel 2", "Cuartel 3", "Cuartel 4", "Predio General")
        return nombresCuarteles.map { nombre ->
            val tempVal = Random.nextDouble(14.0, 26.0)
            val humSueloVal = Random.nextInt(40, 85)
            val caudalVal = Random.nextInt(20, 60)
            val radVal = Random.nextInt(400, 800)
            val humRelVal = Random.nextInt(50, 90)

            val tieneRiesgo = when (nombre) {
                "Cuartel 3" -> true // Alerta Tipo Helada
                "Cuartel 4" -> true // Alerta Tipo Humedad
                "Predio General" -> true // Alerta Tipo Estanque
                else -> tempVal < 2.0 || humSueloVal < 45
            }

            val estadoGlobal = when (nombre) {
                "Cuartel 3" -> "Riesgo de Helada Crítica"
                "Cuartel 4" -> "Baja Humedad Crítica en Suelo"
                "Predio General" -> "Bajo Nivel de Estanque"
                else -> when {
                    tempVal < 2.0 -> "Riesgo de Helada Crítica"
                    humSueloVal < 45 -> "Baja Humedad Crítica en Suelo"
                    else -> "Estado OK"
                }
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

    // --- Simulaciones Individuales por Cuartel desde Ajustes ---
    fun simularHeladaCuartel(cuartelId: String) {
        val mapaActual = _telemetriaMap.value.toMutableMap()
        val actual = mapaActual[cuartelId] ?: TelemetriaCuartel(
            cuartelId = cuartelId,
            temperatura = "18.0 °C",
            humedadSuelo = "60 %",
            caudal = "40 L/min",
            radiacionSolar = "600 W/m²",
            humedadRelativa = "50 %"
        )

        mapaActual[cuartelId] = actual.copy(
            temperatura = "-1.8 °C",
            humedadRelativa = "88 %",
            tieneRiesgo = true,
            estadoGlobal = "Riesgo de Helada Crítica"
        )
        _telemetriaMap.value = mapaActual
    }

    fun simularBajaHumedadCuartel(cuartelId: String) {
        val mapaActual = _telemetriaMap.value.toMutableMap()
        val actual = mapaActual[cuartelId] ?: TelemetriaCuartel(
            cuartelId = cuartelId,
            temperatura = "24.0 °C",
            humedadSuelo = "60 %",
            caudal = "40 L/min",
            radiacionSolar = "700 W/m²",
            humedadRelativa = "40 %"
        )

        mapaActual[cuartelId] = actual.copy(
            humedadSuelo = "18 %",
            caudal = "5 L/min",
            tieneRiesgo = true,
            estadoGlobal = "Baja Humedad Crítica en Suelo"
        )
        _telemetriaMap.value = mapaActual
    }

    fun simularBajoEstanqueCuartel(cuartelId: String) {
        val mapaActual = _telemetriaMap.value.toMutableMap()
        val actual = mapaActual[cuartelId] ?: TelemetriaCuartel(
            cuartelId = cuartelId,
            temperatura = "20.0 °C",
            humedadSuelo = "55 %",
            caudal = "40 L/min",
            radiacionSolar = "650 W/m²",
            humedadRelativa = "50 %"
        )

        mapaActual[cuartelId] = actual.copy(
            caudal = "0 L/min",
            tieneRiesgo = true,
            estadoGlobal = "Bajo Nivel de Estanque"
        )
        _telemetriaMap.value = mapaActual
    }

    fun restablecerCuartel(cuartelId: String) {
        val mapaActual = _telemetriaMap.value.toMutableMap()
        val actual = mapaActual[cuartelId] ?: TelemetriaCuartel(
            cuartelId = cuartelId,
            temperatura = "19.5 °C",
            humedadSuelo = "65 %",
            caudal = "45 L/min",
            radiacionSolar = "650 W/m²",
            humedadRelativa = "55 %"
        )

        mapaActual[cuartelId] = actual.copy(
            temperatura = "19.5 °C",
            humedadSuelo = "65 %",
            caudal = "45 L/min",
            tieneRiesgo = false,
            estadoGlobal = "Estado OK"
        )
        _telemetriaMap.value = mapaActual
    }

    fun actualizarLecturasAleatorias(cuartelId: String) {}

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

    // Marcar Alerta como Vista con Persistencia en SQLite
    fun marcarAlertaAtendida(cuartel: String, titulo: String) {
        scope.launch {
            dbHelper?.marcarAlertaComoVista(titulo, cuartel)
            val clave = "$cuartel|$titulo"
            _alertasAtendidas.value = _alertasAtendidas.value + clave
        }
    }

    // Restaurar alertas atendidas (para reiniciar el historial desde pruebas)
    fun restaurarAlertas() {
        scope.launch {
            dbHelper?.limpiarAlertasAtendidas()
            _alertasAtendidas.value = emptySet()
            sincronizarTelemetria()
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