package com.mercuriy94.olliechat.presentation.feature.chats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mercuriy94.olliechat.di.ViewModelProvider
import com.mercuriy94.olliechat.presentation.feature.chats.ChatsNews
import com.mercuriy94.olliechat.presentation.feature.chats.ChatsViewModel
import com.mercuriy94.olliechat.presentation.feature.chats.ui.components.ChatsTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatsScreen(
    viewModel: ChatsViewModel = viewModel(factory = ViewModelProvider.Factory),
    onBackClicked: () -> Unit,
    navigateToChat: (Long) -> Unit,
) {

    val chatsUiState by viewModel.uiState.collectAsState()

    val listState = rememberLazyListState()

    val scrollBehavior = if (listState.canScrollForward || listState.canScrollBackward) {
        TopAppBarDefaults.enterAlwaysScrollBehavior(state = rememberTopAppBarState())
    } else {
        TopAppBarDefaults.pinnedScrollBehavior(state = rememberTopAppBarState())
    }
    val focusManager = LocalFocusManager.current

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y > 0) {
                    focusManager.clearFocus()
                }
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(true) {
        viewModel.newsEvent.collect { event ->
            when (event) {
                is ChatsNews.OpenChat -> navigateToChat(event.chatId)
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ChatsTopAppBar(
                scrollBehavior = scrollBehavior,
                onBackClicked = onBackClicked
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.surface)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                        .nestedScroll(nestedScrollConnection),
                    state = listState,
                ) {
                    items(chatsUiState.chats) { chat ->
                        Row(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .clickable { viewModel.chatClicked(chat.id) }
                                .padding(16.dp)
                        ) {
                            Text(text = chat.title)
                        }
                    }
                }
            }
        }
    )

}
