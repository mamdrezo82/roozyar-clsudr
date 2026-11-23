package com.roozyar.app.feature.ai

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.design.RoozYarCard
import com.roozyar.app.core.design.RoozYarTopBar

@Composable
fun AssistantScreen(onBack: () -> Unit, viewModel: AssistantViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.size - 1)
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        RoozYarTopBar("دستیار روزیار", onBack = onBack)
        Surface(
            modifier = Modifier.padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            color = scheme.secondary.copy(alpha = 0.12f),
        ) {
            Text(
                "موتور: قواعد ساده (بدون مدل زبانی). فقط فرمان‌های مشخص را می‌فهمد؛ هر تغییری فقط با تأیید شما انجام می‌شود. دستیار با مدل زبانی محلی بعداً اضافه می‌شود.",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.secondary,
            )
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.messages.isEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("چه کاری برایت انجام بدهم؟ مثلاً:", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistantViewModel.examples.forEach { ex ->
                                AssistChip(onClick = { viewModel.send(ex) }, label = { Text(ex) })
                            }
                        }
                    }
                }
            }
            items(state.messages, key = { it.id }) { m -> Bubble(m, viewModel) }
        }
        Surface(color = scheme.surfaceContainerLow, tonalElevation = 2.dp) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = state.input,
                    onValueChange = viewModel::onInput,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("فرمان را بنویسید…") },
                    shape = RoundedCornerShape(50),
                    maxLines = 3,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = scheme.surfaceContainerHigh,
                        unfocusedContainerColor = scheme.surfaceContainerHigh,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                )
                IconButton(onClick = { viewModel.send() }, enabled = state.input.isNotBlank() && !state.busy) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "ارسال", tint = scheme.primary)
                }
            }
        }
    }
}

@Composable
private fun Bubble(m: ChatMessage, viewModel: AssistantViewModel) {
    val scheme = MaterialTheme.colorScheme
    // RTL: the start side is the right; the user's messages sit there.
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.fromUser) Arrangement.Start else Arrangement.End) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (m.fromUser) scheme.primaryContainer else scheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth(0.85f),
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    m.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (m.fromUser) scheme.onPrimaryContainer else scheme.onSurface,
                )
                m.pending?.let { p ->
                    RoozYarCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(p.actionTitle, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                            Text(p.summary, style = MaterialTheme.typography.titleSmall)
                            if (!m.resolved) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.approve(m.id) },
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(containerColor = scheme.primaryContainer, contentColor = scheme.onPrimaryContainer),
                                    ) { Text("تأیید") }
                                    OutlinedButton(onClick = { viewModel.cancel(m.id) }, shape = RoundedCornerShape(50)) { Text("لغو") }
                                }
                            } else {
                                Text("پاسخ داده شد", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
