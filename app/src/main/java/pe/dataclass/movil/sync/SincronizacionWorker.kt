package pe.dataclass.movil.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import pe.dataclass.movil.data.repo.AsistenciaRepositorio
import pe.dataclass.movil.data.repo.SesionExpiradaException
import java.io.IOException

class SincronizacionWorker(contexto: Context, parametros: WorkerParameters) :
    CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        return try {
            AsistenciaRepositorio(applicationContext).sincronizarPendientes()
            Result.success()
        } catch (e: IOException) {
            Result.retry()
        } catch (e: SesionExpiradaException) {
            Result.failure()
        }
    }
}

object Sincronizacion {
    fun programar(contexto: Context) {
        val trabajo = OneTimeWorkRequestBuilder<SincronizacionWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(contexto).enqueueUniqueWork(
            "sincronizar_asistencias",
            ExistingWorkPolicy.REPLACE,
            trabajo
        )
    }
}