package com.tuanhoang.chrome.di

import com.tuanhoang.chrome.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

@JvmField
val apiModule = module {

    single {
        OkHttpClient
            .Builder()
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .connectTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE))
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("http://45.77.206.34:3000/")
            .addConverterFactory(JacksonConverterFactory.create())
            .client(get())
            .build()
    }
}