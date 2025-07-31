package com.mercuriy94.olliechat.presentation.feature.chat.ui.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.mercuriy94.olliechat.presentation.feature.chat.model.ChatOllieMessageUi

@Composable
internal fun UserMessageBubble(
    message: ChatOllieMessageUi.UserMessage,
    onActionClick: (action: ChatOllieMessageUi.Action) -> Unit,
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    var isContextMenuVisible by rememberSaveable { mutableStateOf(false) }
    var pressOffset by remember { mutableStateOf(DpOffset.Zero) }
    var itemHeight by remember { mutableStateOf(0.dp) }
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(start = 40.dp)
            .onSizeChanged { itemHeight = with(density) { it.height.toDp() } },
        horizontalAlignment = Alignment.End
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 0.dp,
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp
                    )
                )
                .background(MaterialTheme.colorScheme.primaryContainer)
                .indication(interactionSource, LocalIndication.current)
                .pointerInput(true) {
                    detectTapGestures(onLongPress = { offset ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isContextMenuVisible = true
                        pressOffset = DpOffset(x = offset.x.dp, y = offset.y.dp)
                    })
                }
                .padding(all = 12.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                MarkdownText(text = message.text, smallFontSize = false)
            }

            DropdownMenu(
                expanded = isContextMenuVisible,
                onDismissRequest = { isContextMenuVisible = false },
                modifier = Modifier.widthIn(min = 160.dp).padding(0.dp),
                shape = RoundedCornerShape(12.dp),
                offset = pressOffset.copy(y = pressOffset.y - itemHeight)
            ) {
                message.actions.forEach { action ->
                    DropdownMenuItem(
                        text = { Text(action.text) },
                        leadingIcon = {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.contentDescription,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        onClick = {
                            onActionClick(action)
                            isContextMenuVisible = false
                        }
                    )
                }
            }
        }
    }
}
