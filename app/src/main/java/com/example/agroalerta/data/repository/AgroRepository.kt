package com.example.agroalerta.data.repository

import android.content.Context
import com.example.agroalerta.data.local.AgroDatabaseHelper
import com.example.agroalerta.data.model.AlertaItem
import com.example.agroalerta.data.model.TelemetriaCuartel
import com.example.agroalerta.data.model.VisitaGps
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

data class ActuadorState(
    val cuartelId: String,
    val bombaEncendida: Boolean = false,
    val valvulaAbierta: Boolean = false,
    val antiHeladasActivo: Boolean = false
)

object AgroRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var dbHelper: AgroDatabaseHelper? = null

    private val _telemetriaMap = MutableStateFlow<Map<String, TelemetriaCuartel>>(emptyMap())
    val telemetriaMap: StateFlow<Map<String, TelemetriaCuartel>> = _telemetriaMap.asStateFlow()

    private val _actuadoresMap = MutableStateFlow<Map<String, ActuadorState>>(emptyMap())
    val actuadoresMap: StateFlow<Map<String, ActuadorState>> = _actuadoresMap.asStateFlow()

    private val _visitasGps = MutableStateFlow<List<VisitaGps>>(emptyList())
    val visitasGps: StateFlow<List<VisitaGps>> = _visitasGps.asStateFlow()

    private val _historialAlertas = MutableStateFlow<List<AlertaItem>>(emptyList())
    val historialAlertas: StateFlow<List<AlertaItem>> = _historialAlertas.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    // MANTENIDA TAL CUAL COMO EN TU CÓDIGO ORIGINAL
    fun init(context: Context) {
        if (dbHelper == null) {
            dbHelper = AgroDatabaseHelper(context)

            scope.launch {
                // 1. Cargar Visitas GPS
                val guardadas = dbHelper?.obtenerVisitas() ?: emptyList()
                _visitasGps.value = guardadas

                // 2. Cargar Historial de Alertas Atendidas desde SQLite
                val historial = dbHelper?.obtenerHistorialAlertasAtendidas() ?: emptyList()
                _historialAlertas.value = historial

                // 3. Cargar Actuadores desde SQLite (RESPECTANDO EL ESTADO PREVIO)
                val actuadoresPersistidos = dbHelper?.obtenerActuadores() ?: emptyMap()
                if (actuadoresPersistidos.isNotEmpty()) {
                    _actuadoresMap.value = actuadoresPersistidos
                } else {
                    // Solo si la base de datos está totalmente vacía asignamos el estado inicial por defecto
                    val defaultActuadores = mapOf(
                        "Cuartel 1" to ActuadorState("Cuartel 1", bombaEncendida = false, valvulaAbierta = false, antiHeladasActivo = false),
                        "Cuartel 2" to ActuadorState("Cuartel 2", bombaEncendida = false, valvulaAbierta = false, antiHeladasActivo = false),
                        "Cuartel 3" to ActuadorState("Cuartel 3", bombaEncendida = false, valvulaAbierta = false, antiHeladasActivo = false),
                        "Cuartel 4" to ActuadorState("Cuartel 4", bombaEncendida = false, valvulaAbierta = false, antiHeladasActivo = false)
                    )
                    _actuadoresMap.value = defaultActuadores
                    defaultActuadores.values.forEach { dbHelper?.guardarActuador(it) }
                }

                // 4. Cargar Telemetría local
                val datosPersistidos = dbHelper?.obtenerTelemetriaLocal() ?: emptyList()
                if (datosPersistidos.isNotEmpty()) {
                    _telemetriaMap.value = datosPersistidos.associateBy { it.cuartelId }
                } else {
                    sincronizarTelemetria()
                }
            }
        }
    }

    fun sincronizarTelemetria(simularErrorRed: Boolean = false) {
        scope.launch {
            try {
                if (simularErrorRed) {
                    throw Exception("Error de red simulado para probar Modo Offline")
                }

                val nuevosDatos = generarTelemetriaDinamica()
                dbHelper?.guardarTelemetriaLocal(nuevosDatos)
                _telemetriaMap.value = nuevosDatos.associateBy { it.cuartelId }
                _isOffline.value = false

            } catch (e: Exception) {
                val datosOffline = dbHelper?.obtenerTelemetriaLocal() ?: emptyList()
                if (datosOffline.isNotEmpty()) {
                    _telemetriaMap.value = datosOffline.associateBy { it.cuartelId }
                }
                _isOffline.value = true
            }
        }
    }

    private fun generarTelemetriaDinamica(): List<TelemetriaCuartel> {
        val nombresCuarteles = listOf("Cuartel 1", "Cuartel 2", "Cuartel 3", "Cuartel 4")
        return nombresCuarteles.map { nombre ->
            var tempVal = Random.nextDouble(14.0, 24.0)
            var humSueloVal = Random.nextInt(55, 80)
            val caudalVal = Random.nextInt(20, 60)
            val radVal = Random.nextInt(400, 800)
            val humRelVal = Random.nextInt(50, 90)

            val tieneRiesgo = when (nombre) {
                "Cuartel 3" -> true
                "Cuartel 4" -> true
                else -> false
            }

            when (nombre) {
                "Cuartel 3" -> { tempVal = Random.nextDouble(-2.5, 0.5) }
                "Cuartel 4" -> { humSueloVal = Random.nextInt(15, 35) }
            }

            val estadoGlobal = when {
                tempVal < 2.0 -> "Riesgo de Helada Crítica"
                humSueloVal < 45 -> "Baja Humedad Crítica en Suelo"
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

    // --- Simulaciones Individuales ---
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

        val modificado = actual.copy(
            temperatura = "-1.8 °C",
            humedadRelativa = "88 %",
            tieneRiesgo = true,
            estadoGlobal = "Riesgo de Helada Crítica"
        )
        mapaActual[cuartelId] = modificado
        _telemetriaMap.value = mapaActual

        val actuadorActual = _actuadoresMap.value[cuartelId] ?: ActuadorState(cuartelId)
        val nuevoActuador = actuadorActual.copy(antiHeladasActivo = false)
        val nuevoMapActuadores = _actuadoresMap.value.toMutableMap()
        nuevoMapActuadores[cuartelId] = nuevoActuador
        _actuadoresMap.value = nuevoMapActuadores

        scope.launch {
            dbHelper?.guardarTelemetriaLocal(mapaActual.values.toList())
            dbHelper?.guardarActuador(nuevoActuador)
        }
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

        val modificado = actual.copy(
            humedadSuelo = "18 %",
            caudal = "5 L/min",
            tieneRiesgo = true,
            estadoGlobal = "Baja Humedad Crítica en Suelo"
        )
        mapaActual[cuartelId] = modificado
        _telemetriaMap.value = mapaActual

        val actuadorActual = _actuadoresMap.value[cuartelId] ?: ActuadorState(cuartelId)
        val nuevoActuador = actuadorActual.copy(valvulaAbierta = false)
        val nuevoMapActuadores = _actuadoresMap.value.toMutableMap()
        nuevoMapActuadores[cuartelId] = nuevoActuador
        _actuadoresMap.value = nuevoMapActuadores

        scope.launch {
            dbHelper?.guardarTelemetriaLocal(mapaActual.values.toList())
            dbHelper?.guardarActuador(nuevoActuador)
        }
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

        val modificado = actual.copy(
            caudal = "0 L/min",
            tieneRiesgo = true,
            estadoGlobal = "Bajo Nivel de Estanque"
        )
        mapaActual[cuartelId] = modificado
        _telemetriaMap.value = mapaActual

        val actuadorActual = _actuadoresMap.value[cuartelId] ?: ActuadorState(cuartelId)
        val nuevoActuador = actuadorActual.copy(bombaEncendida = false)
        val nuevoMapActuadores = _actuadoresMap.value.toMutableMap()
        nuevoMapActuadores[cuartelId] = nuevoActuador
        _actuadoresMap.value = nuevoMapActuadores

        scope.launch {
            dbHelper?.guardarTelemetriaLocal(mapaActual.values.toList())
            dbHelper?.guardarActuador(nuevoActuador)
        }
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

        val modificado = actual.copy(
            temperatura = "19.5 °C",
            humedadSuelo = "65 %",
            caudal = "45 L/min",
            tieneRiesgo = false,
            estadoGlobal = "Estado OK"
        )
        mapaActual[cuartelId] = modificado
        _telemetriaMap.value = mapaActual

        scope.launch { dbHelper?.guardarTelemetriaLocal(mapaActual.values.toList()) }
    }

    fun registrarVisitaTerreno(cuartel: String, lat: Double, lng: Double) {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fecha = formato.format(Date())

        scope.launch {
            dbHelper?.insertarVisita(cuartel, lat, lng, fecha)
            val actualizadas = dbHelper?.obtenerVisitas() ?: emptyList()
            _visitasGps.value = actualizadas
        }
    }

    fun marcarAlertaAtendida(cuartel: String, titulo: String, fechaHora: String) {
        scope.launch {
            // 1. Guardar la alerta en SQLite para el historial
            dbHelper?.marcarAlertaComoVista(titulo, cuartel, fechaHora)

            // 2. Cargar el historial actualizado de alertas
            val historialActualizado = dbHelper?.obtenerHistorialAlertasAtendidas() ?: emptyList()
            _historialAlertas.value = historialActualizado

            // 3. Quitar la alerta de las activas cambiando únicamente tieneRiesgo = false
            // IMPORTANTE: No se altera la telemetría ni se fuerza 'Estado OK' aquí;
            // la telemetría depende exclusivamente de los actuadores o del restablecimiento manual.
            val mapaActual = _telemetriaMap.value.toMutableMap()
            mapaActual[cuartel]?.let { actual ->
                val modificado = actual.copy(
                    tieneRiesgo = false // Se retira de la lista de Alertas Activas
                )
                mapaActual[cuartel] = modificado
                _telemetriaMap.value = mapaActual
                dbHelper?.guardarTelemetriaLocal(mapaActual.values.toList())
            }
        }
    }

    fun restaurarAlertas() {
        scope.launch {
            dbHelper?.limpiarAlertasAtendidas()
            _historialAlertas.value = emptyList()
            sincronizarTelemetria()
        }
    }

    // --- MODIFICACIÓN ÚNICA NECESARIA: LOS ACTUADORES NORMALIZAN TELEMETRÍA A 'Estado OK' PERO MANTIENEN 'tieneRiesgo = true' ---
    fun toggleBomba(cuartelId: String) {
        val actual = _actuadoresMap.value[cuartelId] ?: ActuadorState(cuartelId)
        val nuevoEstadoBomba = !actual.bombaEncendida
        val nuevoEstado = actual.copy(bombaEncendida = nuevoEstadoBomba)

        val nuevoMap = _actuadoresMap.value.toMutableMap()
        nuevoMap[cuartelId] = nuevoEstado
        _actuadoresMap.value = nuevoMap

        if (nuevoEstadoBomba) {
            val mapaTele = _telemetriaMap.value.toMutableMap()
            mapaTele[cuartelId]?.let { tel ->
                if (tel.estadoGlobal.contains("Estanque")) {
                    mapaTele[cuartelId] = tel.copy(
                        caudal = "45 L/min",
                        estadoGlobal = "Estado OK" // Mantiene tieneRiesgo = true para no quitar la alerta
                    )
                    _telemetriaMap.value = mapaTele
                    scope.launch { dbHelper?.guardarTelemetriaLocal(mapaTele.values.toList()) }
                }
            }
        }

        scope.launch { dbHelper?.guardarActuador(nuevoEstado) }
    }

    fun toggleValvula(cuartelId: String) {
        val actual = _actuadoresMap.value[cuartelId] ?: ActuadorState(cuartelId)
        val nuevoEstadoValvula = !actual.valvulaAbierta
        val nuevoEstado = actual.copy(valvulaAbierta = nuevoEstadoValvula)

        val nuevoMap = _actuadoresMap.value.toMutableMap()
        nuevoMap[cuartelId] = nuevoEstado
        _actuadoresMap.value = nuevoMap

        if (nuevoEstadoValvula) {
            val mapaTele = _telemetriaMap.value.toMutableMap()
            mapaTele[cuartelId]?.let { tel ->
                if (tel.estadoGlobal.contains("Humedad")) {
                    mapaTele[cuartelId] = tel.copy(
                        humedadSuelo = "65 %",
                        caudal = "40 L/min",
                        estadoGlobal = "Estado OK" // Mantiene tieneRiesgo = true para no quitar la alerta
                    )
                    _telemetriaMap.value = mapaTele
                    scope.launch { dbHelper?.guardarTelemetriaLocal(mapaTele.values.toList()) }
                }
            }
        }

        scope.launch { dbHelper?.guardarActuador(nuevoEstado) }
    }

    fun toggleAntiHeladas(cuartelId: String) {
        val actual = _actuadoresMap.value[cuartelId] ?: ActuadorState(cuartelId)
        val nuevoEstadoAntihelada = !actual.antiHeladasActivo
        val nuevoEstado = actual.copy(antiHeladasActivo = nuevoEstadoAntihelada)

        val nuevoMap = _actuadoresMap.value.toMutableMap()
        nuevoMap[cuartelId] = nuevoEstado
        _actuadoresMap.value = nuevoMap

        if (nuevoEstadoAntihelada) {
            val mapaTele = _telemetriaMap.value.toMutableMap()
            mapaTele[cuartelId]?.let { tel ->
                if (tel.estadoGlobal.contains("Helada")) {
                    mapaTele[cuartelId] = tel.copy(
                        temperatura = "4.5 °C",
                        estadoGlobal = "Estado OK" // Mantiene tieneRiesgo = true para no quitar la alerta
                    )
                    _telemetriaMap.value = mapaTele
                    scope.launch { dbHelper?.guardarTelemetriaLocal(mapaTele.values.toList()) }
                }
            }
        }

        scope.launch { dbHelper?.guardarActuador(nuevoEstado) }
    }
}