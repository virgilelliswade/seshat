package android.graphics.pdf

import android.graphics.Canvas
import java.io.OutputStream
import java.nio.charset.StandardCharsets

class PdfDocument {

    data class PageInfo(val pageWidth: Int, val pageHeight: Int, val pageNumber: Int) {
        class Builder(private val width: Int, private val height: Int, private val pageNumber: Int) {
            fun create(): PageInfo = PageInfo(width, height, pageNumber)
        }
    }

    class Page(val canvas: Canvas, val info: PageInfo)

    private val pages = mutableListOf<Page>()

    fun startPage(pageInfo: PageInfo): Page {
        val canvas = Canvas(pageInfo.pageWidth.toFloat(), pageInfo.pageHeight.toFloat())
        val page = Page(canvas, pageInfo)
        return page
    }

    fun finishPage(page: Page) {
        pages.add(page)
    }

    fun writeTo(out: OutputStream) {
        val objects = mutableListOf<String>()
        val offsets = mutableListOf<Int>()
        var currentOffset = 0

        fun write(bytes: ByteArray) {
            out.write(bytes)
            currentOffset += bytes.size
        }

        fun writeString(str: String) {
            write(str.toByteArray(StandardCharsets.ISO_8859_1))
        }

        writeString("%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n")

        val totalPages = pages.size
        val pageObjIds = (1..totalPages).map { it * 2 + 1 }
        val kidsStr = pageObjIds.joinToString(" ") { "$it 0 R" }

        // Object 1: Catalog
        offsets.add(currentOffset)
        writeString("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n")

        // Object 2: Pages
        offsets.add(currentOffset)
        writeString("2 0 obj\n<< /Type /Pages /Kids [ $kidsStr ] /Count $totalPages >>\nendobj\n")

        for (i in 0 until totalPages) {
            val page = pages[i]
            val pageObjId = i * 2 + 3
            val contentObjId = i * 2 + 4
            val streamData = page.canvas.content.toString().toByteArray(StandardCharsets.ISO_8859_1)

            // Page obj
            offsets.add(currentOffset)
            writeString("$pageObjId 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [ 0 0 ${page.info.pageWidth} ${page.info.pageHeight} ] /Contents $contentObjId 0 R /Resources << /Font << /F1 << /Type /Font /Subtype /Type1 /BaseFont /Courier >> /F2 << /Type /Font /Subtype /Type1 /BaseFont /Courier-Bold >> >> >> >>\nendobj\n")

            // Contents obj
            offsets.add(currentOffset)
            writeString("$contentObjId 0 obj\n<< /Length ${streamData.size} >>\nstream\n")
            write(streamData)
            writeString("\nendstream\nendobj\n")
        }

        val startXref = currentOffset
        val totalObjects = offsets.size + 1
        writeString("xref\n0 $totalObjects\n0000000000 65535 f \n")
        for (off in offsets) {
            writeString(String.format(java.util.Locale.US, "%010d 00000 n \n", off))
        }

        writeString("trailer\n<< /Size $totalObjects /Root 1 0 R >>\nstartxref\n$startXref\n%%EOF\n")
        out.flush()
    }

    fun close() {
        pages.clear()
    }
}
