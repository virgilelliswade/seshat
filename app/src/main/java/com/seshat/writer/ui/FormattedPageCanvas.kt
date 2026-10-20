package com.seshat.writer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.screenplay.ScreenplayElement
import com.seshat.writer.screenplay.ScreenplayLayoutStandards
import com.seshat.writer.ui.theme.ScreenplayCourierFontFamily

@Composable
fun FormattedPageCanvas(
    elements: List<ScreenplayElement>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .background(Color(0xFF1A1D20))
            .padding(vertical = 24.dp)
            .verticalScroll(scrollState)
    ) {
        // Authentic US Letter Screenplay Paper Sheet (Final Draft White / Cream)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAF7)),
            shape = RoundedCornerShape(2.dp),
            modifier = Modifier
                .widthIn(min = 600.dp, max = 750.dp)
                .fillMaxWidth(0.92f)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(2.dp))
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 36.dp)
            ) {
                val pageWidth = maxWidth

                // Proportional calculations based on 8.5" page width
                val leftMargin = pageWidth * (ScreenplayLayoutStandards.PAGE_LEFT_MARGIN_INCHES / ScreenplayLayoutStandards.PAGE_WIDTH_INCHES)
                val characterOffset = pageWidth * (ScreenplayLayoutStandards.CHARACTER_LEFT_INCHES / ScreenplayLayoutStandards.PAGE_WIDTH_INCHES)
                val parentheticalOffset = pageWidth * (ScreenplayLayoutStandards.PARENTHETICAL_LEFT_INCHES / ScreenplayLayoutStandards.PAGE_WIDTH_INCHES)
                val dialogueOffset = pageWidth * (ScreenplayLayoutStandards.DIALOGUE_LEFT_INCHES / ScreenplayLayoutStandards.PAGE_WIDTH_INCHES)
                val transitionOffset = pageWidth * (ScreenplayLayoutStandards.TRANSITION_LEFT_INCHES / ScreenplayLayoutStandards.PAGE_WIDTH_INCHES)

                val actionWidth = pageWidth * (ScreenplayLayoutStandards.ACTION_WIDTH_INCHES / ScreenplayLayoutStandards.PAGE_WIDTH_INCHES)
                val dialogueWidth = pageWidth * (ScreenplayLayoutStandards.DIALOGUE_WIDTH_INCHES / ScreenplayLayoutStandards.PAGE_WIDTH_INCHES)
                val parentheticalWidth = pageWidth * (ScreenplayLayoutStandards.PARENTHETICAL_WIDTH_INCHES / ScreenplayLayoutStandards.PAGE_WIDTH_INCHES)

                Column(modifier = Modifier.fillMaxWidth()) {
                    elements.forEach { element ->
                        when (element) {
                            is ScreenplayElement.SceneHeading -> {
                                Spacer(modifier = Modifier.height(18.dp))
                                Text(
                                    text = element.text.uppercase(),
                                    style = screenplayTextStyle(isBold = true),
                                    modifier = Modifier
                                        .padding(start = leftMargin)
                                        .width(actionWidth)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            is ScreenplayElement.Action -> {
                                Text(
                                    text = element.text,
                                    style = screenplayTextStyle(),
                                    modifier = Modifier
                                        .padding(start = leftMargin)
                                        .width(actionWidth)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            is ScreenplayElement.Character -> {
                                Spacer(modifier = Modifier.height(8.dp))
                                val displayName = if (element.extension != null) {
                                    "${element.name.uppercase()} (${element.extension})"
                                } else {
                                    element.name.uppercase()
                                }
                                Text(
                                    text = displayName,
                                    style = screenplayTextStyle(isBold = false),
                                    modifier = Modifier
                                        .padding(start = characterOffset)
                                )
                            }
                            is ScreenplayElement.Parenthetical -> {
                                Text(
                                    text = element.text,
                                    style = screenplayTextStyle(),
                                    modifier = Modifier
                                        .padding(start = parentheticalOffset)
                                        .width(parentheticalWidth)
                                )
                            }
                            is ScreenplayElement.Dialogue -> {
                                Text(
                                    text = element.text,
                                    style = screenplayTextStyle(),
                                    modifier = Modifier
                                        .padding(start = dialogueOffset)
                                        .width(dialogueWidth)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            is ScreenplayElement.Transition -> {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = element.text.uppercase(),
                                    textAlign = TextAlign.End,
                                    style = screenplayTextStyle(),
                                    modifier = Modifier
                                        .padding(start = transitionOffset)
                                        .fillMaxWidth(0.9f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            else -> {
                                // Section heading / Synopsis
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun screenplayTextStyle(isBold: Boolean = false) = androidx.compose.ui.text.TextStyle(
    fontFamily = ScreenplayCourierFontFamily,
    fontSize = 12.sp,
    lineHeight = 18.sp,
    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
    color = Color(0xFF111111),
    letterSpacing = 0.4.sp
)
