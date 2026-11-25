package pe.dataclass.movil.data.repo

import android.content.Context
import pe.dataclass.movil.data.local.SesionLocal
import pe.dataclass.movil.data.remote.ClienteApi
import retrofit2.HttpException
import java.io.IOException

class AuthRepositorio(contexto: Context) {
    private val sesion = SesionLocal(contexto)

    suspend fun iniciarSesion(correo: String, clave: String): Result<String> {
        return try {
            val respuesta = ClienteApi.servicio.login(correo.trim().lowercase(), clave)
            sesion.guardar(respuesta.access_token, respuesta.rol, respuesta.nombre_completo)
            Result.success(respuesta.nombre_completo)
        } catch (e: HttpException) {
            val mensaje = when (e.code()) {
                401 -> "Correo o contraseña incorrectos."
                429 -> "Demasiados intentos. Espera unos minutos."
                else -> "El servidor respondió con error ${e.code()}."
            }
            Result.failure(Exception(mensaje))
        } catch (e: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor."))
        }
    }
}