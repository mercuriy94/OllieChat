package com.mercuriy94.olliechat

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.mercuriy94.olliechat.di.OllieChatDataModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

internal class OllieChatApplication : Application() {

    companion object {

        @SuppressLint("StaticFieldLeak")
        @JvmStatic
        lateinit var context: Context

        @JvmStatic
        lateinit var applicationScope: CoroutineScope
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        applicationScope = ProcessLifecycleOwner.get().lifecycleScope

        applicationScope.launch {

            val workManager = WorkManager.getInstance(context)
            OllieChatDataModule.ollieChatWorksRepository.getAllChatWorkRequestIds()
                .also { workRequestIds ->
                    workRequestIds.forEach { workRequestId ->
                        workManager.cancelWorkById(UUID.fromString(workRequestId))
                    }

                    OllieChatDataModule.ollieChatWorksRepository.deleteWorks(workRequestIds)
                }
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
