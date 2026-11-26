package pe.dataclass.movil.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.almacenSesion by preferencesDataStore(name = "sesion")

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

    suspend fun guardar(token: String, rol: String, nombre: String, aula: String?, debeCambiar: Boolean) {
        contexto.almacenSesion.edit {
            it[claveToken] = token
            it[claveRol] = rol
            it[claveNombre] = nombre
            it[claveDebeCambiar] = debeCambiar
            if (aula != null) it[claveAula] = aula else it.remove(claveAula)
        }
    }

    suspend fun leer(): SesionGuardada? {
        val datos = contexto.almacenSesion.data.first()
        val token = datos[claveToken] ?: return null
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