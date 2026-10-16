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

object AgroRepository {

    private var dbHelper: AgroDatabaseHelper? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    // Telemetría de los 4 Cuarteles
    private val _telemetriaMap = MutableStateFlow(
        mapOf(
            "Cuartel 1" to TelemetriaCuartel("Cuartel 1", "19.2 °C", "64 %", "42 L/min", "620 W/m²", "55 %", estadoGlobal = "Estado OK", tieneRiesgo = false),
            "Cuartel 2" to TelemetriaCuartel("Cuartel 2", "18.5 °C", "68 %", "45 L/min", "610 W/m²", "58 %", estadoGlobal = "Estado OK", tieneRiesgo = false),
            "Cuartel 3" to TelemetriaCuartel("Cuartel 3", "-1.5 °C", "40 %", "0 L/min", "120 W/m²", "85 %", estadoGlobal = "Riesgo Helada", tieneRiesgo = true),
            "Cuartel 4" to TelemetriaCuartel("Cuartel 4", "28.4 °C", "22 %", "10 L/min", "890 W/m²", "30 %", estadoGlobal = "Falta de Riego", tieneRiesgo = true)
        )
    )
    val telemetriaMap: StateFlow<Map<String, TelemetriaCuartel>> = _telemetriaMap.asStateFlow()

    // Estado de actuadores independiente
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

    // Inicializar SQLite
    fun init(context: Context) {
        if (dbHelper != null) return
        dbHelper = AgroDatabaseHelper(context.applicationContext)

        // Cargar datos persistidos desde SQLite
        scope.launch {
            val guardados = dbHelper?.obtenerTodosLosActuadores() ?: emptyMap()
            if (guardados.isNotEmpty()) {
                val mapaActualizado = _actuadoresMap.value.toMutableMap()
                mapaActualizado.putAll(guardados)
                _actuadoresMap.value = mapaActualizado
            }

            val visitasGuardadas = dbHelper?.obtenerVisitas() ?: emptyList()
            _visitasGps.value = visitasGuardadas
        }
    }

    fun actualizarBomba(cuartelId: String, encendida: Boolean) {
        val estadoNuevo = (_actuadoresMap.value[cuartelId] ?: EstadoActuadores(cuartelId = cuartelId)).copy(bombaEncendida = encendida)
        guardarActuador(estadoNuevo)
    }

    fun actualizarValvula(cuartelId: String, abierta: Boolean) {
        val estadoNuevo = (_actuadoresMap.value[cuartelId] ?: EstadoActuadores(cuartelId = cuartelId)).copy(valvulaAbierta = abierta)
        guardarActuador(estadoNuevo)
    }

    fun actualizarAntiHeladas(cuartelId: String, activo: Boolean) {
        val estadoNuevo = (_actuadoresMap.value[cuartelId] ?: EstadoActuadores(cuartelId = cuartelId)).copy(antiHeladasActivo = activo)
        guardarActuador(estadoNuevo)
    }

    private fun guardarActuador(estado: EstadoActuadores) {
        val mapaActual = _actuadoresMap.value.toMutableMap()
        mapaActual[estado.cuartelId] = estado
        _actuadoresMap.value = mapaActual

        scope.launch {
            dbHelper?.guardarOActualizarActuador(estado)
        }
    }

    fun descartarAlerta(id: Int) {
        _alertasActivas.value = _alertasActivas.value.filterNot { it.id == id }
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

    // Modificar la telemetría de un cuartel específico en tiempo real
    fun actualizarTelemetriaCuartel(
        cuartelId: String,
        nuevaTemp: String,
        nuevaHumedad: String,
        nuevoEstado: String,
        tieneRiesgo: Boolean
    ) {
        val mapaActual = _telemetriaMap.value.toMutableMap()
        val cuartelActual = mapaActual[cuartelId]

        if (cuartelActual != null) {
            mapaActual[cuartelId] = cuartelActual.copy(
                temperatura = nuevaTemp,
                humedadSuelo = nuevaHumedad,
                estadoGlobal = nuevoEstado,
                tieneRiesgo = tieneRiesgo
            )
            _telemetriaMap.value = mapaActual
        }
    }

    // Simular un evento de helada crítica en un cuartel especificado
    fun simularHelada(cuartelId: String) {
        actualizarTelemetriaCuartel(
            cuartelId = cuartelId,
            nuevaTemp = "-2.1 °C",
            nuevaHumedad = "38 %",
            nuevoEstado = "Riesgo Helada",
            tieneRiesgo = true
        )
    }

    // Restaurar un cuartel a condiciones óptimas
    fun restaurarEstadoNormal(cuartelId: String) {
        actualizarTelemetriaCuartel(
            cuartelId = cuartelId,
            nuevaTemp = "19.5 °C",
            nuevaHumedad = "65 %",
            nuevoEstado = "Estado OK",
            tieneRiesgo = false
        )
    }


    // Genera lecturas fluctuantes en rangos realistas para un cuartel
    fun actualizarLecturasAleatorias(cuartelId: String) {
        val mapaActual = _telemetriaMap.value.toMutableMap()
        val cuartelActual = mapaActual[cuartelId] ?: return

        // Generar variaciones dentro de rangos agrícolas coherentes
        val tempRandom = String.format(Locale.US, "%.1f °C", Random.nextDouble(16.0, 26.0))
        val humSueloRandom = "${Random.nextInt(50, 85)} %"
        val caudalRandom = "${Random.nextInt(35, 55)} L/min"
        val radRandom = "${Random.nextInt(500, 850)} W/m²"
        val humAmbRandom = "${Random.nextInt(45, 70)} %"

        mapaActual[cuartelId] = cuartelActual.copy(
            temperatura = tempRandom,
            humedadSuelo = humSueloRandom,
            caudal = caudalRandom,                     // Corregido: 'caudal' en lugar de 'caudalRiego'
            radiacionSolar = radRandom,
            humedadRelativa = humAmbRandom             // Corregido: 'humedadRelativa' en lugar de 'humedadAmbiente'
        )

        _telemetriaMap.value = mapaActual
    }

}