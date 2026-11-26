package pe.dataclass.movil.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    nombre: String,
    rol: String,
    aula: String?,
    onVerAulas: () -> Unit,
    onVerEstudiantes: () -> Unit,
    onPasarAsistencia: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val esDirectivo = rol == "directivo"
    val esDocenteConAula = rol == "docente" && !aula.isNullOrBlank()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text(
            if (esDirectivo) "Panel de dirección" else "Mi panel",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text("Hola, $nombre", style = MaterialTheme.typography.headlineMedium)
        Text(
            when {
                esDirectivo -> "Acceso a todas las aulas"
                esDocenteConAula -> "Aula: $aula"
                else -> "Tu cuenta no tiene un aula asignada. Pide a un directivo que la configure."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))

        if (esDirectivo) {
            OpcionInicio("Aulas", "Aulas, materias y número de estudiantes", onVerAulas)
            OpcionInicio("Estudiantes", "Lista completa de estudiantes", onVerEstudiantes)
            OpcionInicio("Reportes", "Próximamente", null)
        }
        if (esDocenteConAula) {
            OpcionInicio("Mi aula", "Materias y estudiantes de $aula", onVerAulas)
            OpcionInicio("Pasar asistencia", "Marca quién asistió hoy", onPasarAsistencia)
        }

        Spacer(Modifier.weight(1f))
        TextButton(
            onClick = onCerrarSesion,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) { Text("Cerrar sesión") }
    }
}

@Composable
private fun OpcionInicio(titulo: String, descripcion: String, onClick: (() -> Unit)?) {
    Card(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(
                descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}