package com.mercuriy94.olliechat.presentation.feature.chat.model

import com.mercuriy94.olliechat.presentation.common.model.LlmModelDetailsUi
import com.mercuriy94.olliechat.presentation.common.model.LlmModelUi

internal data class ChatOllieModelUi(
    override val id: Long,
    override val name: String,
    override val size: Long,
    override val digest: String,
    override val details: LlmModelDetailsUi,
) : LlmModelUi
