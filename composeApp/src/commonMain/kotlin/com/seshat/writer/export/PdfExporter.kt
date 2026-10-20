package com.seshat.writer.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.seshat.writer.model.BlockType

import com.seshat.writer.model.RevisionColor
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.analytics.ScriptAnalytics
import com.seshat.writer.analytics.LocationAnalytics
import com.seshat.writer.analytics.CastSceneMatrix
import com.seshat.writer.screenplay.ScreenplayPagination
import com.seshat.writer.titlepage.TitlePageData
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Locale

/**
 * Industry-standard US Letter (8.5" x 11") Screenplay PDF Exporter.
 * Adheres strictly to AMPAS / Nicholl / Final Draft Hollywood specifications.
 */
object PdfExporter {

    const val PAGE_WIDTH = 612 // 8.5 inches * 72 pt/in
    const val PAGE_HEIGHT = 792 // 11.0 inches * 72 pt/in

    const val MARGIN_LEFT = 108f // 1.5 inches binding margin
    const val MARGIN_RIGHT = 72f // 1.0 inch
    const val MARGIN_TOP = 72f // 1.0 inch
    const val MARGIN_BOTTOM = 60f // ~0.83 inch
    const val HEADER_Y = 36f // 0.5 inch from top
    const val HEADER_X = 540f // 1.0 inch from right

    const val LINE_HEIGHT = 12f // 6 lines per inch (12 pt font pitch)

    // Indentations from left paper edge (x coordinates)
    const val ACTION_X = 108f // 1.5"
    const val ACTION_WIDTH = 432f // 6.0" max width

    const val CHARACTER_X = 270f // 3.75"
    const val CHARACTER_WIDTH = 250f

    const val PARENTHETICAL_X = 216f // 3.0"
    const val PARENTHETICAL_WIDTH = 216f // 3.0" max width

    const val DIALOGUE_X = 180f // 2.5"
    const val DIALOGUE_WIDTH = 252f // 3.5" max width

    const val TRANSITION_RIGHT_X = 540f // Right margin alignment

    data class PdfExportOptions(
        val includeTitlePage: Boolean = true,
        val showPageNumbers: Boolean = true,
        val showSceneNumbers: Boolean = false,
        val watermarkText: String? = null,
        val watermarkOpacity: Float = 0.25f,
        val watermarkRecipient: String? = null,
        val headerText: String? = null,
        val includeNotes: Boolean = false,
        val revisionColor: RevisionColor = RevisionColor.WHITE,
        val revisionDate: String? = null,
        val printRevisionAsterisks: Boolean = true,
        val printColoredPages: Boolean = false
    )

    fun getCourierTypeface(context: Context? = null, bold: Boolean = false): Typeface {
        val style = if (bold) Typeface.BOLD else Typeface.NORMAL
        return Typeface(style)
    }


    fun exportToStream(
        blocks: List<ScreenplayBlock>,
        titlePageData: TitlePageData,
        outputStream: OutputStream,
        options: PdfExportOptions = PdfExportOptions(),
        context: Context? = null
    ) {
        val document = PdfDocument()

        val courierRegular = getCourierTypeface(context, bold = false)
        val courierBold = getCourierTypeface(context, bold = true)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 12f // Authentic 12 pt Courier standard (10-cpi pitch)
            typeface = courierRegular
        }

        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 12f
            typeface = courierBold
        }

        var pageIndex = 1

        val effectiveWatermarkText = if (!options.watermarkText.isNullOrBlank()) {
            if (!options.watermarkRecipient.isNullOrBlank()) {
                "${options.watermarkText.trim()} • COPY FOR: ${options.watermarkRecipient.trim()}"
            } else {
                options.watermarkText.trim()
            }
        } else null

        // 1. Render Title Page (if requested)
        if (options.includeTitlePage) {
            val titlePageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex++).create()
            val titlePage = document.startPage(titlePageInfo)
            if (options.printColoredPages && options.revisionColor != RevisionColor.WHITE) {
                val bgPaint = Paint().apply {
                    color = options.revisionColor.printColorInt
                    style = Paint.Style.FILL
                }
                titlePage.canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)
            }
            renderTitlePage(titlePage.canvas, titlePageData, courierRegular, courierBold, options)
            if (effectiveWatermarkText != null) {
                renderWatermark(titlePage.canvas, effectiveWatermarkText, courierBold, options.watermarkOpacity)
            }
            document.finishPage(titlePage)
        }

        // 2. Paginate Blocks using ScreenplayPagination engine
        val pagination = ScreenplayPagination.calculatePagination(blocks)
        val totalScriptPages = pagination.totalPages

        var sceneIdx = 1
        val sceneNumberMap = mutableMapOf<String, String>()
        for (b in blocks) {
            if (b.type == BlockType.SCENE_HEADING && com.seshat.writer.screenplay.ScreenplayViewModel.isCountableSceneHeading(b)) {
                val sNum = b.sceneNumber ?: (sceneIdx++).toString()
                sceneNumberMap[b.id] = sNum
            }
        }

        // Group blocks by page number
        val blocksByPage = mutableMapOf<Int, MutableList<ScreenplayBlock>>()
        for (block in blocks) {
            val p = pagination.blockPageNumbers[block.id] ?: 1
            blocksByPage.getOrPut(p) { mutableListOf() }.add(block)
        }

        for (scriptPageNum in 1..totalScriptPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex++).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Page Background Tint (for colored revision drafts)
            if (options.printColoredPages && options.revisionColor != RevisionColor.WHITE) {
                val bgPaint = Paint().apply {
                    color = options.revisionColor.printColorInt
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)
            }

            // Watermark (rendered across page)
            if (effectiveWatermarkText != null) {
                renderWatermark(canvas, effectiveWatermarkText, courierBold, options.watermarkOpacity)
            }

            // Running Header: Page number and optional custom script header / revision tag starting at Page 2
            if (scriptPageNum > 1) {
                val headerLeftText = if (options.revisionColor != RevisionColor.WHITE) {
                    val datePart = if (!options.revisionDate.isNullOrBlank()) " - ${options.revisionDate}" else ""
                    val revStr = "${options.revisionColor.draftTag}$datePart"
                    if (!options.headerText.isNullOrBlank()) "${options.headerText.uppercase(Locale.US)} - $revStr" else revStr
                } else {
                    options.headerText?.uppercase(Locale.US)
                }

                if (!headerLeftText.isNullOrBlank()) {
                    val customHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.BLACK
                        textSize = 10f
                        typeface = courierRegular
                        textAlign = Paint.Align.LEFT
                    }
                    canvas.drawText(headerLeftText, MARGIN_LEFT, HEADER_Y, customHeaderPaint)
                }

                if (options.showPageNumbers) {
                    val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.BLACK
                        textSize = 12f
                        typeface = courierRegular
                        textAlign = Paint.Align.RIGHT
                    }
                    canvas.drawText("$scriptPageNum.", HEADER_X, HEADER_Y, headerPaint)
                }
            }

            // Draw blocks for this page
            var currentY = MARGIN_TOP
            val pageBlocks = blocksByPage[scriptPageNum] ?: emptyList()

            var bIdx = 0
            while (bIdx < pageBlocks.size) {
                val block = pageBlocks[bIdx]

                if (block.isOmitted && block.type != BlockType.SCENE_HEADING) {
                    bIdx++
                    continue
                }

                if (block.dualDialogueId != null) {
                    val dualId = block.dualDialogueId
                    val dualGroup = mutableListOf<ScreenplayBlock>()
                    while (bIdx < pageBlocks.size && pageBlocks[bIdx].dualDialogueId == dualId) {
                        dualGroup.add(pageBlocks[bIdx])
                        bIdx++
                    }

                    val leftBlocks = dualGroup.filter { it.dualSide == com.seshat.writer.model.DualDialogueSide.LEFT }
                    val rightBlocks = dualGroup.filter { it.dualSide == com.seshat.writer.model.DualDialogueSide.RIGHT }

                    val startDualY = currentY
                    var leftY = startDualY
                    var rightY = startDualY

                    // Hollywood Standard Dual Dialogue column geometry:
                    // Printable width = 432f (from X = 108f to X = 540f)
                    // Gutter = 36f (0.5 inch / ~5 characters Courier)
                    // Each column = 198f (2.75 inches / 27.5 characters)
                    val colWidth = 198f
                    val leftX = MARGIN_LEFT // 108f
                    val rightX = MARGIN_LEFT + colWidth + 36f // 342f

                    for (lBlock in leftBlocks) {
                        leftY = renderDualColumnBlock(canvas, lBlock, leftY, leftX, colWidth, textPaint, boldPaint)
                    }

                    for (rBlock in rightBlocks) {
                        rightY = renderDualColumnBlock(canvas, rBlock, rightY, rightX, colWidth, textPaint, boldPaint)
                    }

                    if (options.printRevisionAsterisks && dualGroup.any { it.isRevised }) {
                        canvas.drawText("*", 552f, startDualY, boldPaint)
                    }

                    currentY = maxOf(leftY, rightY)
                    if (bIdx < pageBlocks.size) {
                        val nextBlock = pageBlocks[bIdx]
                        currentY += LINE_HEIGHT
                        if (nextBlock.type == BlockType.SCENE_HEADING) {
                            currentY += LINE_HEIGHT
                        }
                    }
                    continue
                }

                if (options.printRevisionAsterisks && block.isRevised) {
                    canvas.drawText("*", 552f, currentY, boldPaint)
                }

                currentY = renderBlock(
                    canvas = canvas,
                    block = block,
                    startY = currentY,
                    textPaint = textPaint,
                    boldPaint = boldPaint,
                    showSceneNumbers = options.showSceneNumbers,
                    sceneNumber = sceneNumberMap[block.id],
                    includeNotes = options.includeNotes
                )

                // Add blank line between blocks (Hollywood standard spacing)
                if (bIdx < pageBlocks.size - 1) {
                    val nextBlock = pageBlocks[bIdx + 1]
                    // Do not add double blank line between Character and Parenthetical/Dialogue, or Parenthetical and Dialogue
                    val isContinuousDialogue = (block.type == BlockType.CHARACTER && (nextBlock.type == BlockType.PARENTHETICAL || nextBlock.type == BlockType.DIALOGUE)) ||
                            (block.type == BlockType.PARENTHETICAL && nextBlock.type == BlockType.DIALOGUE)
                    if (!isContinuousDialogue) {
                        currentY += LINE_HEIGHT
                        // Hollywood standard: 2 blank lines before Scene Heading (if not at top of page)
                        if (nextBlock.type == BlockType.SCENE_HEADING) {
                            currentY += LINE_HEIGHT
                        }
                    }
                }
                bIdx++
            }

            document.finishPage(page)
        }

        document.writeTo(outputStream)
        document.close()
    }

    fun exportToFile(
        blocks: List<ScreenplayBlock>,
        titlePageData: TitlePageData,
        destinationFile: File,
        options: PdfExportOptions = PdfExportOptions(),
        context: Context? = null
    ) {
        destinationFile.parentFile?.mkdirs()
        FileOutputStream(destinationFile).use { out ->
            exportToStream(
                blocks = blocks,
                titlePageData = titlePageData,
                outputStream = out,
                options = options,
                context = context
            )
        }
    }

    private fun renderTitlePage(
        canvas: Canvas,
        data: TitlePageData,
        regularTypeface: Typeface,
        boldTypeface: Typeface,
        options: PdfExportOptions = PdfExportOptions()
    ) {
        val centerX = PAGE_WIDTH / 2f

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 22f
            typeface = boldTypeface
            textAlign = Paint.Align.CENTER
        }

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 12f
            typeface = regularTypeface
            textAlign = Paint.Align.CENTER
        }

        val authorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 14f
            typeface = boldTypeface
            textAlign = Paint.Align.CENTER
        }

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 11f
            typeface = regularTypeface
            textAlign = Paint.Align.LEFT
        }

        // Title (Upper-middle page)
        val titleText = if (data.title.isNotBlank()) data.title.uppercase(Locale.US) else "UNTITLED SCREENPLAY"
        canvas.drawText(titleText, centerX, 280f, titlePaint)

        // "written by"
        val writtenByText = if (data.writtenByLabel.isNotBlank()) data.writtenByLabel else "written by"
        canvas.drawText(writtenByText, centerX, 320f, subPaint)

        // Author
        if (data.author.isNotBlank()) {
            canvas.drawText(data.author, centerX, 350f, authorPaint)
        }

        // Source Material
        if (data.sourceMaterial.isNotBlank()) {
            canvas.drawText(data.sourceMaterial, centerX, 385f, subPaint)
        }

        // Bottom Left: Contact Info
        if (data.contactInfo.isNotBlank()) {
            var contactY = 660f
            for (line in data.contactInfo.lines()) {
                canvas.drawText(line, ACTION_X, contactY, footerPaint)
                contactY += 12f
            }
        }

        // Bottom Right: Draft Date & Revision Info
        val rightDraftPaint = Paint(footerPaint).apply {
            textAlign = Paint.Align.RIGHT
        }
        var draftY = 660f
        if (data.draftInfo.isNotBlank()) {
            for (line in data.draftInfo.lines()) {
                canvas.drawText(line, HEADER_X, draftY, rightDraftPaint)
                draftY += 12f
            }
        }
        if (options.revisionColor != RevisionColor.WHITE) {
            val revDate = if (!options.revisionDate.isNullOrBlank()) " - ${options.revisionDate}" else ""
            val revLine = "${options.revisionColor.displayName.uppercase(Locale.US)}$revDate"
            canvas.drawText(revLine, HEADER_X, draftY, rightDraftPaint)
        }
    }

    private fun renderBlock(
        canvas: Canvas,
        block: ScreenplayBlock,
        startY: Float,
        textPaint: Paint,
        boldPaint: Paint,
        showSceneNumbers: Boolean = false,
        sceneNumber: String? = null,
        includeNotes: Boolean = false
    ): Float {
        var currentY = startY

        when (block.type) {
            BlockType.SCENE_HEADING -> {
                val headingText = if (block.isOmitted) {
                    if (!showSceneNumbers && sceneNumber != null) "SCENE $sceneNumber - OMITTED" else "OMITTED"
                } else block.text
                val lines = wrapText(headingText.uppercase(Locale.US), ACTION_WIDTH, boldPaint)
                if (showSceneNumbers && sceneNumber != null && lines.isNotEmpty()) {
                    // Left scene number (in left margin)
                    canvas.drawText(sceneNumber, MARGIN_LEFT - 36f, currentY, boldPaint)
                    // Right scene number (aligned with right margin)
                    val rightSceneWidth = boldPaint.measureText(sceneNumber)
                    canvas.drawText(sceneNumber, ACTION_X + ACTION_WIDTH - rightSceneWidth + 24f, currentY, boldPaint)
                }
                for (line in lines) {
                    canvas.drawText(line, ACTION_X, currentY, boldPaint)
                    currentY += LINE_HEIGHT
                }
            }
            BlockType.ACTION -> {
                val lines = wrapText(block.text, ACTION_WIDTH, textPaint)
                for (line in lines) {
                    canvas.drawText(line, ACTION_X, currentY, textPaint)
                    currentY += LINE_HEIGHT
                }
            }
            BlockType.STAGE_ACT_SCENE -> {
                val upper = block.text.uppercase(Locale.US)
                val lines = wrapText(upper, ACTION_WIDTH, boldPaint)
                for (line in lines) {
                    val textW = boldPaint.measureText(line)
                    val centerX = ACTION_X + (ACTION_WIDTH - textW).coerceAtLeast(0f) / 2f
                    canvas.drawText(line, centerX, currentY, boldPaint)
                    currentY += LINE_HEIGHT
                }
            }
            BlockType.STAGE_DIRECTION -> {
                val italicPaint = Paint(textPaint).apply {
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC)
                }
                val lines = wrapText(block.text, ACTION_WIDTH - 30f, italicPaint)
                for (line in lines) {
                    canvas.drawText(line, ACTION_X + 30f, currentY, italicPaint)
                    currentY += LINE_HEIGHT
                }
            }
            BlockType.STAGE_CURTAIN -> {
                val upper = block.text.uppercase(Locale.US)
                val curtainPaint = Paint(boldPaint).apply {
                    textAlign = Paint.Align.RIGHT
                }
                canvas.drawText(upper, TRANSITION_RIGHT_X, currentY, curtainPaint)
                currentY += LINE_HEIGHT
            }
            BlockType.COMIC_PAGE -> {
                val upper = block.text.uppercase(Locale.US)
                val lines = wrapText(upper, ACTION_WIDTH, boldPaint)
                for (line in lines) {
                    canvas.drawText(line, ACTION_X, currentY, boldPaint)
                    currentY += LINE_HEIGHT
                }
            }
            BlockType.COMIC_PANEL -> {
                val upper = block.text.uppercase(Locale.US)
                val lines = wrapText(upper, ACTION_WIDTH, boldPaint)
                for (line in lines) {
                    canvas.drawText(line, ACTION_X, currentY, boldPaint)
                    currentY += LINE_HEIGHT
                }
            }
            BlockType.COMIC_ART_NOTE -> {
                val lines = wrapText(block.text, ACTION_WIDTH, textPaint)
                for (line in lines) {
                    canvas.drawText(line, ACTION_X, currentY, textPaint)
                    currentY += LINE_HEIGHT
                }
            }
            BlockType.COMIC_CAPTION -> {
                val italicPaint = Paint(textPaint).apply {
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC)
                }
                val captionText = if (block.text.uppercase(Locale.US).startsWith("CAPTION")) block.text else "CAPTION: ${block.text}"
                val lines = wrapText(captionText, ACTION_WIDTH - 20f, italicPaint)
                for (line in lines) {
                    canvas.drawText(line, ACTION_X + 20f, currentY, italicPaint)
                    currentY += LINE_HEIGHT
                }
            }
            BlockType.COMIC_SFX -> {
                val upper = block.text.uppercase(Locale.US)
                val lines = wrapText(upper, ACTION_WIDTH, boldPaint)
                for (line in lines) {
                    canvas.drawText(line, ACTION_X + 40f, currentY, boldPaint)
                    currentY += LINE_HEIGHT
                }
            }
            BlockType.CHARACTER -> {
                val upperName = block.text.uppercase(Locale.US)
                canvas.drawText(upperName, CHARACTER_X, currentY, boldPaint)
                currentY += LINE_HEIGHT
            }
            BlockType.PARENTHETICAL -> {
                val formatted = if (block.text.startsWith("(") && block.text.endsWith(")")) block.text else "(${block.text.trim('(', ')')})"
                val lines = wrapText(formatted, PARENTHETICAL_WIDTH, textPaint)
                for (line in lines) {
                    canvas.drawText(line, PARENTHETICAL_X, currentY, textPaint)
                    currentY += LINE_HEIGHT
                }
            }
            BlockType.DIALOGUE, BlockType.COMIC_BALLOON -> {
                val lines = wrapText(block.text, DIALOGUE_WIDTH, textPaint)
                for (line in lines) {
                    canvas.drawText(line, DIALOGUE_X, currentY, textPaint)
                    currentY += LINE_HEIGHT
                }
            }
            BlockType.TRANSITION -> {
                val upper = block.text.uppercase(Locale.US)
                val transitionPaint = Paint(boldPaint).apply {
                    textAlign = Paint.Align.RIGHT
                }
                canvas.drawText(upper, TRANSITION_RIGHT_X, currentY, transitionPaint)
                currentY += LINE_HEIGHT
            }
        }

        // Optional Writer's Notes in PDF
        if (includeNotes && !block.note.isNullOrBlank()) {
            val notePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(100, 100, 100)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC)
            }
            val noteLines = wrapText("[[NOTE: ${block.note}]]", ACTION_WIDTH, notePaint)
            currentY += 2f
            for (line in noteLines) {
                currentY += 12f
                canvas.drawText(line, ACTION_X, currentY, notePaint)
            }
            currentY += 4f
        }

        return currentY
    }

    private fun renderWatermark(canvas: Canvas, text: String, typeface: Typeface, opacity: Float = 0.25f) {
        canvas.save()
        val centerX = PAGE_WIDTH / 2f
        val centerY = PAGE_HEIGHT / 2f
        canvas.translate(centerX, centerY)
        canvas.rotate(-45f) // 45 degree upward diagonal across page

        val alphaInt = (opacity.coerceIn(0.05f, 0.85f) * 255).toInt()
        val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(alphaInt, 110, 110, 110)
            textSize = if (text.length > 28) 36f else if (text.length > 18) 44f else 52f
            this.typeface = typeface
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.16f
        }

        canvas.drawText(text.uppercase(Locale.US), 0f, 0f, watermarkPaint)
        canvas.restore()
    }

    private fun renderDualColumnBlock(
        canvas: Canvas,
        block: ScreenplayBlock,
        startY: Float,
        columnStartX: Float,
        columnWidth: Float,
        textPaint: Paint,
        boldPaint: Paint
    ): Float {
        var currentY = startY
        when (block.type) {
            BlockType.CHARACTER -> {
                val upperName = block.text.uppercase(Locale.US)
                val textWidth = boldPaint.measureText(upperName)
                val centerX = columnStartX + (columnWidth - textWidth).coerceAtLeast(0f) / 2f
                canvas.drawText(upperName, centerX, currentY, boldPaint)
                currentY += LINE_HEIGHT + 2f // Micro-space so character name doesn't sit flush on dialogue
            }
            BlockType.PARENTHETICAL -> {
                val formatted = if (block.text.startsWith("(") && block.text.endsWith(")")) block.text else "(${block.text.trim('(', ')')})"
                val lines = wrapText(formatted, columnWidth - 8f, textPaint)
                for (line in lines) {
                    val textWidth = textPaint.measureText(line)
                    val centerX = columnStartX + (columnWidth - textWidth).coerceAtLeast(0f) / 2f
                    canvas.drawText(line, centerX, currentY, textPaint)
                    currentY += LINE_HEIGHT
                }
                currentY += 2f
            }
            BlockType.DIALOGUE -> {
                val lines = wrapText(block.text, columnWidth - 4f, textPaint)
                for (line in lines) {
                    canvas.drawText(line, columnStartX, currentY, textPaint)
                    currentY += LINE_HEIGHT
                }
            }
            else -> {
                val lines = wrapText(block.text, columnWidth - 4f, textPaint)
                for (line in lines) {
                    canvas.drawText(line, columnStartX, currentY, textPaint)
                    currentY += LINE_HEIGHT
                }
            }
        }
        return currentY
    }

    private fun wrapText(text: String, maxWidth: Float, paint: Paint): List<String> {
        if (text.isEmpty()) return listOf("")
        val words = text.split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            if (currentLine.isEmpty()) {
                currentLine.append(word)
            } else {
                val testLine = "$currentLine $word"
                val width = paint.measureText(testLine)
                if (width <= maxWidth) {
                    currentLine.append(" ").append(word)
                } else {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder(word)
                }
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }

    fun exportCharacterReportToStream(
        analytics: ScriptAnalytics,
        scriptTitle: String,
        outputStream: OutputStream,
        context: Context? = null
    ) {
        val document = PdfDocument()
        val courierRegular = getCourierTypeface(context, bold = false)
        val courierBold = getCourierTypeface(context, bold = true)

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 14f
            typeface = courierBold
        }

        val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 10f
            typeface = courierRegular
        }

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10f
            typeface = courierRegular
        }

        val boldBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10f
            typeface = courierBold
        }

        var pageIndex = 1
        fun startNewReportPage(): Pair<PdfDocument.Page, Canvas> {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex++).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            canvas.drawText("CHARACTER BREAKDOWN & DIALOGUE REPORT", MARGIN_LEFT, HEADER_Y, subHeaderPaint)
            val pageStr = "${pageIndex - 1}."
            val pWidth = subHeaderPaint.measureText(pageStr)
            canvas.drawText(pageStr, PAGE_WIDTH - MARGIN_RIGHT - pWidth, HEADER_Y, subHeaderPaint)
            return Pair(page, canvas)
        }

        var (currentPage, canvas) = startNewReportPage()
        var currentY = MARGIN_TOP

        // Title Block
        canvas.drawText("CHARACTER BREAKDOWN & DIALOGUE REPORT", MARGIN_LEFT, currentY, headerPaint)
        currentY += 16f
        canvas.drawText("SCRIPT: ${scriptTitle.ifBlank { "UNTITLED SCREENPLAY" }.uppercase(Locale.US)}", MARGIN_LEFT, currentY, boldBodyPaint)
        currentY += 20f

        // Summary Statistics Box
        val boxPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val fillBoxPaint = Paint().apply {
            color = Color.rgb(245, 245, 245)
            style = Paint.Style.FILL
        }
        canvas.drawRect(MARGIN_LEFT, currentY, PAGE_WIDTH - MARGIN_RIGHT, currentY + 45f, fillBoxPaint)
        canvas.drawRect(MARGIN_LEFT, currentY, PAGE_WIDTH - MARGIN_RIGHT, currentY + 45f, boxPaint)

        val col1X = MARGIN_LEFT + 12f
        val col2X = MARGIN_LEFT + 150f
        val col3X = MARGIN_LEFT + 300f

        canvas.drawText("Speaking Characters: ${analytics.totalCharacters}", col1X, currentY + 18f, boldBodyPaint)
        canvas.drawText("Total Dialogue Cues: ${analytics.totalDialogueCues}", col1X, currentY + 32f, bodyPaint)

        canvas.drawText("Spoken Words: ${analytics.totalDialogueWords}", col2X, currentY + 18f, boldBodyPaint)
        canvas.drawText("Total Script Words: ${analytics.totalScriptWords}", col2X, currentY + 32f, bodyPaint)

        canvas.drawText("Dialogue Volume: ${String.format(Locale.US, "%.1f%%", analytics.percentageSpoken)}", col3X, currentY + 18f, boldBodyPaint)
        currentY += 65f

        // Ranked Characters Table
        canvas.drawText("CHARACTER SUMMARY RANKING:", MARGIN_LEFT, currentY, boldBodyPaint)
        currentY += 14f

        val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 9f
            typeface = courierBold
        }
        canvas.drawText("RANK  CHARACTER                 WORDS    % DIALOGUE   CUES   SCENES", MARGIN_LEFT, currentY, tableHeaderPaint)
        currentY += 4f
        canvas.drawLine(MARGIN_LEFT, currentY, PAGE_WIDTH - MARGIN_RIGHT, currentY, boxPaint)
        currentY += 12f

        for ((idx, char) in analytics.characterBreakdowns.withIndex()) {
            if (currentY > PAGE_HEIGHT - MARGIN_BOTTOM - 20f) {
                document.finishPage(currentPage)
                val newPagePair = startNewReportPage()
                currentPage = newPagePair.first
                canvas = newPagePair.second
                currentY = MARGIN_TOP
            }

            val rankStr = String.format(Locale.US, "%2d.", idx + 1)
            val nameStr = char.characterName.take(24).padEnd(25)
            val wordsStr = String.format(Locale.US, "%5d", char.totalWords)
            val pctStr = String.format(Locale.US, "%6.1f%%", char.percentageOfDialogue)
            val cuesStr = String.format(Locale.US, "%4d", char.totalSpeeches)
            val scenesStr = String.format(Locale.US, "%3d scenes (Sc. %s)", char.speakingSceneNumbers.size, char.speakingSceneNumbers.take(6).joinToString(","))

            val lineStr = "$rankStr  $nameStr  $wordsStr     $pctStr   $cuesStr   $scenesStr"
            canvas.drawText(lineStr, MARGIN_LEFT, currentY, bodyPaint)
            currentY += 12f
        }

        currentY += 15f

        // Detailed Breakdown per Character
        for (char in analytics.characterBreakdowns) {
            if (currentY > PAGE_HEIGHT - MARGIN_BOTTOM - 60f) {
                document.finishPage(currentPage)
                val newPagePair = startNewReportPage()
                currentPage = newPagePair.first
                canvas = newPagePair.second
                currentY = MARGIN_TOP
            }

            canvas.drawLine(MARGIN_LEFT, currentY, PAGE_WIDTH - MARGIN_RIGHT, currentY, boxPaint)
            currentY += 14f
            canvas.drawText("CHARACTER: ${char.characterName}", MARGIN_LEFT, currentY, boldBodyPaint)
            currentY += 12f
            canvas.drawText(
                "Speeches: ${char.totalSpeeches} | Words: ${char.totalWords} (${String.format(Locale.US, "%.1f%%", char.percentageOfDialogue)}) | Scenes: ${char.speakingSceneNumbers.joinToString(", ")}",
                MARGIN_LEFT,
                currentY,
                subHeaderPaint
            )
            currentY += 14f

            for (speech in char.speeches) {
                if (currentY > PAGE_HEIGHT - MARGIN_BOTTOM - 40f) {
                    document.finishPage(currentPage)
                    val newPagePair = startNewReportPage()
                    currentPage = newPagePair.first
                    canvas = newPagePair.second
                    currentY = MARGIN_TOP
                }

                val speechHeader = "[Scene ${speech.sceneNumber} | Page ${speech.pageNumber} | ${speech.sceneHeading}] (${speech.wordCount} words)"
                canvas.drawText(speechHeader, MARGIN_LEFT + 10f, currentY, boldBodyPaint)
                currentY += 11f

                if (!speech.parenthetical.isNullOrBlank()) {
                    canvas.drawText("(${speech.parenthetical})", MARGIN_LEFT + 20f, currentY, subHeaderPaint)
                    currentY += 10f
                }

                val wrapped = wrapText("\"${speech.dialogueText}\"", PAGE_WIDTH - MARGIN_RIGHT - MARGIN_LEFT - 30f, bodyPaint)
                for (wLine in wrapped) {
                    if (currentY > PAGE_HEIGHT - MARGIN_BOTTOM) {
                        document.finishPage(currentPage)
                        val newPagePair = startNewReportPage()
                        currentPage = newPagePair.first
                        canvas = newPagePair.second
                        currentY = MARGIN_TOP
                    }
                    canvas.drawText(wLine, MARGIN_LEFT + 20f, currentY, bodyPaint)
                    currentY += 11f
                }
                currentY += 4f
            }
            currentY += 8f
        }

        document.finishPage(currentPage)
        document.writeTo(outputStream)
        document.close()
    }

    fun exportCharacterReportToFile(
        analytics: ScriptAnalytics,
        scriptTitle: String,
        destinationFile: File,
        context: Context? = null
    ) {
        destinationFile.parentFile?.mkdirs()
        FileOutputStream(destinationFile).use { out ->
            exportCharacterReportToStream(
                analytics = analytics,
                scriptTitle = scriptTitle,
                outputStream = out,
                context = context
            )
        }
    }

    fun exportLocationReportToStream(
        analytics: LocationAnalytics,
        scriptTitle: String,
        outputStream: OutputStream,
        context: Context? = null
    ) {
        val document = PdfDocument()
        val courierRegular = getCourierTypeface(context, bold = false)
        val courierBold = getCourierTypeface(context, bold = true)

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 14f
            typeface = courierBold
        }

        val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 10f
            typeface = courierRegular
        }

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10f
            typeface = courierRegular
        }

        val boldBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10f
            typeface = courierBold
        }

        val boxPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val fillBoxPaint = Paint().apply {
            color = Color.rgb(245, 245, 245)
            style = Paint.Style.FILL
        }

        var pageIndex = 1
        fun startNewReportPage(): Pair<PdfDocument.Page, Canvas> {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex++).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            canvas.drawText("LOCATION & SET BREAKDOWN REPORT", MARGIN_LEFT, HEADER_Y, subHeaderPaint)
            val pageStr = "${pageIndex - 1}."
            val pWidth = subHeaderPaint.measureText(pageStr)
            canvas.drawText(pageStr, PAGE_WIDTH - MARGIN_RIGHT - pWidth, HEADER_Y, subHeaderPaint)
            return Pair(page, canvas)
        }

        var (currentPage, canvas) = startNewReportPage()
        var currentY = MARGIN_TOP

        // Title Block
        canvas.drawText("LOCATION & SET BREAKDOWN REPORT", MARGIN_LEFT, currentY, headerPaint)
        currentY += 16f
        canvas.drawText("SCRIPT: ${scriptTitle.ifBlank { "UNTITLED SCREENPLAY" }.uppercase(Locale.US)}", MARGIN_LEFT, currentY, boldBodyPaint)
        currentY += 20f

        // Overview Box
        canvas.drawRect(MARGIN_LEFT, currentY, PAGE_WIDTH - MARGIN_RIGHT, currentY + 45f, fillBoxPaint)
        canvas.drawRect(MARGIN_LEFT, currentY, PAGE_WIDTH - MARGIN_RIGHT, currentY + 45f, boxPaint)

        val col1X = MARGIN_LEFT + 12f
        val col2X = MARGIN_LEFT + 150f
        val col3X = MARGIN_LEFT + 300f

        val intPct = if (analytics.totalScenes > 0) (analytics.intScenesCount * 100f / analytics.totalScenes) else 0f
        val extPct = if (analytics.totalScenes > 0) (analytics.extScenesCount * 100f / analytics.totalScenes) else 0f

        canvas.drawText("Unique Locations: ${analytics.totalUniqueLocations}", col1X, currentY + 18f, boldBodyPaint)
        canvas.drawText("Total Scenes: ${analytics.totalScenes}", col1X, currentY + 32f, bodyPaint)

        canvas.drawText("Interior (INT): ${analytics.intScenesCount} (${String.format(Locale.US, "%.1f%%", intPct)})", col2X, currentY + 18f, boldBodyPaint)
        canvas.drawText("Exterior (EXT): ${analytics.extScenesCount} (${String.format(Locale.US, "%.1f%%", extPct)})", col2X, currentY + 32f, bodyPaint)

        canvas.drawText("Day Scenes: ${analytics.dayScenesCount} | Night: ${analytics.nightScenesCount}", col3X, currentY + 18f, boldBodyPaint)
        canvas.drawText("Other Time: ${analytics.otherTimeScenesCount}", col3X, currentY + 32f, bodyPaint)
        currentY += 65f

        // Table Header
        canvas.drawText("SETS SORTED BY SCENE VOLUME:", MARGIN_LEFT, currentY, boldBodyPaint)
        currentY += 14f

        val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 9f
            typeface = courierBold
        }
        canvas.drawText("RANK  LOCATION NAME                   TYPE       SCENES  DAY/NIGHT  SCENE LIST", MARGIN_LEFT, currentY, tableHeaderPaint)
        currentY += 4f
        canvas.drawLine(MARGIN_LEFT, currentY, PAGE_WIDTH - MARGIN_RIGHT, currentY, boxPaint)
        currentY += 12f

        for ((idx, loc) in analytics.locationBreakdowns.withIndex()) {
            if (currentY > PAGE_HEIGHT - MARGIN_BOTTOM - 20f) {
                document.finishPage(currentPage)
                val newPagePair = startNewReportPage()
                currentPage = newPagePair.first
                canvas = newPagePair.second
                currentY = MARGIN_TOP
            }

            val rankStr = String.format(Locale.US, "%2d.", idx + 1)
            val nameStr = loc.locationName.take(24).padEnd(25)
            val typeStr = loc.primarySetting.prefix.padEnd(10)
            val countStr = String.format(Locale.US, "%2d", loc.totalScenes)
            val dayNightStr = "D:${loc.dayScenes}/N:${loc.nightScenes}".padEnd(10)
            val scenesStr = "Sc. ${loc.sceneNumbers.take(6).joinToString(",")}"

            val lineStr = "$rankStr  $nameStr  $typeStr $countStr      $dayNightStr $scenesStr"
            canvas.drawText(lineStr, MARGIN_LEFT, currentY, bodyPaint)
            currentY += 12f
        }

        currentY += 15f

        // Detailed Location & Cast Breakdown
        for (loc in analytics.locationBreakdowns) {
            if (currentY > PAGE_HEIGHT - MARGIN_BOTTOM - 60f) {
                document.finishPage(currentPage)
                val newPagePair = startNewReportPage()
                currentPage = newPagePair.first
                canvas = newPagePair.second
                currentY = MARGIN_TOP
            }

            canvas.drawLine(MARGIN_LEFT, currentY, PAGE_WIDTH - MARGIN_RIGHT, currentY, boxPaint)
            currentY += 14f
            canvas.drawText("SET / LOCATION: ${loc.locationName} (${loc.primarySetting.prefix})", MARGIN_LEFT, currentY, boldBodyPaint)
            currentY += 12f
            val castSummary = if (loc.uniqueCharacters.isNotEmpty()) loc.uniqueCharacters.joinToString(", ") else "Silent / Non-speaking"
            canvas.drawText(
                "Scenes: ${loc.totalScenes} (Sc. ${loc.sceneNumbers.joinToString(", ")}) | Cast: $castSummary",
                MARGIN_LEFT,
                currentY,
                subHeaderPaint
            )
            currentY += 14f

            for (scene in loc.scenes) {
                if (currentY > PAGE_HEIGHT - MARGIN_BOTTOM - 30f) {
                    document.finishPage(currentPage)
                    val newPagePair = startNewReportPage()
                    currentPage = newPagePair.first
                    canvas = newPagePair.second
                    currentY = MARGIN_TOP
                }
                val castList = if (scene.speakingCharacters.isNotEmpty()) scene.speakingCharacters.joinToString(", ") else "Silent"
                val scHeader = "[Scene ${scene.sceneNumber} | Pg ${scene.pageNumber} | ${scene.timeOfDay}] ${scene.sceneHeading}"
                canvas.drawText(scHeader, MARGIN_LEFT + 10f, currentY, boldBodyPaint)
                currentY += 11f
                canvas.drawText("  Speaking Cast: $castList", MARGIN_LEFT + 10f, currentY, bodyPaint)
                currentY += 12f
            }
            currentY += 6f
        }

        document.finishPage(currentPage)
        document.writeTo(outputStream)
        document.close()
    }

    fun exportLocationReportToFile(
        analytics: LocationAnalytics,
        scriptTitle: String,
        destinationFile: File,
        context: Context? = null
    ) {
        destinationFile.parentFile?.mkdirs()
        FileOutputStream(destinationFile).use { out ->
            exportLocationReportToStream(
                analytics = analytics,
                scriptTitle = scriptTitle,
                outputStream = out,
                context = context
            )
        }
    }

    fun exportCastMatrixToStream(
        matrix: CastSceneMatrix,
        scriptTitle: String,
        outputStream: OutputStream,
        context: Context? = null
    ) {
        val document = PdfDocument()
        val courierRegular = getCourierTypeface(context, bold = false)
        val courierBold = getCourierTypeface(context, bold = true)

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 14f
            typeface = courierBold
        }

        val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 10f
            typeface = courierRegular
        }

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10f
            typeface = courierRegular
        }

        val boldBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10f
            typeface = courierBold
        }

        val boxPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val fillBoxPaint = Paint().apply {
            color = Color.rgb(245, 245, 245)
            style = Paint.Style.FILL
        }

        var pageIndex = 1
        fun startNewReportPage(): Pair<PdfDocument.Page, Canvas> {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex++).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            canvas.drawText("CAST-SCENE BREAKDOWN MATRIX (CALL SHEET)", MARGIN_LEFT, HEADER_Y, subHeaderPaint)
            val pageStr = "${pageIndex - 1}."
            val pWidth = subHeaderPaint.measureText(pageStr)
            canvas.drawText(pageStr, PAGE_WIDTH - MARGIN_RIGHT - pWidth, HEADER_Y, subHeaderPaint)
            return Pair(page, canvas)
        }

        var (currentPage, canvas) = startNewReportPage()
        var currentY = MARGIN_TOP

        // Title Block
        canvas.drawText("CAST-SCENE BREAKDOWN MATRIX", MARGIN_LEFT, currentY, headerPaint)
        currentY += 16f
        canvas.drawText("SCRIPT: ${scriptTitle.ifBlank { "UNTITLED SCREENPLAY" }.uppercase(Locale.US)}", MARGIN_LEFT, currentY, boldBodyPaint)
        currentY += 20f

        // Overview Box
        canvas.drawRect(MARGIN_LEFT, currentY, PAGE_WIDTH - MARGIN_RIGHT, currentY + 36f, fillBoxPaint)
        canvas.drawRect(MARGIN_LEFT, currentY, PAGE_WIDTH - MARGIN_RIGHT, currentY + 36f, boxPaint)

        canvas.drawText("Total Cast Members: ${matrix.allCharacters.size}", MARGIN_LEFT + 12f, currentY + 22f, boldBodyPaint)
        canvas.drawText("Total Production Scenes: ${matrix.scenes.size}", MARGIN_LEFT + 220f, currentY + 22f, boldBodyPaint)
        currentY += 50f

        // Scene-by-scene Call Sheet Grid
        canvas.drawText("SCENE-BY-SCENE CALL SHEET BREAKDOWN:", MARGIN_LEFT, currentY, boldBodyPaint)
        currentY += 14f

        val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 9f
            typeface = courierBold
        }
        canvas.drawText("SCENE   PG   SETTING / SLUGLINE                   SPEAKING CAST", MARGIN_LEFT, currentY, tableHeaderPaint)
        currentY += 4f
        canvas.drawLine(MARGIN_LEFT, currentY, PAGE_WIDTH - MARGIN_RIGHT, currentY, boxPaint)
        currentY += 12f

        for (sc in matrix.scenes) {
            if (currentY > PAGE_HEIGHT - MARGIN_BOTTOM - 20f) {
                document.finishPage(currentPage)
                val newPagePair = startNewReportPage()
                currentPage = newPagePair.first
                canvas = newPagePair.second
                currentY = MARGIN_TOP
            }

            val scNumStr = String.format(Locale.US, "Sc %-4s", sc.sceneNumber)
            val pgStr = String.format(Locale.US, "p%-3d", sc.pageNumber)
            val headStr = sc.sceneHeading.take(30).padEnd(32)
            val castStr = if (sc.isOmitted) "[OMITTED]" else if (sc.characters.isNotEmpty()) sc.characters.joinToString(", ") else "[SILENT]"

            val lineStr = "$scNumStr  $pgStr  $headStr  $castStr"
            canvas.drawText(lineStr, MARGIN_LEFT, currentY, if (sc.isOmitted) subHeaderPaint else bodyPaint)
            currentY += 12f
        }

        currentY += 15f

        // Cast Appearance Summary
        if (currentY > PAGE_HEIGHT - MARGIN_BOTTOM - 60f) {
            document.finishPage(currentPage)
            val newPagePair = startNewReportPage()
            currentPage = newPagePair.first
            canvas = newPagePair.second
            currentY = MARGIN_TOP
        }

        canvas.drawLine(MARGIN_LEFT, currentY, PAGE_WIDTH - MARGIN_RIGHT, currentY, boxPaint)
        currentY += 14f
        canvas.drawText("CAST APPEARANCE SUMMARY:", MARGIN_LEFT, currentY, boldBodyPaint)
        currentY += 14f

        for (char in matrix.allCharacters) {
            if (currentY > PAGE_HEIGHT - MARGIN_BOTTOM - 20f) {
                document.finishPage(currentPage)
                val newPagePair = startNewReportPage()
                currentPage = newPagePair.first
                canvas = newPagePair.second
                currentY = MARGIN_TOP
            }

            val scList = matrix.appearancesByCharacter[char] ?: emptyList()
            val charStr = char.take(24).padEnd(26)
            val countStr = String.format(Locale.US, "%2d scenes", scList.size)
            val scenesJoined = " (Sc. ${scList.joinToString(", ")})"

            canvas.drawText("$charStr : $countStr$scenesJoined", MARGIN_LEFT, currentY, bodyPaint)
            currentY += 12f
        }

        document.finishPage(currentPage)
        document.writeTo(outputStream)
        document.close()
    }

    fun exportCastMatrixToFile(
        matrix: CastSceneMatrix,
        scriptTitle: String,
        destinationFile: File,
        context: Context? = null
    ) {
        destinationFile.parentFile?.mkdirs()
        FileOutputStream(destinationFile).use { out ->
            exportCastMatrixToStream(
                matrix = matrix,
                scriptTitle = scriptTitle,
                outputStream = out,
                context = context
            )
        }
    }
}
