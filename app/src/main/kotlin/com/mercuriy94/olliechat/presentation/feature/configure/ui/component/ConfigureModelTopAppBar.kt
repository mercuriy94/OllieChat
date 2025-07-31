package com.mercuriy94.olliechat.presentation.feature.configure.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mercuriy94.olliechat.presentation.common.model.LlmModelUi
import com.mercuriy94.olliechat.presentation.common.ui.OllieChatTitle
import com.mercuriy94.olliechat.presentation.feature.chat.ui.component.ModelPickerChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConfigureModelTopAppBar(
    modifier: Modifier = Modifier,
    selectedModel: LlmModelUi?,
    availableModels: List<LlmModelUi>,
    onModelSelected: (LlmModelUi) -> Unit,
    onBackClicked: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {

    CenterAlignedTopAppBar(
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OllieChatTitle()
                ModelPickerChip(
                    selectedModel = selectedModel,
                    availableModels = availableModels,
                    onModelSelected = onModelSelected,
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClicked,
                modifier = Modifier.size(40.dp).clip(CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
        ),
        scrollBehavior = scrollBehavior,
        modifier = modifier,
    )
}
