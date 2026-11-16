package com.roozyar.app.feature.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.core.date.JalaliDate
import com.roozyar.app.core.date.toJalali
import com.roozyar.app.domain.model.Event
import com.roozyar.app.domain.model.EventCategory
import com.roozyar.app.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class EventEditUiState(
    val isLoading: Boolean = true,
    val isNew: Boolean = true,
    val title: String = "",
    val date: JalaliDate = LocalDate.now().toJalali(),
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val category: EventCategory = EventCategory.PERSONAL,
    val location: String = "",
    val titleError: Boolean = false,
    val timeError: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
)

@HiltViewModel
class EventEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: EventRepository,
    private val clock: Clock,
) : ViewModel() {

    private val eventId: Long = savedStateHandle.get<Long>("eventId") ?: -1L
    private val initialEpochDay: Long = savedStateHandle.get<Long>("date") ?: -1L
    private var original: Event? = null

    val today: JalaliDate get() = LocalDate.now(clock).toJalali()

    private val _uiState = MutableStateFlow(
        EventEditUiState(
            isLoading = eventId > 0,
            isNew = eventId <= 0,
            date = (if (initialEpochDay >= 0) LocalDate.ofEpochDay(initialEpochDay) else LocalDate.now(clock)).toJalali(),
        ),
    )
    val uiState: StateFlow<EventEditUiState> = _uiState.asStateFlow()

    init {
        if (eventId > 0) load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val e = repository.getEvent(eventId)
                if (e == null) {
                    _uiState.update { it.copy(isLoading = false, error = "این رویداد پیدا نشد.") }
                    return@launch
                }
                original = e
                val start = e.start.atZone(clock.zone)
                _uiState.update {
                    it.copy(
                        isLoading = false, isNew = false, title = e.title,
                        date = start.toLocalDate().toJalali(),
                        startTime = if (e.hasTime) start.toLocalTime() else null,
                        endTime = if (e.hasTime) e.end?.atZone(clock.zone)?.toLocalTime() else null,
                        category = e.category, location = e.location,
                    )
                }
            } catch (ex: Exception) {
                if (ex is CancellationException) throw ex
                _uiState.update { it.copy(isLoading = false, error = "خواندن رویداد ممکن نشد.") }
            }
        }
    }

    fun onTitleChange(v: String) = _uiState.update { it.copy(title = v, titleError = false) }
    fun onDateChange(v: JalaliDate) = _uiState.update { it.copy(date = v) }
    fun onCategoryChange(v: EventCategory) = _uiState.update { it.copy(category = v) }
    fun onLocationChange(v: String) = _uiState.update { it.copy(location = v) }

    /** Clearing the start time also clears the end time: an event without a start is all-day. */
    fun onStartTimeChange(v: LocalTime?) = _uiState.update { it.copy(startTime = v, endTime = if (v == null) null else it.endTime, timeError = false) }
    fun onEndTimeChange(v: LocalTime?) = _uiState.update { it.copy(endTime = v, timeError = false) }

    fun save() {
        val s = _uiState.value
        if (s.isSaving) return
        val titleBad = s.title.isBlank()
        val timeBad = s.startTime != null && s.endTime != null && !s.endTime.isAfter(s.startTime)
        if (titleBad || timeBad) {
            _uiState.update { it.copy(titleError = titleBad, timeError = timeBad) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val day = s.date.toLocalDate()
                val start = day.atTime(s.startTime ?: LocalTime.MIDNIGHT).atZone(clock.zone).toInstant()
                val end = if (s.startTime != null && s.endTime != null) day.atTime(s.endTime).atZone(clock.zone).toInstant() else null
                repository.saveEvent(
                    Event(
                        id = original?.id ?: 0L,
                        title = s.title.trim(),
                        start = start,
                        end = end,
                        hasTime = s.startTime != null,
                        category = s.category,
                        location = s.location.trim(),
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
                repository.deleteEvent(id)
                _uiState.update { it.copy(done = true) }
            } catch (ex: Exception) {
                if (ex is CancellationException) throw ex
                _uiState.update { it.copy(error = "حذف انجام نشد.") }
            }
        }
    }
}
