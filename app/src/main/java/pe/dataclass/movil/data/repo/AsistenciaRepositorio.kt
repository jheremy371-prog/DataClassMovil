package pe.dataclass.movil.data.repo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import pe.dataclass.movil.data.local.BaseLocal
import pe.dataclass.movil.data.local.SesionLocal
import pe.dataclass.movil.data.remote.AsistenciasLoteCuerpo
import pe.dataclass.movil.data.remote.ClienteApi
import pe.dataclass.movil.data.remote.RegistroLote
import retrofit2.HttpException
import java.io.IOException

data class EstudianteLocal(val idEstudiante: String, val nombre: String, val aula: String)

private data class PendienteLocal(
    val idEstudiante: String,
    val fecha: String,
    val estado: String,
    val justificada: Boolean
)

class AsistenciaRepositorio(contexto: Context) {
    private val base = BaseLocal(contexto)
    private val sesion = SesionLocal(contexto)

    // Descarga los estudiantes del aula y reemplaza la copia local
    suspend fun actualizarEstudiantes(): Result<Int> {
        val token = sesion.token()
            ?: return Result.failure(Exception("Tu sesión expiró. Inicia sesión nuevamente."))
        return try {
            val estudiantes = ClienteApi.servicio.estudiantes("Bearer $token").items
            val db = base.writableDatabase
            db.beginTransaction()
            try {
                db.delete("estudiantes_local", null, null)
                for (e in estudiantes) {
                    db.insert("estudiantes_local", null, ContentValues().apply {
                        put("id_estudiante", e.id_estudiante)
                        put("nombre_completo", e.nombre_completo)
                        put("aula", e.aula.orEmpty())
                    })
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            Result.success(estudiantes.size)
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión: se usan los estudiantes guardados."))
        } catch (e: HttpException) {
            Result.failure(Exception("El servidor respondió con error ${e.code()}."))
        }
    }

    fun estudiantesDelAula(): List<EstudianteLocal> {
        val lista = mutableListOf<EstudianteLocal>()
        base.readableDatabase.query(
            "estudiantes_local",
            arrayOf("id_estudiante", "nombre_completo", "aula"),
            null, null, null, null, "nombre_completo"
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(EstudianteLocal(c.getString(0), c.getString(1), c.getString(2)))
            }
        }
        return lista
    }

    // Devuelve estudiante -> estado para una fecha
    fun asistenciasDelDia(fecha: String): Map<String, String> {
        val mapa = mutableMapOf<String, String>()
        base.readableDatabase.query(
            "asistencias_local",
            arrayOf("id_estudiante", "estado"),
            "fecha = ?", arrayOf(fecha),
            null, null, null
        ).use { c ->
            while (c.moveToNext()) {
                mapa[c.getString(0)] = c.getString(1)
            }
        }
        return mapa
    }

    // Guarda o corrige la asistencia; queda pendiente de envío
    fun guardarAsistencia(idEstudiante: String, fecha: String, estado: String, justificada: Boolean) {
        val valores = ContentValues().apply {
            put("id_estudiante", idEstudiante)
            put("fecha", fecha)
            put("estado", estado)
            put("justificada", if (justificada) 1 else 0)
            put("is_synced", 0)
            putNull("error_sync")
        }
        base.writableDatabase.insertWithOnConflict(
            "asistencias_local", null, valores, SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    // Quita el error de los rechazados para que se reintenten al abrir la pantalla
    fun reintentarRechazados() {
        val valores = ContentValues().apply { putNull("error_sync") }
        base.writableDatabase.update(
            "asistencias_local", valores, "is_synced = 0 AND error_sync IS NOT NULL", null
        )
    }

    fun contarPendientes(): Int =
        base.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM asistencias_local WHERE is_synced = 0 AND error_sync IS NULL",
            null
        ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }

    fun contarRechazados(): Int =
        base.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM asistencias_local WHERE is_synced = 0 AND error_sync IS NOT NULL",
            null
        ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }

    // Envía las asistencias pendientes, agrupadas por fecha.
    // Lanza IOException si no hay red o el servidor falla (WorkManager reintenta)
    // y SesionExpiradaException si el token venció.
    suspend fun sincronizarPendientes(): Int {
        val token = sesion.token() ?: throw SesionExpiradaException()
        var enviadas = 0

        for ((fecha, grupo) in pendientesSinError().groupBy { it.fecha }) {
            val ids = grupo.map { it.idEstudiante }
            try {
                ClienteApi.servicio.enviarAsistencias(
                    "Bearer $token",
                    AsistenciasLoteCuerpo(
                        fecha = fecha,
                        registros = grupo.map {
                            RegistroLote(it.idEstudiante, it.estado, it.justificada)
                        }
                    )
                )
                actualizarRegistros(fecha, ids, sincronizado = true, error = null)
                enviadas += grupo.size
            } catch (e: HttpException) {
                when (e.code()) {
                    401 -> throw SesionExpiradaException()
                    in 400..499 -> actualizarRegistros(
                        fecha, ids, sincronizado = false, error = detalleError(e)
                    )
                    else -> throw IOException("Error ${e.code()} del servidor")
                }
            }
        }
        return enviadas
    }

    // Se llama al cerrar sesión, para que otro docente no vea estos datos
    fun borrarTodo() {
        val db = base.writableDatabase
        db.delete("asistencias_local", null, null)
        db.delete("estudiantes_local", null, null)
    }

    private fun pendientesSinError(): List<PendienteLocal> {
        val lista = mutableListOf<PendienteLocal>()
        base.readableDatabase.query(
            "asistencias_local",
            arrayOf("id_estudiante", "fecha", "estado", "justificada"),
            "is_synced = 0 AND error_sync IS NULL",
            null, null, null, "fecha"
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    PendienteLocal(
                        c.getString(0), c.getString(1), c.getString(2), c.getInt(3) == 1
                    )
                )
            }
        }
        return lista
    }

    private fun actualizarRegistros(
        fecha: String,
        ids: List<String>,
        sincronizado: Boolean,
        error: String?
    ) {
        val valores = ContentValues().apply {
            put("is_synced", if (sincronizado) 1 else 0)
            if (error != null) put("error_sync", error) else putNull("error_sync")
        }
        val db = base.writableDatabase
        db.beginTransaction()
        try {
            for (id in ids) {
                db.update("asistencias_local", valores, "id_estudiante = ? AND fecha = ?", arrayOf(id, fecha))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun detalleError(e: HttpException): String {
        val cuerpo = e.response()?.errorBody()?.string().orEmpty()
        return Regex("\"detail\"\\s*:\\s*\"([^\"]+)\"").find(cuerpo)?.groupValues?.get(1)
            ?: "El servidor rechazó el registro (${e.code()})."
    }
}