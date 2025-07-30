package com.mercuriy94.olliechat.presentation.feature.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mercuriy94.olliechat.R
import com.mercuriy94.olliechat.presentation.common.model.LlmModelUi
import com.mercuriy94.olliechat.presentation.feature.chat.ui.component.MessageInputText
import com.mercuriy94.olliechat.presentation.feature.welcome.WelcomeNews.OpenChats
import com.mercuriy94.olliechat.presentation.feature.welcome.WelcomeNews.OpenNewChat
import com.mercuriy94.olliechat.presentation.feature.welcome.ui.WelcomeTopAppBar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun WelcomeScreen(
    viewModel: WelcomeViewModel,
    onConfigureModelClicked: (LlmModelUi) -> Unit,
    openChat: (OpenNewChat) -> Unit,
    openChats: () -> Unit,
) {

    val welcomeUiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val sendClickDebouncer = rememberDebouncer<String>(DEFAULT_DEBOUNCE_TIME) { newMessage ->
        viewModel.onNewMessage(newMessage)
    }

    LaunchedEffect(true) {
        viewModel.newsEvent.collect { event ->
            when (event) {
                is OpenChats -> openChats()
                is OpenNewChat -> openChat(event)
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            WelcomeTopAppBar(
                viewModel = viewModel,
                isConfigureModelButtonEnabled = welcomeUiState.selectedModel != null,
                onModelSelected = viewModel::selectModel,
                onConfigureModelClicked = {
                    viewModel.uiState.value.selectedModel?.let(onConfigureModelClicked)
                },
                onChatsClicked = viewModel::onChatsClicked
            )
        },
        content = { innerPadding ->
            var message by remember { mutableStateOf("Tell me a story") }
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.surface)
                    .fillMaxSize()
            ) {

                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.logo),
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "Logo icon",
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Hi, I'm Ollie",
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        ),
                        fontSize = 32.sp,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "How can I help you today?",
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.secondary,
                        ),
                        fontSize = 16.sp,
                    )

                }

                MessageInputText(
                    isEnabled = welcomeUiState.selectedModel != null,
                    message = message,
                    inProgress = welcomeUiState.inProgress,
                    placeHolder = "Ask me anything...",
                    onTextChanged = { message = it },
                    onSendClicked = {
                        keyboardController?.hide()
                        sendClickDebouncer(it)
                    },
                    onStopClicked = { },
                )
            }
        }
    )
}

const val DEFAULT_DEBOUNCE_TIME = 300L

@Composable
fun <T> rememberDebouncer(
    debounceTimeMillis: Long = DEFAULT_DEBOUNCE_TIME,
    action: (T) -> Unit,
): (T) -> Unit {
    val scope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }
    return remember(action) {
        {
            debounceJob?.cancel()
            debounceJob = scope.launch {
                delay(debounceTimeMillis)
                action(it)
            }
        }
    }
}
