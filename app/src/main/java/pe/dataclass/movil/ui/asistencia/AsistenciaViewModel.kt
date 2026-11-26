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
    val fecha: String = LocalDate.now().toString()

    var estudiantes by mutableStateOf<List<EstudianteLocal>>(emptyList())
    var estados by mutableStateOf<Map<String, String>>(emptyMap())
    var mensaje by mutableStateOf<String?>(null)
    var pendientes by mutableStateOf(0)

    fun cargar() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repo.actualizarEstudiantes() }
                .onFailure { mensaje = it.message }
                .onSuccess { mensaje = null }

            estudiantes = withContext(Dispatchers.IO) { repo.estudiantesDelAula() }
            estados = withContext(Dispatchers.IO) { repo.asistenciasDelDia(fecha) }
            pendientes = withContext(Dispatchers.IO) { repo.contarPendientes() }
            val rechazados = withContext(Dispatchers.IO) { repo.contarRechazados() }
            if (rechazados > 0) {
                mensaje = "$rechazados registro(s) rechazados por el servidor; revísalos en la web."
            }
            Sincronizacion.programar(getApplication())
        }
    }

    fun marcar(idEstudiante: String, estado: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repo.guardarAsistencia(idEstudiante, fecha, estado, false)
            }
            estados = estados + (idEstudiante to estado)
            pendientes = withContext(Dispatchers.IO) { repo.contarPendientes() }
            Sincronizacion.programar(getApplication())
        }
    }
}