package com.mercuriy94.olliechat.di

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mercuriy94.olliechat.presentation.feature.chats.ChatsViewModel
import com.mercuriy94.olliechat.presentation.feature.configure.ConfigureModelViewModel

object ViewModelProvider {

    val Factory = viewModelFactory {

        initializer {
            ConfigureModelViewModel(
                ollieModelRepository = OllieChatDataModule.ollieModelRepository
            )
        }

        initializer {
            ChatsViewModel(
                coroutineDispatchers = CoroutineDispatchersModule.coroutineDispatchers,
                chatRepository = OllieChatDataModule.ollieChatRepository
            )
        }
    }
}
