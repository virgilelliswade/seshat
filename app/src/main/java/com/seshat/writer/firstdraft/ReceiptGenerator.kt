package com.seshat.writer.firstdraft

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.neonBloom
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

sealed class AccountabilityReceipt {
    data class StageGateHighFive(
        val projectTitle: String,
        val data: StageGateReceiptData
    ) : AccountabilityReceipt()

    data class DailyDraftReceipt(
        val projectTitle: String,
        val netNewWords24h: Int,
        val activeStreakDays: Int,
        val totalWords: Int,
        val totalPages: Int,
        val backspaceShamePercent: Float,
        val hoursUntilDecay: Float
    ) : AccountabilityReceipt()

    data class MandatoryDecayPenalty(
        val projectTitle: String
    ) : AccountabilityReceipt()
}

/**
 * Social Accountability Engine (ReceiptGenerator.kt).
 * Renders fixed 1080x1920 (9:16 vertical) PNG graphics using Concept B pitch-black obsidian
 * and ruby-neon aesthetic, exports to local .png, and passes to native Android Intent.ACTION_SEND Share Sheet.
 */
object ReceiptGenerator {

    const val RECEIPT_WIDTH = 1080
    const val RECEIPT_HEIGHT = 1920
    private const val RECEIPTS_DIR = "seshat_receipts"

    fun renderReceiptPng(context: Context, receipt: AccountabilityReceipt): File? {
        return try {
            val bitmap = Bitmap.createBitmap(RECEIPT_WIDTH, RECEIPT_HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val isDecay = receipt is AccountabilityReceipt.MandatoryDecayPenalty
            val bgColor = if (isDecay) AndroidColor.rgb(34, 4, 10) else AndroidColor.rgb(7, 9, 14)
            val neonRed = AndroidColor.rgb(255, 30, 66)
            val gold = AndroidColor.rgb(245, 158, 11)
            val white = AndroidColor.WHITE
            val slate = AndroidColor.rgb(148, 163, 184)

            canvas.drawColor(bgColor)

            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = neonRed
                style = Paint.Style.STROKE
                strokeWidth = 8f
            }
            canvas.drawRect(44f, 44f, RECEIPT_WIDTH - 44f, RECEIPT_HEIGHT - 44f, borderPaint)

            val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isDecay) neonRed else gold
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRect(64f, 64f, RECEIPT_WIDTH - 64f, RECEIPT_HEIGHT - 64f, innerBorderPaint)

            val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = neonRed
                textSize = 36f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("SESHAT STUDIO • FIRST DRAFT ENGINE", RECEIPT_WIDTH / 2f, 160f, brandPaint)

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = white
                textSize = 54f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }

            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = slate
                textSize = 30f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }

            val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = white
                textSize = 64f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }

            when (receipt) {
                is AccountabilityReceipt.StageGateHighFive -> {
                    canvas.drawText("STAGE-GATE AIRLOCK SEALED", RECEIPT_WIDTH / 2f, 290f, titlePaint)
                    canvas.drawText(receipt.projectTitle.uppercase(Locale.US), RECEIPT_WIDTH / 2f, 380f, labelPaint)

                    canvas.drawText("STAGE SEALED (READ-ONLY)", RECEIPT_WIDTH / 2f, 620f, labelPaint)
                    valuePaint.color = neonRed
                    canvas.drawText("${receipt.data.stageSealed.label} SEALED", RECEIPT_WIDTH / 2f, 710f, valuePaint)

                    canvas.drawText("TIME REMAINING WHEN LOCKED", RECEIPT_WIDTH / 2f, 960f, labelPaint)
                    valuePaint.color = white
                    canvas.drawText(receipt.data.timeRemainingFormatted, RECEIPT_WIDTH / 2f, 1050f, valuePaint)

                    canvas.drawText("NEXT STAGE DEADLINE TIMESTAMP", RECEIPT_WIDTH / 2f, 1300f, labelPaint)
                    valuePaint.color = gold
                    valuePaint.textSize = 46f
                    canvas.drawText(receipt.data.nextStageDeadlineFormatted, RECEIPT_WIDTH / 2f, 1390f, valuePaint)
                }

                is AccountabilityReceipt.DailyDraftReceipt -> {
                    canvas.drawText("DAILY DRAFT ACCOUNTABILITY RECEIPT", RECEIPT_WIDTH / 2f, 280f, titlePaint)
                    canvas.drawText(receipt.projectTitle.uppercase(Locale.US), RECEIPT_WIDTH / 2f, 360f, labelPaint)

                    canvas.drawText("NET-NEW WORDS (24H)", RECEIPT_WIDTH / 2f, 540f, labelPaint)
                    valuePaint.color = neonRed
                    canvas.drawText("+${receipt.netNewWords24h} WORDS", RECEIPT_WIDTH / 2f, 625f, valuePaint)

                    canvas.drawText("ACTIVE STREAK DAYS", RECEIPT_WIDTH / 2f, 790f, labelPaint)
                    valuePaint.color = gold
                    canvas.drawText("${receipt.activeStreakDays} DAYS", RECEIPT_WIDTH / 2f, 875f, valuePaint)

                    canvas.drawText("TOTAL WORD / PAGE COUNT", RECEIPT_WIDTH / 2f, 1040f, labelPaint)
                    valuePaint.color = white
                    canvas.drawText("${receipt.totalWords} WDS • ${receipt.totalPages} PGS", RECEIPT_WIDTH / 2f, 1125f, valuePaint)

                    canvas.drawText("BACKSPACE / SHAME RATIO", RECEIPT_WIDTH / 2f, 1290f, labelPaint)
                    valuePaint.color = neonRed
                    canvas.drawText(String.format(Locale.US, "%.1f%%", receipt.backspaceShamePercent), RECEIPT_WIDTH / 2f, 1375f, valuePaint)

                    canvas.drawText("HOURS UNTIL DECAY", RECEIPT_WIDTH / 2f, 1540f, labelPaint)
                    valuePaint.color = white
                    canvas.drawText(String.format(Locale.US, "%.1f HRS", receipt.hoursUntilDecay), RECEIPT_WIDTH / 2f, 1625f, valuePaint)
                }

                is AccountabilityReceipt.MandatoryDecayPenalty -> {
                    titlePaint.color = neonRed
                    titlePaint.textSize = 58f
                    canvas.drawText("48-HOUR QUOTA FAILED", RECEIPT_WIDTH / 2f, 680f, titlePaint)
                    canvas.drawText("DRAFT DECAYED", RECEIPT_WIDTH / 2f, 840f, titlePaint)
                    valuePaint.color = white
                    valuePaint.textSize = 68f
                    canvas.drawText("0 PAGES WRITTEN", RECEIPT_WIDTH / 2f, 1040f, valuePaint)
                    canvas.drawText(receipt.projectTitle.uppercase(Locale.US), RECEIPT_WIDTH / 2f, 1260f, labelPaint)
                }
            }

            canvas.drawText("SESHAT v2.0.0 • ANTI-PROCRASTINATION ENGINE", RECEIPT_WIDTH / 2f, 1810f, labelPaint)

            val dir = File(context.filesDir, RECEIPTS_DIR)
            if (!dir.exists()) dir.mkdirs()
            val fileName = "seshat_receipt_${System.currentTimeMillis()}.png"
            val outFile = File(dir, fileName)
            FileOutputStream(outFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            outFile
        } catch (_: Exception) {
            null
        }
    }

    fun shareReceiptViaNativeOs(context: Context, receipt: AccountabilityReceipt): File? {
        val file = renderReceiptPng(context, receipt) ?: return null
        try {
            val uri: Uri = try {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (_: Throwable) {
                Uri.fromFile(file)
            }
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Seshat Accountability Receipt").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Throwable) {}
        return file
    }
}

@Composable
fun AccountabilityReceiptModal(
    receipt: AccountabilityReceipt,
    onShareClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDecay = receipt is AccountabilityReceipt.MandatoryDecayPenalty

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xD9030508))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.widthIn(max = 420.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(9f / 16f)
                        .neonBloom(
                            color = ScribeStarNeonRed,
                            cornerRadius = 22.dp,
                            glowRadius = 18.dp,
                            strokeWidth = 2.dp,
                            outerAlpha = 0.50f,
                            midAlpha = 0.85f
                        )
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            if (isDecay) {
                                Brush.verticalGradient(
                                    listOf(Color(0xFF3B040E), Color(0xFF1A0206), Color(0xFF3B040E))
                                )
                            } else {
                                Brush.verticalGradient(
                                    listOf(Color(0xFF07090E), Color(0xFF101522), Color(0xFF07090E))
                                )
                            }
                        )
                        .border(BorderStroke(2.dp, ScribeStarNeonRed), RoundedCornerShape(22.dp))
                        .padding(22.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SESHAT • FIRST DRAFT ENGINE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = MainAppFontFamily,
                                    color = ScribeStarNeonRed,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.6.sp,
                                    fontSize = 10.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = ScribeStarNeonRed.copy(alpha = 0.4f))
                        }

                        when (receipt) {
                            is AccountabilityReceipt.StageGateHighFive -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "⚡ STAGE-GATE SEALED ⚡",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = MainAppFontFamily,
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.2.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                    ReceiptMetricRow("STAGE SEALED", "${receipt.data.stageSealed.label} (READ-ONLY)", ScribeStarNeonRed)
                                    ReceiptMetricRow("TIME REMAINING WHEN LOCKED", receipt.data.timeRemainingFormatted, Color.White)
                                    ReceiptMetricRow("NEXT STAGE DEADLINE", receipt.data.nextStageDeadlineFormatted, Color(0xFFF59E0B))
                                }
                            }

                            is AccountabilityReceipt.DailyDraftReceipt -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "🔥 DAILY DRAFT RECEIPT 🔥",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = MainAppFontFamily,
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.1.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                    ReceiptMetricRow("NET-NEW WORDS (24H)", "+${receipt.netNewWords24h} WORDS", ScribeStarNeonRed)
                                    ReceiptMetricRow("ACTIVE STREAK DAYS", "${receipt.activeStreakDays} DAYS", Color(0xFFF59E0B))
                                    ReceiptMetricRow("TOTAL WORD / PAGE COUNT", "${receipt.totalWords} WDS • ${receipt.totalPages} PGS", Color.White)
                                    ReceiptMetricRow(
                                        "BACKSPACE / SHAME RATIO",
                                        String.format(Locale.US, "%.1f%%", receipt.backspaceShamePercent),
                                        ScribeStarNeonRed
                                    )
                                    ReceiptMetricRow(
                                        "HOURS UNTIL DECAY",
                                        String.format(Locale.US, "%.1f HRS", receipt.hoursUntilDecay),
                                        Color(0xFF38BDF8)
                                    )
                                }
                            }

                            is AccountabilityReceipt.MandatoryDecayPenalty -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(18.dp)
                                ) {
                                    Text(
                                        text = "🚨 MANDATORY PENALTY CARD 🚨",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = MainAppFontFamily,
                                            color = ScribeStarNeonRed,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.4.sp
                                        )
                                    )
                                    Text(
                                        text = "48-HOUR QUOTA FAILED — DRAFT DECAYED — 0 PAGES WRITTEN",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontFamily = MainAppFontFamily,
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            lineHeight = 30.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            HorizontalDivider(color = ScribeStarNeonRed.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "1080x1920 • VERIFIED LOCAL ACCOUNTABILITY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = MainAppFontFamily,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.sp,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = ScribeStarNeonRed,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onShareClicked() }
                    ) {
                        Text(
                            text = "📤 Export .PNG & Share",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = MainAppFontFamily,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }

                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFF475569)),
                        modifier = Modifier.clickable { onDismiss() }
                    ) {
                        Text(
                            text = "Close",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = MainAppFontFamily,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptMetricRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x40000000))
            .border(BorderStroke(0.8.dp, Color(0x30FFFFFF)), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = MainAppFontFamily,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Bold,
                fontSize = 9.5.sp,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = MainAppFontFamily,
                color = valueColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}
