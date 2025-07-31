package com.mercuriy94.olliechat.presentation.feature.chat.ui

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.mercuriy94.olliechat.presentation.common.model.LlmModelUi
import com.mercuriy94.olliechat.presentation.feature.chat.ChatNews
import com.mercuriy94.olliechat.presentation.feature.chat.ChatViewModel
import com.mercuriy94.olliechat.presentation.feature.chat.model.ChatOllieMessageUi
import com.mercuriy94.olliechat.presentation.feature.chat.model.ChatOllieMessageUi.AssistantMessage
import com.mercuriy94.olliechat.presentation.feature.chat.ui.component.AssistantMessage
import com.mercuriy94.olliechat.presentation.feature.chat.ui.component.AssistantMessageTokenUsageStatistics
import com.mercuriy94.olliechat.presentation.feature.chat.ui.component.ChatTopAppBar
import com.mercuriy94.olliechat.presentation.feature.chat.ui.component.MessageInputText
import com.mercuriy94.olliechat.presentation.feature.chat.ui.component.UserMessageBubble
import com.mercuriy94.olliechat.presentation.feature.welcome.DEFAULT_DEBOUNCE_TIME
import com.mercuriy94.olliechat.presentation.feature.welcome.rememberDebouncer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ChatScreen(
    viewModel: ChatViewModel,
    onConfigureModelClicked: (LlmModelUi) -> Unit,
    onBackClicked: () -> Unit,
) {
    val modelManagerUiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    val scrollBehavior = if (listState.canScrollForward || listState.canScrollBackward) {
        TopAppBarDefaults.enterAlwaysScrollBehavior(state = rememberTopAppBarState())
    } else {
        TopAppBarDefaults.pinnedScrollBehavior(state = rememberTopAppBarState())
    }

    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var showTokenUsageDialog by remember { mutableStateOf<AssistantMessage.TokenUsage?>(null) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y > 0) {
                    focusManager.clearFocus()
                }
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(true) {
        viewModel.newsEvent.collect { event ->
            when (event) {
                is ChatNews.CopyMessage -> {
                    scope.launch {
                        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(event.label, event.text)))
                    }
                }

                is ChatNews.ShowTokenUsageStatistics -> showTokenUsageDialog = event.tokenUsage
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ChatTopAppBar(
                viewModel = viewModel,
                scrollBehavior = scrollBehavior,
                isConfigureModelButtonEnabled = modelManagerUiState.selectedModel != null,
                onModelSelected = viewModel::selectModel,
                onConfigureModelClicked = {
                    viewModel.uiState.value.selectedModel?.let(onConfigureModelClicked)
                },
                onBackClicked = onBackClicked
            )
        },
        content = { innerPadding ->
            var message by remember { mutableStateOf("") }
            val sendClickDebouncer = rememberDebouncer<String>(DEFAULT_DEBOUNCE_TIME) { newMessage ->
                viewModel.send(newMessage)
                message = ""
            }
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.surface)
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                        .padding(horizontal = 16.dp, vertical = 0.dp)
                        .nestedScroll(nestedScrollConnection),
                    state = listState,
                ) {
                    items(
                        items = modelManagerUiState.messages,
                        key = { item -> item.id }) { message ->
                        when (message) {
                            is AssistantMessage -> {
                                AssistantMessage(
                                    message = message,
                                    onActionClick = { action ->
                                        viewModel.onAssistantMessageActionClick(
                                            message = message,
                                            action = action,
                                        )
                                    }
                                )
                            }

                            is ChatOllieMessageUi.UserMessage -> UserMessageBubble(
                                message = message,
                                onActionClick = { action ->
                                    viewModel.onUserMessageActionClick(
                                        message = message,
                                        action = action,
                                    )
                                }
                            )
                        }
                    }
                }

                MessageInputText(
                    isEnabled = modelManagerUiState.selectedModel != null,
                    message = message,
                    inProgress = modelManagerUiState.inProgress,
                    onTextChanged = { message = it },
                    placeHolder = "How can I help you?",
                    onSendClicked = { newMessage ->
                        keyboardController?.hide()
                        sendClickDebouncer(newMessage)
                    },
                    onStopClicked = {
                        viewModel.stopCurrentStreamingChat()
                    },
                )
            }

            val tokenUsage = showTokenUsageDialog
            if (tokenUsage != null) {
                AssistantMessageTokenUsageStatistics(
                    tokenUsageStatistics = tokenUsage,
                    onDismissed = {
                        showTokenUsageDialog = null
                    })
            }
        }
    )
}

