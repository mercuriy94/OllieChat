package com.mercuriy94.olliechat.data.repository.model

import com.mercuriy94.olliechat.data.db.dao.OllieModelDao
import com.mercuriy94.olliechat.data.db.model.OllieModelDbEntityWithRelationships
import com.mercuriy94.olliechat.data.mapper.model.OllieModelApiToDbEntityWithRelationshipsMapper
import com.mercuriy94.olliechat.data.mapper.model.OllieModelDbToDomainEntityMapper
import com.mercuriy94.olliechat.data.mapper.model.OllieModelDomainToDbEntityWithRelationshipsMapper
import com.mercuriy94.olliechat.data.repository.config.AiModelsParamsConfigRepositoryImpl
import com.mercuriy94.olliechat.domain.entity.model.OllieModel
import com.mercuriy94.olliechat.domain.repository.model.OllieModelRepository
import com.mercuriy94.olliechat.utils.CoroutineDispatchers
import com.mercuriy94.olliechat.utils.ext.runSuspendCatching
import dev.langchain4j.model.ollama.OllamaModel
import dev.langchain4j.model.ollama.OllamaModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import timber.log.Timber

private const val TAG = "OllieModelRepositoryImpl"

@Suppress("LongParameterList")
internal class OllieModelRepositoryImpl(
    private val dispatchers: CoroutineDispatchers,
    private val ollamaModels: OllamaModels,
    private val ollieModelDao: OllieModelDao,
    private val dbToDomainEntityMapper: OllieModelDbToDomainEntityMapper,
    private val domainToDbEntityMapper: OllieModelDomainToDbEntityWithRelationshipsMapper,
    private val apiToDbEntityMapper: OllieModelApiToDbEntityWithRelationshipsMapper,
    private val modelsParamsConfigRepository: AiModelsParamsConfigRepositoryImpl,
) : OllieModelRepository {

    override suspend fun getModels(): Flow<List<OllieModel>> = flow {
        val cachedModels = runSuspendCatching { getFromDb() }
            .onFailure { Timber.tag(TAG).e(it, "Couldn't get old cached models from db") }
            .getOrThrow()

        emit(dbToDomainEntityMapper(cachedModels))

        val modelsParams = runSuspendCatching { modelsParamsConfigRepository.getConfig() }
            .onFailure { Timber.tag(TAG).e(it, "Couldn't get models params from config") }
            .getOrNull()

        val newDbModels = runSuspendCatching {
            mergeModels(
                oldModels = cachedModels,
                newModels = apiToDbEntityMapper(
                    apiModels = getFromRemote(),
                    modelsParamsConfigApi = modelsParams
                )
            )
        }.onFailure { Timber.tag(TAG).e(it, "Couldn't get models from server") }
            .getOrThrow()

        ollieModelDao.insertOrReplaceModels(newDbModels)

        val cachedNewModels = runSuspendCatching { getFromDb() }
            .onFailure { Timber.tag(TAG).e(it, "Couldn't get new cached models from db") }
            .getOrThrow()

        emit(dbToDomainEntityMapper(cachedNewModels))
    }.flowOn(dispatchers.io)

    override suspend fun getModelById(id: Long): OllieModel? {
        return ollieModelDao.getById(id)
            ?.let { dbToDomainEntityMapper(it) }
    }

    override suspend fun updateModel(model: OllieModel) {
        val dbModel = withContext(Dispatchers.Default) { domainToDbEntityMapper(model) }
        ollieModelDao.insertOrReplaceModel(dbModel)
    }

    private suspend fun getFromDb(): List<OllieModelDbEntityWithRelationships> =
        ollieModelDao.getAll()

    private fun getFromRemote(): List<OllamaModel> = ollamaModels.availableModels().content()

    private fun mergeModels(
        oldModels: List<OllieModelDbEntityWithRelationships>,
        newModels: List<OllieModelDbEntityWithRelationships>,
    ): List<OllieModelDbEntityWithRelationships> =
        newModels.map { newModel ->
            oldModels.firstOrNull { oldModel ->
                @Suppress("ForbiddenComment")
                // TODO: How to match ?
                oldModel.model.name == newModel.model.name &&
                        oldModel.model.digest == newModel.model.digest
            }?.let { oldModel ->
                val modelId = oldModel.model.id
                newModel.copy(
                    model = newModel.model.copy(id = modelId),
                    details = newModel.details.copy(
                        id = oldModel.details.id,
                        modelId = modelId,
                    ),
                    params = oldModel.params.takeIf { it.isNotEmpty() } ?: newModel.params
                )
            } ?: newModel
        }
}
