package com.mercuriy94.olliechat.presentation.feature.chat.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import com.mercuriy94.olliechat.presentation.common.model.LlmModelUi

@Composable
internal fun ModelPicker(
    selectedModel: LlmModelUi?,
    availableModels: List<LlmModelUi>,
    onModelSelected: (LlmModelUi) -> Unit,
) {

    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Available models",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        LazyColumn(
            modifier = Modifier.padding(vertical = 16.dp)
        ) {

            items(availableModels) { model ->

                val isSelected = selectedModel != null && model == selectedModel

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.surfaceContainer
                            } else {
                                Color.Transparent
                            }
                        )
                        .clickable { onModelSelected(model) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {

                    Column(modifier = Modifier.weight(1f)) {

                        Text(
                            text = model.name,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row {

                            Text(
                                text = model.details.parameterSize,
                                style = MaterialTheme.typography.labelSmall,
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = model.details.quantizationLevel,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }

                    if (isSelected) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            modifier = Modifier.size(20.dp),
                            contentDescription = ""
                        )
                    }
                }

            }

        }

    }
}
