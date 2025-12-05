package com.innovadata.inventarioapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // ───────────────────────
    // Productos
    // ───────────────────────
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAll(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity)

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ProductEntity?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE qty <= :minQty")
    fun getLowStock(minQty: Int): Flow<List<ProductEntity>>


    // ───────────────────────
    // Ventas
    // ───────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Query("SELECT SUM(price) FROM sales")
    fun getTotalSales(): Flow<Double?>

    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleEntity>>
}
