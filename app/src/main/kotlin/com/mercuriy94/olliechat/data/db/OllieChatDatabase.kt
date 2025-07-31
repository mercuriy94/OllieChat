package com.mercuriy94.olliechat.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mercuriy94.olliechat.data.db.converter.OffsetDateTimeConverter
import com.mercuriy94.olliechat.data.db.converter.OllieChatMessageAuthorDbEntityConverter
import com.mercuriy94.olliechat.data.db.converter.OllieChatMessageStatusDbEntityConverter
import com.mercuriy94.olliechat.data.db.converter.OllieChatWorkTypeDbEntityConverter
import com.mercuriy94.olliechat.data.db.converter.OllieModelParamKeyDbEntityConverter
import com.mercuriy94.olliechat.data.db.converter.OllieModelParamValueDbEntityConverter
import com.mercuriy94.olliechat.data.db.dao.OllieChatDao
import com.mercuriy94.olliechat.data.db.dao.OllieChatWorkDao
import com.mercuriy94.olliechat.data.db.dao.OllieMessageDao
import com.mercuriy94.olliechat.data.db.dao.OllieModelDao
import com.mercuriy94.olliechat.data.db.model.OllieChatDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageAiModelDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageTokenUsageDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieChatWorkDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieChatWorkTaskDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieDetailsModelDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieModelDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieModelParamDbEntity

@Database(
    entities = [
        OllieModelDbEntity::class,
        OllieDetailsModelDbEntity::class,
        OllieModelParamDbEntity::class,
        OllieChatDbEntity::class,
        OllieChatMessageDbEntity::class,
        OllieChatMessageTokenUsageDbEntity::class,
        OllieChatMessageAiModelDbEntity::class,
        OllieChatWorkDbEntity::class,
        OllieChatWorkTaskDbEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(
    OffsetDateTimeConverter::class,
    OllieModelParamKeyDbEntityConverter::class,
    OllieModelParamValueDbEntityConverter::class,
    OllieChatMessageAuthorDbEntityConverter::class,
    OllieChatMessageStatusDbEntityConverter::class,
    OllieChatWorkTypeDbEntityConverter::class,
)
internal abstract class OllieChatDatabase : RoomDatabase() {

    abstract fun ollieModelDao(): OllieModelDao

    abstract fun ollieChatDao(): OllieChatDao

    abstract fun ollieMessageDao(): OllieMessageDao

    abstract fun ollieChatWorksDao(): OllieChatWorkDao
}
