package pe.dataclass.movil.ui.estudiantes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.dataclass.movil.data.remote.EstudianteApi

@Composable
fun EstudiantesScreen(estudiantes: List<EstudianteApi>, error: String?, onVolver: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
        TextButton(onClick = onVolver) { Text("Volver") }
        Text("Estudiantes", style = MaterialTheme.typography.titleLarge)
        Text("${estudiantes.size} en total", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))

        when {
            error != null -> Text(error, color = MaterialTheme.colorScheme.error)
            estudiantes.isEmpty() -> Text("No hay estudiantes para mostrar.")
            else -> LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(estudiantes, key = { it.id_estudiante }) { e ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(e.nombre_completo, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Aula ${e.aula.orEmpty()} · ${e.id_estudiante}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}