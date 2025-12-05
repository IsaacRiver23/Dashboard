package com.innovadata.inventarioapp.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.innovadata.inventarioapp.data.local.ProductEntity
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun generateInventoryPdf(
    context: Context,
    products: List<ProductEntity>
): ByteArray {

    val pageWidth = 595
    val pageHeight = 842

    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    val titlePaint = Paint().apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = 18f
    }

    val headerPaint = Paint().apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = 12f
    }

    val textPaint = Paint().apply {
        textSize = 11f
    }

    var y = 40f

    // Título
    canvas.drawText("Reporte de Inventario", 40f, y, titlePaint)
    y += 25f

    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        .format(Date())
    canvas.drawText("Generado: $date", 40f, y, textPaint)
    y += 30f

    // Encabezados de tabla
    canvas.drawText("Nombre", 40f, y, headerPaint)
    canvas.drawText("Cant.", 260f, y, headerPaint)
    canvas.drawText("Descripción", 310f, y, headerPaint)
    y += 15f

    canvas.drawLine(40f, y, (pageWidth - 40).toFloat(), y, textPaint)
    y += 15f

    // Filas
    products.forEach { p ->
        if (y > pageHeight - 40) return@forEach

        canvas.drawText(p.name.take(20), 40f, y, textPaint)
        canvas.drawText(p.qty.toString(), 260f, y, textPaint)
        canvas.drawText(p.description.take(30), 310f, y, textPaint)
        y += 15f
    }

    pdfDocument.finishPage(page)

    val baos = ByteArrayOutputStream()
    pdfDocument.writeTo(baos)
    pdfDocument.close()

    return baos.toByteArray()
}
