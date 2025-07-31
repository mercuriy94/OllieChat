package com.mercuriy94.olliechat.presentation.feature.configure.model

import com.mercuriy94.olliechat.domain.entity.model.OllieModel
import com.mercuriy94.olliechat.presentation.common.model.LlmModelDetailsUi
import com.mercuriy94.olliechat.presentation.common.model.LlmModelUi

data class ConfigureLlmModelUi(
    override val id: Long,
    override val name: String,
    override val size: Long,
    override val digest: String,
    override val details: LlmModelDetailsUi,
    val params: List<EditableLlmModelParam>
) : LlmModelUi

data class EditableLlmModelParam(
    val key: OllieModel.Param.Key,
    val name: String,
    val value: EditableModelValue,
)

sealed interface EditableModelValue {

    data class NumberRangedValue(
        val value: Float,
        val minValue: Float,
        val maxValue: Float,
        val valueType: NumberValueType,
    ) : EditableModelValue {

        enum class NumberValueType {
            INT,
            FLOAT
        }
    }

    data class StringValue(val value: String) : EditableModelValue

}
