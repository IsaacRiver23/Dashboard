package com.innovadata.inventarioapp.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.innovadata.inventarioapp.data.local.ProductEntity
import com.innovadata.inventarioapp.viewmodel.InventoryViewModel

// Colores estilo Dashboard
private val DashboardBackground = Color(0xFF101017)
private val DashboardCard = Color(0xFF5C3B8E)
private val DashboardButton = Color(0xFF2C464F)
private val DashboardButtonText = Color.White
private val DashboardText = Color(0xFFEAEAEA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavController,
    productId: Int,
    viewModel: InventoryViewModel
) {
    val productState by viewModel.selectedProduct.collectAsState()

    // Cargar producto al entrar
    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    Scaffold(
        containerColor = DashboardBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalle del producto", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DashboardBackground
                )
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            if (productState == null) {
                // Aún cargando o no encontrado
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            } else {
                val product: ProductEntity = productState!!

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Tarjeta morada con imagen + datos
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = DashboardCard
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            // IMAGEN (si existe)
                            if (!product.imagePath.isNullOrBlank()) {
                                val img = remember(product.imagePath) {
                                    Uri.parse(product.imagePath)
                                }
                                Image(
                                    painter = rememberAsyncImagePainter(model = img),
                                    contentDescription = "Imagen del producto",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // DATOS
                            Text(
                                text = "Nombre: ${product.name}",
                                color = DashboardText
                            )
                            Text(
                                text = "Cantidad: ${product.qty}",
                                color = DashboardText
                            )
                            Text(
                                text = "Precio: $${product.price}",
                                color = DashboardText
                            )
                            Text(
                                text = "Descripción: ${product.description}",
                                color = DashboardText
                            )
                        }
                    }

                    // VENDER 1
                    Button(
                        onClick = { viewModel.sellOne(product) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DashboardButton,
                            contentColor = DashboardButtonText
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Vender 1 unidad")
                    }

                    // EDITAR
                    Button(
                        onClick = {
                            navController.navigate("edit/${product.id}")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DashboardButton,
                            contentColor = DashboardButtonText
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Editar")
                    }

                    // ELIMINAR
                    Button(
                        onClick = {
                            viewModel.deleteProduct(product)
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEA6A6A),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}
