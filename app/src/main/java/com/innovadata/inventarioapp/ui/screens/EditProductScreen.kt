package com.innovadata.inventarioapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.innovadata.inventarioapp.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    viewModel: InventoryViewModel,
    navController: NavHostController,
    productId: Int
) {
    val selectedProduct by viewModel.selectedProduct.collectAsState()

    // Cargar el producto cuando se abre la pantalla
    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    selectedProduct?.let { product ->

        var name by remember(product) { mutableStateOf(product.name) }
        var quantity by remember(product) { mutableStateOf(product.qty.toString()) }
        var price by remember(product) { mutableStateOf(product.price.toString()) }
        var description by remember(product) { mutableStateOf(product.description) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Editar producto") }
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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

                Button(
                    onClick = {
                        val qtyInt = quantity.toIntOrNull() ?: 0
                        val priceDouble = price.toDoubleOrNull() ?: 0.0

                        val updated = product.copy(
                            name = name,
                            qty = qtyInt,
                            price = priceDouble,
                            description = description
                        )
                        viewModel.updateProduct(updated)
                        navController.popBackStack()
                    },
                    enabled = name.isNotBlank() &&
                            quantity.isNotBlank() &&
                            price.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar cambios")
                }
            }
        }
    } ?: run {
        // Si todavía no está cargado el producto, podrías mostrar un texto simple
        Text("Cargando producto...")
    }
}
