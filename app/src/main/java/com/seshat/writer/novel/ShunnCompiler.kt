package com.seshat.writer.novel

/**
 * Compiles a NovelManuscript into William Shunn Standard Manuscript Format,
 * Markdown, and clean semantic HTML.
 */
object ShunnCompiler {

    /**
     * Generates a Standard Manuscript Format text document (William Shunn guidelines).
     */
    fun compileToShunnFormat(
        manuscript: NovelManuscript,
        options: CompilerOptions = CompilerOptions()
    ): String {
        val selectedChapters = if (options.selectedChapterIds.isNotEmpty()) {
            manuscript.chapters.filter { options.selectedChapterIds.contains(it.id) && !it.isOmitted }
        } else {
            manuscript.chapters.filterNot { it.isOmitted }
        }

        val totalWords = selectedChapters.sumOf { it.wordCount() }
        val roundedWords = (Math.round(totalWords / 100.0) * 100).toInt()

        return buildString {
            // Header Page (Shunn Submission Format)
            if (options.includeTitlePage) {
                append("${manuscript.author.ifBlank { "Author Name" }}\n")
                append("c/o Seshat Studio\n")
                append("Words: ~$roundedWords\n\n\n\n\n\n")

                append("                          ${manuscript.title.uppercase()}\n\n")
                if (manuscript.subtitle.isNotBlank()) {
                    append("                          ${manuscript.subtitle}\n\n")
                }
                append("                             by\n\n")
                append("                      ${manuscript.author.ifBlank { "Author Name" }}\n\n\n\n\n")
                append("========================================================================\n\n")
            }

            for ((idx, ch) in selectedChapters.withIndex()) {
                val headerText = ch.paragraphs.firstOrNull { it.type == NovelParagraphType.CHAPTER_HEADER }?.text
                    ?: "CHAPTER ${idx + 1}"

                append("\n\n                      ${headerText.uppercase()}\n\n\n")

                for (p in ch.paragraphs) {
                    when (p.type) {
                        NovelParagraphType.CHAPTER_HEADER -> {
                            // Already rendered as chapter title
                        }
                        NovelParagraphType.SCENE_BREAK -> {
                            append("\n                          #\n\n")
                        }
                        NovelParagraphType.NARRATIVE, NovelParagraphType.DIALOGUE -> {
                            if (p.text.isNotBlank()) {
                                // Shunn 5-space paragraph indent
                                append("     ${p.text}\n\n")
                            }
                        }
                    }
                }
                append("\n\n")
            }
        }
    }

    /**
     * Compiles the manuscript into clean Markdown format.
     */
    fun compileToMarkdown(
        manuscript: NovelManuscript,
        options: CompilerOptions = CompilerOptions()
    ): String {
        val selectedChapters = if (options.selectedChapterIds.isNotEmpty()) {
            manuscript.chapters.filter { options.selectedChapterIds.contains(it.id) && !it.isOmitted }
        } else {
            manuscript.chapters.filterNot { it.isOmitted }
        }

        return buildString {
            append("# ${manuscript.title.uppercase()}\n\n")
            if (manuscript.subtitle.isNotBlank()) append("*${manuscript.subtitle}*\n\n")
            if (manuscript.author.isNotBlank()) append("**By:** ${manuscript.author}\n\n")
            append("Total Words: ~${manuscript.totalWordCount()}\n\n---\n\n")

            for ((idx, ch) in selectedChapters.withIndex()) {
                val headerText = ch.paragraphs.firstOrNull { it.type == NovelParagraphType.CHAPTER_HEADER }?.text
                    ?: "Chapter ${idx + 1}"

                append("## $headerText\n\n")

                for (p in ch.paragraphs) {
                    when (p.type) {
                        NovelParagraphType.CHAPTER_HEADER -> {}
                        NovelParagraphType.SCENE_BREAK -> {
                            append("\n\n${options.sceneBreakSymbol}\n\n")
                        }
                        NovelParagraphType.NARRATIVE, NovelParagraphType.DIALOGUE -> {
                            if (p.text.isNotBlank()) {
                                append("${p.text}\n\n")
                            }
                        }
                    }
                }
                append("\n---\n\n")
            }
        }
    }

    /**
     * Compiles the manuscript into a self-contained HTML page.
     */
    fun compileToHtml(
        manuscript: NovelManuscript,
        options: CompilerOptions = CompilerOptions()
    ): String {
        val selectedChapters = if (options.selectedChapterIds.isNotEmpty()) {
            manuscript.chapters.filter { options.selectedChapterIds.contains(it.id) && !it.isOmitted }
        } else {
            manuscript.chapters.filterNot { it.isOmitted }
        }

        return buildString {
            append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n")
            append("<meta charset=\"UTF-8\"/>\n")
            append("<title>${manuscript.title}</title>\n")
            append("<style>\n")
            append("body { font-family: 'Georgia', serif; max-width: 720px; margin: 40px auto; padding: 0 20px; line-height: 1.6; color: #222; }\n")
            append("h1.title { text-align: center; margin-top: 60px; font-size: 2.5em; }\n")
            append("h2.author { text-align: center; color: #666; font-weight: normal; margin-bottom: 60px; }\n")
            append("h2.chapter { text-align: center; margin-top: 50px; text-transform: uppercase; letter-spacing: 2px; }\n")
            append("p { text-indent: 2em; margin: 0; }\n")
            append("p.first-p { text-indent: 0; }\n")
            append("p.break { text-align: center; text-indent: 0; margin: 30px 0; letter-spacing: 6px; color: #888; }\n")
            append("</style>\n</head>\n<body>\n")

            if (options.includeTitlePage) {
                append("<h1 class=\"title\">${manuscript.title}</h1>\n")
                if (manuscript.author.isNotBlank()) {
                    append("<h2 class=\"author\">by ${manuscript.author}</h2>\n")
                }
            }

            for ((idx, ch) in selectedChapters.withIndex()) {
                val headerText = ch.paragraphs.firstOrNull { it.type == NovelParagraphType.CHAPTER_HEADER }?.text
                    ?: "Chapter ${idx + 1}"
                append("<h2 class=\"chapter\">$headerText</h2>\n")

                var first = true
                for (p in ch.paragraphs) {
                    when (p.type) {
                        NovelParagraphType.CHAPTER_HEADER -> {}
                        NovelParagraphType.SCENE_BREAK -> {
                            append("<p class=\"break\">${options.sceneBreakSymbol}</p>\n")
                            first = true
                        }
                        NovelParagraphType.NARRATIVE, NovelParagraphType.DIALOGUE -> {
                            if (p.text.isNotBlank()) {
                                val cls = if (first) " class=\"first-p\"" else ""
                                append("<p$cls>${p.text}</p>\n")
                                first = false
                            }
                        }
                    }
                }
            }
            append("</body>\n</html>")
        }
    }
}
