package com.mercuriy94.olliechat.data.langchain4j

import com.mercuriy94.olliechat.data.repository.chat.memory.OllieChatMemoryId
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.model.output.structured.Description
import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.TokenStream
import dev.langchain4j.service.UserMessage
import dev.langchain4j.service.V

internal interface ChatAssistant {

    fun chat(@MemoryId chatMemoryId: OllieChatMemoryId, @UserMessage text: String): TokenStream

}

internal interface TitleGenerationAssistant {

    companion object {

        const val DEFAULT_TITLE_GENERATION_PROMPT = """### Task:
            Generate a concise, 3-5 word title with an emoji summarizing the chat history.
            ### Guidelines:
            - The title should clearly represent the main theme or subject of the conversation.
            - Use emojis that enhance understanding of the topic, but avoid quotation marks or special formatting.
            - Write the title in the chat's primary language; default to English if multilingual.
            - Prioritize accuracy over excessive creativity; keep it clear and simple.
            ### Output:
            JSON format: { "title": "your concise title here" }
            ### Examples:
            - { "title": "📉 Stock Market Trends" },
            - { "title": "🍪 Perfect Chocolate Chip Recipe" },
            - { "title": "Evolution of Music Streaming" },
            - { "title": "Remote Work Productivity Tips" },
            - { "title": "Artificial Intelligence in Healthcare" },
            - { "title": "🎮 Video Game Development Insights" }
            ### Chat History:
            <chat_history>
            {{chat_history}}
            </chat_history>"""
    }

    @UserMessage(DEFAULT_TITLE_GENERATION_PROMPT)
    fun generateTitle(
        @V("chat_history") messages: List<ChatMessage>,
    ): Title

    data class Title(
        @Description("title")
        val title: String? = null,
    )

}
