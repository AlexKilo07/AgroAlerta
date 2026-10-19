package com.example.agroalerta.data.model

// Telemetría de sensores por cuartel
data class TelemetriaCuartel(
    val cuartelId: String,
    val temperatura: String,
    val humedadSuelo: String,
    val caudal: String,
    val radiacionSolar: String,
    val humedadRelativa: String,
    val estadoGlobal: String = "Normal",
    val tieneRiesgo: Boolean = false
)

// Estado de actuadores independiente por cuartel
data class EstadoActuadores(
    val cuartelId: String = "Cuartel 1",
    val bombaEncendida: Boolean = true,
    val valvulaAbierta: Boolean = true,
    val antiHeladasActivo: Boolean = false
)

// Alertas del predio
data class AlertaItem(
    val id: Int,
    val titulo: String,
    val cuartel: String,
    val esCritica: Boolean,
    val fechaHora: String
)

// Registro de visita técnica por GPS
data class VisitaGps(
    val id: Long = System.currentTimeMillis(),
    val cuartel: String,
    val latitud: Double,
    val longitud: Double,
    val fechaHora: String
)