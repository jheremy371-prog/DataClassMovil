package pe.dataclass.movil.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun CambiarPasswordScreen(modelo: LoginViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Cambia tu contraseña", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "Tu cuenta necesita una contraseña nueva antes de continuar.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))

        CampoClave("Contraseña actual", modelo.claveActual) { modelo.claveActual = it }
        Spacer(Modifier.height(12.dp))
        CampoClave("Contraseña nueva", modelo.claveNueva) { modelo.claveNueva = it }
        Spacer(Modifier.height(12.dp))
        CampoClave("Confirmar contraseña nueva", modelo.claveConfirmar) { modelo.claveConfirmar = it }
        Spacer(Modifier.height(16.dp))

        modelo.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = { modelo.cambiarClave() },
            enabled = !modelo.cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (modelo.cargando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Guardar contraseña")
            }
        }
    }
}

@Composable
private fun CampoClave(etiqueta: String, valor: String, alCambiar: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth()
    )
}