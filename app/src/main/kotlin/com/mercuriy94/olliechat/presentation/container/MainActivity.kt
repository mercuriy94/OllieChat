package com.mercuriy94.olliechat.presentation.container

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.mercuriy94.olliechat.di.ViewModelProvider
import com.mercuriy94.olliechat.presentation.container.ChatScreenNavKey.ChatType.ExistedChat
import com.mercuriy94.olliechat.presentation.container.theme.OllieChatTheme
import com.mercuriy94.olliechat.presentation.feature.chat.ChatViewModel
import com.mercuriy94.olliechat.presentation.feature.chat.ui.ChatScreen
import com.mercuriy94.olliechat.presentation.feature.chats.ui.ChatsScreen
import com.mercuriy94.olliechat.presentation.feature.configure.ui.ConfigureModelScreen
import com.mercuriy94.olliechat.presentation.feature.welcome.WelcomeScreen
import com.mercuriy94.olliechat.presentation.feature.welcome.WelcomeViewModel
import com.mercuriy94.olliechat.utils.ext.creationExtras
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data object WelcomeScreenKey : NavKey

@Serializable
data class ChatScreenNavKey @OptIn(ExperimentalUuidApi::class) constructor(
    val type: ChatType,
) : NavKey {

    @Serializable
    sealed interface ChatType {

        val chatId: Long

        @Serializable
        data class NewChat(
            override val chatId: Long,
            val messageId: Long,
            val selectedModel: Long,
        ) : ChatType

        @Serializable
        data class ExistedChat(override val chatId: Long) : ChatType

    }

}

@Serializable
data object ChatsScreenNavKey : NavKey

@Serializable
data class ConfigureModelScreenNavKey(
    val selectedModel: Long,
) : NavKey

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Fix for three-button nav not properly going edge-to-edge.
            // See: https://issuetracker.google.com/issues/298296168
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            OllieChatTheme {
                val backStack = rememberNavBackStack(WelcomeScreenKey)
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryDecorators = listOf(
                        rememberSceneSetupNavEntryDecorator(),
                        rememberSavedStateNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = entryProvider {
                        entry<WelcomeScreenKey> {
                            WelcomeScreen(
                                viewModel = viewModel(factory = WelcomeViewModel.Factory),
                                onConfigureModelClicked = { model ->
                                    backStack.add(
                                        ConfigureModelScreenNavKey(selectedModel = model.id)
                                    )
                                },
                                openChat = {
                                    backStack.add(
                                        ChatScreenNavKey(
                                            ChatScreenNavKey.ChatType.NewChat(
                                                chatId = it.chatId,
                                                messageId = it.chatMessage,
                                                selectedModel = it.selectedModel,
                                            )
                                        )
                                    )
                                },
                                openChats = {
                                    backStack.add(ChatsScreenNavKey)
                                },
                            )
                        }

                        entry<ChatScreenNavKey> { key ->
                            ChatScreen(
                                viewModel = viewModel(
                                    factory = ChatViewModel.Factory,
                                    extras = creationExtras {
                                        set(ChatViewModel.CHAT_TYPE_KEY, key.type)
                                    }
                                ),
                                onConfigureModelClicked = { model ->
                                    backStack.add(
                                        ConfigureModelScreenNavKey(selectedModel = model.id)
                                    )
                                },
                                onBackClicked = {
                                    backStack.removeLastOrNull()
                                },
                            )
                        }

                        entry<ConfigureModelScreenNavKey> { key ->
                            ConfigureModelScreen(
                                initialSelectedModel = key.selectedModel,
                                viewModel(factory = ViewModelProvider.Factory),
                                onBackClicked = { backStack.removeLastOrNull() }
                            )
                        }

                        entry<ChatsScreenNavKey> {
                            ChatsScreen(
                                onBackClicked = { backStack.removeLastOrNull() },
                                navigateToChat = { id ->
                                    backStack.add(ChatScreenNavKey(ExistedChat(id)))
                                }
                            )
                        }
                    },
                )
            }
        }
    }
}
