package com.seshat.writer.screenplay

/**
 * Industry-standard screenplay page dimensions and element margins (Final Draft / AMPAS / WGA standard).
 * 
 * Standard page: US Letter 8.5" x 11"
 * Courier 12pt (10 pitch = 10 characters per inch)
 */
object ScreenplayLayoutStandards {
    // Page Dimensions in inches
    const val PAGE_WIDTH_INCHES = 8.5f
    const val PAGE_HEIGHT_INCHES = 11.0f
    
    // Page Margins in inches
    const val PAGE_TOP_MARGIN_INCHES = 1.0f
    const val PAGE_BOTTOM_MARGIN_INCHES = 1.0f
    const val PAGE_LEFT_MARGIN_INCHES = 1.5f   // 1.5" binding margin (3-hole punch)
    const val PAGE_RIGHT_MARGIN_INCHES = 1.0f  // 1.0" right margin

    // Element Left Offsets from physical left page edge (in inches)
    const val SCENE_HEADING_LEFT_INCHES = 1.5f  // Left aligned with margin
    const val ACTION_LEFT_INCHES = 1.5f         // Left aligned with margin
    const val CHARACTER_LEFT_INCHES = 3.7f      // 2.2" from left margin (approx. center)
    const val PARENTHETICAL_LEFT_INCHES = 3.1f  // 1.6" from left margin
    const val DIALOGUE_LEFT_INCHES = 2.5f       // 1.0" from left margin
    const val TRANSITION_LEFT_INCHES = 5.5f     // Right-aligned to 7.5"
    
    // Element Maximum Widths (in inches)
    const val ACTION_WIDTH_INCHES = 6.0f        // 8.5 - 1.5 - 1.0 = 6.0" (approx 60 chars)
    const val DIALOGUE_WIDTH_INCHES = 3.5f      // 3.5" (approx 35 chars)
    const val PARENTHETICAL_WIDTH_INCHES = 2.5f // 2.5" (approx 25 chars)
    const val CHARACTER_WIDTH_INCHES = 3.8f
    const val TRANSITION_WIDTH_INCHES = 2.0f

    // Dual Dialogue Standards (Final Draft / Hollywood layout)
    const val DUAL_DIALOGUE_LEFT_COLUMN_LEFT_INCHES = 1.5f   // Left aligned with binding margin
    const val DUAL_DIALOGUE_COLUMN_WIDTH_INCHES = 2.75f     // ~27.5 chars of 12pt Courier (198 pt)
    const val DUAL_DIALOGUE_COLUMN_GUTTER_INCHES = 0.5f     // 0.5" gutter between columns (36 pt)
    const val DUAL_DIALOGUE_RIGHT_COLUMN_LEFT_INCHES = 4.75f // 1.5 + 2.75 + 0.5 = 4.75"

    // Element Right Margins / Offsets (in inches from right page edge)
    const val ACTION_RIGHT_INCHES = 1.0f
    const val SCENE_HEADING_RIGHT_INCHES = 1.0f
    const val DIALOGUE_RIGHT_INCHES = 2.5f       // 2.5" from right edge
    const val PARENTHETICAL_RIGHT_INCHES = 2.9f  // 2.9" from right edge
    const val TRANSITION_RIGHT_INCHES = 1.0f     // 1.0" flush with right margin

    // Header & Pagination (Final Draft rule: numbering begins on page 2)
    const val HEADER_TOP_INCHES = 0.5f
    const val FIRST_NUMBERED_PAGE = 2

    // Vertical Spacing Rules
    const val SCENE_HEADING_BLANK_LINES_BEFORE = 2 // 2 blank lines before scene heading

    // Line Budgeting & Typography
    const val LINES_PER_PAGE = 55                // roughly 55 lines per page
    const val LINE_HEIGHT_SP = 22f
    const val FONT_SIZE_SP = 12f

    // Monospace Advance Metrics (Courier 12pt = ~7.2sp per char)
    const val CHAR_WIDTH_SP = 7.2f

    // Element Indents from 1.5" Left Margin (10-cpi pitch)
    // Dialogue = 10 spaces (~1.0 inch)
    const val DIALOGUE_INDENT_SP = 72f
    // Parenthetical = 16 spaces (~1.6 inches)
    const val PARENTHETICAL_INDENT_SP = 115f
    // Character = 22 spaces (~2.2 inches)
    const val CHARACTER_INDENT_SP = 158f

    // US Letter Page Canvas Margins (scaled for 720dp width canvas)
    const val PAGE_LEFT_MARGIN_DP = 120f   // 1.5" binding margin (allows room for brads)
    const val PAGE_RIGHT_MARGIN_DP = 80f   // 1.0" right margin
    const val PAGE_TOP_MARGIN_DP = 64f     // 1.0" top margin
    const val PAGE_BOTTOM_MARGIN_DP = 72f  // 1.0" bottom margin
}
