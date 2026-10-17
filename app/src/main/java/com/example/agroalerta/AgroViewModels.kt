package com.example.agroalerta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AgroViewModel : ViewModel() {

    // 1. Escuchar la telemetría desde AgroRepository
    val telemetriaMap: StateFlow<Map<String, TelemetriaCuartel>> = AgroRepository.telemetriaMap

    // 2. Escuchar el estado de actuadores desde AgroRepository
    val actuadoresMap: StateFlow<Map<String, ActuadorState>> = AgroRepository.actuadoresMap

    // 3. Escuchar las visitas GPS guardadas en SQLite
    val visitasGps: StateFlow<List<VisitaGps>> = AgroRepository.visitasGps

    // 4. Indicador de conectividad Online/Offline
    val isOffline: StateFlow<Boolean> = AgroRepository.isOffline

    // 5. Transformar la telemetría en una lista de alertas activas dinámicas
    val alertasActivas: StateFlow<List<AlertaItem>> = telemetriaMap.map { mapa ->
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaActual = formatoHora.format(Date())

        mapa.values.filter { it.tieneRiesgo }.mapIndexed { index, telemetria ->
            AlertaItem(
                id = index + 1,
                cuartel = telemetria.cuartelId,
                titulo = telemetria.estadoGlobal,
                fechaHora = horaActual,
                esCritica = telemetria.estadoGlobal == "Riesgo Helada"
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Métodos para controlar actuadores
    fun actualizarBomba(cuartelId: String) {
        AgroRepository.toggleBomba(cuartelId)
    }

    fun actualizarValvula(cuartelId: String) {
        AgroRepository.toggleValvula(cuartelId)
    }

    fun actualizarAntiHeladas(cuartelId: String) {
        AgroRepository.toggleBomba(cuartelId)
    }

    // Método para forzar la sincronización de datos
    fun refrescarTelemetria() {
        AgroRepository.sincronizarTelemetria()
    }

    // Métodos para simulaciones de riesgo desde Ajustes
    fun simularHelada() {
        AgroRepository.sincronizarTelemetria(simularErrorRed = false)
    }

    fun restaurarEstadoNormal() {
        AgroRepository.sincronizarTelemetria(simularErrorRed = false)
    }
}