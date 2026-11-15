package com.roozyar.app.feature.journal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.domain.model.JournalEntry
import com.roozyar.app.domain.model.JournalMood
import com.roozyar.app.domain.model.parseTags
import com.roozyar.app.domain.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

data class JournalEditUiState(
    val isLoading: Boolean = true,
    val isNew: Boolean = true,
    val title: String = "",
    val body: String = "",
    val mood: JournalMood? = null,
    val tagsText: String = "",
    val starred: Boolean = false,
    val contentError: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
)

@HiltViewModel
class JournalEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: JournalRepository,
    private val clock: Clock,
) : ViewModel() {

    private val entryId: Long = savedStateHandle.get<Long>("entryId") ?: -1L
    private var original: JournalEntry? = null

    private val _uiState = MutableStateFlow(JournalEditUiState(isLoading = entryId > 0, isNew = entryId <= 0))
    val uiState: StateFlow<JournalEditUiState> = _uiState.asStateFlow()

    init {
        if (entryId > 0) load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val e = repository.getEntry(entryId)
                if (e == null) {
                    _uiState.update { it.copy(isLoading = false, error = "این یادداشت پیدا نشد.") }
                    return@launch
                }
                original = e
                _uiState.update {
                    it.copy(
                        isLoading = false, isNew = false, title = e.title, body = e.body, mood = e.mood,
                        tagsText = e.tags.joinToString("، "), starred = e.starred,
                    )
                }
            } catch (ex: Exception) {
                if (ex is CancellationException) throw ex
                _uiState.update { it.copy(isLoading = false, error = "خواندن یادداشت ممکن نشد.") }
            }
        }
    }

    fun onTitleChange(v: String) = _uiState.update { it.copy(title = v) }
    fun onBodyChange(v: String) = _uiState.update { it.copy(body = v, contentError = false) }
    fun onMoodChange(v: JournalMood?) = _uiState.update { it.copy(mood = if (it.mood == v) null else v) }
    fun onTagsChange(v: String) = _uiState.update { it.copy(tagsText = v) }
    fun onStarToggle() = _uiState.update { it.copy(starred = !it.starred) }

    /** [asDraft] true keeps the entry out of streaks and statistics until it is saved normally. */
    fun save(asDraft: Boolean) {
        val s = _uiState.value
        if (s.isSaving) return
        if (s.title.isBlank() && s.body.isBlank()) {
            _uiState.update { it.copy(contentError = true) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val base = original
                val now = clock.instant()
                repository.saveEntry(
                    JournalEntry(
                        id = base?.id ?: 0L,
                        title = s.title.trim(),
                        body = s.body.trim(),
                        mood = s.mood,
                        tags = parseTags(s.tagsText),
                        starred = s.starred,
                        isDraft = asDraft,
                        createdAt = base?.createdAt ?: now,
                        updatedAt = now,
                    ),
                )
                _uiState.update { it.copy(isSaving = false, done = true) }
            } catch (ex: Exception) {
                if (ex is CancellationException) throw ex
                _uiState.update { it.copy(isSaving = false, error = "ذخیره‌سازی انجام نشد.") }
            }
        }
    }

    fun delete() {
        val id = original?.id ?: return
        viewModelScope.launch {
            try {
                repository.deleteEntry(id)
                _uiState.update { it.copy(done = true) }
            } catch (ex: Exception) {
                if (ex is CancellationException) throw ex
                _uiState.update { it.copy(error = "حذف انجام نشد.") }
            }
        }
    }
}
