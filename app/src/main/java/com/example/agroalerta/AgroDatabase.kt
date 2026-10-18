package com.example.agroalerta

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AgroDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "agroalerta.db"
        private const val DATABASE_VERSION = 3

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

        // Tabla Alertas Atendidas (Persistencia)
        private const val TABLE_ALERTAS_ATENDIDAS = "alertas_atendidas"
        private const val COLUMN_ALERTA_TITULO = "titulo"
        private const val COLUMN_ALERTA_CUARTEL = "cuartel_id"
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
                $COLUMN_ALERTA_TITULO TEXT,
                $COLUMN_ALERTA_CUARTEL TEXT,
                PRIMARY KEY ($COLUMN_ALERTA_TITULO, $COLUMN_ALERTA_CUARTEL)
            )
        """.trimIndent()

        db.execSQL(createVisitasTable)
        db.execSQL(createTelemetriaTable)
        db.execSQL(createAlertasAtendidasTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_VISITAS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TELEMETRIA")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ALERTAS_ATENDIDAS")
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

    // --- Métodos de Telemetría (Caché Offline) ---
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

    // --- Métodos de Alertas Atendidas (Persistencia) ---
    fun marcarAlertaComoVista(titulo: String, cuartel: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ALERTA_TITULO, titulo)
            put(COLUMN_ALERTA_CUARTEL, cuartel)
        }
        db.insertWithOnConflict(TABLE_ALERTAS_ATENDIDAS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun obtenerAlertasAtendidas(): Set<String> {
        val set = mutableSetOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ALERTAS_ATENDIDAS", null)
        if (cursor.moveToFirst()) {
            do {
                val titulo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALERTA_TITULO))
                val cuartel = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALERTA_CUARTEL))
                set.add("$cuartel|$titulo")
            } while (cursor.moveToNext())
        }
        cursor.close()
        return set
    }

    fun limpiarAlertasAtendidas() {
        val db = writableDatabase
        db.delete(TABLE_ALERTAS_ATENDIDAS, null, null)
    }
}