package com.seshat.writer.novel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted

/**
 * Interactive novel paragraph component handling the TAB + ENTER prose writing engine.
 * - Chapter Number behaves like FADE IN (centered, stately).
 * - Enter creates a new paragraph, which is always indented.
 * - Auto-capitalizes the first letter of the paragraph.
 * - Tab at start switches from narration to dialogue, inserting quotation mark with cursor inside.
 * - Typing terminal punctuation inside quote closes quote and suggests dialogue attribution verbs.
 * - Mouse click and drag in left gutter highlights paragraphs seamlessly.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NovelParagraphItem(
    paragraph: NovelParagraph,
    isFocused: Boolean,
    isFirstParagraphInChapter: Boolean,
    isHighlighted: Boolean = false,
    onTextChange: (String) -> Unit,
    onTypeChange: (NovelParagraphType) -> Unit,
    onEnterPressed: () -> Unit,
    onBackspaceOnEmpty: () -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateDown: () -> Unit,
    onFocused: () -> Unit,
    onToggleHighlight: (Boolean) -> Unit = {},
    onDragHighlightStart: () -> Unit = {},
    onDragHighlightMove: (Float) -> Unit = {},
    onDragHighlightEnd: () -> Unit = {},
    onCoordinatesChanged: (LayoutCoordinates) -> Unit = {},
    onInsertDialogueVerb: (String) -> Unit,
    isCompactScreen: Boolean = false,
    isKeyboardActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = paragraph.text,
                selection = TextRange(paragraph.text.length)
            )
        )
    }

    var detectedPunctuation by remember { mutableStateOf<Char?>(null) }
    var suggestedVerbs by remember { mutableStateOf<List<String>>(emptyList()) }
    var itemCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val highlightBg = if (isHighlighted) Color(0xFFFEF3C7) else Color.Transparent
    val highlightBorder = if (isHighlighted) BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.75f)) else null

    LaunchedEffect(paragraph.text) {
        if (textFieldValue.text != paragraph.text) {
            textFieldValue = textFieldValue.copy(
                text = paragraph.text,
                selection = TextRange(paragraph.text.length)
            )
        }
    }

    LaunchedEffect(isFocused, isKeyboardActive) {
        if (isFocused) {
            try {
                focusRequester.requestFocus()
                bringIntoViewRequester.bringIntoView()
            } catch (_: Exception) {}
            if (!isKeyboardActive && isCompactScreen) {
                keyboardController?.hide()
            }
        }
    }

    // Check for closing quote + dialogue verb suggestions
    LaunchedEffect(paragraph.text, paragraph.type) {
        if (paragraph.type == NovelParagraphType.DIALOGUE) {
            val punct = DialogueVerbSuggester.detectTerminalQuotePunctuation(paragraph.text)
            if (punct != null) {
                detectedPunctuation = punct
                suggestedVerbs = DialogueVerbSuggester.getVerbsForPunctuation(punct)
            } else {
                detectedPunctuation = null
                suggestedVerbs = emptyList()
            }
        } else {
            detectedPunctuation = null
            suggestedVerbs = emptyList()
        }
    }

    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onGloballyPositioned { coords ->
                itemCoordinates = coords
                onCoordinatesChanged(coords)
            }
            .background(highlightBg, RoundedCornerShape(6.dp))
            .then(if (highlightBorder != null) Modifier.border(highlightBorder, RoundedCornerShape(6.dp)) else Modifier)
            .padding(
                start = if (isHighlighted) 6.dp else 2.dp,
                end = if (isHighlighted) 6.dp else 2.dp,
                top = if (paragraph.type == NovelParagraphType.SCENE_BREAK) 12.dp else if (isHighlighted) 4.dp else 2.dp,
                bottom = if (paragraph.type == NovelParagraphType.SCENE_BREAK) 12.dp else if (isHighlighted) 4.dp else 2.dp
            )
    ) {
        // Gutter Selector / Mouse Drag Handle
        Box(
            modifier = Modifier
                .padding(
                    top = when (paragraph.type) {
                        NovelParagraphType.CHAPTER_HEADER -> 36.dp
                        NovelParagraphType.SCENE_BREAK -> 16.dp
                        else -> 6.dp
                    },
                    end = 8.dp
                )
                .size(14.dp)
                .clip(RoundedCornerShape(3.dp))
                .pointerHoverIcon(PointerIcon.Hand)
                .background(if (isHighlighted) ElectrumGold else Color(0x18000000))
                .border(
                    BorderStroke(1.dp, if (isHighlighted) ElectrumGold else Color(0x28000000)),
                    RoundedCornerShape(3.dp)
                )
                .clickable { onToggleHighlight(false) }
                .pointerInput(paragraph.id) {
                    detectDragGestures(
                        onDragStart = {
                            onDragHighlightStart()
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val rootY = itemCoordinates?.localToRoot(change.position)?.y ?: 0f
                            onDragHighlightMove(rootY)
                        },
                        onDragEnd = { onDragHighlightEnd() },
                        onDragCancel = { onDragHighlightEnd() }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (isHighlighted) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(ObsidianBlack, RoundedCornerShape(1.dp))
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (paragraph.type) {
                NovelParagraphType.CHAPTER_HEADER -> {
                // CHAPTER HEADER: Stately, centered, behaves like FADE IN
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp, bottom = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newVal ->
                            if (newVal.text.contains('\n')) {
                                val stripped = newVal.text.replace("\n", "").uppercase()
                                textFieldValue = newVal.copy(text = stripped)
                                onTextChange(stripped)
                                onEnterPressed()
                            } else {
                                val cap = newVal.text.uppercase()
                                textFieldValue = newVal.copy(text = cap)
                                onTextChange(cap)
                            }
                        },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            textAlign = TextAlign.Center,
                            letterSpacing = 2.sp
                        ),
                        cursorBrush = SolidColor(ElectrumGold),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (textFieldValue.text.isEmpty()) {
                                    Text(
                                        text = "CHAPTER 1",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFA0AEC0),
                                            textAlign = TextAlign.Center,
                                            letterSpacing = 2.sp
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { onEnterPressed() },
                            onDone = { onEnterPressed() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .focusRequester(focusRequester)
                            .onFocusChanged { if (it.isFocused) onFocused() }
                            .onPreviewKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    if (keyEvent.key == Key.Enter) {
                                        onEnterPressed()
                                        return@onPreviewKeyEvent true
                                    }
                                    if (keyEvent.key == Key.Backspace && textFieldValue.text.isEmpty()) {
                                        onBackspaceOnEmpty()
                                        return@onPreviewKeyEvent true
                                    }
                                }
                                false
                            }
                    )
                }
            }

            NovelParagraphType.SCENE_BREAK -> {
                // SCENE BREAK: Centered glyph (e.g. * * *)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = paragraph.text.ifBlank { "* * *" },
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp,
                            color = Color(0xFF64748B)
                        ),
                        modifier = Modifier.clickable {
                            onTypeChange(NovelParagraphType.NARRATIVE)
                        }
                    )
                }
            }

            NovelParagraphType.NARRATIVE, NovelParagraphType.DIALOGUE -> {
                // PROSE PARAGRAPH: Always indented (standard book typography)
                // First-line indent: 32sp, continuation lines wrap flush to margin (0sp)
                val isDialogue = paragraph.type == NovelParagraphType.DIALOGUE
                val indentSp = 32.sp
                val indentDp = 32.dp

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newVal ->
                            if (newVal.text.contains('\n')) {
                                val stripped = newVal.text.replace("\n", "")
                                onTextChange(stripped)
                                onEnterPressed()
                            } else {
                                var processedText = newVal.text
                                var newSelection = newVal.selection

                                // 1. AUTO-CAPITALIZE FIRST LETTER OF THE PARAGRAPH
                                if (processedText.length == 1 && processedText[0].isLowerCase()) {
                                    processedText = processedText.replaceFirstChar { it.uppercase() }
                                } else if (isDialogue && processedText.length == 2 && processedText.startsWith("“") && processedText[1].isLowerCase()) {
                                    processedText = "“" + processedText[1].uppercase()
                                }

                                // 2. CLOSING QUOTE AUTO-INSERTION
                                // If user is in dialogue and types a quote closure punctuation:
                                if (isDialogue && processedText.length > textFieldValue.text.length) {
                                    val lastChar = processedText.lastOrNull()
                                    if (lastChar in listOf('.', '!', '?', ',', '—') && !processedText.endsWith("”")) {
                                        // Append closing quotation mark automatically!
                                        processedText = "$processedText” "
                                        newSelection = TextRange(processedText.length)
                                    }
                                }

                                textFieldValue = newVal.copy(
                                    text = processedText,
                                    selection = newSelection
                                )
                                onTextChange(processedText)
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { onEnterPressed() },
                            onDone = { onEnterPressed() }
                        ),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontSize = 17.sp,
                            lineHeight = 27.sp,
                            color = if (isDialogue) Color(0xFF0F172A) else Color(0xFF1E293B),
                            fontWeight = FontWeight.Normal,
                            textIndent = TextIndent(firstLine = indentSp, restLine = 0.sp)
                        ),
                        cursorBrush = SolidColor(ElectrumGold),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = if (textFieldValue.text.isEmpty()) indentDp else 0.dp)
                            ) {
                                if (textFieldValue.text.isEmpty()) {
                                    Text(
                                        text = if (isDialogue) "“Dialogue...”" else "Begin writing...",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 17.sp,
                                            lineHeight = 27.sp,
                                            color = Color(0xFFA0AEC0),
                                            fontWeight = FontWeight.Normal
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { if (it.isFocused) onFocused() }
                            .onPreviewKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    // TAB KEY: At start of paragraph, changes Narration to Dialogue & inserts quotation mark
                                    if (keyEvent.key == Key.Tab) {
                                        if (textFieldValue.selection.start == 0 || textFieldValue.text.isBlank()) {
                                            onTypeChange(NovelParagraphType.DIALOGUE)
                                            val quoteText = if (textFieldValue.text.startsWith("“")) {
                                                textFieldValue.text
                                            } else {
                                                "“" + textFieldValue.text
                                            }
                                            textFieldValue = TextFieldValue(
                                                text = quoteText,
                                                selection = TextRange(1.coerceAtMost(quoteText.length))
                                            )
                                            onTextChange(quoteText)
                                            return@onPreviewKeyEvent true
                                        }
                                    }

                                    // ENTER KEY: Creates a new paragraph, which is always indented
                                    if (keyEvent.key == Key.Enter) {
                                        // Chapter Number behaves like FADE IN
                                        if (NovelChapter.isChapterHeaderTrigger(textFieldValue.text)) {
                                            val headerText = textFieldValue.text.trim().uppercase()
                                            onTypeChange(NovelParagraphType.CHAPTER_HEADER)
                                            onTextChange(headerText)
                                            onEnterPressed()
                                            return@onPreviewKeyEvent true
                                        }
                                        onEnterPressed()
                                        return@onPreviewKeyEvent true
                                    }

                                    // BACKSPACE ON EMPTY: Removes empty paragraph
                                    if (keyEvent.key == Key.Backspace && textFieldValue.text.isEmpty()) {
                                        onBackspaceOnEmpty()
                                        return@onPreviewKeyEvent true
                                    }

                                    // ARROW NAVIGATION
                                    if (keyEvent.key == Key.DirectionUp && textFieldValue.selection.start == 0) {
                                        onNavigateUp()
                                        return@onPreviewKeyEvent true
                                    }
                                    if (keyEvent.key == Key.DirectionDown && textFieldValue.selection.end == textFieldValue.text.length) {
                                        onNavigateDown()
                                        return@onPreviewKeyEvent true
                                    }
                                }
                                false
                            }
                    )
                }

                // AI & HEURISTIC DIALOGUE VERB SUGGESTION BAR
                AnimatedVisibility(visible = isDialogue && isFocused && suggestedVerbs.isNotEmpty()) {
                    DialogueVerbBar(
                        punctuation = detectedPunctuation ?: '.',
                        verbs = suggestedVerbs,
                        onSelectVerb = { verb ->
                            // Insert verb into text
                            val current = textFieldValue.text
                            val updated = if (current.endsWith("”") || current.endsWith("” ")) {
                                current.trimEnd() + " $verb "
                            } else {
                                "$current $verb "
                            }
                            textFieldValue = TextFieldValue(
                                text = updated,
                                selection = TextRange(updated.length)
                            )
                            onTextChange(updated)
                            onInsertDialogueVerb(verb)
                        }
                    )
                }
            }
        }
    }
}
}

/**
 * Suggested Dialogue Attribution Verbs Bar.
 * Appears below quotation when closing punctuation is detected.
 */
@Composable
private fun DialogueVerbBar(
    punctuation: Char,
    verbs: List<String>,
    onSelectVerb: (String) -> Unit
) {
    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.5f)),
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 6.dp, bottom = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = ElectrumGold.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.6f))
            ) {
                Text(
                    text = "‘$punctuation’ VERBS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectrumGold,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                verbs.forEach { verb ->
                    Surface(
                        color = ObsidianBlack,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.35f)),
                        modifier = Modifier.clickable { onSelectVerb(verb) }
                    ) {
                        Text(
                            text = verb,
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontSize = 12.5.sp,
                                color = ElectrumGold,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
