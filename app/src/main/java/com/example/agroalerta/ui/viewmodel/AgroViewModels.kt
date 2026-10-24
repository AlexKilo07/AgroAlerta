package com.example.agroalerta.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agroalerta.data.model.AlertaItem
import com.example.agroalerta.data.model.TelemetriaCuartel
import com.example.agroalerta.data.model.VisitaGps
import com.example.agroalerta.data.repository.ActuadorState
import com.example.agroalerta.data.repository.AgroRepository
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

    // 5. Historial acumulado de alertas marcadas como atendidas desde AgroRepository
    val historialAlertas: StateFlow<List<AlertaItem>> = AgroRepository.historialAlertas

    // 6. Alertas Activas (Se generan dinámicamente según la telemetría de cada cuartel)
    val alertasActivas: StateFlow<List<AlertaItem>> = telemetriaMap.map { mapa ->
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaActual = formatoHora.format(Date())

        mapa.values.filter { it.tieneRiesgo }.mapIndexed { index, telemetria ->
            AlertaItem(
                id = index + 1,
                cuartel = telemetria.cuartelId,
                titulo = telemetria.estadoGlobal,
                fechaHora = horaActual,
                esCritica = telemetria.estadoGlobal.contains("Helada") || telemetria.estadoGlobal.contains("Crítica")
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Marcar como vista: registra la alerta atendida en SQLite con la hora exacta
    // la traslada de Activas al Historial
    fun marcarAlertaComoVista(alerta: AlertaItem) {
        val formatoHora = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault())
        val fechaHoraActual = formatoHora.format(Date())

        // 1. Guardar en SQLite y quitar el riesgo temporalmente de la vista activa
        AgroRepository.marcarAlertaAtendida(alerta.cuartel, alerta.titulo, fechaHoraActual)
    }

    // Métodos para controlar actuadores
    fun actualizarBomba(cuartelId: String) {
        AgroRepository.toggleBomba(cuartelId)
    }

    fun actualizarValvula(cuartelId: String) {
        AgroRepository.toggleValvula(cuartelId)
    }

    fun actualizarAntiHeladas(cuartelId: String) {
        AgroRepository.toggleAntiHeladas(cuartelId)
    }

    fun refrescarTelemetria() {
        AgroRepository.sincronizarTelemetria()
    }

    // Simulaciones de prueba individuales
    fun simularHeladaCuartel(cuartelId: String) {
        AgroRepository.simularHeladaCuartel(cuartelId)
    }

    fun simularBajaHumedadCuartel(cuartelId: String) {
        AgroRepository.simularBajaHumedadCuartel(cuartelId)
    }

    fun simularBajoEstanqueCuartel(cuartelId: String) {
        AgroRepository.simularBajoEstanqueCuartel(cuartelId)
    }

    fun restablecerCuartel(cuartelId: String) {
        AgroRepository.restablecerCuartel(cuartelId)
    }

    fun restaurarEstadoNormal() {
        AgroRepository.restaurarAlertas()
    }
}