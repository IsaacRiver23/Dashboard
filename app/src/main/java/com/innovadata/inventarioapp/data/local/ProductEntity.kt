package com.innovadata.inventarioapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val qty: Int,
    val description: String,
    val price: Double,                // ← IMPORTANTE
    val imagePath: String? = null
)
