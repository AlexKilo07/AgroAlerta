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

    val telemetriaMap: StateFlow<Map<String, TelemetriaCuartel>> = AgroRepository.telemetriaMap
    val actuadoresMap: StateFlow<Map<String, ActuadorState>> = AgroRepository.actuadoresMap
    val visitasGps: StateFlow<List<VisitaGps>> = AgroRepository.visitasGps
    val isOffline: StateFlow<Boolean> = AgroRepository.isOffline
    val historialAlertas: StateFlow<List<AlertaItem>> = AgroRepository.historialAlertas

    // Alertas Activas: conserva el título de la alerta aun cuando el estado se normalice a Estado OK
    val alertasActivas: StateFlow<List<AlertaItem>> = telemetriaMap.map { mapa ->
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaActual = formatoHora.format(Date())

        mapa.values.filter { it.tieneRiesgo }.mapIndexed { index, telemetria ->
            val tituloMostrar = if (telemetria.estadoGlobal != "Estado OK") {
                telemetria.estadoGlobal
            } else {
                when (telemetria.cuartelId) {
                    "Cuartel 3" -> "Riesgo de Helada Crítica"
                    "Cuartel 4" -> "Baja Humedad Crítica en Suelo"
                    else -> "Bajo Nivel de Estanque"
                }
            }

            AlertaItem(
                id = index + 1,
                cuartel = telemetria.cuartelId,
                titulo = tituloMostrar,
                fechaHora = horaActual,
                esCritica = true
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun marcarAlertaComoVista(alerta: AlertaItem) {
        val formatoHora = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault())
        val fechaHoraActual = formatoHora.format(Date())

        AgroRepository.marcarAlertaAtendida(alerta.cuartel, alerta.titulo, fechaHoraActual)
    }

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