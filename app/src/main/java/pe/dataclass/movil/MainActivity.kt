package pe.dataclass.movil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.dataclass.movil.ui.aulas.AulasScreen
import pe.dataclass.movil.ui.home.HomeScreen
import pe.dataclass.movil.ui.login.CambiarPasswordScreen
import pe.dataclass.movil.ui.login.LoginScreen
import pe.dataclass.movil.ui.login.LoginViewModel
import pe.dataclass.movil.ui.theme.DataClassTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DataClassTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val modelo: LoginViewModel = viewModel()
                    var viendoAulas by remember { mutableStateOf(false) }

                    when {
                        !modelo.sesionIniciada -> LoginScreen(modelo)
                        modelo.debeCambiar -> CambiarPasswordScreen(modelo)
                        viendoAulas -> AulasScreen(
                            aulas = modelo.aulas,
                            onVolver = { viendoAulas = false }
                        )
                        else -> HomeScreen(
                            nombre = modelo.nombre,
                            rol = modelo.rol,
                            aula = modelo.aula,
                            onVerAulas = {
                                modelo.cargarAulas()
                                viendoAulas = true
                            }
                        )
                    }
                }
            }
        }
    }
}