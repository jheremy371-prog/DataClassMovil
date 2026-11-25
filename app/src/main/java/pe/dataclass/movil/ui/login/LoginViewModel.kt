package pe.dataclass.movil.ui.login

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.dataclass.movil.data.repo.AuthRepositorio

class LoginViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val repositorio = AuthRepositorio(aplicacion)

    var correo by mutableStateOf("")
    var clave by mutableStateOf("")
    var cargando by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var sesionIniciada by mutableStateOf(false)
    var nombre by mutableStateOf("")

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
                    nombre = it
                    sesionIniciada = true
                }
                .onFailure { error = it.message }
            cargando = false
        }
    }
}