package pe.dataclass.movil.ui.asistencia

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.dataclass.movil.data.repo.EstudianteLocal

private val opciones = listOf(
    "presente" to "P",
    "tardanza" to "T",
    "falta" to "F",
    "justificada" to "J"
)

@Composable
fun AsistenciaScreen(
    aula: String,
    fecha: String,
    estudiantes: List<EstudianteLocal>,
    estados: Map<String, String>,
    pendientes: Int,
    mensaje: String?,
    onMarcar: (String, String) -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        TextButton(onClick = onVolver) { Text("Volver") }
        Text("Asistencia · $aula", style = MaterialTheme.typography.titleLarge)
        Text(
            "Fecha: $fecha · Pendientes de enviar: $pendientes",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            "P presente · T tardanza · F falta · J justificada",
            style = MaterialTheme.typography.bodySmall
        )
        mensaje?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(8.dp))

        if (estudiantes.isEmpty()) {
            Text("No hay estudiantes guardados. Conéctate para descargarlos.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(estudiantes, key = { it.idEstudiante }) { e ->
                    FilaEstudiante(
                        nombre = e.nombre,
                        estadoActual = estados[e.idEstudiante],
                        onElegir = { estado -> onMarcar(e.idEstudiante, estado) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaEstudiante(nombre: String, estadoActual: String?, onElegir: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(nombre, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                opciones.forEach { (estado, letra) ->
                    if (estadoActual == estado) {
                        Button(
                            onClick = { onElegir(estado) },
                            modifier = Modifier.weight(1f)
                        ) { Text(letra) }
                    } else {
                        OutlinedButton(
                            onClick = { onElegir(estado) },
                            modifier = Modifier.weight(1f)
                        ) { Text(letra) }
                    }
                }
            }
        }
    }
}