package com.mercuriy94.olliechat.presentation.feature.welcome.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mercuriy94.olliechat.presentation.common.model.LlmModelUi
import com.mercuriy94.olliechat.presentation.common.ui.OllieChatTitle
import com.mercuriy94.olliechat.presentation.feature.chat.ui.component.ModelPickerChip
import com.mercuriy94.olliechat.presentation.feature.welcome.WelcomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WelcomeTopAppBar(
    viewModel: WelcomeViewModel,
    modifier: Modifier = Modifier,
    isConfigureModelButtonEnabled: Boolean,
    onModelSelected: (LlmModelUi) -> Unit,
    onChatsClicked: () -> Unit,
    onConfigureModelClicked: () -> Unit,
) {

    val welcomeUiState by viewModel.uiState.collectAsState()

    CenterAlignedTopAppBar(
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OllieChatTitle()

                ModelPickerChip(
                    selectedModel = welcomeUiState.selectedModel,
                    availableModels = welcomeUiState.availableModels,
                    onModelSelected = onModelSelected,
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onChatsClicked,
                modifier = Modifier.size(40.dp).clip(CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChatBubbleOutline,
                    contentDescription = "Chat & Bots",
                    modifier = Modifier.size(24.dp).offset(y = 2.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        actions = {
            FilledIconButton(
                onClick = onConfigureModelClicked,
                enabled = isConfigureModelButtonEnabled,
                modifier = Modifier.size(40.dp).offset(x = (-4).dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent,
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = "Tune Model",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = modifier,
    )
}
