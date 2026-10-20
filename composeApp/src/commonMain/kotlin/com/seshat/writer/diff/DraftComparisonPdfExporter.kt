package com.seshat.writer.diff

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.seshat.writer.export.PdfExporter
import java.io.File
import java.io.FileOutputStream

object DraftComparisonPdfExporter {

    private const val PAGE_WIDTH = 612
    private const val PAGE_HEIGHT = 792
    private const val MARGIN_X = 54f
    private const val MARGIN_TOP = 54f
    private const val MARGIN_BOTTOM = 54f
    private const val LINE_HEIGHT = 13f

    fun exportDiffReportToFile(
        result: DraftDiffResult,
        file: File,
        context: Context? = null
    ) {
        val doc = PdfDocument()
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9.5f
            typeface = PdfExporter.getCourierTypeface(context, bold = false)
            color = Color.BLACK
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 10.5f
            typeface = PdfExporter.getCourierTypeface(context, bold = true)
            color = Color.BLACK
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 14f
            typeface = PdfExporter.getCourierTypeface(context, bold = true)
            color = Color.BLACK
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.LTGRAY
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

                // Header on continued pages
                canvas.drawText("${result.titleA} - REVISION REPORT (Page $pageNumber)", MARGIN_X, currentY, boldPaint)
                currentY += 15f
                canvas.drawLine(MARGIN_X, currentY, PAGE_WIDTH - MARGIN_X, currentY, linePaint)
                currentY += 20f
            }
        }

        // --- Cover Banner on Page 1 ---
        canvas.drawText("DRAFT REVISION COMPARISON REPORT", MARGIN_X, currentY, headerPaint)
        currentY += 18f
        canvas.drawText("SCREENPLAY: ${result.titleA.uppercase()}", MARGIN_X, currentY, boldPaint)
        currentY += 14f
        if (result.authorA.isNotBlank()) {
            canvas.drawText("AUTHOR: ${result.authorA}", MARGIN_X, currentY, textPaint)
            currentY += 14f
        }
        canvas.drawText("BASELINE: ${result.draftTagA} (${result.summary.totalScenesA} scenes)  -->  REVISED: ${result.draftTagB} (${result.summary.totalScenesB} scenes)", MARGIN_X, currentY, textPaint)
        currentY += 16f
        canvas.drawLine(MARGIN_X, currentY, PAGE_WIDTH - MARGIN_X, currentY, linePaint)
        currentY += 16f

        // --- Summary Grid ---
        canvas.drawText("EXECUTIVE REVISION SUMMARY", MARGIN_X, currentY, boldPaint)
        currentY += 14f
        canvas.drawText("• Scenes:  +${result.summary.scenesAdded} Added  |  -${result.summary.scenesRemoved} Removed  |  ~${result.summary.scenesModified} Modified  |  ${result.summary.scenesUnchanged} Unchanged", MARGIN_X, currentY, textPaint)
        currentY += 14f
        canvas.drawText("• Blocks:  +${result.summary.blocksAdded} Added  |  -${result.summary.blocksRemoved} Removed  |  ~${result.summary.blocksModified} Modified", MARGIN_X, currentY, textPaint)
        currentY += 14f
        canvas.drawText("• Words:   ${if (result.summary.wordsDelta >= 0) "+" else ""}${result.summary.wordsDelta} words net difference", MARGIN_X, currentY, textPaint)
        currentY += 18f
        canvas.drawLine(MARGIN_X, currentY, PAGE_WIDTH - MARGIN_X, currentY, linePaint)
        currentY += 20f

        // --- Scene Details ---
        for (scene in result.sceneDiffs) {
            if (scene.type == DiffType.UNCHANGED) continue // focus on changes in report

            checkNewPage(45f)
            val num = scene.sceneNumberB ?: scene.sceneNumberA ?: ""
            val numPrefix = if (num.isNotBlank()) "#$num " else ""
            val heading = scene.headingB ?: scene.headingA ?: "SCENE"
            val badge = when (scene.type) {
                DiffType.ADDED -> "[ADDED]"
                DiffType.REMOVED -> "[REMOVED]"
                DiffType.MODIFIED -> "[MODIFIED]"
                DiffType.UNCHANGED -> "[UNCHANGED]"
            }

            canvas.drawText("$badge $numPrefix$heading", MARGIN_X, currentY, boldPaint)
            currentY += 13f
            canvas.drawText("       Changes: +${scene.stats.addedBlocks} added, -${scene.stats.removedBlocks} removed, ~${scene.stats.modifiedBlocks} modified", MARGIN_X, currentY, textPaint)
            currentY += 15f

            for (b in scene.blockDiffs) {
                if (b.type == DiffType.UNCHANGED) continue
                checkNewPage(24f)

                when (b.type) {
                    DiffType.ADDED -> {
                        canvas.drawText("   + [${b.blockType.name}] ${truncate(b.newText ?: "", 75)}", MARGIN_X, currentY, textPaint)
                        currentY += LINE_HEIGHT
                    }
                    DiffType.REMOVED -> {
                        canvas.drawText("   - [${b.blockType.name}] ${truncate(b.oldText ?: "", 75)}", MARGIN_X, currentY, textPaint)
                        currentY += LINE_HEIGHT
                    }
                    DiffType.MODIFIED -> {
                        canvas.drawText("   ~ [${b.blockType.name}] OLD: ${truncate(b.oldText ?: "", 70)}", MARGIN_X, currentY, textPaint)
                        currentY += LINE_HEIGHT
                        canvas.drawText("                  NEW: ${truncate(b.newText ?: "", 70)}", MARGIN_X, currentY, textPaint)
                        currentY += LINE_HEIGHT
                    }
                    DiffType.UNCHANGED -> {}
                }
            }
            currentY += 8f
        }

        doc.finishPage(page)
        FileOutputStream(file).use { out ->
            doc.writeTo(out)
        }
        doc.close()
    }

    private fun truncate(str: String, maxLen: Int): String {
        val single = str.replace("\n", " ").trim()
        return if (single.length > maxLen) single.substring(0, maxLen - 3) + "..." else single
    }
}
