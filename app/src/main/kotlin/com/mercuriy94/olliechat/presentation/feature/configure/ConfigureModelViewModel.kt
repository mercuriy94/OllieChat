package com.mercuriy94.olliechat.presentation.feature.configure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercuriy94.olliechat.domain.entity.model.OllieModel
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Details
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Key
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Key.MIN_P
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Key.NUM_CTX
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Key.REPEAT_PENALTY
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Key.TEMPERATURE
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Key.TOP_K
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Key.TOP_P
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Value
import com.mercuriy94.olliechat.domain.repository.model.OllieModelRepository
import com.mercuriy94.olliechat.presentation.common.model.LlmModelDetailsUi
import com.mercuriy94.olliechat.presentation.common.model.LlmModelUi
import com.mercuriy94.olliechat.presentation.feature.configure.model.ConfigureLlmModelUi
import com.mercuriy94.olliechat.presentation.feature.configure.model.EditableLlmModelParam
import com.mercuriy94.olliechat.presentation.feature.configure.model.EditableModelValue
import com.mercuriy94.olliechat.presentation.feature.configure.model.EditableModelValue.NumberRangedValue.NumberValueType
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

private const val TAG = "ConfigureModelViewModel"

internal class ConfigureModelViewModel(
    private val ollieModelRepository: OllieModelRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigureModelUiState())
    val uiState: StateFlow<ConfigureModelUiState> = _uiState.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.tag(TAG).e("Exception caught: ${throwable.message}")
    }

    init {
        getModels()
    }

    fun initialModel(id: Long?) {
        _uiState.update { state ->
            with(state) {

                val selectedConfigureLlmModelUi = selectedModel?.let {
                    availableModels.firstOrNull { model -> model.id == it.id }
                } ?: state.initialSelectedModelId?.let { modelId ->
                    availableModels.firstOrNull { it.id == modelId }
                }
                ?: availableModels.firstOrNull()

                copy(
                    initialSelectedModelId = id,
                    selectedModel = selectedConfigureLlmModelUi,
                    params = selectedConfigureLlmModelUi?.params ?: emptyList()
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getModels() {
        viewModelScope.launch(exceptionHandler) {
            ollieModelRepository.getModels()
                .mapLatest { ollieModels ->
                    ollieModels.map { ollieModel ->
                        ConfigureLlmModelUi(
                            id = ollieModel.id,
                            name = ollieModel.name,
                            digest = ollieModel.digest,
                            size = ollieModel.size,
                            details = LlmModelDetailsUi(
                                id = ollieModel.details.id,
                                parameterSize = ollieModel.details.parameterSize,
                                quantizationLevel = ollieModel.details.quantizationLevel,
                            ),
                            params = createUiParams(params = ollieModel.params)
                        )
                    }
                }
                .flowOn(Dispatchers.Default)
                .collect { ollieModels ->
                    _uiState.update { state ->
                        with(state) {
                            val selectedConfigureLlmModelUi = selectedModel
                                ?.let { ollieModels.firstOrNull { model -> model.id == it.id } }
                                ?: state.initialSelectedModelId
                                    ?.let { modelId ->
                                        ollieModels.firstOrNull { it.id == modelId }
                                    }
                                ?: ollieModels.firstOrNull()
                            copy(
                                availableModels = ollieModels,
                                selectedModel = selectedConfigureLlmModelUi,
                                params = selectedConfigureLlmModelUi?.params ?: emptyList()
                            )
                        }
                    }
                }
        }
    }

    private fun createUiParams(
        params: List<Param>,
    ): List<EditableLlmModelParam> {
        return listOf(
            EditableLlmModelParam(
                TEMPERATURE,
                "Temperature",
                EditableModelValue.NumberRangedValue(
                    value = (params.firstOrNull { it.key == TEMPERATURE }
                        ?.value as? Value.FloatValue)?.value
                        ?: 0f,
                    minValue = 0.0f,
                    maxValue = 2.0f,
                    valueType = NumberValueType.FLOAT
                ),
            ),
            EditableLlmModelParam(
                TOP_P,
                "TopP",
                EditableModelValue.NumberRangedValue(
                    value = (params.firstOrNull { it.key == TOP_P }
                        ?.value as? Value.FloatValue)?.value
                        ?: 0f,
                    minValue = 0.0f,
                    maxValue = 1.0f,
                    valueType = NumberValueType.FLOAT
                ),
            ),
            EditableLlmModelParam(
                TOP_K,
                "TopK",
                EditableModelValue.NumberRangedValue(
                    value = (params.firstOrNull { it.key == TOP_K }
                        ?.value as? Value.FloatValue)?.value
                        ?: 0f,
                    minValue = 0f,
                    maxValue = 99f,
                    valueType = NumberValueType.INT
                ),
            ),

            EditableLlmModelParam(
                REPEAT_PENALTY,
                "Repeat penalty",
                EditableModelValue.NumberRangedValue(
                    value = (params.firstOrNull { it.key == TOP_K }
                        ?.value as? Value.FloatValue)?.value
                        ?: 0f,
                    minValue = -2.0f,
                    maxValue = 2.0f,
                    valueType = NumberValueType.FLOAT
                ),
            ),

            EditableLlmModelParam(
                NUM_CTX,
                "Num_ctx",
                EditableModelValue.NumberRangedValue(
                    value = (params.firstOrNull { it.key == NUM_CTX }
                        ?.value as? Value.FloatValue)?.value
                        ?: 0f,
                    minValue = 0f,
                    maxValue = (1024 * 32).toFloat(),
                    valueType = NumberValueType.INT
                ),
            ),
        )
    }

    fun selectModel(newSelectedModel: LlmModelUi) {
        _uiState.update { state ->

            with(state) {

                val selectedConfigureLlmModelUi = availableModels.firstOrNull { model ->
                    model.id == newSelectedModel.id
                }

                copy(
                    selectedModel = newSelectedModel,
                    params = selectedConfigureLlmModelUi?.params ?: emptyList()
                )
            }
        }
    }

    fun save(params: Map<Key, Any>) {
        _uiState.value.selectedModel?.let { selectedModel ->
            viewModelScope.launch {
                val configuredParams = params.map { (key, value) ->
                    val configuredValue = when (key) {
                        TOP_P, MIN_P, TEMPERATURE, REPEAT_PENALTY -> {
                            (try {
                                value as Float
                            } catch (_: Exception) {
                                0f
                            }).let(Value::FloatValue)
                        }

                        TOP_K, NUM_CTX -> (try {
                            value as Int
                        } catch (_: Exception) {
                            0
                        }).let(Value::IntValue)

                    }
                    Param(key, configuredValue)
                }

                ollieModelRepository.updateModel(
                    OllieModel(
                        id = selectedModel.id,
                        name = selectedModel.name,
                        size = selectedModel.size,
                        digest = selectedModel.digest,
                        details = Details(
                            id = selectedModel.details.id,
                            parameterSize = selectedModel.details.parameterSize,
                            quantizationLevel = selectedModel.details.quantizationLevel,
                        ),
                        params = configuredParams
                    )
                )
            }
        }
    }
}

data class ConfigureModelUiState(
    val initialSelectedModelId: Long? = null,
    val availableModels: List<ConfigureLlmModelUi> = emptyList(),
    val selectedModel: LlmModelUi? = null,
    val params: List<EditableLlmModelParam> = emptyList(),
)
