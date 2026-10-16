package com.example.agroalerta

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AgroRepository {

    // Telemetría de los 4 Cuarteles (2 OK, 2 con Alerta de Riesgo)
    private val _telemetriaMap = MutableStateFlow(
        mapOf(
            "Cuartel 1" to TelemetriaCuartel("Cuartel 1", "19.2 °C", "64 %", "42 L/min", "620 W/m²", "55 %", estadoGlobal = "Estado OK", tieneRiesgo = false),
            "Cuartel 2" to TelemetriaCuartel("Cuartel 2", "18.5 °C", "68 %", "45 L/min", "610 W/m²", "58 %", estadoGlobal = "Estado OK", tieneRiesgo = false),
            "Cuartel 3" to TelemetriaCuartel("Cuartel 3", "-1.5 °C", "40 %", "0 L/min", "120 W/m²", "85 %", estadoGlobal = "Riesgo Helada", tieneRiesgo = true),
            "Cuartel 4" to TelemetriaCuartel("Cuartel 4", "28.4 °C", "22 %", "10 L/min", "890 W/m²", "30 %", estadoGlobal = "Falta de Riego", tieneRiesgo = true)
        )
    )
    val telemetriaMap: StateFlow<Map<String, TelemetriaCuartel>> = _telemetriaMap.asStateFlow()

    // Estado de actuadores independiente para los 4 cuarteles
    private val _actuadoresMap = MutableStateFlow(
        mapOf(
            "Cuartel 1" to EstadoActuadores(cuartelId = "Cuartel 1", bombaEncendida = true, valvulaAbierta = true, antiHeladasActivo = false),
            "Cuartel 2" to EstadoActuadores(cuartelId = "Cuartel 2", bombaEncendida = true, valvulaAbierta = true, antiHeladasActivo = false),
            "Cuartel 3" to EstadoActuadores(cuartelId = "Cuartel 3", bombaEncendida = false, valvulaAbierta = false, antiHeladasActivo = false),
            "Cuartel 4" to EstadoActuadores(cuartelId = "Cuartel 4", bombaEncendida = false, valvulaAbierta = true, antiHeladasActivo = false)
        )
    )
    val actuadoresMap: StateFlow<Map<String, EstadoActuadores>> = _actuadoresMap.asStateFlow()

    private val _alertasActivas = MutableStateFlow(
        listOf(
            AlertaItem(1, "Riesgo de Helada Crítica", "Cuartel 3", esCritica = true, fechaHora = "16/10 05:15"),
            AlertaItem(2, "Baja Humedad Crítica en Suelo", "Cuartel 4", esCritica = true, fechaHora = "16/10 08:30"),
            AlertaItem(3, "Bajo Nivel de Estanque", "Predio General", esCritica = false, fechaHora = "16/10 10:00")
        )
    )
    val alertasActivas: StateFlow<List<AlertaItem>> = _alertasActivas.asStateFlow()

    private val _visitasGps = MutableStateFlow<List<VisitaGps>>(emptyList())
    val visitasGps: StateFlow<List<VisitaGps>> = _visitasGps.asStateFlow()

    fun actualizarBomba(cuartelId: String, encendida: Boolean) {
        val mapaActual = _actuadoresMap.value.toMutableMap()
        val estadoActual = mapaActual[cuartelId] ?: EstadoActuadores(cuartelId = cuartelId)
        mapaActual[cuartelId] = estadoActual.copy(bombaEncendida = encendida)
        _actuadoresMap.value = mapaActual
    }

    fun actualizarValvula(cuartelId: String, abierta: Boolean) {
        val mapaActual = _actuadoresMap.value.toMutableMap()
        val estadoActual = mapaActual[cuartelId] ?: EstadoActuadores(cuartelId = cuartelId)
        mapaActual[cuartelId] = estadoActual.copy(valvulaAbierta = abierta)
        _actuadoresMap.value = mapaActual
    }

    fun actualizarAntiHeladas(cuartelId: String, activo: Boolean) {
        val mapaActual = _actuadoresMap.value.toMutableMap()
        val estadoActual = mapaActual[cuartelId] ?: EstadoActuadores(cuartelId = cuartelId)
        mapaActual[cuartelId] = estadoActual.copy(antiHeladasActivo = activo)
        _actuadoresMap.value = mapaActual
    }

    fun descartarAlerta(id: Int) {
        _alertasActivas.value = _alertasActivas.value.filterNot { it.id == id }
    }

    fun registrarVisitaTerreno(cuartel: String, lat: Double, lng: Double) {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val nuevaVisita = VisitaGps(
            cuartel = cuartel,
            latitud = lat,
            longitud = lng,
            fechaHora = formato.format(Date())
        )
        _visitasGps.value = _visitasGps.value + nuevaVisita
    }
}