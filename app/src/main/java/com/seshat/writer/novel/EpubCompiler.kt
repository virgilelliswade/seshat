package com.seshat.writer.novel

import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Compiles a NovelManuscript into a standards-compliant, beautiful EPUB 3 eBook.
 * Works 100% offline using standard Java/Kotlin compression libraries.
 */
object EpubCompiler {

    fun compileToEpub(
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

            FileOutputStream(outputFile).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    // 1. mimetype (Must be first entry, uncompressed / STORED)
                    val mimeBytes = "application/epub+zip".toByteArray(StandardCharsets.US_ASCII)
                    val mimeEntry = ZipEntry("mimetype").apply {
                        method = ZipEntry.STORED
                        size = mimeBytes.size.toLong()
                        compressedSize = mimeBytes.size.toLong()
                        crc = CRC32().apply { update(mimeBytes) }.value
                    }
                    zos.putNextEntry(mimeEntry)
                    zos.write(mimeBytes)
                    zos.closeEntry()

                    // 2. META-INF/container.xml
                    writeZipFile(zos, "META-INF/container.xml", buildContainerXml())

                    // 3. OEBPS/style.css
                    writeZipFile(zos, "OEBPS/style.css", buildCss())

                    // 4. Cover Page (if enabled and renderable)
                    var hasCoverImage = false
                    try {
                        val coverBmp = NovelCoverGenerator.renderFrontCoverBitmap(manuscript, manuscript.coverDesign, 1200, 1920)
                        val stream = java.io.ByteArrayOutputStream()
                        coverBmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, stream)
                        val coverBytes = stream.toByteArray()
                        val coverEntry = ZipEntry("OEBPS/cover.jpg")
                        zos.putNextEntry(coverEntry)
                        zos.write(coverBytes)
                        zos.closeEntry()

                        writeZipFile(zos, "OEBPS/cover.xhtml", buildCoverPageXhtml())
                        hasCoverImage = true
                    } catch (_: Throwable) {
                        // Safe fallback on pure JVM test environments where android.graphics is mocked
                    }

                    // 5. Chapter files
                    val chapterFileNames = mutableListOf<Pair<String, NovelChapter>>()
                    for ((idx, ch) in selectedChapters.withIndex()) {
                        val fileName = "chapter_${idx + 1}.xhtml"
                        val content = buildChapterXhtml(ch, idx + 1, options, manuscript)
                        writeZipFile(zos, "OEBPS/$fileName", content)
                        chapterFileNames.add(Pair(fileName, ch))
                    }

                    // 6. Front Matter Pages
                    if (options.includeTitlePage) {
                        writeZipFile(zos, "OEBPS/title.xhtml", buildTitlePageXhtml(manuscript))
                    }
                    val hasCopyright = options.includeCopyrightPage || manuscript.frontMatter.includeCopyrightPage
                    if (hasCopyright) {
                        writeZipFile(zos, "OEBPS/copyright.xhtml", buildCopyrightPageXhtml(manuscript, options))
                    }
                    val hasDedication = options.includeDedication || (manuscript.frontMatter.includeDedication && (options.dedicationText.isNotBlank() || manuscript.frontMatter.dedicationText.isNotBlank()))
                    if (hasDedication) {
                        writeZipFile(zos, "OEBPS/dedication.xhtml", buildDedicationPageXhtml(manuscript, options))
                    }
                    val hasEpigraph = options.includeEpigraph || (manuscript.frontMatter.includeEpigraph && (options.epigraphText.isNotBlank() || manuscript.frontMatter.epigraphText.isNotBlank()))
                    if (hasEpigraph) {
                        writeZipFile(zos, "OEBPS/epigraph.xhtml", buildEpigraphPageXhtml(manuscript, options))
                    }

                    // 7. Back Matter Page
                    val hasBackMatter = options.includeAuthorBio || options.includeAlsoByAuthor || options.includeNewsletterCta || options.includeAcknowledgments ||
                            manuscript.backMatter.includeAuthorBio || manuscript.backMatter.includeAlsoByAuthor || manuscript.backMatter.includeNewsletterCta || manuscript.backMatter.includeAcknowledgments
                    if (hasBackMatter) {
                        writeZipFile(zos, "OEBPS/backmatter.xhtml", buildBackMatterPageXhtml(manuscript, options))
                    }

                    // 8. Navigation Document (nav.xhtml - EPUB 3)
                    writeZipFile(zos, "OEBPS/nav.xhtml", buildNavXhtml(manuscript, chapterFileNames))

                    // 9. NCX (toc.ncx - EPUB 2 backward compatibility)
                    writeZipFile(zos, "OEBPS/toc.ncx", buildTocNcx(manuscript, chapterFileNames))

                    // 10. Package Document (OEBPS/content.opf)
                    writeZipFile(zos, "OEBPS/content.opf", buildContentOpf(manuscript, chapterFileNames, options, hasCoverImage, hasCopyright, hasDedication, hasEpigraph, hasBackMatter))
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun writeZipFile(zos: ZipOutputStream, entryName: String, content: String) {
        val entry = ZipEntry(entryName)
        zos.putNextEntry(entry)
        zos.write(content.toByteArray(StandardCharsets.UTF_8))
        zos.closeEntry()
    }

    private fun buildContainerXml(): String = """
        <?xml version="1.0" encoding="UTF-8"?>
        <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
          <rootfiles>
            <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
          </rootfiles>
        </container>
    """.trimIndent()

    private fun buildCss(): String = """
        @charset "UTF-8";
        body {
            font-family: "Georgia", "Garamond", "Palatino", serif;
            font-size: 1.05em;
            line-height: 1.55;
            color: #1A1A1A;
            background-color: #FFFFFF;
            margin: 5% 7%;
            text-align: justify;
        }
        h1.chapter-title {
            text-align: center;
            font-size: 1.8em;
            font-weight: bold;
            margin-top: 2.5em;
            margin-bottom: 1.8em;
            letter-spacing: 0.05em;
            text-transform: uppercase;
        }
        p {
            margin: 0;
            padding: 0;
            text-indent: 1.5em;
        }
        p.first-paragraph {
            text-indent: 0;
        }
        p.first-paragraph::first-letter {
            font-size: 2.2em;
            float: left;
            line-height: 0.9;
            padding-right: 0.1em;
            padding-top: 0.05em;
            font-weight: bold;
            color: #B45309;
        }
        p.scene-break {
            text-indent: 0;
            text-align: center;
            margin: 1.8em 0;
            font-size: 1.2em;
            letter-spacing: 0.3em;
            color: #78716C;
        }
        .titlepage {
            text-align: center;
            margin-top: 25%;
        }
        .titlepage h1 {
            font-size: 2.4em;
            letter-spacing: 0.1em;
            text-transform: uppercase;
            margin-bottom: 0.3em;
        }
        .titlepage h2 {
            font-size: 1.2em;
            font-style: italic;
            font-weight: normal;
            color: #57534E;
            margin-bottom: 2em;
        }
        .titlepage .author {
            font-size: 1.3em;
            font-weight: bold;
            letter-spacing: 0.05em;
        }
        .copyright-page {
            font-size: 0.88em;
            color: #44403C;
            margin-top: 30%;
            text-align: left;
            line-height: 1.6;
        }
        .copyright-page p {
            margin-bottom: 0.8em;
            text-indent: 0;
        }
        .copyright-page .imprint {
            font-weight: bold;
            letter-spacing: 0.05em;
        }
        .copyright-page .disclaimer {
            font-style: italic;
        }
        .dedication-page {
            text-align: center;
            margin-top: 35%;
            font-style: italic;
            font-size: 1.15em;
        }
        .dedication-page p {
            text-indent: 0;
        }
        .epigraph-page {
            margin-top: 30%;
            text-align: center;
        }
        .epigraph-quote {
            font-style: italic;
            font-size: 1.1em;
            margin: 0 auto;
            max-width: 80%;
        }
        .epigraph-source {
            margin-top: 1em;
            font-size: 0.95em;
            color: #57534E;
            text-indent: 0;
        }
        .backmatter-page {
            margin-top: 15%;
        }
        .backmatter-heading {
            text-align: center;
            font-size: 1.5em;
            margin-top: 2em;
            margin-bottom: 1em;
            letter-spacing: 0.05em;
        }
        .author-name {
            text-align: center;
            font-size: 1.2em;
            margin-bottom: 1em;
            text-indent: 0;
        }
        .ornament-divider {
            text-align: center;
            margin: 2em 0;
            letter-spacing: 0.4em;
            color: #B45309;
        }
        .newsletter-box {
            margin-top: 2.5em;
            padding: 1.5em;
            border: 1px solid #D6D3D1;
            border-radius: 6px;
            background-color: #FAFAF9;
            text-align: center;
        }
        .newsletter-box h3 {
            margin-top: 0;
            font-size: 1.2em;
        }
        .newsletter-box p {
            text-indent: 0;
        }
        .newsletter-link a {
            color: #B45309;
            font-weight: bold;
            text-decoration: none;
        }
    """.trimIndent()

    private fun buildChapterXhtml(
        chapter: NovelChapter,
        displayNum: Int,
        options: CompilerOptions,
        manuscript: NovelManuscript
    ): String {
        val headerText = chapter.paragraphs.firstOrNull { it.type == NovelParagraphType.CHAPTER_HEADER }?.text
            ?: "CHAPTER $displayNum"

        val bodyParagraphs = buildString {
            var isFirstNarrative = true
            for (p in chapter.paragraphs) {
                when (p.type) {
                    NovelParagraphType.CHAPTER_HEADER -> {
                        // Handled above in h1
                    }
                    NovelParagraphType.SCENE_BREAK -> {
                        val sym = if (p.text.isNotBlank() && p.text != "* * *") {
                            p.text
                        } else {
                            val candidate = options.sceneBreakStyle.symbol.ifBlank {
                                manuscript.sceneBreakStyle.symbol.ifBlank { options.sceneBreakSymbol }
                            }
                            if (candidate.isBlank() && (options.sceneBreakStyle == SceneBreakStyle.BLANK_SPACE || manuscript.sceneBreakStyle == SceneBreakStyle.BLANK_SPACE)) "" else candidate
                        }
                        if (sym.isNotBlank()) {
                            append("  <p class=\"scene-break\">${escapeXml(sym)}</p>\n")
                        } else {
                            append("  <p class=\"scene-break\">&nbsp;</p>\n")
                        }
                        isFirstNarrative = true
                    }
                    NovelParagraphType.NARRATIVE, NovelParagraphType.DIALOGUE -> {
                        if (p.text.isNotBlank()) {
                            val cls = if (isFirstNarrative) " class=\"first-paragraph\"" else ""
                            append("  <p$cls>${escapeXml(p.text)}</p>\n")
                            isFirstNarrative = false
                        }
                    }
                }
            }
        }

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE html>
            <html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops" xml:lang="en">
            <head>
              <title>${escapeXml(chapter.title)}</title>
              <link rel="stylesheet" type="text/css" href="style.css"/>
            </head>
            <body>
              <section epub:type="chapter">
                <h1 class="chapter-title">${escapeXml(headerText)}</h1>
            $bodyParagraphs
              </section>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildTitlePageXhtml(manuscript: NovelManuscript): String = """
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE html>
        <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
        <head>
          <title>${escapeXml(manuscript.title)}</title>
          <link rel="stylesheet" type="text/css" href="style.css"/>
        </head>
        <body>
          <div class="titlepage">
            <h1>${escapeXml(manuscript.title)}</h1>
            ${if (manuscript.subtitle.isNotBlank()) "<h2>${escapeXml(manuscript.subtitle)}</h2>" else ""}
            <p class="author">${escapeXml(manuscript.author.ifBlank { "Anonymous" })}</p>
          </div>
        </body>
        </html>
    """.trimIndent()

    private fun buildNavXhtml(
        manuscript: NovelManuscript,
        chapters: List<Pair<String, NovelChapter>>
    ): String {
        val listItems = chapters.joinToString("\n") { (file, ch) ->
            "        <li><a href=\"$file\">${escapeXml(ch.title)}</a></li>"
        }

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE html>
            <html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops" xml:lang="en">
            <head>
              <title>Table of Contents</title>
              <link rel="stylesheet" type="text/css" href="style.css"/>
            </head>
            <body>
              <nav epub:type="toc" id="toc">
                <h1>Table of Contents</h1>
                <ol>
            $listItems
                </ol>
              </nav>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildTocNcx(
        manuscript: NovelManuscript,
        chapters: List<Pair<String, NovelChapter>>
    ): String {
        val navPoints = chapters.mapIndexed { idx, (file, ch) ->
            """
              <navPoint id="navPoint-${idx + 1}" playOrder="${idx + 1}">
                <navLabel><text>${escapeXml(ch.title)}</text></navLabel>
                <content src="$file"/>
              </navPoint>
            """.trimIndent()
        }.joinToString("\n")

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
              <head>
                <meta name="dtb:uid" content="${manuscript.id}"/>
                <meta name="dtb:depth" content="1"/>
                <meta name="dtb:totalPageCount" content="0"/>
                <meta name="dtb:maxPageNumber" content="0"/>
              </head>
              <docTitle><text>${escapeXml(manuscript.title)}</text></docTitle>
              <navMap>
            $navPoints
              </navMap>
            </ncx>
        """.trimIndent()
    }

    private fun buildCopyrightPageXhtml(manuscript: NovelManuscript, options: CompilerOptions): String {
        val fm = manuscript.frontMatter
        val year = fm.copyrightYear.ifBlank { "2026" }
        val author = fm.copyrightHolder.ifBlank { manuscript.author.ifBlank { "Author" } }
        val pub = fm.publisherName.ifBlank { manuscript.coverDesign.publisherImprint.ifBlank { "Seshat Press" } }
        val isbn = manuscript.coverDesign.isbn

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE html>
            <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
            <head>
              <title>Copyright</title>
              <link rel="stylesheet" type="text/css" href="style.css"/>
            </head>
            <body>
              <div class="copyright-page">
                <p class="imprint">Published by ${escapeXml(pub)}</p>
                <p>Copyright © $year by ${escapeXml(author)}</p>
                ${if (fm.allRightsReserved) "<p>All rights reserved. No part of this publication may be reproduced, distributed, or transmitted in any form or by any means, including photocopying, recording, or other electronic or mechanical methods, without prior written permission of the publisher, except in the case of brief quotations embodied in critical reviews and certain other noncommercial uses permitted by copyright law.</p>" else ""}
                ${if (fm.fictionDisclaimer) "<p class=\"disclaimer\">This is a work of fiction. Names, characters, places, and incidents either are the product of the author's imagination or are used fictitiously. Any resemblance to actual persons, living or dead, events, or locales is entirely coincidental.</p>" else ""}
                ${if (isbn.isNotBlank()) "<p>ISBN: ${escapeXml(isbn)}</p>" else ""}
                <p class="edition">${escapeXml(fm.editionNotice)}</p>
              </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildDedicationPageXhtml(manuscript: NovelManuscript, options: CompilerOptions): String {
        val text = options.dedicationText.ifBlank { manuscript.frontMatter.dedicationText }
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE html>
            <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
            <head>
              <title>Dedication</title>
              <link rel="stylesheet" type="text/css" href="style.css"/>
            </head>
            <body>
              <div class="dedication-page">
                <p class="dedication-text">${escapeXml(text)}</p>
              </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildEpigraphPageXhtml(manuscript: NovelManuscript, options: CompilerOptions): String {
        val fm = manuscript.frontMatter
        val quote = options.epigraphText.ifBlank { fm.epigraphText }
        val source = options.epigraphSource.ifBlank { fm.epigraphSource }
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE html>
            <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
            <head>
              <title>Epigraph</title>
              <link rel="stylesheet" type="text/css" href="style.css"/>
            </head>
            <body>
              <div class="epigraph-page">
                <blockquote class="epigraph-quote">“${escapeXml(quote)}”</blockquote>
                ${if (source.isNotBlank()) "<p class=\"epigraph-source\">— ${escapeXml(source)}</p>" else ""}
              </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildBackMatterPageXhtml(manuscript: NovelManuscript, options: CompilerOptions): String {
        val bm = manuscript.backMatter
        val authorName = manuscript.author.ifBlank { "The Author" }
        val bioText = options.authorBioText.ifBlank { bm.authorBioText.ifBlank { manuscript.coverDesign.backFlapAuthorBio } }
        val alsoBy = options.alsoByAuthorTitles.ifBlank { bm.alsoByAuthorTitles }
        val cta = options.newsletterCtaText.ifBlank { bm.newsletterCtaText }
        val link = options.newsletterLink.ifBlank { bm.newsletterLink }
        val ack = options.acknowledgmentsText.ifBlank { bm.acknowledgmentsText }

        val hasBio = (options.includeAuthorBio || bm.includeAuthorBio) && bioText.isNotBlank()
        val hasAlsoBy = (options.includeAlsoByAuthor || bm.includeAlsoByAuthor) && alsoBy.isNotBlank()
        val hasCta = (options.includeNewsletterCta || bm.includeNewsletterCta) && cta.isNotBlank()
        val hasAck = (options.includeAcknowledgments || bm.includeAcknowledgments) && ack.isNotBlank()

        return buildString {
            append("""<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
  <title>About the Author</title>
  <link rel="stylesheet" type="text/css" href="style.css"/>
</head>
<body>
  <div class="backmatter-page">
""")
            if (hasAck) {
                append("    <h2 class=\"backmatter-heading\">Acknowledgments</h2>\n")
                append("    <p class=\"first-paragraph\">${escapeXml(ack)}</p>\n")
                append("    <div class=\"ornament-divider\">✦ ✦ ✦</div>\n")
            }
            if (hasBio) {
                append("    <h2 class=\"backmatter-heading\">About the Author</h2>\n")
                append("    <p class=\"author-name\"><strong>${escapeXml(authorName)}</strong></p>\n")
                append("    <p class=\"first-paragraph\">${escapeXml(bioText)}</p>\n")
            }
            if (hasAlsoBy) {
                append("    <h2 class=\"backmatter-heading\">Also by ${escapeXml(authorName)}</h2>\n")
                append("    <p>${escapeXml(alsoBy).replace("\n", "<br/>")}</p>\n")
            }
            if (hasCta) {
                append("    <div class=\"newsletter-box\">\n")
                append("      <h3>Stay Connected</h3>\n")
                append("      <p>${escapeXml(cta)}</p>\n")
                if (link.isNotBlank()) {
                    append("      <p class=\"newsletter-link\"><a href=\"${escapeXml(link)}\">${escapeXml(link)}</a></p>\n")
                }
                append("    </div>\n")
            }
            append("""  </div>
</body>
</html>""")
        }
    }

    private fun buildCoverPageXhtml(): String = """
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE html>
        <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
        <head>
          <title>Cover</title>
          <style>
            body { margin: 0; padding: 0; text-align: center; background-color: #000000; }
            img { max-width: 100%; height: auto; display: block; margin: 0 auto; }
          </style>
        </head>
        <body>
          <div style="text-align: center;">
            <img src="cover.jpg" alt="Cover Image"/>
          </div>
        </body>
        </html>
    """.trimIndent()

    private fun buildContentOpf(
        manuscript: NovelManuscript,
        chapters: List<Pair<String, NovelChapter>>,
        options: CompilerOptions,
        hasCoverImage: Boolean = false,
        hasCopyright: Boolean = false,
        hasDedication: Boolean = false,
        hasEpigraph: Boolean = false,
        hasBackMatter: Boolean = false
    ): String {
        val manifestItems = buildString {
            append("    <item id=\"style\" href=\"style.css\" media-type=\"text/css\"/>\n")
            append("    <item id=\"toc\" href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\"/>\n")
            append("    <item id=\"nav\" href=\"nav.xhtml\" media-type=\"application/xhtml+xml\" properties=\"nav\"/>\n")
            if (hasCoverImage) {
                append("    <item id=\"cover-image\" href=\"cover.jpg\" media-type=\"image/jpeg\" properties=\"cover-image\"/>\n")
                append("    <item id=\"coverpage\" href=\"cover.xhtml\" media-type=\"application/xhtml+xml\"/>\n")
            }
            if (options.includeTitlePage) {
                append("    <item id=\"titlepage\" href=\"title.xhtml\" media-type=\"application/xhtml+xml\"/>\n")
            }
            if (hasCopyright) {
                append("    <item id=\"copyright\" href=\"copyright.xhtml\" media-type=\"application/xhtml+xml\"/>\n")
            }
            if (hasDedication) {
                append("    <item id=\"dedication\" href=\"dedication.xhtml\" media-type=\"application/xhtml+xml\"/>\n")
            }
            if (hasEpigraph) {
                append("    <item id=\"epigraph\" href=\"epigraph.xhtml\" media-type=\"application/xhtml+xml\"/>\n")
            }
            chapters.forEachIndexed { idx, (file, _) ->
                append("    <item id=\"ch_${idx + 1}\" href=\"$file\" media-type=\"application/xhtml+xml\"/>\n")
            }
            if (hasBackMatter) {
                append("    <item id=\"backmatter\" href=\"backmatter.xhtml\" media-type=\"application/xhtml+xml\"/>\n")
            }
        }

        val spineItems = buildString {
            if (hasCoverImage) {
                append("    <itemref idref=\"coverpage\"/>\n")
            }
            if (options.includeTitlePage) {
                append("    <itemref idref=\"titlepage\"/>\n")
            }
            if (hasCopyright) {
                append("    <itemref idref=\"copyright\"/>\n")
            }
            if (hasDedication) {
                append("    <itemref idref=\"dedication\"/>\n")
            }
            if (hasEpigraph) {
                append("    <itemref idref=\"epigraph\"/>\n")
            }
            chapters.forEachIndexed { idx, _ ->
                append("    <itemref idref=\"ch_${idx + 1}\"/>\n")
            }
            if (hasBackMatter) {
                append("    <itemref idref=\"backmatter\"/>\n")
            }
        }

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <package xmlns="http://www.idpf.org/2007/opf" unique-identifier="pub-id" version="3.0">
              <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
                <dc:identifier id="pub-id">urn:uuid:${manuscript.id}</dc:identifier>
                <dc:title>${escapeXml(manuscript.title)}</dc:title>
                <dc:creator>${escapeXml(manuscript.author.ifBlank { "Anonymous" })}</dc:creator>
                <dc:language>en</dc:language>
                <meta property="dcterms:modified">2026-10-11T00:00:00Z</meta>
              </metadata>
              <manifest>
            $manifestItems
              </manifest>
              <spine toc="toc">
            $spineItems
              </spine>
            </package>
        """.trimIndent()
    }

    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
