package com.innovadata.inventarioapp.viewmodel

import android.app.Application
import android.content.ContentValues
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.innovadata.inventarioapp.data.local.ProductEntity
import com.innovadata.inventarioapp.data.repository.InventoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InventoryViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = InventoryRepository(app.applicationContext)

    // ---------------- BÚSQUEDA ----------------
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val products: StateFlow<List<ProductEntity>> =
        _searchQuery
            .debounce(250)
            .flatMapLatest { query -> repo.searchProducts(query) }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // ------------- PRODUCTO SELECCIONADO -------------
    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()

    // Resultado de la última importación
    private val _lastImportCount = MutableStateFlow<Int?>(null)
    val lastImportCount: StateFlow<Int?> = _lastImportCount.asStateFlow()

    private val _lastImportDuplicates = MutableStateFlow<Int?>(null)
    val lastImportDuplicates: StateFlow<Int?> = _lastImportDuplicates.asStateFlow()

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun loadProduct(id: Int) {
        viewModelScope.launch {
            _selectedProduct.value = repo.getProductById(id)
        }
    }

    // ---------------- CRUD PRODUCTOS ----------------
    fun addProduct(
        name: String,
        qty: Int,
        desc: String,
        price: Double,
        imagePath: String?
    ) {
        viewModelScope.launch {
            repo.addProduct(name, qty, desc, price, imagePath)
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repo.updateProduct(product)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repo.deleteProduct(product)
        }
    }

    fun getLowStockFlow(minQty: Int): Flow<List<ProductEntity>> =
        repo.getLowStockProducts(minQty)

    // ---------------- VENTAS ----------------
    fun sellOne(product: ProductEntity) {
        viewModelScope.launch {
            if (product.qty > 0) {
                // Registrar venta
                repo.recordSale(product)

                // Actualizar inventario
                val updated = product.copy(qty = product.qty - 1)
                repo.updateProduct(updated)

                // Actualizar detalle si está abierto
                _selectedProduct.value = updated
            }
        }
    }

    val totalSales: Flow<Double?> = repo.getTotalSales()
    val salesHistory = repo.getSalesHistory()

    // ---------------- EXPORTAR PDF ----------------
    suspend fun exportToPdf(): Uri {
        val context = getApplication<Application>()

        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 20f
        }
        val textPaint = Paint().apply { textSize = 12f }

        var y = 40f
        canvas.drawText("Reporte de Inventario", 40f, y, titlePaint)
        y += 30f

        val date = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.getDefault()
        ).format(Date())
        canvas.drawText("Generado: $date", 40f, y, textPaint)
        y += 25f

        val list = products.value

        list.forEach {
            val line =
                "${it.name} | Cant: ${it.qty} | $${it.price} | ${it.description.take(40)}"
            canvas.drawText(line, 40f, y, textPaint)
            y += 18f
            if (y > 800f) return@forEach
        }

        pdf.finishPage(page)

        // Guardar en almacenamiento externo → /Download (en Android 10+)
        val resolver = context.contentResolver
        val fileName = "inventario_${System.currentTimeMillis()}.pdf"

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/")
            }
        }

        val collection = MediaStore.Files.getContentUri("external")

        val uri = resolver.insert(collection, values)
            ?: throw IllegalStateException("No se pudo crear el PDF")

        resolver.openOutputStream(uri)?.use { output ->
            pdf.writeTo(output)
        }

        pdf.close()
        return uri
    }

    // ---------------- IMPORTAR DESDE CSV (EXCEL) ----------------
    /**
     * Importa productos desde un archivo CSV con formato:
     * name,qty,price,description
     *
     * El CSV lo puedes generar desde Excel con "Guardar como → CSV".
     * Cuenta también cuántos productos ya existían (duplicados por nombre).
     */
    fun importFromCsv(uri: Uri) {
        val context = getApplication<Application>()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                var importedCount = 0
                var duplicateCount = 0

                // Tomamos los nombres que ya existen actualmente en BD
                val existingNames = products.value
                    .map { it.name.trim().lowercase() }
                    .toMutableSet()

                context.contentResolver.openInputStream(uri)?.use { input ->
                    val reader = BufferedReader(InputStreamReader(input))

                    val lines = reader.lineSequence().toList()
                    if (lines.isEmpty()) return@use

                    // Saltamos la primera línea (encabezados)
                    lines
                        .drop(1)
                        .filter { it.isNotBlank() }
                        .forEach { line ->
                            // Soportar coma o punto y coma como separador
                            val parts = line.split(';', ',')

                            val name = parts.getOrNull(0)?.trim().orEmpty()
                            val qty = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
                            val price = parts.getOrNull(2)?.trim()?.toDoubleOrNull() ?: 0.0
                            val desc = parts.getOrNull(3)?.trim().orEmpty()

                            if (name.isNotBlank()) {
                                val lowered = name.lowercase()
                                if (existingNames.contains(lowered)) {
                                    duplicateCount++
                                } else {
                                    existingNames.add(lowered)
                                }

                                repo.addProduct(
                                    name = name,
                                    qty = qty,
                                    desc = desc,
                                    price = price,
                                    imagePath = null
                                )
                                importedCount++
                            }
                        }
                }

                // Avisar a la UI cuántos se subieron y cuántos eran duplicados
                _lastImportCount.value = importedCount
                _lastImportDuplicates.value = duplicateCount

            } catch (e: Exception) {
                e.printStackTrace()
                _lastImportCount.value = 0
                _lastImportDuplicates.value = 0
            }
        }
    }

    fun clearImportResult() {
        _lastImportCount.value = null
        _lastImportDuplicates.value = null
    }
}
