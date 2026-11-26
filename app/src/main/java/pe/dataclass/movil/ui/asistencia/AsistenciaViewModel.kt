package pe.dataclass.movil.ui.asistencia

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pe.dataclass.movil.data.repo.AsistenciaRepositorio
import pe.dataclass.movil.data.repo.EstudianteLocal
import pe.dataclass.movil.sync.Sincronizacion
import java.time.LocalDate

class AsistenciaViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val repo = AsistenciaRepositorio(aplicacion)

    var aula by mutableStateOf("")
    var fecha by mutableStateOf(LocalDate.now())
    var estudiantes by mutableStateOf<List<EstudianteLocal>>(emptyList())
    var estados by mutableStateOf<Map<String, String>>(emptyMap())
    var mensaje by mutableStateOf<String?>(null)
    var pendientes by mutableStateOf(0)

    val esHoy: Boolean get() = fecha == LocalDate.now()

    // Se llama al elegir un aula (docente: la suya; directivo: la que toque)
    fun abrir(aulaElegida: String) {
        aula = aulaElegida
        fecha = LocalDate.now()
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repo.reintentarRechazados() }
            withContext(Dispatchers.IO) { repo.actualizarEstudiantes(aula) }
                .onFailure { mensaje = it.message }
                .onSuccess { mensaje = null }
            cargarDia()
            Sincronizacion.programar(getApplication())
        }
    }

    // Solo el directivo llama a esta función; no permite ir a fechas futuras
    fun cambiarDia(dias: Long) {
        val nueva = fecha.plusDays(dias)
        if (nueva.isAfter(LocalDate.now())) return
        fecha = nueva
        viewModelScope.launch { cargarDia() }
    }

    fun marcar(idEstudiante: String, estado: String) {
        val dia = fecha.toString()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repo.guardarAsistencia(idEstudiante, dia, estado, false)
            }
            estados = estados + (idEstudiante to estado)
            pendientes = withContext(Dispatchers.IO) { repo.contarPendientes() }
            Sincronizacion.programar(getApplication())
        }
    }

    private suspend fun cargarDia() {
        val dia = fecha.toString()
        estudiantes = withContext(Dispatchers.IO) { repo.estudiantesDelAula(aula) }
        estados = withContext(Dispatchers.IO) { repo.asistenciasDelDia(dia) }
        pendientes = withContext(Dispatchers.IO) { repo.contarPendientes() }
        val total = withContext(Dispatchers.IO) { repo.contarRechazados() }
        if (total > 0) {
            val detalles = withContext(Dispatchers.IO) { repo.listarRechazados() }
            val resto = if (total > detalles.size) " y ${total - detalles.size} más" else ""
            mensaje = "El servidor rechazó $total registro(s): " +
                    detalles.joinToString("; ") + resto + ". Revísalos en la web."
        }
    }

    // Cuenta por separado lo que se enviará solo (pendientes) y lo que el servidor rechazó
    fun contarSinEnviar(alResultado: (Int, Int) -> Unit) {
        viewModelScope.launch {
            val (pendientes, rechazados) = withContext(Dispatchers.IO) {
                repo.contarPendientes() to repo.contarRechazados()
            }
            alResultado(pendientes, rechazados)
        }
    }
}