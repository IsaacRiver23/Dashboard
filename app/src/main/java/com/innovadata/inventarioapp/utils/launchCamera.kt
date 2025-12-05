package com.innovadata.inventarioapp.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

/**
 * Crea un URI en Pictures/InventarioApp y lo regresa para usarlo con TakePicture().
 */
fun launchCamera(context: Context, onReady: (Uri) -> Unit) {
    val values = ContentValues().apply {
        put(
            MediaStore.MediaColumns.DISPLAY_NAME,
            "producto_${System.currentTimeMillis()}.jpg"
        )
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        // En Android 10+ se puede indicar carpeta relativa
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/InventarioApp")
        }
    }

    // Colección compatible con API 26+
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val uri = context.contentResolver.insert(collection, values)
        ?: throw IllegalStateException("No se pudo crear el archivo de imagen")

    onReady(uri)
}
