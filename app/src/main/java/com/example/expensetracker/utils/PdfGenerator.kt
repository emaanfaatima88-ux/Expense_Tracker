package com.example.expensetracker.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.OutputStream

object PdfGenerator {

    fun generateExpensePdf(
        context: Context,
        expenses: List<ExpenseEntity>
    ): Uri? {

        return try {

            val resolver =
                context.contentResolver

            val contentValues =
                ContentValues().apply {

                    put(
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        "Expense_Report.pdf"
                    )

                    put(
                        MediaStore.MediaColumns.MIME_TYPE,
                        "application/pdf"
                    )

                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS
                    )
                }

            val uri =
                resolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )

            uri?.let {

                val outputStream: OutputStream? =
                    resolver.openOutputStream(it)

                outputStream?.let { stream ->

                    val writer =
                        PdfWriter(stream)

                    val pdfDocument =
                        PdfDocument(writer)

                    val document =
                        Document(pdfDocument)

                    document.add(
                        Paragraph("Expense Tracker Report")
                            .setBold()
                            .setFontSize(22f)
                    )

                    document.add(
                        Paragraph(" ")
                    )

                    expenses.forEach { expense ->

                        val text =
                            """
                            Title: ${expense.title}
                            Amount: ${expense.amount}
                            Category: ${expense.category}
                            Date: ${expense.date}
                            
                            """.trimIndent()

                        document.add(
                            Paragraph(text)
                        )
                    }

                    document.close()
                }
            }

            uri

        } catch (e: Exception) {

            null
        }
    }
}