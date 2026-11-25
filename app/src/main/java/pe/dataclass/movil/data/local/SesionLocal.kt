package pe.dataclass.movil.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.almacenSesion by preferencesDataStore(name = "sesion")

class SesionLocal(private val contexto: Context) {
    private val claveToken = stringPreferencesKey("token")
    private val claveRol = stringPreferencesKey("rol")
    private val claveNombre = stringPreferencesKey("nombre")

    suspend fun guardar(token: String, rol: String, nombre: String) {
        contexto.almacenSesion.edit {
            it[claveToken] = token
            it[claveRol] = rol
            it[claveNombre] = nombre
        }
    }

    suspend fun token(): String? =
        contexto.almacenSesion.data.map { it[claveToken] }.first()

    suspend fun cerrar() {
        contexto.almacenSesion.edit { it.clear() }
    }
}