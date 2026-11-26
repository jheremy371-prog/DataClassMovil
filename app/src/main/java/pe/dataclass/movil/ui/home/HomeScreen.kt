package pe.dataclass.movil.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(nombre: String, rol: String, aula: String?, onVerAulas: () -> Unit) {
    val puedeVerAulas = rol == "directivo" || !aula.isNullOrBlank()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bienvenido, $nombre", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            when {
                rol == "directivo" -> "Acceso: todas las aulas."
                aula.isNullOrBlank() -> "Tu cuenta no tiene un aula asignada. Pide a un directivo que la configure."
                else -> "Tu aula: $aula"
            },
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))
        if (puedeVerAulas) {
            Button(onClick = onVerAulas) { Text("Ver aulas") }
        }
    }
}