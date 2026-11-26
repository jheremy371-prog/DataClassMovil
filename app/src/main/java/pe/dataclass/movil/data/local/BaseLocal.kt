package pe.dataclass.movil.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BaseLocal(contexto: Context) : SQLiteOpenHelper(contexto, "dataclass_local.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE estudiantes_local (
                id_estudiante TEXT PRIMARY KEY,
                nombre_completo TEXT NOT NULL,
                aula TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE asistencias_local (
                id_estudiante TEXT NOT NULL,
                fecha TEXT NOT NULL,
                estado TEXT NOT NULL,
                justificada INTEGER NOT NULL DEFAULT 0,
                is_synced INTEGER NOT NULL DEFAULT 0,
                error_sync TEXT,
                PRIMARY KEY (id_estudiante, fecha)
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, versionAntigua: Int, versionNueva: Int) {
        db.execSQL("DROP TABLE IF EXISTS asistencias_local")
        db.execSQL("DROP TABLE IF EXISTS estudiantes_local")
        onCreate(db)
    }
}