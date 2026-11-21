package com.roozyar.app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.core.datastore.UserPreferences
import com.roozyar.app.domain.model.DashboardCard
import com.roozyar.app.domain.model.DashboardLayout
import com.roozyar.app.domain.model.DashboardMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardCustomizeViewModel @Inject constructor(private val prefs: UserPreferences) : ViewModel() {

    /** Null until the stored layout has been read. */
    val layout: StateFlow<DashboardLayout?> = prefs.dashboardLayout
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Choosing a mode replaces the current layout with that mode's preset. */
    fun setMode(mode: DashboardMode) = save { DashboardLayout.preset(mode) }

    fun toggle(card: DashboardCard) = update { it.toggled(card) }

    fun move(card: DashboardCard, delta: Int) = update { it.moved(card, delta) }

    fun resetToMode() = update { DashboardLayout.preset(it.mode) }

    private fun update(change: (DashboardLayout) -> DashboardLayout) {
        val current = layout.value ?: return
        save { change(current) }
    }

    private fun save(build: () -> DashboardLayout) {
        viewModelScope.launch { prefs.setDashboardLayout(build()) }
    }
}
