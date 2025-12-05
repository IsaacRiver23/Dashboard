package com.innovadata.inventarioapp.data.repository

import android.content.Context
import com.innovadata.inventarioapp.data.local.InventoryDatabase
import com.innovadata.inventarioapp.data.local.ProductEntity
import com.innovadata.inventarioapp.data.local.SaleEntity
import kotlinx.coroutines.flow.Flow

class InventoryRepository(context: Context) {

    private val dao = InventoryDatabase.getInstance(context).productDao()

    // ────────────────────────────────
    // Productos
    // ────────────────────────────────
    fun getAllProducts(): Flow<List<ProductEntity>> = dao.getAll()

    fun searchProducts(query: String): Flow<List<ProductEntity>> =
        if (query.isBlank()) dao.getAll() else dao.search(query)

    suspend fun getProductById(id: Int): ProductEntity? = dao.getById(id)

    suspend fun addProduct(
        name: String,
        qty: Int,
        desc: String,
        price: Double,
        imagePath: String?
    ) {
        dao.insert(
            ProductEntity(
                name = name,
                qty = qty,
                description = desc,
                price = price,
                imagePath = imagePath
            )
        )
    }

    suspend fun updateProduct(product: ProductEntity) = dao.update(product)

    suspend fun deleteProduct(product: ProductEntity) = dao.delete(product)

    fun getLowStockProducts(minQty: Int): Flow<List<ProductEntity>> =
        dao.getLowStock(minQty)

    // ────────────────────────────────
    // Ventas
    // ────────────────────────────────
    suspend fun recordSale(product: ProductEntity) {
        val sale = SaleEntity(
            productId = product.id,
            productName = product.name,
            price = product.price,
            timestamp = System.currentTimeMillis()
        )
        dao.insertSale(sale)
    }

    fun getTotalSales(): Flow<Double?> = dao.getTotalSales()

    fun getSalesHistory() = dao.getAllSales()
}
