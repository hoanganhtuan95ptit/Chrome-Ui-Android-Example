package com.tuanhoang.chrome

import com.one.coreapp.App
import com.one.coreapp.Module
import com.one.coreapp.di.coreCacheModule
import com.one.coreapp.utils.extentions.logException
import com.tuanhoang.chrome.di.*
import kotlinx.coroutines.CoroutineExceptionHandler
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import kotlin.coroutines.CoroutineContext

class ChromeApp : App() {

    open val handler = CoroutineExceptionHandler { _: CoroutineContext, throwable: Throwable ->
        logException(throwable)
    }

    private val multiSDK by lazy {
        getKoin().getAll<Module>()
    }

    override fun onCreate() {

        startKoin {
            androidContext(this@ChromeApp)
            androidLogger(Level.NONE)

            modules(
                appModule,

                apiModule,

                daoModule,

                taskModule,

                cacheModule,

                memoryModule,

                coreCacheModule,

                realtimeModule,

                repositoryModule,

                interactModule,

                exceptionModule,
                viewModelModule
            )
        }

        super.onCreate()

        multiSDK.map {

            it.init(this)
        }
    }
}