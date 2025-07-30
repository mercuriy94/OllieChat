package com.mercuriy94.olliechat.presentation.feature.chat.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mercuriy94.olliechat.presentation.common.model.LlmModelUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ModelPickerChip(
    selectedModel: LlmModelUi?,
    availableModels: List<LlmModelUi>,
    onModelSelected: (LlmModelUi) -> Unit,
) {
    var showModelPicker by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable { showModelPicker = true }
            .padding(start = 8.dp, end = 2.dp, top = 4.dp, bottom = 4.dp)

    ) {
        Text(
            text = selectedModel?.name ?: "Choose model",
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
        )
        Icon(
            imageVector = Icons.Rounded.ArrowDropDown,
            modifier = Modifier.size(20.dp),
            contentDescription = "",
        )
    }

    if (showModelPicker) {
        ModalBottomSheet(
            modifier = Modifier.statusBarsPadding(),
            onDismissRequest = { showModelPicker = false },
            sheetState = bottomSheetState,
        ) {
            ModelPicker(
                selectedModel = selectedModel,
                availableModels = availableModels,
                onModelSelected = { selectedModel ->
                    scope.launch(Dispatchers.Default) {
                        bottomSheetState.hide()
                        showModelPicker = false
                    }
                    onModelSelected(selectedModel)
                }
            )
        }
    }
}
