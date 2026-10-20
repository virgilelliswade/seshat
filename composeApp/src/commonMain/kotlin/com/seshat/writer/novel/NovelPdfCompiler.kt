package com.seshat.writer.novel

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * Compiles a NovelManuscript into a William Shunn / Industry-Standard Manuscript PDF.
 * Follows classical publishing guidelines:
 * - US Letter (8.5" x 11") with 1-inch margins
 * - Professional 12pt Serif typography
 * - Double-spaced lines (~250 words per page industry standard)
 * - 0.5-inch first-line paragraph indentation
 * - Running headers (SURNAME / TITLE / PAGE#)
 * - Standard title page with author info and estimated word count
 */
object NovelPdfCompiler {

    const val PAGE_WIDTH = 612 // 8.5 inches * 72 pt/in
    const val PAGE_HEIGHT = 792 // 11.0 inches * 72 pt/in

    const val MARGIN_LEFT = 72f // 1.0 inch
    const val MARGIN_RIGHT = 72f // 1.0 inch
    const val MARGIN_TOP = 72f // 1.0 inch
    const val MARGIN_BOTTOM = 72f // 1.0 inch
    const val USABLE_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT // 468 pt

    const val HEADER_Y = 36f // 0.5 inch from top
    const val FIRST_LINE_INDENT = 36f // 0.5 inch indent

    const val FONT_SIZE_BODY = 12f
    const val LINE_HEIGHT_DOUBLE = 24f // Double-spaced for 12pt font

    fun compileToPdf(
        context: Context,
        manuscript: NovelManuscript,
        options: CompilerOptions = CompilerOptions(),
        outputFile: File
    ): Boolean {
        return try {
            if (outputFile.exists()) outputFile.delete()
            outputFile.parentFile?.mkdirs()

            val selectedChapters = if (options.selectedChapterIds.isNotEmpty()) {
                manuscript.chapters.filter { options.selectedChapterIds.contains(it.id) && !it.isOmitted }
            } else {
                manuscript.chapters.filterNot { it.isOmitted }
            }

            val totalWords = selectedChapters.sumOf { it.wordCount() }
            val roundedWords = (Math.round(totalWords / 100.0) * 100).toInt()

            val doc = PdfDocument()

            // Typography Paints
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = FONT_SIZE_BODY
                typeface = Typeface.create("serif", Typeface.NORMAL)
            }
            val bodyBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = FONT_SIZE_BODY
                typeface = Typeface.create("serif", Typeface.BOLD)
            }
            val chapterHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 15f
                typeface = Typeface.create("serif", Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val chapterTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.DKGRAY
                textSize = 13f
                typeface = Typeface.create("serif", Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
            }
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 20f
                typeface = Typeface.create("serif", Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.DKGRAY
                textSize = 13f
                typeface = Typeface.create("serif", Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
            }
            val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 10f
                typeface = Typeface.create("serif", Typeface.NORMAL)
                textAlign = Paint.Align.RIGHT
            }
            val sceneBreakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = FONT_SIZE_BODY
                typeface = Typeface.create("serif", Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            var pdfPageNumber = 1
            var manuscriptPageNumber = 1

            val authorSurname = if (manuscript.author.isNotBlank()) {
                manuscript.author.trim().split(Regex("\\s+")).last().uppercase(Locale.US)
            } else {
                "AUTHOR"
            }
            val shortTitle = manuscript.title.trim().take(25).uppercase(Locale.US)

            fun drawRunningHeader(canvas: android.graphics.Canvas, pageNum: Int) {
                val headerText = "$authorSurname / $shortTitle / $pageNum"
                canvas.drawText(headerText, PAGE_WIDTH - MARGIN_RIGHT, HEADER_Y, headerPaint)
            }

            // 1. TITLE PAGE
            if (options.includeTitlePage) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pdfPageNumber++).create()
                val page = doc.startPage(pageInfo)
                val canvas = page.canvas

                // Upper Left: Contact / Author Info
                val contactPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.BLACK
                    textSize = 11f
                    typeface = Typeface.create("serif", Typeface.NORMAL)
                }
                var contactY = MARGIN_TOP
                canvas.drawText(manuscript.author.ifBlank { "Author Name" }, MARGIN_LEFT, contactY, contactPaint)
                contactY += 16f
                canvas.drawText("c/o Seshat Studio", MARGIN_LEFT, contactY, contactPaint)
                contactY += 16f
                canvas.drawText("Standard Manuscript Submission", MARGIN_LEFT, contactY, contactPaint)

                // Upper Right: Word Count
                val wordCountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.BLACK
                    textSize = 11f
                    typeface = Typeface.create("serif", Typeface.NORMAL)
                    textAlign = Paint.Align.RIGHT
                }
                canvas.drawText("Words: ~$roundedWords", PAGE_WIDTH - MARGIN_RIGHT, MARGIN_TOP, wordCountPaint)

                // Center: Title, Subtitle, Byline
                val centerY = PAGE_HEIGHT * 0.44f
                canvas.drawText(manuscript.title.uppercase(Locale.US), PAGE_WIDTH / 2f, centerY, titlePaint)
                if (manuscript.subtitle.isNotBlank()) {
                    canvas.drawText(manuscript.subtitle, PAGE_WIDTH / 2f, centerY + 24f, subtitlePaint)
                }

                val bylineY = centerY + if (manuscript.subtitle.isNotBlank()) 60f else 46f
                canvas.drawText("by", PAGE_WIDTH / 2f, bylineY, contactPaint.apply { textAlign = Paint.Align.CENTER })
                canvas.drawText(manuscript.author.ifBlank { "Author Name" }, PAGE_WIDTH / 2f, bylineY + 24f, bodyBoldPaint.apply { textAlign = Paint.Align.CENTER })
                bodyBoldPaint.textAlign = Paint.Align.LEFT

                doc.finishPage(page)
            }

            // 1B. COPYRIGHT PAGE (Optional)
            if (options.includeCopyrightPage || manuscript.frontMatter.includeCopyrightPage) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pdfPageNumber++).create()
                val page = doc.startPage(pageInfo)
                val canvas = page.canvas
                val copyrightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.BLACK
                    textSize = 10f
                    typeface = Typeface.create("serif", Typeface.NORMAL)
                }
                val holder = manuscript.frontMatter.copyrightHolder.ifBlank { manuscript.author.ifBlank { "Author" } }
                val year = manuscript.frontMatter.copyrightYear.ifBlank { "2026" }
                val publisher = manuscript.frontMatter.publisherName.ifBlank { "SESHAT PRESS" }
                var cY = PAGE_HEIGHT * 0.55f
                canvas.drawText("Published by $publisher", MARGIN_LEFT, cY, copyrightPaint); cY += 16f
                canvas.drawText("Copyright © $year by $holder", MARGIN_LEFT, cY, copyrightPaint); cY += 16f
                if (manuscript.frontMatter.allRightsReserved) {
                    canvas.drawText("All rights reserved.", MARGIN_LEFT, cY, copyrightPaint); cY += 24f
                }
                if (manuscript.frontMatter.fictionDisclaimer) {
                    val disclaimerText = "This is a work of fiction. Names, characters, places, and incidents either are the product of the author's imagination or are used fictitiously. Any resemblance to actual persons, living or dead, events, or locales is entirely coincidental."
                    val disLines = wrapTextIntoLines(disclaimerText, copyrightPaint, USABLE_WIDTH, 0f)
                    for (line in disLines) {
                        canvas.drawText(line, MARGIN_LEFT, cY, copyrightPaint)
                        cY += 14f
                    }
                    cY += 12f
                }
                val ed = manuscript.frontMatter.editionNotice.ifBlank { "First Edition" }
                canvas.drawText(ed, MARGIN_LEFT, cY, copyrightPaint)
                doc.finishPage(page)
            }

            // 2. DEDICATION PAGE (Optional)
            val dedication = options.dedicationText.ifBlank { manuscript.frontMatter.dedicationText }
            if ((options.includeDedication || manuscript.frontMatter.includeDedication) && dedication.isNotBlank()) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pdfPageNumber++).create()
                val page = doc.startPage(pageInfo)
                val canvas = page.canvas

                val dedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.BLACK
                    textSize = 12f
                    typeface = Typeface.create("serif", Typeface.ITALIC)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(dedication, PAGE_WIDTH / 2f, PAGE_HEIGHT * 0.45f, dedPaint)
                doc.finishPage(page)
            }

            // 2B. EPIGRAPH PAGE (Optional)
            val epigraph = options.epigraphText.ifBlank { manuscript.frontMatter.epigraphText }
            if ((options.includeEpigraph || manuscript.frontMatter.includeEpigraph) && epigraph.isNotBlank()) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pdfPageNumber++).create()
                val page = doc.startPage(pageInfo)
                val canvas = page.canvas

                val epiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.BLACK
                    textSize = 12f
                    typeface = Typeface.create("serif", Typeface.ITALIC)
                    textAlign = Paint.Align.CENTER
                }
                val epiLines = wrapTextIntoLines("“$epigraph”", epiPaint, USABLE_WIDTH * 0.8f, 0f)
                var epiY = PAGE_HEIGHT * 0.42f
                for (line in epiLines) {
                    canvas.drawText(line, PAGE_WIDTH / 2f, epiY, epiPaint)
                    epiY += 18f
                }
                val source = options.epigraphSource.ifBlank { manuscript.frontMatter.epigraphSource }
                if (source.isNotBlank()) {
                    val srcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.DKGRAY
                        textSize = 11f
                        typeface = Typeface.create("serif", Typeface.NORMAL)
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText("— $source", PAGE_WIDTH / 2f, epiY + 12f, srcPaint)
                }
                doc.finishPage(page)
            }

            // 3. TABLE OF CONTENTS (Optional)
            if (options.includeTableOfContents) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pdfPageNumber++).create()
                val page = doc.startPage(pageInfo)
                val canvas = page.canvas

                canvas.drawText("TABLE OF CONTENTS", PAGE_WIDTH / 2f, MARGIN_TOP + 30f, chapterHeaderPaint)
                var tocY = MARGIN_TOP + 80f
                val tocPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.BLACK
                    textSize = 12f
                    typeface = Typeface.create("serif", Typeface.NORMAL)
                }

                for ((idx, ch) in selectedChapters.withIndex()) {
                    if (tocY > PAGE_HEIGHT - MARGIN_BOTTOM) break
                    canvas.drawText("${idx + 1}.  ${ch.title}", MARGIN_LEFT, tocY, tocPaint)
                    tocY += 22f
                }
                doc.finishPage(page)
            }

            // 4. CHAPTER PAGES (Main Manuscript Body)
            for ((chIdx, ch) in selectedChapters.withIndex()) {
                var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pdfPageNumber++).create()
                var page = doc.startPage(pageInfo)
                var canvas = page.canvas

                drawRunningHeader(canvas, manuscriptPageNumber)

                // Chapter Head dropped ~1/4 down the page (William Shunn standard)
                var currentY = MARGIN_TOP + 110f

                val headerText = ch.paragraphs.firstOrNull { it.type == NovelParagraphType.CHAPTER_HEADER }?.text
                    ?: "CHAPTER ${chIdx + 1}"

                canvas.drawText(headerText.uppercase(Locale.US), PAGE_WIDTH / 2f, currentY, chapterHeaderPaint)
                currentY += 26f

                // If chapter has distinct title from header, print it beneath
                if (ch.title.isNotBlank() && !ch.title.equals(headerText, ignoreCase = true) && !ch.title.startsWith("Chapter", ignoreCase = true)) {
                    canvas.drawText(ch.title, PAGE_WIDTH / 2f, currentY, chapterTitlePaint)
                    currentY += 24f
                }

                currentY += 24f // Space before body text begins

                fun checkNewPage(neededHeight: Float) {
                    if (currentY + neededHeight > PAGE_HEIGHT - MARGIN_BOTTOM) {
                        doc.finishPage(page)
                        manuscriptPageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pdfPageNumber++).create()
                        page = doc.startPage(pageInfo)
                        canvas = page.canvas
                        drawRunningHeader(canvas, manuscriptPageNumber)
                        currentY = MARGIN_TOP
                    }
                }

                for (p in ch.paragraphs) {
                    when (p.type) {
                        NovelParagraphType.CHAPTER_HEADER -> {
                            // Already drawn above
                        }
                        NovelParagraphType.SCENE_BREAK -> {
                            checkNewPage(LINE_HEIGHT_DOUBLE * 2)
                            currentY += LINE_HEIGHT_DOUBLE * 0.5f
                            val sym = if (p.text.isNotBlank() && p.text != "* * *") {
                                p.text
                            } else {
                                options.sceneBreakStyle.symbol.ifBlank {
                                    manuscript.sceneBreakStyle.symbol.ifBlank { options.sceneBreakSymbol }
                                }
                            }
                            canvas.drawText(sym, PAGE_WIDTH / 2f, currentY, sceneBreakPaint)
                            currentY += LINE_HEIGHT_DOUBLE * 1.5f
                        }
                        NovelParagraphType.NARRATIVE, NovelParagraphType.DIALOGUE -> {
                            if (p.text.isBlank()) continue

                            val lines = wrapTextIntoLines(p.text, bodyPaint, USABLE_WIDTH, FIRST_LINE_INDENT)
                            for ((lineIdx, line) in lines.withIndex()) {
                                checkNewPage(LINE_HEIGHT_DOUBLE)
                                val x = if (lineIdx == 0) MARGIN_LEFT + FIRST_LINE_INDENT else MARGIN_LEFT
                                canvas.drawText(line, x, currentY, bodyPaint)
                                currentY += LINE_HEIGHT_DOUBLE
                            }
                        }
                    }
                }

                doc.finishPage(page)
                manuscriptPageNumber++
            }

            // 5. BACK MATTER - ACKNOWLEDGMENTS (Optional)
            val ackText = options.acknowledgmentsText.ifBlank { manuscript.backMatter.acknowledgmentsText }
            if ((options.includeAcknowledgments || manuscript.backMatter.includeAcknowledgments) && ackText.isNotBlank()) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pdfPageNumber++).create()
                val page = doc.startPage(pageInfo)
                val canvas = page.canvas
                drawRunningHeader(canvas, manuscriptPageNumber++)
                var ackY = MARGIN_TOP + 110f
                canvas.drawText("ACKNOWLEDGMENTS", PAGE_WIDTH / 2f, ackY, chapterHeaderPaint)
                ackY += 40f
                val ackLines = wrapTextIntoLines(ackText, bodyPaint, USABLE_WIDTH, FIRST_LINE_INDENT)
                for (line in ackLines) {
                    if (ackY > PAGE_HEIGHT - MARGIN_BOTTOM) break
                    canvas.drawText(line, MARGIN_LEFT, ackY, bodyPaint)
                    ackY += LINE_HEIGHT_DOUBLE
                }
                doc.finishPage(page)
            }

            // 6. BACK MATTER - ABOUT THE AUTHOR (Optional)
            val bioText = options.authorBioText.ifBlank { manuscript.backMatter.authorBioText }
            if ((options.includeAuthorBio || manuscript.backMatter.includeAuthorBio) && bioText.isNotBlank()) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pdfPageNumber++).create()
                val page = doc.startPage(pageInfo)
                val canvas = page.canvas
                drawRunningHeader(canvas, manuscriptPageNumber++)
                var bioY = MARGIN_TOP + 110f
                canvas.drawText("ABOUT THE AUTHOR", PAGE_WIDTH / 2f, bioY, chapterHeaderPaint)
                bioY += 40f
                val bioLines = wrapTextIntoLines(bioText, bodyPaint, USABLE_WIDTH, FIRST_LINE_INDENT)
                for (line in bioLines) {
                    if (bioY > PAGE_HEIGHT - MARGIN_BOTTOM) break
                    canvas.drawText(line, MARGIN_LEFT, bioY, bodyPaint)
                    bioY += LINE_HEIGHT_DOUBLE
                }
                doc.finishPage(page)
            }

            FileOutputStream(outputFile).use { out ->
                doc.writeTo(out)
            }
            doc.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Splits a paragraph of prose into wrapped lines.
     * The first line takes into account [firstLineIndent], while subsequent lines
     * span the full [usableWidth].
     */
    private fun wrapTextIntoLines(
        text: String,
        paint: Paint,
        usableWidth: Float,
        firstLineIndent: Float
    ): List<String> {
        val words = text.trim().split(Regex("\\s+"))
        if (words.isEmpty()) return emptyList()

        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val isFirstLine = lines.isEmpty()
            val maxAllowedWidth = if (isFirstLine) usableWidth - firstLineIndent else usableWidth

            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val measuredWidth = paint.measureText(testLine)

            if (measuredWidth <= maxAllowedWidth) {
                currentLine = StringBuilder(testLine)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder(word)
                } else {
                    // Single word exceeds line width (very long word), force wrap
                    lines.add(word)
                    currentLine = StringBuilder()
                }
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }

        return lines
    }
}
