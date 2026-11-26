package pe.dataclass.movil

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.lifecycle.viewmodel.compose.viewModel

import pe.dataclass.movil.ui.aulas.AulasScreen
import pe.dataclass.movil.ui.asistencia.AsistenciaScreen
import pe.dataclass.movil.ui.asistencia.AsistenciaViewModel
import pe.dataclass.movil.ui.home.HomeScreen
import pe.dataclass.movil.ui.login.CambiarPasswordScreen
import pe.dataclass.movil.ui.login.LoginScreen
import pe.dataclass.movil.ui.login.LoginViewModel
import pe.dataclass.movil.ui.theme.DataClassTheme
import pe.dataclass.movil.ui.estudiantes.EstudiantesScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DataClassTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val modelo: LoginViewModel = viewModel()
                    val asistenciaVm: AsistenciaViewModel = viewModel()
                    var viendoAulas by remember { mutableStateOf(false) }
                    var viendoAsistencia by remember { mutableStateOf(false) }
                    var viendoEstudiantes by remember { mutableStateOf(false) }
                    var avisoCierre by remember { mutableStateOf<Pair<Int, Int>?>(null) }

                    val cerrarTodo: () -> Unit = {
                        viendoAulas = false
                        viendoAsistencia = false
                        viendoEstudiantes = false
                        modelo.cerrarSesion()
                    }

                    avisoCierre?.let { (pendientes, rechazados) ->
                        val texto = when {
                            pendientes > 0 && rechazados > 0 ->
                                "Quedan $pendientes registro(s) sin enviar y $rechazados rechazado(s) por el servidor. " +
                                        "Si cierras sesión, se perderán."
                            pendientes > 0 ->
                                "Quedan $pendientes registro(s) sin enviar al servidor. " +
                                        "Si cierras sesión ahora, se perderán. Conéctate y espera a que se envíen."
                            else ->
                                "Hay $rechazados registro(s) que el servidor rechazó. " +
                                        "Si cierras sesión, se perderán; corrígelos antes si quieres conservarlos."
                        }
                        AlertDialog(
                            onDismissRequest = { avisoCierre = null },
                            title = { Text("Asistencias sin enviar") },
                            text = { Text(texto) },
                            confirmButton = {
                                TextButton(onClick = {
                                    avisoCierre = null
                                    cerrarTodo()
                                }) { Text("Cerrar igual") }
                            },
                            dismissButton = {
                                TextButton(onClick = { avisoCierre = null }) { Text("Cancelar") }
                            }
                        )
                    }

                    when {
                        modelo.restaurando -> Box(modifier = Modifier.fillMaxSize()) {}
                        !modelo.sesionIniciada -> LoginScreen(modelo)
                        modelo.debeCambiar -> CambiarPasswordScreen(modelo)
                        viendoAulas -> AulasScreen(
                            aulas = modelo.aulas,
                            error = modelo.error,
                            onVolver = { viendoAulas = false }
                        )
                        viendoAsistencia -> AsistenciaScreen(
                            aula = modelo.aula.orEmpty(),
                            fecha = asistenciaVm.fecha,
                            estudiantes = asistenciaVm.estudiantes,
                            estados = asistenciaVm.estados,
                            pendientes = asistenciaVm.pendientes,
                            mensaje = asistenciaVm.mensaje,
                            onMarcar = { id, estado -> asistenciaVm.marcar(id, estado) },
                            onVolver = { viendoAsistencia = false }
                        )
                        viendoEstudiantes -> EstudiantesScreen(
                            estudiantes = modelo.estudiantes,
                            error = modelo.error,
                            onVolver = { viendoEstudiantes = false }
                        )

                        else -> HomeScreen(
                            nombre = modelo.nombre,
                            rol = modelo.rol,
                            aula = modelo.aula,
                            onVerAulas = {
                                modelo.cargarAulas()
                                viendoAulas = true
                            },
                            onVerEstudiantes = {
                                modelo.cargarEstudiantes()
                                viendoEstudiantes = true
                            },
                            onPasarAsistencia = {
                                asistenciaVm.cargar()
                                viendoAsistencia = true
                            },
                            onCerrarSesion = {
                                asistenciaVm.contarSinEnviar { pendientes, rechazados ->
                                    if (pendientes == 0 && rechazados == 0) {
                                        cerrarTodo()
                                    } else {
                                        avisoCierre = pendientes to rechazados
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}