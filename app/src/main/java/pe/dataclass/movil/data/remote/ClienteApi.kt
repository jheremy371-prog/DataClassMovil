package pe.dataclass.movil.data.remote

import pe.dataclass.movil.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

data class RespuestaLogin(
    val access_token: String,
    val token_type: String,
    val rol: String,
    val nombre_completo: String,
    val debe_cambiar: Boolean = false
)

interface ApiDataClass {
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") correo: String,
        @Field("password") clave: String
    ): RespuestaLogin
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