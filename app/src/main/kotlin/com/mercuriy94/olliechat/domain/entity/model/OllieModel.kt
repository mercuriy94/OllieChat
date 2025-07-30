package com.mercuriy94.olliechat.domain.entity.model

data class OllieModel(
    val id: Long,
    val name: String,
    val size: Long,
    val details: Details,
    val digest: String,
    val params: List<Param> = emptyList(),
) {

    data class Details(
        val id: Long,
        val parameterSize: String,
        val quantizationLevel: String,
    )

    data class Param(
        val key: Key,
        val value: Value,
    ) {

        enum class Key {
            TOP_K,
            TOP_P,
            MIN_P,
            TEMPERATURE,
            NUM_CTX,
            REPEAT_PENALTY,
        }

        sealed interface Value {
            data class IntValue(val value: Int) : Value
            data class FloatValue(val value: Float) : Value
            data class StringValue(val value: String) : Value
        }
    }
}
