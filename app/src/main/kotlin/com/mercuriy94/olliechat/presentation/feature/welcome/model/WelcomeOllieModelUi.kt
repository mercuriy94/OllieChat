package com.mercuriy94.olliechat.presentation.feature.welcome.model

import com.mercuriy94.olliechat.presentation.common.model.LlmModelDetailsUi
import com.mercuriy94.olliechat.presentation.common.model.LlmModelUi

data class WelcomeOllieModelUi(
    override val id: Long,
    override val name: String,
    override val size: Long,
    override val digest: String,
    override val details: LlmModelDetailsUi,
) : LlmModelUi
