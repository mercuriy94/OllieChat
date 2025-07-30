package com.mercuriy94.olliechat.domain.repository.model

import com.mercuriy94.olliechat.domain.entity.model.OllieModel
import kotlinx.coroutines.flow.Flow

interface OllieModelRepository {

    suspend fun getModels(): Flow<List<OllieModel>>

    suspend fun getModelById(id: Long): OllieModel?

    suspend fun updateModel(model: OllieModel)

}
