package com.mercuriy94.olliechat.di

import com.mercuriy94.olliechat.OllieChatApplication
import com.mercuriy94.olliechat.data.db.dao.OllieChatDao
import com.mercuriy94.olliechat.data.db.dao.OllieChatWorkDao
import com.mercuriy94.olliechat.data.db.dao.OllieMessageDao
import com.mercuriy94.olliechat.data.db.dao.OllieModelDao
import com.mercuriy94.olliechat.data.mapper.chat.OllieChatMessageDbToDomainEntityMapper
import com.mercuriy94.olliechat.data.mapper.chat.OllieChatMessageDomainToDbEntityMapper
import com.mercuriy94.olliechat.data.mapper.model.OllieModelApiToDbEntityWithRelationshipsMapper
import com.mercuriy94.olliechat.data.mapper.model.OllieModelDbToDomainEntityMapper
import com.mercuriy94.olliechat.data.mapper.model.OllieModelDomainToDbEntityWithRelationshipsMapper
import com.mercuriy94.olliechat.data.repository.chat.PersistentOllieChatRepositoryImpl
import com.mercuriy94.olliechat.data.repository.chat.PersistentOllieMessageRepositoryImpl
import com.mercuriy94.olliechat.data.repository.chat.manager.OllamaChatModelFactory
import com.mercuriy94.olliechat.data.repository.chat.manager.OllamaChatModelFactoryImpl
import com.mercuriy94.olliechat.data.repository.chat.manager.OllieChatAssistantManager
import com.mercuriy94.olliechat.data.repository.chat.manager.OllieChatManager
import com.mercuriy94.olliechat.data.repository.chat.manager.OllieChatManagerImpl
import com.mercuriy94.olliechat.data.repository.chat.memory.OllieChatMemoryProvider
import com.mercuriy94.olliechat.data.repository.chat.memory.PersistentOllieChatMemoryLocalStore
import com.mercuriy94.olliechat.data.repository.chat.title.TitleGenerator
import com.mercuriy94.olliechat.data.repository.config.AiModelsParamsConfigRepositoryImpl
import com.mercuriy94.olliechat.data.repository.model.OllieModelRepositoryImpl
import com.mercuriy94.olliechat.data.repository.work.OllieChatWorkProgressManager
import com.mercuriy94.olliechat.data.repository.work.OllieChatWorksRepository
import com.mercuriy94.olliechat.di.langchain4j.Langchain4jModule
import com.mercuriy94.olliechat.domain.repository.chat.OllieChatRepository
import com.mercuriy94.olliechat.domain.repository.chat.OllieMessageRepository
import com.mercuriy94.olliechat.domain.repository.model.OllieModelRepository
import kotlin.LazyThreadSafetyMode.NONE

internal object OllieChatDataModule {

    private val ollieModelDao: OllieModelDao by lazy(NONE) {
        DatabaseModule.database.ollieModelDao()
    }

    private val ollieChatDao: OllieChatDao by lazy(NONE) {
        DatabaseModule.database.ollieChatDao()
    }


    private val ollieMessageDao: OllieMessageDao by lazy(NONE) {
        DatabaseModule.database.ollieMessageDao()
    }

    private val ollieChatWorksDao: OllieChatWorkDao by lazy(NONE) {
        DatabaseModule.database.ollieChatWorksDao()
    }

    private val dbToDomainEntityMapper: OllieModelDbToDomainEntityMapper by lazy(
        NONE
    ) {
        OllieModelDbToDomainEntityMapper()
    }

    private val domainToDbEntityMapper by lazy(NONE) {
        OllieModelDomainToDbEntityWithRelationshipsMapper()
    }

    private val apiToDbEntityMapper by lazy(NONE) {
        OllieModelApiToDbEntityWithRelationshipsMapper()
    }

    private val ollieChatMemoryLocalStore: PersistentOllieChatMemoryLocalStore by lazy(NONE) {
        PersistentOllieChatMemoryLocalStore(ollieMessageDao)
    }

    private val ollieChatMemoryProvider: OllieChatMemoryProvider by lazy(NONE) {
        OllieChatMemoryProvider(
            ollieChatMemoryLocalStore
        )
    }

    val ollamaChatModelFactory: OllamaChatModelFactory by lazy(NONE) {
        OllamaChatModelFactoryImpl()
    }

    val ollieModelRepository: OllieModelRepository by lazy(NONE) {
        OllieModelRepositoryImpl(
            ollamaModels = Langchain4jModule.ollamaModels,
            ollieModelDao = ollieModelDao,
            dispatchers = CoroutineDispatchersModule.coroutineDispatchers,
            dbToDomainEntityMapper = dbToDomainEntityMapper,
            domainToDbEntityMapper = domainToDbEntityMapper,
            apiToDbEntityMapper = apiToDbEntityMapper,
            modelsParamsConfigRepository = AiModelsParamsConfigRepositoryImpl(
                coroutineDispatchers = CoroutineDispatchersModule.coroutineDispatchers,
                appContext = OllieChatApplication.context,
                json = NetworkModule.json,
            ),
        )
    }

    val ollieChatAssistantManager: OllieChatAssistantManager by lazy(NONE) {
        OllieChatAssistantManager(
            ollieModelRepository = ollieModelRepository,
            okHttpClient = NetworkModule.okHttpClient,
            ollieChatMemoryProvider = ollieChatMemoryProvider,
            ollamaChatModelFactory = ollamaChatModelFactory,
            coroutineDispatchers = CoroutineDispatchersModule.coroutineDispatchers,
        )
    }

    val ollieChatRepository: OllieChatRepository by lazy(NONE) {
        PersistentOllieChatRepositoryImpl(
            coroutineDispatchers = CoroutineDispatchersModule.coroutineDispatchers,
            ollieChatDao = ollieChatDao,
            messageDbToDomainEntityMapper = OllieChatMessageDbToDomainEntityMapper(),
        )
    }

    val ollieMessageRepository: OllieMessageRepository by lazy(NONE) {
        PersistentOllieMessageRepositoryImpl(
            coroutineDispatchers = CoroutineDispatchersModule.coroutineDispatchers,
            messageDbToDomainEntityMapper = OllieChatMessageDbToDomainEntityMapper(),
            messageDomainToDbEntityMapper = OllieChatMessageDomainToDbEntityMapper(),
            ollieMessageDao = ollieMessageDao,
        )
    }

    val ollieChatWorksRepository: OllieChatWorksRepository by lazy(NONE) {
        OllieChatWorksRepository(
            coroutineDispatchers = CoroutineDispatchersModule.coroutineDispatchers,
            ollieChatWorksDao = ollieChatWorksDao,
        )
    }

    val ollieChatManager: OllieChatManager by lazy(NONE) {
        OllieChatManagerImpl(
            ollieChatAssistantManager = ollieChatAssistantManager,
            persistentOllieChatRepository = ollieChatRepository,
            modelRepository = ollieModelRepository,
            messageRepository = ollieMessageRepository,
            olleChatTitleGenerator = TitleGenerator(
                okHttpClient = NetworkModule.okHttpClient,
                coroutineDispatchers = CoroutineDispatchersModule.coroutineDispatchers,
                modelRepository = ollieModelRepository,
                chatRepository = ollieChatRepository,
                messageRepository = ollieMessageRepository
            )
        )
    }

    val chatWorkProgressManager: OllieChatWorkProgressManager by lazy(NONE) {
        OllieChatWorkProgressManager()
    }
}
