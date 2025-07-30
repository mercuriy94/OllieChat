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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mercuriy94.olliechat.presentation.feature.chat.model.ChatOllieMessageUi

@Composable
internal fun AssistantMessage(
    message: ChatOllieMessageUi.AssistantMessage,
    onActionClick: (action: ChatOllieMessageUi.Action) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {

        Text(
            text = message.aiModel.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (!message.think.isNullOrBlank()) {
            AssistantThinkMessage(
                thinkText = message.think,
                isCompletedThinking = !message.text.isNullOrBlank()
            )
        }

        if (message.text != null) {
            SelectionContainer {
                MarkdownText(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = message.text,
                    smallFontSize = false
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            items(
                items = message.actions,
                key = { item -> item.id }) { action ->
                AssistantMessageAction(
                    onClick = { onActionClick(action) },
                    imageVector = action.icon,
                    contentDescription = action.contentDescription,
                )
            }
        }
    }
}

@Composable
fun AssistantMessageAction(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = imageVector,
            contentDescription = contentDescription,
        )
    }
}

@Composable
private fun AssistantThinkMessage(thinkText: String, isCompletedThinking: Boolean) {

    var expandThinking by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.clip(RoundedCornerShape(24.dp))
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .clickable { expandThinking = !expandThinking }
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            val arrowRotateAngles = if (expandThinking) 90f else 270f
            Text(
                text = if (isCompletedThinking) "Thinking completed" else "Thinking ...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Rounded.ArrowBackIosNew,
                modifier = Modifier.size(16.dp).rotate(arrowRotateAngles),
                contentDescription = "",
            )
        }

        if (expandThinking) {
            SelectionContainer {
                MarkdownText(
                    modifier = Modifier.weight(1f)
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp, top = 4.dp),
                    text = thinkText,
                    smallFontSize = false
                )
            }
        }
    }

}
