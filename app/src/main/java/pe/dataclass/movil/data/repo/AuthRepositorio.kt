package pe.dataclass.movil.data.repo

import android.content.Context
import pe.dataclass.movil.data.local.SesionLocal
import pe.dataclass.movil.data.remote.Aula
import pe.dataclass.movil.data.remote.CambioPasswordCuerpo
import pe.dataclass.movil.data.remote.ClienteApi
import pe.dataclass.movil.data.remote.RespuestaLogin
import retrofit2.HttpException
import java.io.IOException

class AuthRepositorio(contexto: Context) {
    private val sesion = SesionLocal(contexto)

    suspend fun iniciarSesion(correo: String, clave: String): Result<RespuestaLogin> =
        ejecutar {
            val respuesta = ClienteApi.servicio.login(correo.trim().lowercase(), clave)
            sesion.guardar(respuesta.access_token, respuesta.rol, respuesta.nombre_completo)
            respuesta
        }

    suspend fun cambiarPassword(actual: String, nueva: String): Result<RespuestaLogin> {
        val token = sesion.token()
            ?: return Result.failure(Exception("Tu sesión expiró. Inicia sesión nuevamente."))
        return ejecutar {
            val respuesta = ClienteApi.servicio.cambiarPassword(
                "Bearer $token",
                CambioPasswordCuerpo(actual, nueva)
            )
            sesion.guardar(respuesta.access_token, respuesta.rol, respuesta.nombre_completo)
            respuesta
        }
    }

    suspend fun listarAulas(): Result<List<Aula>> {
        val token = sesion.token()
            ?: return Result.failure(Exception("Tu sesión expiró. Inicia sesión nuevamente."))
        return ejecutar {
            ClienteApi.servicio.aulas("Bearer $token")
        }
    }

    private suspend fun <T> ejecutar(bloque: suspend () -> T): Result<T> = try {
        Result.success(bloque())
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: HttpException) {
        Result.failure(Exception(mensajeDeError(e)))
    } catch (e: IOException) {
        Result.failure(Exception("No se pudo conectar con el servidor."))
    } catch (e: Exception) {
        Result.failure(Exception("Error al leer la respuesta del servidor: ${e.message}"))
    }

    // La API envía el motivo en {"detail": "..."}; si no viene, usamos un texto según el código
    private fun mensajeDeError(e: HttpException): String {
        val cuerpo = e.response()?.errorBody()?.string().orEmpty()
        val detalle = Regex("\"detail\"\\s*:\\s*\"([^\"]+)\"").find(cuerpo)?.groupValues?.get(1)
        return detalle ?: when (e.code()) {
            401 -> "Correo o contraseña incorrectos."
            429 -> "Demasiados intentos. Espera unos minutos."
            else -> "El servidor respondió con error ${e.code()}."
        }
    }

}