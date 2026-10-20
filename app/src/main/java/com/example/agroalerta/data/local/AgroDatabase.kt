package com.example.agroalerta.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.agroalerta.data.model.AlertaItem
import com.example.agroalerta.data.model.TelemetriaCuartel
import com.example.agroalerta.data.model.VisitaGps
import com.example.agroalerta.data.repository.ActuadorState

class AgroDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "agroalerta.db"
        private const val DATABASE_VERSION = 5 // Versión actualizada para soporte de ID único e historial

        // Tabla Visitas GPS
        private const val TABLE_VISITAS = "visitas_gps"
        private const val COLUMN_ID = "id"
        private const val COLUMN_CUARTEL = "cuartel"
        private const val COLUMN_LATITUD = "latitud"
        private const val COLUMN_LONGITUD = "longitud"
        private const val COLUMN_FECHA = "fecha_hora"

        // Tabla Telemetría (Caché Offline)
        private const val TABLE_TELEMETRIA = "telemetria"
        private const val COLUMN_TEMP = "temperatura"
        private const val COLUMN_HUM_SUELO = "humedad_suelo"
        private const val COLUMN_CAUDAL = "caudal"
        private const val COLUMN_RAD_SOLAR = "radiacion_solar"
        private const val COLUMN_HUM_REL = "humedad_relativa"
        private const val COLUMN_RIESGO = "tiene_riesgo"
        private const val COLUMN_ESTADO = "estado_global"

        // Tabla Alertas Atendidas (Historial)
        private const val TABLE_ALERTAS_ATENDIDAS = "alertas_atendidas"
        private const val COLUMN_ALERTA_TITULO = "titulo"
        private const val COLUMN_ALERTA_CUARTEL = "cuartel_id"
        private const val COLUMN_ALERTA_FECHA = "fecha_hora"

        // Tabla Actuadores (Persistencia)
        private const val TABLE_ACTUADORES = "actuadores"
        private const val COLUMN_ACTUADOR_CUARTEL = "cuartel_id"
        private const val COLUMN_BOMBA = "bomba_encendida"
        private const val COLUMN_VALVULA = "valvula_abierta"
        private const val COLUMN_ANTIHELADA = "antiheladas_activo"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createVisitasTable = """
            CREATE TABLE $TABLE_VISITAS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CUARTEL TEXT,
                $COLUMN_LATITUD REAL,
                $COLUMN_LONGITUD REAL,
                $COLUMN_FECHA TEXT
            )
        """.trimIndent()

        val createTelemetriaTable = """
            CREATE TABLE $TABLE_TELEMETRIA (
                $COLUMN_CUARTEL TEXT PRIMARY KEY,
                $COLUMN_TEMP TEXT,
                $COLUMN_HUM_SUELO TEXT,
                $COLUMN_CAUDAL TEXT,
                $COLUMN_RAD_SOLAR TEXT,
                $COLUMN_HUM_REL TEXT,
                $COLUMN_RIESGO INTEGER,
                $COLUMN_ESTADO TEXT
            )
        """.trimIndent()

        val createAlertasAtendidasTable = """
            CREATE TABLE $TABLE_ALERTAS_ATENDIDAS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ALERTA_TITULO TEXT,
                $COLUMN_ALERTA_CUARTEL TEXT,
                $COLUMN_ALERTA_FECHA TEXT
            )
        """.trimIndent()

        val createActuadoresTable = """
            CREATE TABLE $TABLE_ACTUADORES (
                $COLUMN_ACTUADOR_CUARTEL TEXT PRIMARY KEY,
                $COLUMN_BOMBA INTEGER,
                $COLUMN_VALVULA INTEGER,
                $COLUMN_ANTIHELADA INTEGER
            )
        """.trimIndent()

        db.execSQL(createVisitasTable)
        db.execSQL(createTelemetriaTable)
        db.execSQL(createAlertasAtendidasTable)
        db.execSQL(createActuadoresTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_VISITAS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TELEMETRIA")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ALERTAS_ATENDIDAS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ACTUADORES")
        onCreate(db)
    }

    // --- Métodos de Visitas GPS ---
    fun insertarVisita(cuartel: String, latitud: Double, longitud: Double, fechaHora: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CUARTEL, cuartel)
            put(COLUMN_LATITUD, latitud)
            put(COLUMN_LONGITUD, longitud)
            put(COLUMN_FECHA, fechaHora)
        }
        return db.insert(TABLE_VISITAS, null, values)
    }

    fun obtenerVisitas(): List<VisitaGps> {
        val lista = mutableListOf<VisitaGps>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_VISITAS ORDER BY $COLUMN_ID DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val cuartel = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUARTEL))
                val lat = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUD))
                val lng = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUD))
                val fecha = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FECHA))
                lista.add(VisitaGps(id, cuartel, lat, lng, fecha))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    // --- Métodos de Telemetría (Caché Local) ---
    fun guardarTelemetriaLocal(telemetriaList: List<TelemetriaCuartel>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            telemetriaList.forEach { item ->
                val values = ContentValues().apply {
                    put(COLUMN_CUARTEL, item.cuartelId)
                    put(COLUMN_TEMP, item.temperatura)
                    put(COLUMN_HUM_SUELO, item.humedadSuelo)
                    put(COLUMN_CAUDAL, item.caudal)
                    put(COLUMN_RAD_SOLAR, item.radiacionSolar)
                    put(COLUMN_HUM_REL, item.humedadRelativa)
                    put(COLUMN_RIESGO, if (item.tieneRiesgo) 1 else 0)
                    put(COLUMN_ESTADO, item.estadoGlobal)
                }
                db.insertWithOnConflict(TABLE_TELEMETRIA, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun obtenerTelemetriaLocal(): List<TelemetriaCuartel> {
        val lista = mutableListOf<TelemetriaCuartel>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TELEMETRIA", null)

        if (cursor.moveToFirst()) {
            do {
                val cuartelId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUARTEL))
                val temp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEMP))
                val humSuelo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HUM_SUELO))
                val caudal = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAUDAL))
                val radSolar = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RAD_SOLAR))
                val humRel = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HUM_REL))
                val riesgo = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RIESGO)) == 1
                val estado = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ESTADO))

                lista.add(
                    TelemetriaCuartel(
                        cuartelId = cuartelId,
                        temperatura = temp,
                        humedadSuelo = humSuelo,
                        caudal = caudal,
                        radiacionSolar = radSolar,
                        humedadRelativa = humRel,
                        tieneRiesgo = riesgo,
                        estadoGlobal = estado
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    // --- Métodos de Alertas Atendidas (Historial) ---
    fun marcarAlertaComoVista(titulo: String, cuartel: String, fechaHora: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ALERTA_TITULO, titulo)
            put(COLUMN_ALERTA_CUARTEL, cuartel)
            put(COLUMN_ALERTA_FECHA, fechaHora)
        }
        db.insert(TABLE_ALERTAS_ATENDIDAS, null, values)
    }

    fun obtenerHistorialAlertasAtendidas(): List<AlertaItem> {
        val lista = mutableListOf<AlertaItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ALERTAS_ATENDIDAS ORDER BY id DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val titulo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALERTA_TITULO))
                val cuartel = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALERTA_CUARTEL))
                val fechaHora = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALERTA_FECHA))

                lista.add(
                    AlertaItem(
                        id = id,
                        cuartel = cuartel,
                        titulo = titulo,
                        fechaHora = fechaHora,
                        esCritica = titulo.contains("Helada") || titulo.contains("Crítica")
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun limpiarAlertasAtendidas() {
        val db = writableDatabase
        db.delete(TABLE_ALERTAS_ATENDIDAS, null, null)
    }

    // --- Métodos de Actuadores (Persistencia Local) ---
    fun guardarActuador(state: ActuadorState) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ACTUADOR_CUARTEL, state.cuartelId)
            put(COLUMN_BOMBA, if (state.bombaEncendida) 1 else 0)
            put(COLUMN_VALVULA, if (state.valvulaAbierta) 1 else 0)
            put(COLUMN_ANTIHELADA, if (state.antiHeladasActivo) 1 else 0)
        }
        db.insertWithOnConflict(TABLE_ACTUADORES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun obtenerActuadores(): Map<String, ActuadorState> {
        val map = mutableMapOf<String, ActuadorState>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ACTUADORES", null)

        if (cursor.moveToFirst()) {
            do {
                val cuartelId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTUADOR_CUARTEL))
                val bomba = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOMBA)) == 1
                val valvula = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VALVULA)) == 1
                val antihelada = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ANTIHELADA)) == 1

                map[cuartelId] = ActuadorState(
                    cuartelId = cuartelId,
                    bombaEncendida = bomba,
                    valvulaAbierta = valvula,
                    antiHeladasActivo = antihelada
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return map
    }

    // --- Gestión de Sesión Persistente ---
    object SessionManager {
        private const val PREF_NAME = "AgroAlertaSession"
        private const val KEY_LOGIN_TIME = "login_time"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_PUSH = "push_notifications_enabled"
        private const val SESSION_DURATION_MS = 60 * 60 * 1000L // 1 hora en milisegundos

        fun guardarSesion(context: Context, nombre: String, rol: String) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
                putString(KEY_USER_NAME, nombre)
                putString(KEY_USER_ROLE, rol)
                apply()
            }
        }

        fun esSesionValida(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val loginTime = prefs.getLong(KEY_LOGIN_TIME, 0L)
            if (loginTime == 0L) return false

            val tiempoTranscurrido = System.currentTimeMillis() - loginTime
            return tiempoTranscurrido < SESSION_DURATION_MS
        }

        fun obtenerUsuarioNombre(context: Context): String {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_USER_NAME, "Administrador General") ?: "Administrador General"
        }

        fun obtenerUsuarioRol(context: Context): String {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_USER_ROLE, "Usuario Administrador") ?: "Usuario Administrador"
        }

        fun guardarNotificacionesPush(context: Context, activado: Boolean) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_PUSH, activado).apply()
        }

        fun obtenerNotificacionesPush(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_PUSH, true)
        }

        fun cerrarSesion(context: Context) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
        }
    }
}