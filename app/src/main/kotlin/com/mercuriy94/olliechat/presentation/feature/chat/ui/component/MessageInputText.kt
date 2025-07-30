package com.mercuriy94.olliechat.presentation.feature.chat.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MessageInputText(
    isEnabled: Boolean,
    inProgress: Boolean,
    modifier: Modifier = Modifier,
    message: String,
    onSendClicked: (String) -> Unit,
    onStopClicked: () -> Unit,
    onTextChanged: (String) -> Unit,
    placeHolder: String,
) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 1.dp, bottom = 8.dp)
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(28.dp)
                ),
            shape = RoundedCornerShape(28.dp),
            color = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                TextField(
                    value = message,
                    minLines = 1,
                    enabled = isEnabled,
                    onValueChange = onTextChanged,
                    colors =
                        TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = modifier.fillMaxWidth()
                        .heightIn(max = 150.dp),
                    placeholder = { Text(placeHolder) },
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    FilledIconButton(
                        modifier = Modifier.size(44.dp),
                        onClick = { },
                        enabled = !inProgress,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent,
                        )
                    ) {
                        Icon(
                            modifier = Modifier.size(28.dp),
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Attach file",
                        )
                    }

                    if (inProgress) {
                        FilledIconButton(
                            onClick = onStopClicked,
                            modifier = Modifier.size(44.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Stop,
                                contentDescription = "Stop",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    } else {
                        FilledIconButton(
                            enabled = isEnabled && message.isNotBlank(),
                            onClick = { onSendClicked(message) },
                            modifier = Modifier.size(44.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Send,
                                contentDescription = "Send Message",
                                modifier = Modifier.size(28.dp).offset(x = 3.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                }
            }
        }
    }
}
