package com.mercuriy94.olliechat.presentation.feature.chat.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mercuriy94.olliechat.presentation.feature.chat.model.ChatOllieMessageUi

@Composable
internal fun AssistantMessageTokenUsageStatistics(
    tokenUsageStatistics: ChatOllieMessageUi.AssistantMessage.TokenUsage,
    onDismissed: () -> Unit,
) {

    Dialog(onDismissRequest = onDismissed) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Token usage statistics",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                tokenUsageStatistics.inputTokenCount?.let { inputTokenCount ->
                    TokenUsageParam(
                        label = "Input tokens:",
                        value = inputTokenCount.toString()
                    )
                }

                tokenUsageStatistics.outputTokenCount?.let { outputTokenCount ->
                    TokenUsageParam(
                        label = "Output tokens:",
                        value = outputTokenCount.toString()
                    )
                }

                tokenUsageStatistics.totalTokenCount?.let { totalTokenCount ->
                    TokenUsageParam(
                        label = "Total tokens:",
                        value = totalTokenCount.toString()
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                ) {

                    // Ok button
                    Button(
                        onClick = { onDismissed() }
                    ) {
                        Text("Ok")
                    }
                }
            }
        }
    }

}

@Composable
fun TokenUsageParam(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium

        )
    }
}
