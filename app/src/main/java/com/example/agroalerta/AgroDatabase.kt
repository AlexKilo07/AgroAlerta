package com.example.agroalerta

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AgroDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "agroalerta_local.db"
        private const val DATABASE_VERSION = 1

        // Tabla Actuadores
        const val TABLE_ACTUADORES = "actuadores"
        const val COLUMN_CUARTEL_ID = "cuartel_id"
        const val COLUMN_BOMBA = "bomba_encendida"
        const val COLUMN_VALVULA = "valvula_abierta"
        const val COLUMN_ANTIHELADAS = "antiheladas_activo"

        // Tabla Visitas GPS
        const val TABLE_VISITAS = "visitas_gps"
        const val COLUMN_VISITA_ID = "id"
        const val COLUMN_CUARTEL = "cuartel"
        const val COLUMN_LAT = "latitud"
        const val COLUMN_LNG = "longitud"
        const val COLUMN_FECHA = "fecha_hora"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createActuadoresTable = """
            CREATE TABLE $TABLE_ACTUADORES (
                $COLUMN_CUARTEL_ID TEXT PRIMARY KEY,
                $COLUMN_BOMBA INTEGER NOT NULL,
                $COLUMN_VALVULA INTEGER NOT NULL,
                $COLUMN_ANTIHELADAS INTEGER NOT NULL
            )
        """.trimIndent()

        val createVisitasTable = """
            CREATE TABLE $TABLE_VISITAS (
                $COLUMN_VISITA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CUARTEL TEXT NOT NULL,
                $COLUMN_LAT REAL NOT NULL,
                $COLUMN_LNG REAL NOT NULL,
                $COLUMN_FECHA TEXT NOT NULL
            )
        """.trimIndent()

        db.execSQL(createActuadoresTable)
        db.execSQL(createVisitasTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ACTUADORES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_VISITAS")
        onCreate(db)
    }

    // --- OPERACIONES SQLITE: ACTUADORES ---

    fun guardarOActualizarActuador(actuador: EstadoActuadores) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CUARTEL_ID, actuador.cuartelId)
            put(COLUMN_BOMBA, if (actuador.bombaEncendida) 1 else 0)
            put(COLUMN_VALVULA, if (actuador.valvulaAbierta) 1 else 0)
            put(COLUMN_ANTIHELADAS, if (actuador.antiHeladasActivo) 1 else 0)
        }
        db.insertWithOnConflict(TABLE_ACTUADORES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun obtenerTodosLosActuadores(): Map<String, EstadoActuadores> {
        val resultado = mutableMapOf<String, EstadoActuadores>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ACTUADORES", null)

        cursor.use { c ->
            while (c.moveToNext()) {
                val cuartelId = c.getString(c.getColumnIndexOrThrow(COLUMN_CUARTEL_ID))
                val bomba = c.getInt(c.getColumnIndexOrThrow(COLUMN_BOMBA)) == 1
                val valvula = c.getInt(c.getColumnIndexOrThrow(COLUMN_VALVULA)) == 1
                val antiheladas = c.getInt(c.getColumnIndexOrThrow(COLUMN_ANTIHELADAS)) == 1

                resultado[cuartelId] = EstadoActuadores(
                    cuartelId = cuartelId,
                    bombaEncendida = bomba,
                    valvulaAbierta = valvula,
                    antiHeladasActivo = antiheladas
                )
            }
        }
        return resultado
    }

    // --- OPERACIONES SQLITE: VISITAS GPS ---

    fun insertarVisita(cuartel: String, lat: Double, lng: Double, fechaHora: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CUARTEL, cuartel)
            put(COLUMN_LAT, lat)
            put(COLUMN_LNG, lng)
            put(COLUMN_FECHA, fechaHora)
        }
        db.insert(TABLE_VISITAS, null, values)
    }

    fun obtenerVisitas(): List<VisitaGps> {
        val lista = mutableListOf<VisitaGps>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_VISITAS ORDER BY $COLUMN_VISITA_ID DESC", null)

        cursor.use { c ->
            while (c.moveToNext()) {
                val id = c.getLong(c.getColumnIndexOrThrow(COLUMN_VISITA_ID))
                val cuartel = c.getString(c.getColumnIndexOrThrow(COLUMN_CUARTEL))
                val lat = c.getDouble(c.getColumnIndexOrThrow(COLUMN_LAT))
                val lng = c.getDouble(c.getColumnIndexOrThrow(COLUMN_LNG))
                val fecha = c.getString(c.getColumnIndexOrThrow(COLUMN_FECHA))

                lista.add(VisitaGps(id = id, cuartel = cuartel, latitud = lat, longitud = lng, fechaHora = fecha))
            }
        }
        return lista
    }
}