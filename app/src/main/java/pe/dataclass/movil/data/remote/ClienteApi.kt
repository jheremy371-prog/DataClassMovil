package pe.dataclass.movil.data.remote

import pe.dataclass.movil.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

data class RespuestaLogin(
    val access_token: String,
    val token_type: String,
    val rol: String,
    val nombre_completo: String,
    val aula: String? = null,
    val debe_cambiar: Boolean = false
)

data class CambioPasswordCuerpo(
    val password_actual: String,
    val password_nueva: String
)

data class Aula(
    val nombre: String,
    val grado: String,
    val seccion: String,
    val materias: List<String>,
    val estudiantes: Int
)

interface ApiDataClass {
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") correo: String,
        @Field("password") clave: String
    ): RespuestaLogin

    @POST("auth/cambiar-password")
    suspend fun cambiarPassword(
        @Header("Authorization") bearer: String,
        @Body cuerpo: CambioPasswordCuerpo
    ): RespuestaLogin

    @GET("aulas")
    suspend fun aulas(@Header("Authorization") bearer: String): List<Aula>
}

object ClienteApi {
    val servicio: ApiDataClass by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiDataClass::class.java)
    }
}