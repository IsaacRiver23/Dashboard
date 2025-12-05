package com.innovadata.inventarioapp.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.innovadata.inventarioapp.data.local.ProductEntity
import com.innovadata.inventarioapp.viewmodel.InventoryViewModel
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

@Composable
fun StatsScreen(
    viewModel: InventoryViewModel
) {
    val products by viewModel.products.collectAsState()
    val totalSales by viewModel.totalSales.collectAsState(initial = 0.0)

    val totalProducts = products.size
    val totalUnits = products.sumOf { it.qty }
    val totalValue = products.sumOf { it.qty * it.price }
    val lowStockCount = products.count { it.qty in 1..4 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Estadísticas de Inventario",
            style = MaterialTheme.typography.headlineSmall
        )

        // Tarjetas de resumen
        StatCard(
            title = "Productos registrados",
            value = "$totalProducts"
        )

        StatCard(
            title = "Unidades totales en stock",
            value = "$totalUnits"
        )

        StatCard(
            title = "Valor estimado del inventario",
            value = "$${"%.2f".format(totalValue)}"
        )

        StatCard(
            title = "Productos con poco stock (<5)",
            value = "$lowStockCount"
        )

        StatCard(
            title = "Ventas registradas",
            value = "$${"%.2f".format(totalSales ?: 0.0)}"
        )

        Spacer(Modifier.height(16.dp))

        // Gráfica de barras
        Text(
            text = "Gráfica: unidades por producto",
            style = MaterialTheme.typography.titleMedium
        )
        InventoryBarChart(products = products)

        Spacer(Modifier.height(24.dp))

        // Gráfica de pastel
        Text(
            text = "Gráfica pastel: participación en el inventario",
            style = MaterialTheme.typography.titleMedium
        )
        InventoryPieChart(products = products)
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

/**
 * Gráfica de barras: cantidad en stock de cada producto.
 */
@Composable
fun InventoryBarChart(products: List<ProductEntity>) {
    if (products.isEmpty()) {
        Text("No hay datos suficientes para graficar.")
        return
    }

    val barColor: Color = MaterialTheme.colorScheme.primary
    val maxQty = products.maxOf { it.qty }.coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 8.dp)
        ) {
            val width = size.width
            val height = size.height
            val barCount = products.size
            val barSpacing = width / (barCount.coerceAtLeast(1))
            val barWidth = barSpacing * 0.6f

            products.forEachIndexed { index, product ->
                val ratio = product.qty / maxQty.toFloat()
                val barHeight = ratio * (height * 0.8f)

                val left = index * barSpacing + (barSpacing - barWidth) / 2f
                val top = height - barHeight
                val right = left + barWidth
                val bottom = height

                drawRect(
                    color = barColor,
                    topLeft = Offset(left, top),
                    size = Size(
                        width = barWidth,
                        height = bottom - top
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            products.forEach { product ->
                Text(
                    text = "• ${product.name} (${product.qty})",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Gráfica de pastel: muestra la proporción de unidades que aporta cada producto
 * al total del inventario.
 */
@Composable
fun InventoryPieChart(products: List<ProductEntity>) {
    val totalUnits = products.sumOf { it.qty }

    if (products.isEmpty() || totalUnits == 0) {
        Text("No hay datos suficientes para la gráfica de pastel.")
        return
    }

    // Colores para las rebanadas (se repiten si hay muchos productos)
    val sliceColors = listOf(
        Color(0xFFEF5350), // rojo
        Color(0xFF42A5F5), // azul
        Color(0xFF66BB6A), // verde
        Color(0xFFFFCA28), // amarillo
        Color(0xFFAB47BC), // morado
        Color(0xFFFF7043)  // naranja
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Canvas(
            modifier = Modifier
                .size(220.dp)
                .padding(8.dp)
        ) {
            val diameter = size.minDimension
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val pieSize = Size(diameter, diameter)

            var startAngle = -90f // empezamos hacia arriba

            products.forEachIndexed { index, product ->
                val sweepAngle = 360f * (product.qty / totalUnits.toFloat())
                val color = sliceColors[index % sliceColors.size]

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = topLeft,
                    size = pieSize
                )

                startAngle += sweepAngle
            }
        }

        Spacer(Modifier.height(8.dp))

        // Leyenda
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            products.forEachIndexed { index, product ->
                val color = sliceColors[index % sliceColors.size]
                val percentage = product.qty * 100f / totalUnits.toFloat()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    // Cuadrito de color
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .padding(end = 8.dp)
                            .background(color = color)
                    )
                    Text(
                        text = "${product.name} - ${"%.1f".format(percentage)}% (${product.qty})",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
