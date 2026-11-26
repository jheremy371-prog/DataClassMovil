package pe.dataclass.movil.ui.login

import android.app.Application

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


import pe.dataclass.movil.data.remote.Aula
import pe.dataclass.movil.data.repo.AsistenciaRepositorio
import pe.dataclass.movil.data.repo.AuthRepositorio
import pe.dataclass.movil.sync.Sincronizacion
import pe.dataclass.movil.data.repo.SesionExpiradaException
import pe.dataclass.movil.data.remote.EstudianteApi

class LoginViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val repositorio = AuthRepositorio(aplicacion)

    // Estado de la sesión
    var restaurando by mutableStateOf(true)
    var sesionIniciada by mutableStateOf(false)
    var nombre by mutableStateOf("")
    var rol by mutableStateOf("")

    var aula by mutableStateOf<String?>(null)

    var estudiantes by mutableStateOf<List<EstudianteApi>>(emptyList())
    var debeCambiar by mutableStateOf(false)
    var aulas by mutableStateOf<List<Aula>>(emptyList())

    // Login
    var correo by mutableStateOf("")
    var clave by mutableStateOf("")
    var cargando by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    // Cambio de contraseña
    var claveActual by mutableStateOf("")
    var claveNueva by mutableStateOf("")
    var claveConfirmar by mutableStateOf("")

    init {
        viewModelScope.launch {
            repositorio.sesionGuardada()?.let { s ->
                nombre = s.nombre
                rol = s.rol
                aula = s.aula
                debeCambiar = s.debeCambiar
                sesionIniciada = true
            }
            restaurando = false
        }
    }

    fun entrar() {
        if (correo.isBlank() || clave.isBlank()) {
            error = "Escribe tu correo y contraseña."
            return
        }
        cargando = true
        error = null
        viewModelScope.launch {
            val correoNuevo = correo.trim().lowercase()

            // Si hay asistencias pendientes de otra cuenta, no se deja entrar con esta
            val bloqueadas = withContext(Dispatchers.IO) {
                val pendientes = AsistenciaRepositorio(getApplication()).contarPendientes()
                val dueno = repositorio.ultimoCorreo()
                if (pendientes > 0 && dueno != null && dueno != correoNuevo) pendientes else 0
            }
            if (bloqueadas > 0) {
                error = "Este teléfono tiene $bloqueadas asistencia(s) sin enviar de otra cuenta. " +
                        "Inicia sesión con esa cuenta para enviarlas."
                cargando = false
                return@launch
            }

            repositorio.iniciarSesion(correo, clave)
                .onSuccess {
                    nombre = it.nombre_completo
                    rol = it.rol
                    aula = it.aula
                    debeCambiar = it.debe_cambiar
                    clave = ""
                    sesionIniciada = true
                    // Si quedaron pendientes de esta misma cuenta, se envían ahora
                    Sincronizacion.programar(getApplication())
                }
                .onFailure { error = it.message }
            cargando = false
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

    fun cargarAulas() {
        error = null
        viewModelScope.launch {
            repositorio.listarAulas()
                .onSuccess { aulas = it }
                .onFailure {
                    if (it is SesionExpiradaException) {
                        sesionIniciada = false
                        aulas = emptyList()
                    }
                    error = it.message
                }
        }
    }

    fun cargarEstudiantes() {
        error = null
        viewModelScope.launch {
            repositorio.listarEstudiantes()
                .onSuccess { estudiantes = it }
                .onFailure {
                    if (it is SesionExpiradaException) {
                        sesionIniciada = false
                        estudiantes = emptyList()
                    }
                    error = it.message
                }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                AsistenciaRepositorio(getApplication()).borrarTodo()
            }
            repositorio.cerrarSesion()
            sesionIniciada = false
            nombre = ""
            rol = ""
            aula = null
            debeCambiar = false
            aulas = emptyList()
            correo = ""
            clave = ""
            error = null
        }
    }
}