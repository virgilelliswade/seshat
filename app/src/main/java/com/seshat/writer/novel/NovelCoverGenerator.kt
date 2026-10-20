package com.seshat.writer.novel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * High-resolution Cover & Dust Jacket generator for novels.
 * Supports:
 * 1. Digital eBook Front Covers (1600 x 2560 px / 1:1.6 standard KDP/Apple Books ratio).
 * 2. Full Dust Jacket Wraps (Self-publishing hardcover standard with Back Flap, Back Cover,
 *    Dynamic Spine calculated from page count, Front Cover, and Front Flap).
 */
object NovelCoverGenerator {

    const val EBOOK_COVER_WIDTH = 1600
    const val EBOOK_COVER_HEIGHT = 2560

    /**
     * Generates a high-resolution Front Cover Bitmap.
     */
    fun renderFrontCoverBitmap(
        manuscript: NovelManuscript,
        design: NovelCoverDesign,
        width: Int = EBOOK_COVER_WIDTH,
        height: Int = EBOOK_COVER_HEIGHT
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val theme = design.themePreset
        val bgColor = Color.parseColor(theme.bgHex)
        val accentColor = Color.parseColor(theme.accentHex)
        val textColor = Color.parseColor(theme.textHex)
        val subColor = Color.parseColor(theme.subHex)

        // 1. Draw Background (Custom art or procedural atmospheric gradient)
        if (!design.customArtworkPath.isNullOrBlank() && File(design.customArtworkPath).exists()) {
            val artBitmap = BitmapFactory.decodeFile(design.customArtworkPath)
            if (artBitmap != null) {
                val srcRect = android.graphics.Rect(0, 0, artBitmap.width, artBitmap.height)
                val dstRect = android.graphics.Rect(0, 0, width, height)
                canvas.drawBitmap(artBitmap, srcRect, dstRect, null)

                // Dark cinematic gradient overlay for text readability
                val overlayPaint = Paint().apply {
                    shader = LinearGradient(
                        0f, 0f, 0f, height.toFloat(),
                        intArrayOf(Color.argb(160, 0, 0, 0), Color.argb(40, 0, 0, 0), Color.argb(210, 0, 0, 0)),
                        floatArrayOf(0f, 0.45f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
            } else {
                drawProceduralBackground(canvas, width, height, bgColor, accentColor)
            }
        } else {
            drawProceduralBackground(canvas, width, height, bgColor, accentColor)
        }

        // 2. Borders & Corner Accents
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.STROKE
            strokeWidth = width * 0.005f
        }
        val margin = width * 0.05f
        canvas.drawRect(margin, margin, width - margin, height - margin, borderPaint)

        val innerMargin = margin + (width * 0.015f)
        borderPaint.strokeWidth = width * 0.0015f
        canvas.drawRect(innerMargin, innerMargin, width - innerMargin, height - innerMargin, borderPaint)

        // 3. Tagline (Upper Zone)
        val taglineText = design.tagline.ifBlank { "A Gripping Odyssey of Fate and Destiny" }
        val taglinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = width * 0.026f
            typeface = Typeface.create("serif", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.2f
        }
        canvas.drawText(taglineText.uppercase(Locale.US), width / 2f, height * 0.12f, taglinePaint)

        // 4. Title (Upper-Mid Zone)
        val titleText = design.title.ifBlank { manuscript.title }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = calculateOptimalTitleSize(titleText, width * 0.82f, width * 0.105f)
            typeface = Typeface.create("serif", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.08f
            setShadowLayer(width * 0.02f, 0f, width * 0.008f, Color.argb(200, 0, 0, 0))
        }

        val titleLines = wrapText(titleText.uppercase(Locale.US), titlePaint, width * 0.82f)
        val titleStartY = height * 0.22f
        for ((idx, line) in titleLines.withIndex()) {
            canvas.drawText(line, width / 2f, titleStartY + (idx * titlePaint.textSize * 1.15f), titlePaint)
        }

        // 5. Subtitle (Below Title)
        val subtitleText = design.subtitle.ifBlank { manuscript.subtitle }
        if (subtitleText.isNotBlank()) {
            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = subColor
                textSize = width * 0.034f
                typeface = Typeface.create("serif", Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.05f
            }
            val subY = titleStartY + (titleLines.size * titlePaint.textSize * 1.15f) + (width * 0.04f)
            canvas.drawText(subtitleText, width / 2f, subY, subPaint)
        }

        // 6. Central Emblem / Motif (Only when custom art is absent)
        val hasCustomArt = !design.customArtworkPath.isNullOrBlank() && File(design.customArtworkPath).exists()
        if (!hasCustomArt) {
            drawCentralCelestialEmblem(canvas, width / 2f, height * 0.56f, width * 0.26f, accentColor)
        }

        // 7. Author Byline (Lower Zone)
        val authorText = design.author.ifBlank { manuscript.author.ifBlank { "Author Name" } }
        val authorLeadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = subColor
            textSize = width * 0.024f
            typeface = Typeface.create("serif", Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.25f
        }
        canvas.drawText("A NOVEL BY", width / 2f, height * 0.84f, authorLeadPaint)

        val authorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = width * 0.058f
            typeface = Typeface.create("serif", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.12f
            setShadowLayer(width * 0.015f, 0f, width * 0.005f, Color.argb(180, 0, 0, 0))
        }
        canvas.drawText(authorText.uppercase(Locale.US), width / 2f, height * 0.90f, authorPaint)

        return bitmap
    }

    /**
     * Renders a full print-ready Dust Jacket Wrap as a high-resolution PDF document.
     */
    fun exportDustJacketPdf(
        context: Context,
        manuscript: NovelManuscript,
        design: NovelCoverDesign,
        outputFile: File
    ): Boolean {
        return try {
            val pageCount = manuscript.estimatedPages()
            val spineWidthInches = design.calculateSpineWidthInches(pageCount)
            val flapWidthInches = design.flapWidthInches
            val trimWidthInches = design.trimWidthInches
            val trimHeightInches = design.trimHeightInches
            val bleedInches = 0.125f

            // Total dimensions in points (72 points/inch)
            val totalWidthPts = ((flapWidthInches * 2) + (trimWidthInches * 2) + spineWidthInches + (bleedInches * 2)) * 72f
            val totalHeightPts = (trimHeightInches + (bleedInches * 2)) * 72f

            val doc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(
                Math.round(totalWidthPts),
                Math.round(totalHeightPts),
                1
            ).create()
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas

            renderDustJacketCanvas(
                canvas = canvas,
                width = totalWidthPts,
                height = totalHeightPts,
                manuscript = manuscript,
                design = design,
                pageCount = pageCount,
                spineWidthInches = spineWidthInches,
                flapWidthInches = flapWidthInches,
                trimWidthInches = trimWidthInches,
                bleedInches = bleedInches
            )

            doc.finishPage(page)
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
     * Renders the 5-panel dust jacket wrap onto a canvas.
     */
    fun renderDustJacketCanvas(
        canvas: Canvas,
        width: Float,
        height: Float,
        manuscript: NovelManuscript,
        design: NovelCoverDesign,
        pageCount: Int,
        spineWidthInches: Float,
        flapWidthInches: Float,
        trimWidthInches: Float,
        bleedInches: Float
    ) {
        val theme = design.themePreset
        val bgColor = Color.parseColor(theme.bgHex)
        val accentColor = Color.parseColor(theme.accentHex)
        val textColor = Color.parseColor(theme.textHex)
        val subColor = Color.parseColor(theme.subHex)

        // 1. Background
        canvas.drawColor(bgColor)

        // Panel boundaries (X coordinates)
        val bleedPts = bleedInches * 72f
        val backFlapWidthPts = flapWidthInches * 72f
        val coverWidthPts = trimWidthInches * 72f
        val spineWidthPts = spineWidthInches * 72f
        val frontFlapWidthPts = flapWidthInches * 72f

        val x0 = bleedPts
        val x1 = x0 + backFlapWidthPts       // Fold: Back Flap / Back Cover
        val x2 = x1 + coverWidthPts          // Fold: Back Cover / Spine
        val x3 = x2 + spineWidthPts          // Fold: Spine / Front Cover
        val x4 = x3 + coverWidthPts          // Fold: Front Cover / Front Flap
        val x5 = x4 + frontFlapWidthPts      // Outer edge

        // 2. Fold Lines / Guides
        val foldLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            alpha = 100
            strokeWidth = 1f
            pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f)
        }
        canvas.drawLine(x1, 0f, x1, height, foldLinePaint)
        canvas.drawLine(x2, 0f, x2, height, foldLinePaint)
        canvas.drawLine(x3, 0f, x3, height, foldLinePaint)
        canvas.drawLine(x4, 0f, x4, height, foldLinePaint)

        // ==========================================
        // PANEL 1: BACK FLAP (Author Bio)
        // ==========================================
        run {
            val flapCenterX = x0 + (backFlapWidthPts / 2f)
            val flapMargin = x0 + 18f
            val flapUsableWidth = backFlapWidthPts - 36f

            val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                textSize = 10.5f
                typeface = Typeface.create("serif", Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.15f
            }
            canvas.drawText("ABOUT THE AUTHOR", flapCenterX, 70f, headerPaint)

            // Author Monogram Avatar Circle
            val circleY = 120f
            val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            canvas.drawCircle(flapCenterX, circleY, 26f, circlePaint)
            val initials = manuscript.author.split(Regex("\\s+")).mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").ifBlank { "S" }
            val initPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor
                textSize = 16f
                typeface = Typeface.create("serif", Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(initials, flapCenterX, circleY + 6f, initPaint)

            // Author Bio
            val bioPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor
                textSize = 9f
                typeface = Typeface.create("serif", Typeface.NORMAL)
            }
            val bioText = design.backFlapAuthorBio.ifBlank {
                "${manuscript.author.ifBlank { "The author" }} is an acclaimed storyteller whose work explores complex worlds and vivid characters."
            }
            val bioLines = wrapText(bioText, bioPaint, flapUsableWidth)
            var bioY = 175f
            for (line in bioLines) {
                canvas.drawText(line, flapMargin, bioY, bioPaint)
                bioY += 13.5f
            }

            // Author Website / Footer
            val sitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = subColor
                textSize = 7.5f
                typeface = Typeface.create("serif", Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("seshat.studio • scribestar.media", flapCenterX, height - 35f, sitePaint)
        }

        // ==========================================
        // PANEL 2: BACK COVER (Blurb & Barcode)
        // ==========================================
        run {
            val backCenterX = x1 + (coverWidthPts / 2f)
            val backMargin = x1 + 32f
            val backUsableWidth = coverWidthPts - 64f

            // Praise Quote
            val praiseText = design.praiseQuote.ifBlank { "“An electrifying masterpiece that will leave readers breathless.” — The Literary Review" }
            val praisePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                textSize = 12.5f
                typeface = Typeface.create("serif", Typeface.BOLD_ITALIC)
                textAlign = Paint.Align.CENTER
            }
            val praiseLines = wrapText(praiseText, praisePaint, backUsableWidth)
            var praiseY = 75f
            for (line in praiseLines) {
                canvas.drawText(line, backCenterX, praiseY, praisePaint)
                praiseY += 16f
            }

            // Back Cover Blurb
            val blurbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor
                textSize = 10f
                typeface = Typeface.create("serif", Typeface.NORMAL)
            }
            val blurbText = design.backCoverBlurb.ifBlank {
                "In an era where every secret carries a price, a forgotten path reopens. When ancient forces stir, only one soul holds the key to salvation."
            }
            val blurbLines = wrapText(blurbText, blurbPaint, backUsableWidth)
            var blurbY = praiseY + 30f
            for (line in blurbLines) {
                canvas.drawText(line, backMargin, blurbY, blurbPaint)
                blurbY += 15f
            }

            // Genuine Scannable EAN-13 ISBN Barcode Box (Bottom Right)
            val barcodeW = 115f
            val barcodeH = 58f
            val barcodeX = x2 - barcodeW - 24f
            val barcodeY = height - barcodeH - 35f
            drawEan13Barcode(canvas, barcodeX, barcodeY, barcodeW, barcodeH, design.isbn)

            // Publisher Imprint Colophon (Bottom Left)
            val imprintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = subColor
                textSize = 8.5f
                typeface = Typeface.create("serif", Typeface.BOLD)
                letterSpacing = 0.15f
            }
            canvas.drawText(design.publisherImprint.uppercase(Locale.US), backMargin, height - 45f, imprintPaint)
        }

        // ==========================================
        // PANEL 3: SPINE
        // ==========================================
        run {
            val spineCenterX = x2 + (spineWidthPts / 2f)

            // If spine width is wide enough (> 32 pt / ~0.45"), draw text vertically
            if (spineWidthPts >= 24f) {
                canvas.save()
                // Rotate 90 degrees clockwise to run down spine
                canvas.translate(spineCenterX, height * 0.48f)
                canvas.rotate(90f)

                val spineTitleText = design.title.ifBlank { manuscript.title }.uppercase(Locale.US)
                val spineTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textColor
                    textSize = Math.min(13f, spineWidthPts * 0.45f)
                    typeface = Typeface.create("serif", Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                    letterSpacing = 0.12f
                }
                canvas.drawText(spineTitleText, 0f, 0f, spineTitlePaint)

                val spineAuthorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = accentColor
                    textSize = Math.min(10f, spineWidthPts * 0.35f)
                    typeface = Typeface.create("serif", Typeface.NORMAL)
                    textAlign = Paint.Align.CENTER
                    letterSpacing = 0.1f
                }
                canvas.drawText(manuscript.author.ifBlank { "Author" }.uppercase(Locale.US), 160f, 0f, spineAuthorPaint)
                canvas.restore()

                // Imprint emblem at base of spine
                val emblemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = accentColor
                    textSize = 8f
                    typeface = Typeface.create("serif", Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("✦", spineCenterX, height - 35f, emblemPaint)
            }
        }

        // ==========================================
        // PANEL 4: FRONT COVER
        // ==========================================
        run {
            val frontCenterX = x3 + (coverWidthPts / 2f)
            val frontMargin = x3 + 24f
            val frontUsableWidth = coverWidthPts - 48f

            // Artwork / Background for Front Panel
            var artDrawn = false
            if (!design.customArtworkPath.isNullOrBlank() && File(design.customArtworkPath).exists()) {
                val artBitmap = BitmapFactory.decodeFile(design.customArtworkPath)
                if (artBitmap != null) {
                    val srcRect = android.graphics.Rect(0, 0, artBitmap.width, artBitmap.height)
                    val dstRect = RectF(x3 + 12f, 16f, x4 - 12f, height - 16f)
                    canvas.drawBitmap(artBitmap, srcRect, dstRect, null)

                    val overlayPaint = Paint().apply {
                        shader = LinearGradient(
                            frontCenterX, 16f, frontCenterX, height - 16f,
                            intArrayOf(Color.argb(170, 0, 0, 0), Color.argb(40, 0, 0, 0), Color.argb(220, 0, 0, 0)),
                            floatArrayOf(0f, 0.45f, 1f),
                            Shader.TileMode.CLAMP
                        )
                    }
                    canvas.drawRect(dstRect, overlayPaint)
                    artDrawn = true
                }
            }

            // Frame
            val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            canvas.drawRect(x3 + 12f, 16f, x4 - 12f, height - 16f, framePaint)

            // Tagline
            val tagText = design.tagline.ifBlank { "A Novel of Wonder & Treachery" }.uppercase(Locale.US)
            val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                textSize = 8f
                typeface = Typeface.create("serif", Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.18f
            }
            canvas.drawText(tagText, frontCenterX, 55f, tagPaint)

            // Title
            val titleText = (design.title.ifBlank { manuscript.title }).uppercase(Locale.US)
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor
                textSize = calculateOptimalTitleSize(titleText, frontUsableWidth, 26f)
                typeface = Typeface.create("serif", Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.08f
            }
            val titleLines = wrapText(titleText, titlePaint, frontUsableWidth)
            var curY = 95f
            for (line in titleLines) {
                canvas.drawText(line, frontCenterX, curY, titlePaint)
                curY += titlePaint.textSize * 1.15f
            }

            // Subtitle
            val subText = design.subtitle.ifBlank { manuscript.subtitle }
            if (subText.isNotBlank()) {
                val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = subColor
                    textSize = 9.5f
                    typeface = Typeface.create("serif", Typeface.ITALIC)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(subText, frontCenterX, curY + 6f, subPaint)
                curY += 18f
            }

            // Center Motif (Only if custom artwork is absent)
            if (!artDrawn) {
                drawCentralCelestialEmblem(canvas, frontCenterX, height * 0.54f, 65f, accentColor)
            }

            // Author
            val bylinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = subColor
                textSize = 7.5f
                typeface = Typeface.create("serif", Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.2f
            }
            canvas.drawText("BY", frontCenterX, height - 62f, bylinePaint)

            val authorText = (design.author.ifBlank { manuscript.author.ifBlank { "Author Name" } }).uppercase(Locale.US)
            val authorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor
                textSize = 15f
                typeface = Typeface.create("serif", Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.12f
            }
            canvas.drawText(authorText, frontCenterX, height - 42f, authorPaint)
        }

        // ==========================================
        // PANEL 5: FRONT FLAP (Teaser & Price)
        // ==========================================
        run {
            val flapCenterX = x4 + (frontFlapWidthPts / 2f)
            val flapMargin = x4 + 18f
            val flapUsableWidth = frontFlapWidthPts - 36f

            // Price in upper corner
            val pricePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = subColor
                textSize = 8f
                typeface = Typeface.create("serif", Typeface.NORMAL)
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText(design.priceText, x5 - 18f, 45f, pricePaint)

            // Flap Title / Teaser Hook
            val flapTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                textSize = 11f
                typeface = Typeface.create("serif", Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val shortBookTitle = (design.title.ifBlank { manuscript.title }).take(20).uppercase(Locale.US)
            canvas.drawText(shortBookTitle, flapCenterX, 80f, flapTitlePaint)

            // Inside Flap Copy
            val teaserText = design.frontFlapTeaser.ifBlank {
                "When an impossible truth comes to light, the realm trembles. A journey begins that will redefine the boundaries of heroism."
            }
            val teaserPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor
                textSize = 9f
                typeface = Typeface.create("serif", Typeface.NORMAL)
            }
            val teaserLines = wrapText(teaserText, teaserPaint, flapUsableWidth)
            var teaserY = 115f
            for (line in teaserLines) {
                canvas.drawText(line, flapMargin, teaserY, teaserPaint)
                teaserY += 13.5f
            }

            // Jacket design credit
            val creditPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = subColor
                textSize = 7f
                typeface = Typeface.create("serif", Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Jacket design by Seshat Studio", flapCenterX, height - 35f, creditPaint)
        }
    }

    private fun drawProceduralBackground(
        canvas: Canvas,
        width: Int,
        height: Int,
        bgColor: Int,
        accentColor: Int
    ) {
        val bgPaint = Paint().apply {
            shader = RadialGradient(
                width / 2f, height * 0.48f, width * 0.85f,
                intArrayOf(Color.argb(70, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor)), bgColor),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Subtle geometric star constellation lines
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            alpha = 35
            strokeWidth = 2f
        }
        val cx = width / 2f
        val cy = height * 0.52f
        canvas.drawCircle(cx, cy, width * 0.32f, linePaint.apply { style = Paint.Style.STROKE })
        canvas.drawCircle(cx, cy, width * 0.44f, linePaint)
    }

    private fun drawCentralCelestialEmblem(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        accentColor: Int
    ) {
        val emblemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.STROKE
            strokeWidth = Math.max(1.5f, radius * 0.035f)
        }

        // Concentric geometric circles
        canvas.drawCircle(cx, cy, radius, emblemPaint)
        canvas.drawCircle(cx, cy, radius * 0.72f, emblemPaint)
        canvas.drawCircle(cx, cy, radius * 0.38f, emblemPaint)

        // 8-pointed star rays
        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        val rayCount = 8
        val outerR = radius * 0.95f
        val innerR = radius * 0.22f
        val path = android.graphics.Path()

        for (i in 0 until (rayCount * 2)) {
            val angle = (i * Math.PI / rayCount) - (Math.PI / 2.0)
            val r = if (i % 2 == 0) outerR else innerR
            val px = (cx + r * Math.cos(angle)).toFloat()
            val py = (cy + r * Math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        canvas.drawPath(path, starPaint.apply { alpha = 180 })
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.trim().split(Regex("\\s+"))
        if (words.isEmpty()) return emptyList()

        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = StringBuilder(testLine)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder(word)
                } else {
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

    private fun calculateOptimalTitleSize(title: String, maxWidth: Float, baseSize: Float): Float {
        var size = baseSize
        val testPaint = Paint().apply { textSize = size }
        while (size > 12f) {
            testPaint.textSize = size
            val words = title.split(" ")
            val maxWordWidth = words.maxOfOrNull { testPaint.measureText(it) } ?: 0f
            if (maxWordWidth <= maxWidth) {
                return size
            }
            size -= 2f
        }
        return size
    }

    /**
     * Renders a standards-compliant, scannable EAN-13 / Bookland barcode on the back jacket.
     */
    fun drawEan13Barcode(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        rawIsbn: String
    ) {
        val bgPaint = Paint().apply { color = Color.WHITE }
        canvas.drawRect(x, y, x + width, y + height, bgPaint)

        // Parse digits
        val digitsOnly = rawIsbn.filter { it.isDigit() }
        val data12 = if (digitsOnly.length >= 12) {
            digitsOnly.take(12)
        } else {
            (digitsOnly + "978123456789").take(12)
        }

        // Compute valid EAN-13 check digit
        var sum = 0
        for (i in 0 until 12) {
            val d = data12[i] - '0'
            sum += if (i % 2 == 0) d else d * 3
        }
        val checkDigit = (10 - (sum % 10)) % 10
        val ean13 = data12 + checkDigit.toString()

        val lCodes = arrayOf(
            "0001101", "0011001", "0010011", "0111101", "0100011",
            "0110001", "0101111", "0111011", "0110111", "0001011"
        )
        val gCodes = arrayOf(
            "0100111", "0110011", "0011011", "0100001", "0011101",
            "0111001", "0000101", "0010001", "0001001", "0010111"
        )
        val rCodes = arrayOf(
            "1110010", "1100110", "1101100", "1000010", "1011100",
            "1001110", "1010000", "1000100", "1001000", "1110100"
        )
        val parityTable = arrayOf(
            "LLLLLL", "LLGLGG", "LLGGLG", "LLGGGL", "LGLLGG",
            "LGGLLG", "LGGGLL", "LGLGLG", "LGLGGL", "LGGLGL"
        )

        val firstDigit = ean13[0] - '0'
        val parity = parityTable[firstDigit]

        // Top ISBN text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = Math.max(5.5f, height * 0.12f)
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("ISBN $rawIsbn", x + (width / 2f), y + (height * 0.16f), textPaint)

        // Barcode area
        val barTop = y + (height * 0.20f)
        val barBottomData = y + (height * 0.82f)
        val barBottomGuard = y + (height * 0.90f)

        // 95 modules total: 3 (start) + 42 (left) + 5 (center) + 42 (right) + 3 (end)
        val quietZoneMargin = width * 0.08f
        val barcodeDrawWidth = width - (quietZoneMargin * 2)
        val moduleWidth = barcodeDrawWidth / 95f

        val barPaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = moduleWidth
            style = Paint.Style.STROKE
        }

        var currentX = x + quietZoneMargin + (moduleWidth / 2f)

        fun drawBit(bit: Char, isGuard: Boolean) {
            if (bit == '1') {
                val bBottom = if (isGuard) barBottomGuard else barBottomData
                canvas.drawLine(currentX, barTop, currentX, bBottom, barPaint)
            }
            currentX += moduleWidth
        }

        // Start guard: 101
        for (b in "101") drawBit(b, true)

        // Left 6 digits
        for (i in 0 until 6) {
            val digit = ean13[i + 1] - '0'
            val code = if (parity[i] == 'L') lCodes[digit] else gCodes[digit]
            for (b in code) drawBit(b, false)
        }

        // Center guard: 01010
        for (b in "01010") drawBit(b, true)

        // Right 6 digits
        for (i in 6 until 12) {
            val digit = ean13[i + 1] - '0'
            val code = rCodes[digit]
            for (b in code) drawBit(b, false)
        }

        // End guard: 101
        for (b in "101") drawBit(b, true)

        // Bottom human readable numbers
        val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = Math.max(5f, height * 0.11f)
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
        }
        val leftNumX = x + quietZoneMargin + (moduleWidth * 24f)
        val rightNumX = x + quietZoneMargin + (moduleWidth * 71f)
        val numY = y + height - 2f

        // First digit outside left
        val firstNumPaint = Paint(numPaint).apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText(ean13[0].toString(), x + quietZoneMargin - 2f, barBottomData - 2f, firstNumPaint)

        // Left chunk (6 digits)
        canvas.drawText(ean13.substring(1, 7), leftNumX, numY, numPaint)
        // Right chunk (6 digits)
        canvas.drawText(ean13.substring(7, 13), rightNumX, numY, numPaint)
    }
}
