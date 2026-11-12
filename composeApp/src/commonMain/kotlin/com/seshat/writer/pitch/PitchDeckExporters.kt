package com.seshat.writer.pitch

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.seshat.writer.export.ScriptFileManager
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Multi-format exporter for the Pitch Deck Creator:
 * 1. 16:9 Landscape Presentation PDF
 * 2. 1-Page SCWR-style 45-Second Elevator Pitch Sheet PDF
 * 3. Native OpenXML .PPTX (PowerPoint, Google Slides, Apple Keynote compatible)
 */
object PitchDeckExporters {

    private fun safeTitle(title: String): String =
        title.ifBlank { "Pitch_Deck" }.replace(Regex("[^a-zA-Z0-9_-]"), "_")

    private fun hexColor(argb: Long): Int = argb.toInt()

    private fun hexRgb6(argb: Long): String {
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = argb and 0xFF
        return String.format(Locale.US, "%02X%02X%02X", r, g, b)
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        for (paragraph in text.split("\n")) {
            val trimmed = paragraph.trim()
            if (trimmed.isEmpty()) {
                lines.add("")
                continue
            }
            val words = trimmed.split(Regex("\\s+"))
            var current = StringBuilder()
            for (word in words) {
                val candidate = if (current.isEmpty()) word else "$current $word"
                if (paint.measureText(candidate) <= maxWidth) {
                    current = StringBuilder(candidate)
                } else {
                    if (current.isNotEmpty()) lines.add(current.toString())
                    current = StringBuilder(word)
                }
            }
            if (current.isNotEmpty()) lines.add(current.toString())
        }
        return lines
    }

    fun exportDeckPdf(context: Context, deck: PitchDeckDocument): File {
        val dir = ScriptFileManager.getExportsDirectory(context)
        val ts = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        val file = File(dir, "${safeTitle(deck.projectTitle)}_PitchDeck_$ts.pdf")

        val slideW = 960
        val slideH = 540
        val doc = PdfDocument()
        val t = deck.theme

        val bgPaint = Paint().apply { color = hexColor(t.bgHex) }
        val accentPaint = Paint().apply { color = hexColor(t.accentHex) }
        val rulePaint = Paint().apply {
            color = hexColor(t.mutedHex)
            strokeWidth = 1.5f
        }
        val titlePaint = Paint().apply {
            color = hexColor(t.accentHex)
            textSize = 34f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val subtitlePaint = Paint().apply {
            color = hexColor(t.mutedHex)
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = hexColor(t.textHex)
            textSize = 18f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val numPaint = Paint().apply {
            color = hexColor(t.mutedHex)
            textSize = 13f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        deck.slides.forEachIndexed { idx, slide ->
            val pageInfo = PdfDocument.PageInfo.Builder(slideW, slideH, idx + 1).create()
            val page = doc.startPage(pageInfo)
            val c = page.canvas

            // Background + Right Accent Ribbon
            c.drawRect(0f, 0f, slideW.toFloat(), slideH.toFloat(), bgPaint)
            c.drawRect(slideW - 26f, 0f, slideW.toFloat(), slideH.toFloat(), accentPaint)

            // Top & Bottom Editorial Rules
            c.drawLine(56f, 44f, slideW - 56f, 44f, rulePaint)
            c.drawLine(56f, slideH - 44f, slideW - 56f, slideH - 44f, rulePaint)

            // Subtitle + Title
            if (slide.subtitle.isNotBlank()) {
                c.drawText(slide.subtitle.uppercase(Locale.US), 56f, 76f, subtitlePaint)
            }
            var y = 122f
            val titleLines = wrapText(slide.title.ifBlank { deck.projectTitle }, titlePaint, slideW - 140f)
            for (tl in titleLines.take(3)) {
                c.drawText(tl, 56f, y, titlePaint)
                y += 42f
            }

            y += 14f
            val bodyLines = wrapText(slide.body, bodyPaint, slideW - 140f)
            for (bl in bodyLines) {
                if (y > slideH - 64f) break
                c.drawText(bl, 56f, y, bodyPaint)
                y += 26f
            }

            c.drawText(String.format(Locale.US, "%02d", idx + 1), 56f, slideH - 20f, numPaint)
            doc.finishPage(page)
        }

        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    fun exportElevatorSheetPdf(context: Context, deck: PitchDeckDocument): File {
        val dir = ScriptFileManager.getExportsDirectory(context)
        val ts = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        val file = File(dir, "${safeTitle(deck.projectTitle)}_45s_ElevatorPitch_$ts.pdf")

        val pageW = 612
        val pageH = 792
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, 1).create())
        val c = page.canvas
        c.drawColor(android.graphics.Color.WHITE)

        val monoBold = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        val monoNormal = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 11.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            isAntiAlias = true
        }

        var y = 66f
        val left = 64f
        val maxW = pageW - 128f

        val header = deck.elevatorPitch.authorAndCourse.ifBlank {
            "${deck.author.ifBlank { "Writer" }} / ${deck.projectTitle}"
        }
        c.drawText(header, left, y, monoNormal)
        y += 30f

        fun drawSection(heading: String, content: String) {
            c.drawText(heading, left, y, monoBold)
            y += 20f
            val lines = wrapText(content.ifBlank { "(Not yet written)" }, monoNormal, maxW)
            for (line in lines) {
                if (y > pageH - 60f) break
                c.drawText(line, left, y, monoNormal)
                y += 16f
            }
            y += 16f
        }

        drawSection("Logline", deck.elevatorPitch.logline)
        drawSection("Passion", deck.elevatorPitch.passionWhyMe)
        drawSection(
            "45-Second Elevator Pitch (~${deck.elevatorPitch.estimatedSeconds()}s / ${deck.elevatorPitch.wordCount()} words)",
            deck.elevatorPitch.spokenPitch
        )

        doc.finishPage(page)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    /**
     * Generates a genuine OpenXML .pptx presentation (16:9) compatible with
     * Microsoft PowerPoint, Google Slides, and Apple Keynote.
     */
    fun exportDeckPptx(context: Context, deck: PitchDeckDocument): File {
        val dir = ScriptFileManager.getExportsDirectory(context)
        val ts = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        val file = File(dir, "${safeTitle(deck.projectTitle)}_PitchDeck_$ts.pptx")

        val t = deck.theme
        val bgRgb = hexRgb6(t.bgHex)
        val accentRgb = hexRgb6(t.accentHex)
        val textRgb = hexRgb6(t.textHex)
        val mutedRgb = hexRgb6(t.mutedHex)

        val slides = deck.slides.ifEmpty {
            listOf(PitchSlide(title = deck.projectTitle, subtitle = "PITCH DECK", body = deck.elevatorPitch.logline))
        }

        ZipOutputStream(BufferedOutputStream(FileOutputStream(file))).use { zos ->
            // 1. [Content_Types].xml
            val slideOverrides = slides.indices.joinToString("\n") { i ->
                """  <Override PartName="/ppt/slides/slide${i + 1}.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>"""
            }
            putZipEntry(
                zos,
                "[Content_Types].xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>
  <Override PartName="/ppt/slideMasters/slideMaster1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml"/>
  <Override PartName="/ppt/slideLayouts/slideLayout1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml"/>
  <Override PartName="/ppt/theme/theme1.xml" ContentType="application/vnd.openxmlformats-officedocument.theme+xml"/>
$slideOverrides
</Types>"""
            )

            // 2. _rels/.rels
            putZipEntry(
                zos,
                "_rels/.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="ppt/presentation.xml"/>
</Relationships>"""
            )

            // 3. ppt/presentation.xml & ppt/_rels/presentation.xml.rels
            val sldIdList = slides.indices.joinToString("\n") { i ->
                """    <p:sldId id="${256 + i}" r:id="rId${i + 2}"/>"""
            }
            putZipEntry(
                zos,
                "ppt/presentation.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:presentation xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
                xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
                xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:sldMasterIdLst><p:sldMasterId id="2147483648" r:id="rId1"/></p:sldMasterIdLst>
  <p:sldIdLst>
$sldIdList
  </p:sldIdLst>
  <p:sldSz cx="12192000" cy="6858000"/>
  <p:notesSz cx="6858000" cy="9144000"/>
</p:presentation>"""
            )

            val presRels = slides.indices.joinToString("\n") { i ->
                """  <Relationship Id="rId${i + 2}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide" Target="slides/slide${i + 1}.xml"/>"""
            }
            putZipEntry(
                zos,
                "ppt/_rels/presentation.xml.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="slideMasters/slideMaster1.xml"/>
$presRels
</Relationships>"""
            )

            // 4. Minimal SlideMaster, SlideLayout & Theme
            putZipEntry(
                zos,
                "ppt/slideMasters/slideMaster1.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldMaster xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
             xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
             xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:cSld><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr/></p:spTree></p:cSld>
  <p:clrMap bg1="lt1" tx1="dk1" bg2="lt2" tx2="dk2" accent1="accent1" accent2="accent2" accent3="accent3" accent4="accent4" accent5="accent5" accent6="accent6" hlink="hlink" folHlink="folHlink"/>
  <p:sldLayoutIdLst><p:sldLayoutId id="2147483649" r:id="rId1"/></p:sldLayoutIdLst>
</p:sldMaster>"""
            )
            putZipEntry(
                zos,
                "ppt/slideMasters/_rels/slideMaster1.xml.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme" Target="../theme/theme1.xml"/>
</Relationships>"""
            )
            putZipEntry(
                zos,
                "ppt/slideLayouts/slideLayout1.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldLayout xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
             xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
             xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" type="blank">
  <p:cSld><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr/></p:spTree></p:cSld>
</p:sldLayout>"""
            )
            putZipEntry(
                zos,
                "ppt/slideLayouts/_rels/slideLayout1.xml.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="../slideMasters/slideMaster1.xml"/>
</Relationships>"""
            )
            putZipEntry(
                zos,
                "ppt/theme/theme1.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<a:theme xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" name="SeshatTheme">
  <a:themeElements>
    <a:clrScheme name="Seshat">
      <a:dk1><a:srgbClr val="0F172A"/></a:dk1>
      <a:lt1><a:srgbClr val="FFFFFF"/></a:lt1>
      <a:dk2><a:srgbClr val="1E293B"/></a:dk2>
      <a:lt2><a:srgbClr val="F8FAFC"/></a:lt2>
      <a:accent1><a:srgbClr val="$accentRgb"/></a:accent1>
      <a:accent2><a:srgbClr val="EAB308"/></a:accent2>
      <a:accent3><a:srgbClr val="38BDF8"/></a:accent3>
      <a:accent4><a:srgbClr val="10B981"/></a:accent4>
      <a:accent5><a:srgbClr val="A855F7"/></a:accent5>
      <a:accent6><a:srgbClr val="F97316"/></a:accent6>
      <a:hlink><a:srgbClr val="38BDF8"/></a:hlink>
      <a:folHlink><a:srgbClr val="818CF8"/></a:folHlink>
    </a:clrScheme>
    <a:fontScheme name="SeshatFonts">
      <a:majorFont><a:latin typeface="Helvetica Neue"/></a:majorFont>
      <a:minorFont><a:latin typeface="Helvetica Neue"/></a:minorFont>
    </a:fontScheme>
    <a:fmtScheme name="SeshatFmt">
      <a:fillStyleLst><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:fillStyleLst>
      <a:lnStyleLst><a:ln w="9525"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:ln></a:lnStyleLst>
      <a:effectStyleLst><a:effectStyle><a:effectLst/></a:effectStyle></a:effectStyleLst>
      <a:bgFillStyleLst><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:bgFillStyleLst>
    </a:fmtScheme>
  </a:themeElements>
</a:theme>"""
            )

            // 5. Individual Slides
            slides.forEachIndexed { idx, slide ->
                val slideNum = idx + 1
                val bodyParagraphs = slide.body.split("\n").joinToString("") { line ->
                    """<a:p><a:r><a:rPr lang="en-US" sz="1800"><a:solidFill><a:srgbClr val="$textRgb"/></a:solidFill></a:rPr><a:t>${escapeXml(line)}</a:t></a:r></a:p>"""
                }
                val slideXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:slide xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
         xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
         xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:cSld>
    <p:bg><p:bgPr><a:solidFill><a:srgbClr val="$bgRgb"/></a:solidFill><a:effectLst/></p:bgPr></p:bg>
    <p:spTree>
      <p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>
      <p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>
      <p:sp>
        <p:nvSpPr><p:cNvPr id="2" name="Subtitle"/><p:cNvSpPr txBox="1"/><p:nvPr/></p:nvSpPr>
        <p:spPr><a:xfrm><a:off x="685800" y="457200"/><a:ext cx="10500000" cy="400000"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></p:spPr>
        <p:txBody><a:bodyPr/><a:lstStyle/><a:p><a:r><a:rPr lang="en-US" sz="1300" b="1"><a:solidFill><a:srgbClr val="$mutedRgb"/></a:solidFill></a:rPr><a:t>${escapeXml(slide.subtitle.uppercase(Locale.US))}</a:t></a:r></a:p></p:txBody>
      </p:sp>
      <p:sp>
        <p:nvSpPr><p:cNvPr id="3" name="Title"/><p:cNvSpPr txBox="1"/><p:nvPr/></p:nvSpPr>
        <p:spPr><a:xfrm><a:off x="685800" y="880000"/><a:ext cx="10500000" cy="950000"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></p:spPr>
        <p:txBody><a:bodyPr/><a:lstStyle/><a:p><a:r><a:rPr lang="en-US" sz="3200" b="1"><a:solidFill><a:srgbClr val="$accentRgb"/></a:solidFill></a:rPr><a:t>${escapeXml(slide.title.ifBlank { deck.projectTitle })}</a:t></a:r></a:p></p:txBody>
      </p:sp>
      <p:sp>
        <p:nvSpPr><p:cNvPr id="4" name="Body"/><p:cNvSpPr txBox="1"/><p:nvPr/></p:nvSpPr>
        <p:spPr><a:xfrm><a:off x="685800" y="1950000"/><a:ext cx="10500000" cy="4100000"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></p:spPr>
        <p:txBody><a:bodyPr wrap="square"/><a:lstStyle/>$bodyParagraphs</p:txBody>
      </p:sp>
      <p:sp>
        <p:nvSpPr><p:cNvPr id="5" name="SlideNumber"/><p:cNvSpPr txBox="1"/><p:nvPr/></p:nvSpPr>
        <p:spPr><a:xfrm><a:off x="685800" y="6250000"/><a:ext cx="2000000" cy="350000"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></p:spPr>
        <p:txBody><a:bodyPr/><a:lstStyle/><a:p><a:r><a:rPr lang="en-US" sz="1200" b="1"><a:solidFill><a:srgbClr val="$mutedRgb"/></a:solidFill></a:rPr><a:t>${String.format(Locale.US, "%02d", slideNum)}</a:t></a:r></a:p></p:txBody>
      </p:sp>
    </p:spTree>
  </p:cSld>
</p:slide>"""
                putZipEntry(zos, "ppt/slides/slide$slideNum.xml", slideXml)
                putZipEntry(
                    zos,
                    "ppt/slides/_rels/slide$slideNum.xml.rels",
                    """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>
</Relationships>"""
                )
            }
        }
        return file
    }

    private fun putZipEntry(zos: ZipOutputStream, path: String, content: String) {
        zos.putNextEntry(ZipEntry(path))
        zos.write(content.toByteArray(StandardCharsets.UTF_8))
        zos.closeEntry()
    }

    private fun escapeXml(input: String): String = input
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
