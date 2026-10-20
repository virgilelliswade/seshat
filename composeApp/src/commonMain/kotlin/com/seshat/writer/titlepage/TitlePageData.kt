package com.seshat.writer.titlepage

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Represents the industry-standard screenplay title page metadata.
 */
data class TitlePageData(
    val title: String = "",
    val writtenByLabel: String = "written by",
    val author: String = "",
    val sourceMaterial: String = "",
    val contactInfo: String = "",
    val draftInfo: String = ""
) {
    val isComplete: Boolean
        get() = title.isNotBlank() && author.isNotBlank()

    fun formattedTitle(): String = title.trim().uppercase()
}

/**
 * Singleton reactive repository coordinating screenplay metadata & title page state
 * between MainActivity and TitlePageActivity across multiple desktop windows.
 */
object ScreenplayDocumentStore {
    private val _titlePage = MutableStateFlow(
        TitlePageData(
            title = "UNTITLED SCREENPLAY",
            writtenByLabel = "written by",
            author = "",
            sourceMaterial = "",
            contactInfo = "",
            draftInfo = ""
        )
    )
    val titlePage: StateFlow<TitlePageData> = _titlePage.asStateFlow()

    fun updateTitlePage(transform: (TitlePageData) -> TitlePageData) {
        _titlePage.update(transform)
    }

    fun setTitlePage(data: TitlePageData) {
        _titlePage.value = data
    }

    fun updateTitle(newTitle: String) {
        _titlePage.update { it.copy(title = newTitle) }
    }

    fun updateAuthor(newAuthor: String) {
        _titlePage.update { it.copy(author = newAuthor) }
    }

    fun updateWrittenByLabel(newLabel: String) {
        _titlePage.update { it.copy(writtenByLabel = newLabel) }
    }

    fun updateSourceMaterial(newSource: String) {
        _titlePage.update { it.copy(sourceMaterial = newSource) }
    }

    fun updateContactInfo(newContact: String) {
        _titlePage.update { it.copy(contactInfo = newContact) }
    }

    fun updateDraftInfo(newDraft: String) {
        _titlePage.update { it.copy(draftInfo = newDraft) }
    }
}
