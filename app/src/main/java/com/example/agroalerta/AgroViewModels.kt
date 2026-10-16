package com.example.agroalerta

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {
    val telemetriaData: StateFlow<Map<String, TelemetriaCuartel>> = AgroRepository.telemetriaMap
}

class ActuadoresViewModel : ViewModel() {
    val actuadoresMap: StateFlow<Map<String, EstadoActuadores>> = AgroRepository.actuadoresMap

    fun toggleBomba(cuartelId: String, encendida: Boolean) = AgroRepository.actualizarBomba(cuartelId, encendida)
    fun toggleValvula(cuartelId: String, abierta: Boolean) = AgroRepository.actualizarValvula(cuartelId, abierta)
    fun toggleAntiHeladas(cuartelId: String, activo: Boolean) = AgroRepository.actualizarAntiHeladas(cuartelId, activo)
}

class AlertsViewModel : ViewModel() {
    val alertasActivas: StateFlow<List<AlertaItem>> = AgroRepository.alertasActivas

    fun marcarComoVista(id: Int) = AgroRepository.descartarAlerta(id)
}