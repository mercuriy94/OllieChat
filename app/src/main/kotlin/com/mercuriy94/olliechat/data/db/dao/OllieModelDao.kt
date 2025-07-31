package com.mercuriy94.olliechat.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mercuriy94.olliechat.data.db.model.OllieDetailsModelDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieModelDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieModelDbEntityWithRelationships
import com.mercuriy94.olliechat.data.db.model.OllieModelParamDbEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface OllieModelDao {

    @Transaction
    @Query("SELECT * FROM ollie_models")
    fun getAllFlow(): Flow<List<OllieModelDbEntityWithRelationships>>

    @Transaction
    @Query("SELECT * FROM ollie_models")
    suspend fun getAll(): List<OllieModelDbEntityWithRelationships>

    @Transaction
    @Query("SELECT * FROM ollie_models WHERE id = :id")
    suspend fun getById(id: Long): OllieModelDbEntityWithRelationships?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceModel(model: OllieModelDbEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceModelDetails(model: OllieDetailsModelDbEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceModelDetails(details: List<OllieDetailsModelDbEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceModelParams(params: List<OllieModelParamDbEntity>)

    @Transaction
    suspend fun insertOrReplaceModels(dbModels: List<OllieModelDbEntityWithRelationships>) {
        dbModels.forEach { insertOrReplaceModel(it) }
    }

    @Transaction
    suspend fun insertOrReplaceModel(dbModel: OllieModelDbEntityWithRelationships) {
        val id = insertOrReplaceModel(dbModel.model)
        dbModel.details.copy(modelId = id).also { insertOrReplaceModelDetails(it) }
        dbModel.params.map { it.copy(modelId = id) }.also { insertOrReplaceModelParams(it) }
    }
}
