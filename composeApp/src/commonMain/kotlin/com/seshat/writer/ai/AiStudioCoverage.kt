package com.seshat.writer.ai

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class CoverageVerdict(val displayName: String, val badgeColor: Int) {
    RECOMMEND("RECOMMEND", 0xFF4CAF50.toInt()),
    CONSIDER("CONSIDER", 0xFFCFB53B.toInt()),
    PASS("PASS", 0xFFE57373.toInt())
}

data class CharacterArc(
    val name: String,
    val arc: String
)

data class StudioCoverageReport(
    val scriptTitle: String,
    val author: String,
    val logline: String,
    val premiseScore: String,
    val synopsis: String,
    val dialogueReview: String,
    val pacingReview: String,
    val commercialProspects: String,
    val recommendation: CoverageVerdict,
    val characterArcs: List<CharacterArc> = emptyList(),
    val generatedDate: String = SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date())
) {
    fun formatPlainText(): String = buildString {
        append("================================================================================\n")
        append("                      SESHAT STUDIO COVERAGE REPORT                             \n")
        append("================================================================================\n\n")
        append("PROJECT:       ${scriptTitle.uppercase(Locale.US)}\n")
        append("WRITER:        $author\n")
        append("DATE:          $generatedDate\n")
        append("VERDICT:       ${recommendation.displayName}\n")
        append("PREMISE SCORE: $premiseScore\n\n")
        append("--------------------------------------------------------------------------------\n")
        append("1. LOGLINE\n")
        append("--------------------------------------------------------------------------------\n")
        append("$logline\n\n")
        append("--------------------------------------------------------------------------------\n")
        append("2. NARRATIVE SYNOPSIS\n")
        append("--------------------------------------------------------------------------------\n")
        append("$synopsis\n\n")
        if (characterArcs.isNotEmpty()) {
            append("--------------------------------------------------------------------------------\n")
            append("3. CHARACTER ARCS & CRUCIBLES\n")
            append("--------------------------------------------------------------------------------\n")
            for (c in characterArcs) {
                append("• ${c.name.uppercase(Locale.US)}: ${c.arc}\n")
            }
            append("\n")
        }
        append("--------------------------------------------------------------------------------\n")
        append("4. CRAFT EVALUATIONS\n")
        append("--------------------------------------------------------------------------------\n")
        append("DIALOGUE:             $dialogueReview\n\n")
        append("PACING & STRUCTURE:   $pacingReview\n\n")
        append("COMMERCIAL PROSPECTS: $commercialProspects\n\n")
        append("================================================================================\n")
        append("CONFIDENTIAL • SESHAT SCREENPLAY STUDIO READER SERVICES\n")
    }
}

object StudioCoveragePdfExporter {

    private const val PAGE_WIDTH = 612
    private const val PAGE_HEIGHT = 792
    private const val MARGIN_LEFT = 54f
    private const val MARGIN_RIGHT = 558f
    private const val CONTENT_WIDTH = MARGIN_RIGHT - MARGIN_LEFT
    private const val TOP_MARGIN = 54f
    private const val BOTTOM_MARGIN = 738f

    fun exportCoverageReportToFile(
        report: StudioCoverageReport,
        targetFile: File,
        watermarkRecipient: String? = null
    ) {
        FileOutputStream(targetFile).use { out ->
            exportCoverageReportToStream(report, out, watermarkRecipient)
        }
    }

    fun exportCoverageReportToStream(
        report: StudioCoverageReport,
        outputStream: OutputStream,
        watermarkRecipient: String? = null
    ) {
        val document = PdfDocument()

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(40, 40, 40)
            textSize = 12f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        val verdictPaint = Paint().apply {
            color = report.recommendation.badgeColor
            textSize = 14f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        val rulePaint = Paint().apply {
            color = Color.rgb(180, 180, 180)
            strokeWidth = 1f
            isAntiAlias = true
        }

        val watermarkPaint = Paint().apply {
            color = Color.rgb(200, 200, 200)
            alpha = 40
            textSize = 28f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val linesToDraw = mutableListOf<PrintLine>()

        // Header Title
        linesToDraw.add(PrintLine("SESHAT STUDIO READER COVERAGE", headerPaint, 18f))
        linesToDraw.add(PrintLine("CONFIDENTIAL INDUSTRY EVALUATION", boldPaint, 14f))
        linesToDraw.add(PrintLine("RULE", rulePaint, 14f))

        // Metadata grid
        linesToDraw.add(PrintLine("PROJECT:       ${report.scriptTitle.uppercase(Locale.US)}", boldPaint, 13f))
        linesToDraw.add(PrintLine("SCREENWRITER:  ${report.author}", textPaint, 13f))
        linesToDraw.add(PrintLine("DATE:          ${report.generatedDate}", textPaint, 13f))
        linesToDraw.add(PrintLine("VERDICT:       [${report.recommendation.displayName}]", verdictPaint, 16f))
        linesToDraw.add(PrintLine("PREMISE SCORE: ${report.premiseScore}", boldPaint, 14f))
        linesToDraw.add(PrintLine("RULE", rulePaint, 14f))

        // 1. Logline
        linesToDraw.add(PrintLine("1. LOGLINE", headerPaint, 14f))
        addWrappedText(report.logline, textPaint, CONTENT_WIDTH, linesToDraw)
        linesToDraw.add(PrintLine("", textPaint, 8f))

        // 2. Narrative Synopsis
        linesToDraw.add(PrintLine("2. NARRATIVE SYNOPSIS", headerPaint, 14f))
        for (paragraph in report.synopsis.split("\n\n")) {
            if (paragraph.isNotBlank()) {
                addWrappedText(paragraph.trim(), textPaint, CONTENT_WIDTH, linesToDraw)
                linesToDraw.add(PrintLine("", textPaint, 6f))
            }
        }
        linesToDraw.add(PrintLine("", textPaint, 8f))

        // 3. Character Arcs
        if (report.characterArcs.isNotEmpty()) {
            linesToDraw.add(PrintLine("3. CHARACTER ARCS & CRUCIBLES", headerPaint, 14f))
            for (arc in report.characterArcs) {
                linesToDraw.add(PrintLine("• ${arc.name.uppercase(Locale.US)}", boldPaint, 12f))
                addWrappedText("  ${arc.arc}", textPaint, CONTENT_WIDTH, linesToDraw)
                linesToDraw.add(PrintLine("", textPaint, 4f))
            }
            linesToDraw.add(PrintLine("", textPaint, 8f))
        }

        // 4. Craft Evaluations
        linesToDraw.add(PrintLine("4. CRAFT EVALUATION & MARKETABILITY", headerPaint, 14f))
        linesToDraw.add(PrintLine("DIALOGUE & SUBTEXT:", boldPaint, 12f))
        addWrappedText(report.dialogueReview, textPaint, CONTENT_WIDTH, linesToDraw)
        linesToDraw.add(PrintLine("", textPaint, 6f))

        linesToDraw.add(PrintLine("PACING & STRUCTURE:", boldPaint, 12f))
        addWrappedText(report.pacingReview, textPaint, CONTENT_WIDTH, linesToDraw)
        linesToDraw.add(PrintLine("", textPaint, 6f))

        linesToDraw.add(PrintLine("COMMERCIAL PROSPECTS:", boldPaint, 12f))
        addWrappedText(report.commercialProspects, textPaint, CONTENT_WIDTH, linesToDraw)

        // Pagination and page rendering
        var pageNum = 1
        var lineIdx = 0
        val totalLines = linesToDraw.size

        while (lineIdx < totalLines) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Watermark
            val wmText = if (!watermarkRecipient.isNullOrBlank()) {
                "CONFIDENTIAL • COPY FOR: ${watermarkRecipient.uppercase(Locale.US)}"
            } else {
                "CONFIDENTIAL • SESHAT STUDIO COVERAGE"
            }
            canvas.save()
            canvas.rotate(-45f, PAGE_WIDTH / 2f, PAGE_HEIGHT / 2f)
            canvas.drawText(wmText, PAGE_WIDTH / 2f, PAGE_HEIGHT / 2f, watermarkPaint)
            canvas.restore()

            // Header running rule
            canvas.drawText("SESHAT STUDIO COVERAGE — ${report.scriptTitle.uppercase(Locale.US)}", MARGIN_LEFT, 36f, textPaint)
            canvas.drawLine(MARGIN_LEFT, 42f, MARGIN_RIGHT, 42f, rulePaint)

            var curY = TOP_MARGIN
            while (lineIdx < totalLines) {
                val item = linesToDraw[lineIdx]
                if (curY + item.lineSpacing > BOTTOM_MARGIN) {
                    break
                }
                if (item.text == "RULE") {
                    canvas.drawLine(MARGIN_LEFT, curY - 4f, MARGIN_RIGHT, curY - 4f, rulePaint)
                } else if (item.text.isNotEmpty()) {
                    canvas.drawText(item.text, MARGIN_LEFT, curY, item.paint)
                }
                curY += item.lineSpacing
                lineIdx++
            }

            // Footer
            canvas.drawLine(MARGIN_LEFT, BOTTOM_MARGIN + 10f, MARGIN_RIGHT, BOTTOM_MARGIN + 10f, rulePaint)
            canvas.drawText("CONFIDENTIAL & PROPRIETARY", MARGIN_LEFT, BOTTOM_MARGIN + 24f, textPaint)
            val pageStr = "Page $pageNum"
            val pageW = textPaint.measureText(pageStr)
            canvas.drawText(pageStr, MARGIN_RIGHT - pageW, BOTTOM_MARGIN + 24f, textPaint)

            document.finishPage(page)
            pageNum++
        }

        try {
            document.writeTo(outputStream)
        } finally {
            document.close()
        }
    }

    private fun addWrappedText(
        fullText: String,
        paint: Paint,
        maxWidth: Float,
        dest: MutableList<PrintLine>,
        lineSpacing: Float = 12f
    ) {
        val words = fullText.split(Regex("\\s+")).filter { it.isNotEmpty() }
        var currentLine = StringBuilder()

        for (word in words) {
            val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(candidate)
            if (width <= maxWidth) {
                currentLine = StringBuilder(candidate)
            } else {
                if (currentLine.isNotEmpty()) {
                    dest.add(PrintLine(currentLine.toString(), paint, lineSpacing))
                }
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            dest.add(PrintLine(currentLine.toString(), paint, lineSpacing))
        }
    }

    private data class PrintLine(
        val text: String,
        val paint: Paint,
        val lineSpacing: Float
    )
}
