package com.mercuriy94.olliechat.presentation.feature.chat.ui.component


import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.CodeBlockStyle
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.material3.RichText
import com.halilibo.richtext.ui.string.RichTextStringStyle

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    smallFontSize: Boolean = false,
) {
    val fontSize = if (smallFontSize) {
        MaterialTheme.typography.bodyMedium.fontSize
    } else {
        MaterialTheme.typography.bodyLarge.fontSize
    }
    CompositionLocalProvider {
        ProvideTextStyle(value = TextStyle(fontSize = fontSize, lineHeight = fontSize * 1.3)) {
            RichText(
                modifier = modifier,
                style =
                    RichTextStyle(
                        codeBlockStyle =
                            CodeBlockStyle(
                                textStyle =
                                    TextStyle(
                                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.tertiary,
                                    ),
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = 12.dp,
                                            bottomEnd = 12.dp
                                        )
                                    ).background(MaterialTheme.colorScheme.tertiaryContainer)
                            ),
                        stringStyle =
                            RichTextStringStyle(
                                linkStyle = TextLinkStyles(style = SpanStyle(color = Color.Blue))
                            ),
                    ),
            ) {
                Markdown(content = text)
            }
        }
    }
}
