package com.mercuriy94.olliechat.presentation.common.model

interface LlmModelUi {

    val id: Long
    val name: String
    val size: Long
    val digest: String
    val details: LlmModelDetailsUi

}

data class LlmModelDetailsUi(
    val id: Long,
    val parameterSize: String,
    val quantizationLevel: String,
)
