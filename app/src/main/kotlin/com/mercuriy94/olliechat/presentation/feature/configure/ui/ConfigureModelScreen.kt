package com.mercuriy94.olliechat.presentation.feature.configure.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mercuriy94.olliechat.di.ViewModelProvider
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Key
import com.mercuriy94.olliechat.presentation.feature.configure.ConfigureModelViewModel
import com.mercuriy94.olliechat.presentation.feature.configure.model.EditableModelValue.NumberRangedValue
import com.mercuriy94.olliechat.presentation.feature.configure.model.EditableModelValue.NumberRangedValue.NumberValueType
import com.mercuriy94.olliechat.presentation.feature.configure.model.EditableModelValue.StringValue
import com.mercuriy94.olliechat.presentation.feature.configure.ui.component.ConfigureModelTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConfigureModelScreen(
    initialSelectedModel: Long? = null,
    viewModel: ConfigureModelViewModel = viewModel(factory = ViewModelProvider.Factory),
    onBackClicked: () -> Unit,
    ) {

    val configureModelUiState by viewModel.uiState.collectAsState()

    var isFirstLaunched by rememberSaveable { mutableStateOf(true) }

    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    val scrollBehavior = if (listState.canScrollForward || listState.canScrollBackward) {
        TopAppBarDefaults.enterAlwaysScrollBehavior(state = rememberTopAppBarState())
    } else {
        TopAppBarDefaults.pinnedScrollBehavior(state = rememberTopAppBarState())
    }

//    val isSaveButtonEnabled = remember { mutableStateOf(false) }

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

    val values: SnapshotStateMap<Key, Any> = remember {
        mutableStateMapOf<Key, Any>()
    }

    LaunchedEffect(isFirstLaunched) {
        if (isFirstLaunched) {
            viewModel.initialModel(initialSelectedModel)
            isFirstLaunched = true
        }
    }

    LaunchedEffect(configureModelUiState.params) {
        values.clear()
        configureModelUiState.params.forEach {
            values[it.key] = when (it.value) {
                is NumberRangedValue -> it.value.value
                is StringValue -> it.value.value
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ConfigureModelTopAppBar(
                selectedModel = configureModelUiState.selectedModel,
                availableModels = configureModelUiState.availableModels,
                scrollBehavior = scrollBehavior,
                onModelSelected = viewModel::selectModel,
                onBackClicked = onBackClicked,

            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding).padding(top = 16.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                        .padding(horizontal = 16.dp, vertical = 0.dp)
                        .nestedScroll(nestedScrollConnection),
                    state = listState,
                ) {
                    items(configureModelUiState.params) { param ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(param.name, style = MaterialTheme.typography.titleSmall)
                            when (val editableValue = param.value) {
                                is NumberRangedValue -> {
                                    SliderNumberRangeModelParam(
                                        paramKey = param.key,
                                        value = editableValue,
                                        values = values
                                    )
                                }

                                is StringValue -> {

                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.navigationBarsPadding()
                    .imePadding()
                    .padding(bottom = 16.dp),
                elevation = FloatingActionButtonDefaults.elevation(),
                onClick = {
                    viewModel.save(values)
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Save,
                        contentDescription = "Save",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Save & Update")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.EndOverlay
    )
}


@Composable
fun SliderNumberRangeModelParam(
    paramKey: Key,
    value: NumberRangedValue,
    values: SnapshotStateMap<Key, Any>,
) {

    val sliderValue = try {
        values[paramKey] as Float
    } catch (_: Exception) {
        0f
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        var isFocused by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }

        Slider(
            modifier = Modifier.height(32.dp).weight(1f),
            value = sliderValue,
            valueRange = value.minValue..value.maxValue,
            onValueChange = { newValue -> values[paramKey] = newValue },
        )

        Spacer(modifier = Modifier.width(8.dp))

        val textFieldValue = when (value.valueType) {
            NumberValueType.INT -> "${sliderValue.toInt()}"
            NumberValueType.FLOAT -> "%.2f".format(sliderValue)
        }

        BasicTextField(
            value = textFieldValue,
            modifier = Modifier.width(80.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { isFocused = it.isFocused },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = {
                val newValue = it.toFloatOrNull() ?: value.minValue
                values[paramKey] = newValue
            },
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        ) { innerTextField ->
            Box(
                modifier =
                    Modifier.border(
                        width = if (isFocused) 2.dp else 1.dp,
                        color =
                            if (isFocused) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(4.dp),
                    )
            ) {
                Box(modifier = Modifier.padding(8.dp)) { innerTextField() }
            }
        }
    }
}
