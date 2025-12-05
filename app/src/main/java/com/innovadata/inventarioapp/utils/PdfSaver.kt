package com.innovadata.inventarioapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object PdfSaver {

    fun savePdfToDownloads(
        context: Context,
        pdfBytes: ByteArray,
        fileName: String = "inventario.pdf"
    ): File {

        // /storage/emulated/0/Download/InventarioApp/
        val downloadsDir = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        val appFolder = File(downloadsDir, "InventarioApp")

        if (!appFolder.exists()) {
            appFolder.mkdirs()
        }

        val pdfFile = File(appFolder, fileName)

        FileOutputStream(pdfFile).use { fos ->
            fos.write(pdfBytes)
        }

        return pdfFile
    }

    fun openPdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }
}
