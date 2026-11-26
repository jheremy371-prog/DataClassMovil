package pe.dataclass.movil.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.almacenSesion by preferencesDataStore(name = "sesion")

// Debe coincidir con ACCESS_TOKEN_EXPIRE_MINUTES de la API (480 minutos = 8 horas)
private const val DURACION_SESION_MS = 8L * 60 * 60 * 1000

data class SesionGuardada(
    val token: String,
    val rol: String,
    val nombre: String,
    val aula: String?,
    val debeCambiar: Boolean
)

class SesionLocal(private val contexto: Context) {
    private val claveToken = stringPreferencesKey("token")
    private val claveRol = stringPreferencesKey("rol")
    private val claveNombre = stringPreferencesKey("nombre")
    private val claveAula = stringPreferencesKey("aula")
    private val claveDebeCambiar = booleanPreferencesKey("debe_cambiar")
    private val claveGuardadoEn = longPreferencesKey("guardado_en")

    suspend fun guardar(token: String, rol: String, nombre: String, aula: String?, debeCambiar: Boolean) {
        contexto.almacenSesion.edit {
            it[claveToken] = token
            it[claveRol] = rol
            it[claveNombre] = nombre
            it[claveDebeCambiar] = debeCambiar
            it[claveGuardadoEn] = System.currentTimeMillis()
            if (aula != null) it[claveAula] = aula else it.remove(claveAula)
        }
    }

    suspend fun leer(): SesionGuardada? {
        val datos = contexto.almacenSesion.data.first()
        val token = datos[claveToken] ?: return null
        val guardadoEn = datos[claveGuardadoEn] ?: 0L

        // Si la sesión ya venció, se borra y la app pedirá iniciar sesión otra vez
        if (System.currentTimeMillis() - guardadoEn > DURACION_SESION_MS) {
            cerrar()
            return null
        }

        return SesionGuardada(
            token = token,
            rol = datos[claveRol].orEmpty(),
            nombre = datos[claveNombre].orEmpty(),
            aula = datos[claveAula],
            debeCambiar = datos[claveDebeCambiar] ?: false
        )
    }

    suspend fun token(): String? = leer()?.token

    suspend fun cerrar() {
        contexto.almacenSesion.edit { it.clear() }
    }
}