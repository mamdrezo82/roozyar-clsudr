package com.roozyar.app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.core.date.JalaliDate
import com.roozyar.app.core.date.toJalali
import com.roozyar.app.domain.usecase.ObserveOpenTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Error(val message: String) : DashboardUiState
    data class Success(val today: JalaliDate, val openTaskCount: Int) : DashboardUiState
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeOpenTasks: ObserveOpenTasksUseCase,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = observeOpenTasks()
        .map { tasks -> successState(tasks.size) }
        .catch { emit(DashboardUiState.Error("خواندن اطلاعات ممکن نشد.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState.Loading)

    private fun successState(openTaskCount: Int): DashboardUiState =
        DashboardUiState.Success(LocalDate.now().toJalali(), openTaskCount)
}
