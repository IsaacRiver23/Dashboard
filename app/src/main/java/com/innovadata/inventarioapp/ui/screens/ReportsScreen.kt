package com.innovadata.inventarioapp.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.innovadata.inventarioapp.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: InventoryViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Reportes") }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Texto descriptivo arriba
            Text(
                text = "Generación de reportes",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Aquí puedes exportar tu inventario completo a un archivo PDF para impresión o respaldo.",
                style = MaterialTheme.typography.bodyMedium
            )

            // 🔽 Espacio para bajar el botón
            Spacer(Modifier.height(48.dp))

            Button(
                onClick = {
                    scope.launch {
                        try {
                            val uri = viewModel.exportToPdf()

                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)

                            snackbarHostState.showSnackbar("PDF generado correctamente")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            snackbarHostState.showSnackbar("Error al generar el PDF")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Exportar inventario a PDF")
            }

            // Si quieres todavía más abajo, puedes sumar más espacio aquí:
            // Spacer(Modifier.weight(1f))
        }
    }
}
