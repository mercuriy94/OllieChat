package com.mercuriy94.olliechat.di

import androidx.room.Room
import com.mercuriy94.olliechat.OllieChatApplication
import com.mercuriy94.olliechat.data.db.OllieChatDatabase

internal object DatabaseModule {

    const val DB_NAME = "OllieChatDatabase"

    val database: OllieChatDatabase by lazy {
        Room.databaseBuilder(
            context = requireNotNull(OllieChatApplication.context),
            klass = OllieChatDatabase::class.java,
            name = DB_NAME,
        ).fallbackToDestructiveMigration(true)
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .build()
    }
}
