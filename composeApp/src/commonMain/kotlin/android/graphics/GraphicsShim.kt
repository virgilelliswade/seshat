package android.graphics

import androidx.compose.ui.graphics.toComposeImageBitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

object Color {
    const val BLACK = -0x1000000
    const val DKGRAY = -0xbbbbbc
    const val GRAY = -0x777778
    const val LTGRAY = -0x333334
    const val WHITE = -0x1
    const val RED = -0x10000
    const val GREEN = -0xff0100
    const val BLUE = -0xffff01
    const val YELLOW = -0x100
    const val CYAN = -0xff0001
    const val MAGENTA = -0xff01
    const val TRANSPARENT = 0

    fun rgb(r: Int, g: Int, b: Int): Int =
        (0xFF shl 24) or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or (b and 0xFF)

    fun argb(a: Int, r: Int, g: Int, b: Int): Int =
        ((a and 0xFF) shl 24) or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or (b and 0xFF)

    fun parseColor(colorString: String): Int {
        if (colorString.startsWith("#")) {
            var color = colorString.substring(1).toLong(16)
            if (colorString.length == 7) {
                color = color or -0x1000000L
            }
            return color.toInt()
        }
        return WHITE
    }

    fun red(color: Int): Int = (color shr 16) and 0xFF
    fun green(color: Int): Int = (color shr 8) and 0xFF
    fun blue(color: Int): Int = color and 0xFF
    fun alpha(color: Int): Int = (color ushr 24)
}

open class Typeface(val style: Int = 0) {
    companion object {
        const val NORMAL = 0
        const val BOLD = 1
        const val ITALIC = 2
        const val BOLD_ITALIC = 3

        val DEFAULT = Typeface(NORMAL)
        val DEFAULT_BOLD = Typeface(BOLD)
        val SANS_SERIF = Typeface(NORMAL)
        val SERIF = Typeface(NORMAL)
        val MONOSPACE = Typeface(NORMAL)

        fun create(family: String?, style: Int): Typeface = Typeface(style)
        fun create(family: Typeface?, style: Int): Typeface = Typeface(style)
    }
}

class Paint(val flags: Int = 0) {
    companion object {
        const val ANTI_ALIAS_FLAG = 1
    }

    constructor(other: Paint) : this(other.flags) {
        this.color = other.color
        this.textSize = other.textSize
        this.strokeWidth = other.strokeWidth
        this.typeface = other.typeface
        this.style = other.style
        this.textAlign = other.textAlign
        this.isAntiAlias = other.isAntiAlias
        this.shader = other.shader
        this.pathEffect = other.pathEffect
        this.letterSpacing = other.letterSpacing
    }

    var color: Int = Color.BLACK
    var alpha: Int
        get() = Color.alpha(color)
        set(value) {
            color = Color.argb(value, Color.red(color), Color.green(color), Color.blue(color))
        }
    var textSize: Float = 12f
    var strokeWidth: Float = 1f
    var typeface: Typeface = Typeface.DEFAULT
    var style: Style = Style.FILL
    var textAlign: Align = Align.LEFT
    var isAntiAlias: Boolean = true
    var letterSpacing: Float = 0f
    var shader: Any? = null

    var pathEffect: Any? = null

    enum class Style { FILL, STROKE, FILL_AND_STROKE }
    enum class Align { LEFT, CENTER, RIGHT }

    fun measureText(text: String): Float {
        // Approximate standard monospace pitch (10 chars per inch at 12pt = 7.2pt per char)
        val charWidth = textSize * 0.6f
        return text.length * charWidth
    }

    fun setShadowLayer(radius: Float, dx: Float, dy: Float, shadowColor: Int) {}
}

open class Shader {
    enum class TileMode { CLAMP, REPEAT, MIRROR }
}

class LinearGradient(x0: Float, y0: Float, x1: Float, y1: Float, color0: Int, color1: Int, tile: Shader.TileMode) : Shader() {
    constructor(x0: Float, y0: Float, x1: Float, y1: Float, colors: IntArray, positions: FloatArray?, tile: Shader.TileMode) :
        this(x0, y0, x1, y1, colors.firstOrNull() ?: Color.BLACK, colors.lastOrNull() ?: Color.WHITE, tile)
}

class RadialGradient(centerX: Float, centerY: Float, radius: Float, centerColor: Int, edgeColor: Int, tileMode: Shader.TileMode) : Shader() {
    constructor(centerX: Float, centerY: Float, radius: Float, colors: IntArray, stops: FloatArray?, tileMode: Shader.TileMode) :
        this(centerX, centerY, radius, colors.firstOrNull() ?: Color.BLACK, colors.lastOrNull() ?: Color.WHITE, tileMode)
}

class Path {
    fun reset() {}
    fun moveTo(x: Float, y: Float) {}
    fun lineTo(x: Float, y: Float) {}
    fun close() {}
}

class DashPathEffect(intervals: FloatArray, phase: Float)

object BitmapFactory {
    fun decodeFile(pathName: String): Bitmap? = null
    fun decodeByteArray(data: ByteArray, offset: Int, length: Int): Bitmap? = null
}


open class Rect(var left: Int = 0, var top: Int = 0, var right: Int = 0, var bottom: Int = 0) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
}

open class RectF(var left: Float = 0f, var top: Float = 0f, var right: Float = 0f, var bottom: Float = 0f) {
    constructor(r: Rect) : this(r.left.toFloat(), r.top.toFloat(), r.right.toFloat(), r.bottom.toFloat())
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}

class Bitmap(val width: Int, val height: Int) {
    internal val awtImage = java.awt.image.BufferedImage(
        width.coerceAtLeast(1),
        height.coerceAtLeast(1),
        java.awt.image.BufferedImage.TYPE_INT_ARGB
    )

    enum class Config { ARGB_8888, RGB_565 }
    enum class CompressFormat { PNG, JPEG, WEBP }

    fun compress(format: CompressFormat, quality: Int, stream: OutputStream): Boolean {
        return try {
            val fmt = when (format) {
                CompressFormat.JPEG -> "jpg"
                else -> "png"
            }
            javax.imageio.ImageIO.write(awtImage, fmt, stream)
        } catch (_: Throwable) {
            false
        }
    }

    companion object {
        fun createBitmap(width: Int, height: Int, config: Config): Bitmap = Bitmap(width, height)
    }
}

class Canvas(val width: Float = 612f, val height: Float = 792f) {
    private var backingBitmap: Bitmap? = null
    constructor(bitmap: Bitmap) : this(bitmap.width.toFloat(), bitmap.height.toFloat()) {
        this.backingBitmap = bitmap
    }
    internal val content = StringBuilder()

    private fun withAwtGraphics(block: (java.awt.Graphics2D) -> Unit) {
        val bmp = backingBitmap ?: return
        val g = bmp.awtImage.createGraphics()
        try {
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
            g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            block(g)
        } finally {
            g.dispose()
        }
    }

    private fun toAwtColor(c: Int): java.awt.Color {
        return java.awt.Color(Color.red(c), Color.green(c), Color.blue(c), Color.alpha(c))
    }

    fun drawColor(color: Int) {
        val r = Color.red(color) / 255f
        val g = Color.green(color) / 255f
        val b = Color.blue(color) / 255f
        content.append(String.format(java.util.Locale.US, "q %.3f %.3f %.3f rg 0 0 %.2f %.2f re f Q\n", r, g, b, width, height))
        withAwtGraphics { g2 ->
            g2.color = toAwtColor(color)
            g2.fillRect(0, 0, width.toInt(), height.toInt())
        }
    }

    fun drawRect(left: Float, top: Float, right: Float, bottom: Float, paint: Paint) {
        val r = Color.red(paint.color) / 255f
        val g = Color.green(paint.color) / 255f
        val b = Color.blue(paint.color) / 255f
        val w = right - left
        val h = bottom - top
        // PDF coordinate origin is bottom-left
        val pdfY = height - bottom
        content.append(String.format(java.util.Locale.US, "q %.3f %.3f %.3f rg %.2f %.2f %.2f %.2f re f Q\n", r, g, b, left, pdfY, w, h))
        withAwtGraphics { g2 ->
            g2.color = toAwtColor(paint.color)
            if (paint.style == Paint.Style.STROKE) {
                g2.stroke = java.awt.BasicStroke(paint.strokeWidth.coerceAtLeast(1f))
                g2.drawRect(left.toInt(), top.toInt(), w.toInt(), h.toInt())
            } else {
                g2.fillRect(left.toInt(), top.toInt(), w.toInt(), h.toInt())
            }
        }
    }

    fun drawRect(rect: RectF, paint: Paint) = drawRect(rect.left, rect.top, rect.right, rect.bottom, paint)
    fun drawRect(rect: Rect, paint: Paint) = drawRect(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat(), paint)

    fun drawLine(startX: Float, startY: Float, stopX: Float, stopY: Float, paint: Paint) {
        val r = Color.red(paint.color) / 255f
        val g = Color.green(paint.color) / 255f
        val b = Color.blue(paint.color) / 255f
        val y1 = height - startY
        val y2 = height - stopY
        content.append(String.format(java.util.Locale.US, "q %.3f %.3f %.3f RG %.2f w %.2f %.2f m %.2f %.2f l S Q\n", r, g, b, paint.strokeWidth, startX, y1, stopX, y2))
        withAwtGraphics { g2 ->
            g2.color = toAwtColor(paint.color)
            g2.stroke = java.awt.BasicStroke(paint.strokeWidth.coerceAtLeast(1f))
            g2.drawLine(startX.toInt(), startY.toInt(), stopX.toInt(), stopY.toInt())
        }
    }

    fun drawCircle(cx: Float, cy: Float, radius: Float, paint: Paint) {
        drawRect(cx - radius, cy - radius, cx + radius, cy + radius, paint)
    }

    fun drawText(text: String, x: Float, y: Float, paint: Paint) {
        if (text.isEmpty()) return
        val r = Color.red(paint.color) / 255f
        val g = Color.green(paint.color) / 255f
        val b = Color.blue(paint.color) / 255f
        val pdfY = height - y
        val fontRef = if (paint.typeface.style == Typeface.BOLD) "/F2" else "/F1"
        val escaped = text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")

        val posX = when (paint.textAlign) {
            Paint.Align.RIGHT -> x - paint.measureText(text)
            Paint.Align.CENTER -> x - (paint.measureText(text) / 2f)
            else -> x
        }

        content.append(String.format(java.util.Locale.US, "BT %s %.2f Tf %.3f %.3f %.3f rg 1 0 0 1 %.2f %.2f Tm (%s) Tj ET\n", fontRef, paint.textSize, r, g, b, posX, pdfY, escaped))
        withAwtGraphics { g2 ->
            g2.color = toAwtColor(paint.color)
            val fontStyle = if (paint.typeface.style == Typeface.BOLD) java.awt.Font.BOLD else java.awt.Font.PLAIN
            g2.font = java.awt.Font("SansSerif", fontStyle, paint.textSize.toInt().coerceAtLeast(8))
            val fm = g2.fontMetrics
            val awtX = when (paint.textAlign) {
                Paint.Align.RIGHT -> x - fm.stringWidth(text)
                Paint.Align.CENTER -> x - (fm.stringWidth(text) / 2f)
                else -> x
            }
            g2.drawString(text, awtX, y)
        }
    }

    fun save() { content.append("q\n") }
    fun restore() { content.append("Q\n") }
    fun rotate(degrees: Float, px: Float = 0f, py: Float = 0f) {}
    fun translate(dx: Float, dy: Float) {}
    fun drawBitmap(bitmap: Bitmap, left: Float, top: Float, paint: Paint?) {}
    fun drawBitmap(bitmap: Bitmap, src: Rect?, dst: RectF, paint: Paint?) {}
    fun drawBitmap(bitmap: Bitmap, src: Rect?, dst: Rect, paint: Paint?) {}
    fun drawPath(path: Path, paint: Paint) {}
}

fun Bitmap.asImageBitmap(): androidx.compose.ui.graphics.ImageBitmap {
    return try {
        val baos = ByteArrayOutputStream()
        javax.imageio.ImageIO.write(this.awtImage, "png", baos)
        org.jetbrains.skia.Image.makeFromEncoded(baos.toByteArray()).toComposeImageBitmap()
    } catch (_: Throwable) {
        androidx.compose.ui.graphics.ImageBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1))
    }
}

typealias PdfDocument = android.graphics.pdf.PdfDocument

