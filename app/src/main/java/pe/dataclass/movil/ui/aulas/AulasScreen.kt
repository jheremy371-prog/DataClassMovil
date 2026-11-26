package pe.dataclass.movil.ui.aulas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.dataclass.movil.data.remote.Aula

@Composable
fun AulasScreen(aulas: List<Aula>, error: String?, onVolver: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onVolver) { Text("Volver") }
        Text("Aulas", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        when {
            error != null -> Text(error, color = MaterialTheme.colorScheme.error)
            aulas.isEmpty() -> Text("No hay aulas para mostrar.")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(aulas) { aula -> TarjetaAula(aula) }
            }
        }
    }
}

@Composable
private fun TarjetaAula(aula: Aula) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(aula.nombre, style = MaterialTheme.typography.titleMedium)
            Text("${aula.estudiantes} estudiantes", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(aula.materias.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
        }
    }
}