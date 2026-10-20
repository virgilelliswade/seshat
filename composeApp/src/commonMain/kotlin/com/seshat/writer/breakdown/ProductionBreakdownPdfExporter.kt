package com.seshat.writer.breakdown

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.seshat.writer.export.PdfExporter
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object ProductionBreakdownPdfExporter {

    private const val PAGE_WIDTH = 612
    private const val PAGE_HEIGHT = 792
    private const val MARGIN_X = 54f
    private const val MARGIN_TOP = 54f
    private const val MARGIN_BOTTOM = 54f
    private const val LINE_HEIGHT = 13f

    fun exportBreakdownToFile(
        summary: ScriptBreakdownSummary,
        title: String,
        file: File,
        context: Context? = null
    ) {
        val doc = PdfDocument()
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f
            typeface = PdfExporter.getCourierTypeface(context, bold = false)
            color = Color.BLACK
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 10f
            typeface = PdfExporter.getCourierTypeface(context, bold = true)
            color = Color.BLACK
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 13f
            typeface = PdfExporter.getCourierTypeface(context, bold = true)
            color = Color.BLACK
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = doc.startPage(pageInfo)
        var canvas = page.canvas
        var currentY = MARGIN_TOP

        fun checkNewPage(neededHeight: Float) {
            if (currentY + neededHeight > PAGE_HEIGHT - MARGIN_BOTTOM) {
                doc.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = doc.startPage(pageInfo)
                canvas = page.canvas
                currentY = MARGIN_TOP

                // Continued Header
                canvas.drawText("${title.uppercase(Locale.US)} - AD BREAKDOWN (Page $pageNumber)", MARGIN_X, currentY, boldPaint)
                currentY += 14f
                canvas.drawLine(MARGIN_X, currentY, PAGE_WIDTH - MARGIN_X, currentY, linePaint)
                currentY += 18f
            }
        }

        // --- Cover Header ---
        canvas.drawText("ASSISTANT DIRECTOR PRODUCTION BREAKDOWN", MARGIN_X, currentY, titlePaint)
        currentY += 16f
        canvas.drawText("SCRIPT: ${title.uppercase(Locale.US)}", MARGIN_X, currentY, boldPaint)
        currentY += 13f
        canvas.drawText("TOTAL SCENES: ${summary.totalScenes}  |  ESTIMATED LENGTH: ${summary.totalPagesFormatted} (${summary.totalEighths}/8ths)", MARGIN_X, currentY, textPaint)
        currentY += 13f
        canvas.drawText("UNIQUE ELEMENTS: ${summary.uniqueCastCount} Cast  •  ${summary.uniquePropsCount} Props  •  ${summary.uniqueVehiclesCount} Vehicles  •  ${summary.uniqueStuntsCount} Stunts  •  ${summary.uniqueSfxCount} SFX", MARGIN_X, currentY, textPaint)
        currentY += 15f
        canvas.drawLine(MARGIN_X, currentY, PAGE_WIDTH - MARGIN_X, currentY, linePaint)
        currentY += 18f

        // --- Scene Breakdown Cards ---
        for (scene in summary.scenes) {
            checkNewPage(55f)

            canvas.drawRect(MARGIN_X, currentY - 10f, PAGE_WIDTH - MARGIN_X, currentY + 12f, boxPaint)
            canvas.drawText("SCENE ${scene.sceneNumber}: ${scene.heading}", MARGIN_X + 6f, currentY + 4f, boldPaint)
            currentY += 24f

            canvas.drawText("SET: ${scene.intExt} ${scene.location}  |  TIME: ${scene.timeOfDay}  |  LENGTH: ${scene.pageCountEighths}  |  PAGE: ${scene.startPage}", MARGIN_X + 6f, currentY, textPaint)
            currentY += 14f

            if (scene.castMembers.isNotEmpty()) {
                checkNewPage(14f)
                canvas.drawText("CAST: ${scene.castMembers.joinToString(", ")}", MARGIN_X + 6f, currentY, textPaint)
                currentY += 13f
            }
            if (scene.props.isNotEmpty()) {
                checkNewPage(14f)
                canvas.drawText("PROPS: ${scene.props.joinToString(", ")}", MARGIN_X + 6f, currentY, textPaint)
                currentY += 13f
            }
            if (scene.wardrobe.isNotEmpty()) {
                checkNewPage(14f)
                canvas.drawText("WARDROBE: ${scene.wardrobe.joinToString(", ")}", MARGIN_X + 6f, currentY, textPaint)
                currentY += 13f
            }
            if (scene.vehicles.isNotEmpty()) {
                checkNewPage(14f)
                canvas.drawText("VEHICLES: ${scene.vehicles.joinToString(", ")}", MARGIN_X + 6f, currentY, textPaint)
                currentY += 13f
            }
            if (scene.stunts.isNotEmpty()) {
                checkNewPage(14f)
                canvas.drawText("STUNTS: ${scene.stunts.joinToString(", ")}", MARGIN_X + 6f, currentY, textPaint)
                currentY += 13f
            }
            if (scene.specialEffects.isNotEmpty()) {
                checkNewPage(14f)
                canvas.drawText("SFX / VFX: ${scene.specialEffects.joinToString(", ")}", MARGIN_X + 6f, currentY, textPaint)
                currentY += 13f
            }
            if (scene.soundNotes.isNotEmpty()) {
                checkNewPage(14f)
                canvas.drawText("SOUND: ${scene.soundNotes.joinToString(", ")}", MARGIN_X + 6f, currentY, textPaint)
                currentY += 13f
            }

            currentY += 10f
        }

        doc.finishPage(page)
        FileOutputStream(file).use { out ->
            doc.writeTo(out)
        }
        doc.close()
    }
}
