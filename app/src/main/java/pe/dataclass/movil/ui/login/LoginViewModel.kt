package pe.dataclass.movil.ui.login

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.dataclass.movil.data.repo.AuthRepositorio
import pe.dataclass.movil.data.remote.Aula

class LoginViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val repositorio = AuthRepositorio(aplicacion)

    // Login
    var correo by mutableStateOf("")
    var clave by mutableStateOf("")
    var cargando by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var sesionIniciada by mutableStateOf(false)
    var nombre by mutableStateOf("")
    var rol by mutableStateOf("")
    var aula by mutableStateOf<String?>(null)

    var aulas by mutableStateOf<List<Aula>>(emptyList())
    var debeCambiar by mutableStateOf(false)

    // Cambio de contraseña
    var claveActual by mutableStateOf("")
    var claveNueva by mutableStateOf("")
    var claveConfirmar by mutableStateOf("")

    fun entrar() {
        if (correo.isBlank() || clave.isBlank()) {
            error = "Escribe tu correo y contraseña."
            return
        }
        cargando = true
        error = null
        viewModelScope.launch {
            repositorio.iniciarSesion(correo, clave)
                .onSuccess {
                    nombre = it.nombre_completo
                    rol = it.rol
                    aula = it.aula
                    debeCambiar = it.debe_cambiar
                    clave = ""
                    sesionIniciada = true
                }
                .onFailure { error = it.message }
            cargando = false
        }
    }

    fun cargarAulas() {
        viewModelScope.launch {
            repositorio.listarAulas()
                .onSuccess { aulas = it }
                .onFailure { error = it.message }
        }
    }

    fun cambiarClave() {
        error = null
        if (claveNueva != claveConfirmar) {
            error = "La contraseña nueva y su confirmación no coinciden."
            return
        }
        if (claveNueva == claveActual) {
            error = "La contraseña nueva debe ser distinta de la actual."
            return
        }
        cargando = true
        viewModelScope.launch {
            repositorio.cambiarPassword(claveActual, claveNueva)
                .onSuccess {
                    debeCambiar = false
                    claveActual = ""
                    claveNueva = ""
                    claveConfirmar = ""
                }
                .onFailure { error = it.message }
            cargando = false
        }
    }
}