package com.example.agroalerta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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

    // 5. Alertas Activas (Pendientes de revisar)
    val alertasActivas: StateFlow<List<AlertaItem>> = combine(
        telemetriaMap,
        AgroRepository.alertasAtendidas
    ) { mapa, atendidas ->
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaActual = formatoHora.format(Date())

        mapa.values.filter { it.tieneRiesgo }.mapIndexedNotNull { index, telemetria ->
            val clave = "${telemetria.cuartelId}|${telemetria.estadoGlobal}"
            if (!atendidas.contains(clave)) {
                AlertaItem(
                    id = index + 1,
                    cuartel = telemetria.cuartelId,
                    titulo = telemetria.estadoGlobal,
                    fechaHora = horaActual,
                    esCritica = telemetria.estadoGlobal.contains("Helada") || telemetria.estadoGlobal.contains("Crítica")
                )
            } else {
                null
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 6. Historial de Alertas Atendidas (Persistente)
    val historialAlertas: StateFlow<List<AlertaItem>> = combine(
        telemetriaMap,
        AgroRepository.alertasAtendidas
    ) { mapa, atendidas ->
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaActual = formatoHora.format(Date())

        atendidas.mapIndexed { index, clave ->
            val partes = clave.split("|")
            val cuartel = partes.getOrElse(0) { "Predio" }
            val titulo = partes.getOrElse(1) { "Evento Atendido" }

            AlertaItem(
                id = 100 + index,
                cuartel = cuartel,
                titulo = titulo,
                fechaHora = horaActual,
                esCritica = titulo.contains("Helada") || titulo.contains("Crítica")
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Marcar como vista
    fun marcarAlertaComoVista(alerta: AlertaItem) {
        AgroRepository.marcarAlertaAtendida(alerta.cuartel, alerta.titulo)
    }

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