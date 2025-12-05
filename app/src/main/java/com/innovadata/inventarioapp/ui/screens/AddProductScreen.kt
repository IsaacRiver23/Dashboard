package com.innovadata.inventarioapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.innovadata.inventarioapp.viewmodel.InventoryViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavHostController,
    viewModel: InventoryViewModel
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }      // solo texto numérico
    var price by remember { mutableStateOf("") }         // solo texto numérico
    var description by remember { mutableStateOf("") }

    // 🔹 Lo importante es este estado: la ruta que guardaremos en BD
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imagePath by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // Lanzador que toma la foto COMPLETA en el URI que le pasemos
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUri != null) {
            // Si la foto se tomó bien, aseguramos que imagePath lleve el mismo URI
            imagePath = imageUri.toString()
        } else {
            imageUri = null
            imagePath = null
        }
    }

    // Función que crea el archivo y abre la cámara
    fun openCamera() {
        val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: context.filesDir   // respaldo por si acaso

        val file = File.createTempFile("product_", ".jpg", picturesDir)

        // Autoridad = ${applicationId}.provider  (ya definida en el manifest)
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )

        imageUri = uri          // lo usa TakePicture
        imagePath = null        // se fijará en el callback si fue success
        cameraLauncher.launch(uri)
    }

    // Lanzador para pedir permiso de cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openCamera()
        }
    }

    fun launchCamera() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            openCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar producto") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            // CANTIDAD: solo dígitos
            OutlinedTextField(
                value = quantity,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                        quantity = newValue
                    }
                },
                label = { Text("Cantidad") },
                modifier = Modifier.fillMaxWidth()
            )

            // PRECIO: solo números y un punto decimal
            OutlinedTextField(
                value = price,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() ||
                        newValue.matches(Regex("""\d*\.?\d*"""))
                    ) {
                        price = newValue
                    }
                },
                label = { Text("Precio") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            // 🔹 PREVIEW USANDO imagePath como Uri y sin recortar
            if (imagePath != null) {
                val previewModel = remember(imagePath) { Uri.parse(imagePath) }

                Image(
                    painter = rememberAsyncImagePainter(model = previewModel),
                    contentDescription = "Foto del producto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),    // un poco más alto
                    contentScale = ContentScale.Fit   // 👈 ya no recorta, solo ajusta
                )
            }


            Button(
                onClick = { launchCamera() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tomar foto")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val qtyInt = quantity.toIntOrNull() ?: 0
                    val priceDouble = price.toDoubleOrNull() ?: 0.0

                    viewModel.addProduct(
                        name = name,
                        qty = qtyInt,
                        desc = description,
                        price = priceDouble,
                        imagePath = imagePath   // 👈 lo que verá el DetailScreen
                    )
                    navController.popBackStack()
                },
                enabled = name.isNotBlank() &&
                        quantity.isNotBlank() &&
                        price.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar producto")
            }
        }
    }
}

