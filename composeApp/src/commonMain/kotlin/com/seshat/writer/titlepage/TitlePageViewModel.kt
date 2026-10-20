package com.seshat.writer.titlepage

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TitlePageViewModel : ViewModel() {

    val titlePageState: StateFlow<TitlePageData> = ScreenplayDocumentStore.titlePage

    fun updateTitle(newTitle: String) {
        ScreenplayDocumentStore.updateTitle(newTitle)
    }

    fun updateWrittenByLabel(newLabel: String) {
        ScreenplayDocumentStore.updateWrittenByLabel(newLabel)
    }

    fun updateAuthor(newAuthor: String) {
        ScreenplayDocumentStore.updateAuthor(newAuthor)
    }

    fun updateSourceMaterial(newSource: String) {
        ScreenplayDocumentStore.updateSourceMaterial(newSource)
    }

    fun updateContactInfo(newContact: String) {
        ScreenplayDocumentStore.updateContactInfo(newContact)
    }

    fun updateDraftInfo(newDraft: String) {
        ScreenplayDocumentStore.updateDraftInfo(newDraft)
    }

    fun insertCurrentDateToDraft() {
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)
        val todayStr = dateFormat.format(Date())
        val currentDraft = titlePageState.value.draftInfo.trim()
        val updated = if (currentDraft.isEmpty()) {
            todayStr
        } else if (currentDraft.contains(todayStr)) {
            currentDraft
        } else {
            "$currentDraft\n$todayStr"
        }
        ScreenplayDocumentStore.updateDraftInfo(updated)
    }

    fun setDraftPreset(preset: String) {
        val current = titlePageState.value.draftInfo.trim()
        val updated = if (current.isEmpty()) {
            preset
        } else {
            "$preset\n$current"
        }
        ScreenplayDocumentStore.updateDraftInfo(updated)
    }
}
