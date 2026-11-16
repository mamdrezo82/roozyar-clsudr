package com.roozyar.app.feature.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.domain.repository.BackupCounts
import com.roozyar.app.domain.repository.BackupFormatException
import com.roozyar.app.domain.repository.BackupRepository
import com.roozyar.app.domain.usecase.RescheduleAllRemindersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

private const val MAX_BACKUP_CHARS = 20_000_000

data class BackupUiState(
    val busy: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    /** Set after the user picked a valid file; the restore only happens after they confirm. */
    val pendingCounts: BackupCounts? = null,
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: BackupRepository,
    private val rescheduleReminders: RescheduleAllRemindersUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    private var pendingJson: String? = null

    fun suggestedFileName(): String = "roozyar-backup-${LocalDate.now(clock)}.json"

    fun exportTo(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true, message = null) }
            try {
                val json = repository.createBackup()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter(Charsets.UTF_8)?.use { it.write(json) }
                        ?: error("no stream")
                }
                _uiState.update { it.copy(busy = false, message = "نسخه پشتیبان ذخیره شد. این فایل رمزنگاری نشده است؛ آن را جای امن نگه دارید.", isError = false) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(busy = false, message = "ذخیره نسخه پشتیبان انجام نشد.", isError = true) }
            }
        }
    }

    fun onImportPicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true, message = null) }
            try {
                val text = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: error("no stream")
                }
                if (text.length > MAX_BACKUP_CHARS) throw BackupFormatException("فایل بیش از حد بزرگ است.")
                val counts = repository.inspect(text)
                pendingJson = text
                _uiState.update { it.copy(busy = false, pendingCounts = counts) }
            } catch (e: BackupFormatException) {
                _uiState.update { it.copy(busy = false, message = e.message, isError = true) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(busy = false, message = "خواندن فایل ممکن نشد.", isError = true) }
            }
        }
    }

    fun confirmImport() {
        val json = pendingJson ?: return
        pendingJson = null
        _uiState.update { it.copy(busy = true, pendingCounts = null) }
        viewModelScope.launch {
            try {
                repository.restore(json)
                runCatching { rescheduleReminders() }
                _uiState.update { it.copy(busy = false, message = "اطلاعات با موفقیت بازیابی شد.", isError = false) }
            } catch (e: BackupFormatException) {
                _uiState.update { it.copy(busy = false, message = e.message, isError = true) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(busy = false, message = "بازیابی انجام نشد. اطلاعات فعلی دست‌نخورده مانده است.", isError = true) }
            }
        }
    }

    fun cancelImport() {
        pendingJson = null
        _uiState.update { it.copy(pendingCounts = null) }
    }
}
