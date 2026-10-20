package com.seshat.writer.export

import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.titlepage.TitlePageData
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Serializes and deserializes screenplays to and from the Final Draft XML format (.fdx).
 * Fully compatible with Final Draft 10, 11, 12, 13 and other professional screenwriting software.
 */
object FinalDraftSerializer {

    data class FinalDraftDocument(
        val titlePage: TitlePageData,
        val blocks: List<ScreenplayBlock>
    )

    fun exportToFdx(
        blocks: List<ScreenplayBlock>,
        titlePageData: TitlePageData
    ): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n")
        sb.append("<FinalDraft DocumentType=\"Script\" Template=\"No\" Version=\"5\">\n")

        // 1. Script Content Paragraphs
        sb.append("  <Content>\n")
        for (block in blocks) {
            val fdxType = when (block.type) {
                BlockType.SCENE_HEADING, BlockType.STAGE_ACT_SCENE, BlockType.COMIC_PAGE -> "Scene Heading"
                BlockType.COMIC_PANEL -> "Shot"
                BlockType.ACTION, BlockType.STAGE_DIRECTION, BlockType.COMIC_ART_NOTE, BlockType.COMIC_CAPTION, BlockType.COMIC_SFX -> "Action"
                BlockType.CHARACTER -> "Character"
                BlockType.PARENTHETICAL -> "Parenthetical"
                BlockType.DIALOGUE, BlockType.COMIC_BALLOON -> "Dialogue"
                BlockType.TRANSITION, BlockType.STAGE_CURTAIN -> "Transition"
            }
            val numberAttr = if (block.type == BlockType.SCENE_HEADING && block.sceneNumber != null) {
                " Number=\"${escapeXml(block.sceneNumber)}\""
            } else ""
            val escapedText = escapeXml(block.text)
            sb.append("    <Paragraph Type=\"").append(fdxType).append("\"").append(numberAttr).append(">\n")
            sb.append("      <Text>").append(escapedText).append("</Text>\n")
            if (!block.note.isNullOrBlank()) {
                sb.append("      <ScriptAnnotation>\n")
                sb.append("        <Paragraph>\n")
                sb.append("          <Text>").append(escapeXml(block.note)).append("</Text>\n")
                sb.append("        </Paragraph>\n")
                sb.append("      </ScriptAnnotation>\n")
            }
            sb.append("    </Paragraph>\n")
        }
        sb.append("  </Content>\n")

        // 2. Title Page Metadata
        sb.append("  <TitlePage>\n")
        sb.append("    <Content>\n")
        if (titlePageData.title.isNotBlank()) {
            sb.append("      <Paragraph Alignment=\"Center\">\n")
            sb.append("        <Text Style=\"Bold\">").append(escapeXml(titlePageData.title.uppercase(Locale.US))).append("</Text>\n")
            sb.append("      </Paragraph>\n")
        }
        if (titlePageData.writtenByLabel.isNotBlank()) {
            sb.append("      <Paragraph Alignment=\"Center\">\n")
            sb.append("        <Text>").append(escapeXml(titlePageData.writtenByLabel)).append("</Text>\n")
            sb.append("      </Paragraph>\n")
        }
        if (titlePageData.author.isNotBlank()) {
            sb.append("      <Paragraph Alignment=\"Center\">\n")
            sb.append("        <Text Style=\"Bold\">").append(escapeXml(titlePageData.author)).append("</Text>\n")
            sb.append("      </Paragraph>\n")
        }
        if (titlePageData.sourceMaterial.isNotBlank()) {
            sb.append("      <Paragraph Alignment=\"Center\">\n")
            sb.append("        <Text>").append(escapeXml(titlePageData.sourceMaterial)).append("</Text>\n")
            sb.append("      </Paragraph>\n")
        }
        if (titlePageData.contactInfo.isNotBlank()) {
            sb.append("      <Paragraph Alignment=\"Left\">\n")
            sb.append("        <Text>").append(escapeXml(titlePageData.contactInfo)).append("</Text>\n")
            sb.append("      </Paragraph>\n")
        }
        if (titlePageData.draftInfo.isNotBlank()) {
            sb.append("      <Paragraph Alignment=\"Right\">\n")
            sb.append("        <Text>").append(escapeXml(titlePageData.draftInfo)).append("</Text>\n")
            sb.append("      </Paragraph>\n")
        }
        sb.append("    </Content>\n")
        sb.append("  </TitlePage>\n")

        sb.append("</FinalDraft>\n")
        return sb.toString()
    }

    fun importFromFdx(fdxXml: String): FinalDraftDocument {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(InputSource(StringReader(fdxXml)))
        doc.documentElement.normalize()

        val blocks = mutableListOf<ScreenplayBlock>()
        var title = ""
        var writtenByLabel = "written by"
        var author = ""
        var sourceMaterial = ""
        var contactInfo = ""
        var draftInfo = ""

        val titleCenterParagraphs = mutableListOf<String>()

        val rootChildren = doc.documentElement.childNodes
        for (i in 0 until rootChildren.length) {
            val rootChild = rootChildren.item(i)
            if (rootChild is Element) {
                when (rootChild.tagName) {
                    "Content" -> {
                        val paragraphs = rootChild.getElementsByTagName("Paragraph")
                        for (pIdx in 0 until paragraphs.length) {
                            val p = paragraphs.item(pIdx) as? Element ?: continue
                            // Skip paragraphs that are child elements of ScriptAnnotation
                            val parentTag = (p.parentNode as? Element)?.tagName
                            if (parentTag == "ScriptAnnotation") continue

                            val pType = p.getAttribute("Type")
                            val textBuilder = StringBuilder()
                            val noteBuilder = StringBuilder()

                            val pChildren = p.childNodes
                            for (cIdx in 0 until pChildren.length) {
                                val child = pChildren.item(cIdx)
                                if (child is Element) {
                                    when (child.tagName) {
                                        "Text" -> textBuilder.append(child.textContent)
                                        "ScriptAnnotation" -> {
                                            val annotationText = child.textContent.trim()
                                            if (annotationText.isNotEmpty()) {
                                                if (noteBuilder.isNotEmpty()) noteBuilder.append("\n")
                                                noteBuilder.append(annotationText)
                                            }
                                        }
                                    }
                                }
                            }

                            var text = textBuilder.toString().trim()
                            val note = noteBuilder.toString().trim().takeIf { it.isNotEmpty() }

                            // If text is empty but p has textContent (fallback for FDX variants without child Text tags)
                            if (text.isEmpty() && note == null) {
                                text = p.textContent.trim()
                            }

                            if (text.isNotEmpty() || note != null) {
                                val blockType = when (pType.lowercase(Locale.US)) {
                                    "scene heading" -> BlockType.SCENE_HEADING
                                    "character" -> BlockType.CHARACTER
                                    "parenthetical" -> BlockType.PARENTHETICAL
                                    "dialogue" -> BlockType.DIALOGUE
                                    "transition" -> BlockType.TRANSITION
                                    else -> BlockType.ACTION
                                }
                                val sceneNum = if (blockType == BlockType.SCENE_HEADING) p.getAttribute("Number").takeIf { it.isNotBlank() } else null
                                blocks.add(ScreenplayBlock(type = blockType, text = text, sceneNumber = sceneNum, note = note))
                            }
                        }
                    }
                    "TitlePage" -> {
                        val paragraphs = rootChild.getElementsByTagName("Paragraph")
                        for (pIdx in 0 until paragraphs.length) {
                            val p = paragraphs.item(pIdx) as? Element ?: continue
                            val align = p.getAttribute("Alignment").lowercase(Locale.US)
                            val text = p.textContent.trim()
                            if (text.isNotEmpty()) {
                                when (align) {
                                    "left" -> {
                                        contactInfo = if (contactInfo.isEmpty()) text else "$contactInfo\n$text"
                                    }
                                    "right" -> {
                                        draftInfo = if (draftInfo.isEmpty()) text else "$draftInfo\n$text"
                                    }
                                    else -> {
                                        titleCenterParagraphs.add(text)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Map Title Page center paragraphs (Title, Credit, Author, Source)
        if (titleCenterParagraphs.isNotEmpty()) {
            title = titleCenterParagraphs.getOrNull(0) ?: ""
            if (titleCenterParagraphs.size > 1) {
                val second = titleCenterParagraphs[1]
                if (second.equals("written by", ignoreCase = true) || second.equals("by", ignoreCase = true)) {
                    writtenByLabel = second
                    author = titleCenterParagraphs.getOrNull(2) ?: ""
                    sourceMaterial = titleCenterParagraphs.getOrNull(3) ?: ""
                } else {
                    author = second
                    sourceMaterial = titleCenterParagraphs.getOrNull(2) ?: ""
                }
            }
        }

        val finalBlocks = if (blocks.isEmpty()) {
            listOf(ScreenplayBlock(type = BlockType.SCENE_HEADING, text = ""))
        } else {
            blocks
        }

        val titlePageData = TitlePageData(
            title = title,
            writtenByLabel = writtenByLabel,
            author = author,
            sourceMaterial = sourceMaterial,
            contactInfo = contactInfo,
            draftInfo = draftInfo
        )

        return FinalDraftDocument(titlePage = titlePageData, blocks = finalBlocks)
    }

    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
