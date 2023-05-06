package com.tuanhoang.chrome.di

import com.tuanhoang.chrome.utils.WebManager
import org.koin.dsl.module

@JvmField
val appModule = module {

    single { WebManager(get()) }
}

