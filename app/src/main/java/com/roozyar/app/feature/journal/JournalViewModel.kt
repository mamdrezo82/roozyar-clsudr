package com.roozyar.app.feature.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.domain.model.JournalEntry
import com.roozyar.app.domain.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class JournalUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val entries: List<JournalEntry> = emptyList(),
    val hasAny: Boolean = false,
    val stats: JournalStats = JournalStats(0, 0, 0),
    val query: String = "",
    val filter: JournalFilter = JournalFilter.ALL,
    val zone: ZoneId = ZoneId.systemDefault(),
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repository: JournalRepository,
    private val clock: Clock,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(JournalFilter.ALL)

    val uiState: StateFlow<JournalUiState> = combine(repository.observeEntries(), query, filter) { all, q, f ->
        JournalUiState(
            isLoading = false,
            entries = filterEntries(all, q, f),
            hasAny = all.isNotEmpty(),
            stats = computeStats(all, LocalDate.now(clock), clock.zone),
            query = q,
            filter = f,
            zone = clock.zone,
        )
    }
        .catch { emit(JournalUiState(isLoading = false, error = "خواندن یادداشت‌ها ممکن نشد.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JournalUiState())

    fun onQueryChange(v: String) = query.update { v }
    fun onFilterChange(v: JournalFilter) = filter.update { v }

    fun toggleStar(entry: JournalEntry) {
        viewModelScope.launch { repository.setStarred(entry.id, !entry.starred) }
    }
}
