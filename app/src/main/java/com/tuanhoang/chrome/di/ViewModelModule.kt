package com.tuanhoang.chrome.di

import com.tuanhoang.chrome.ui.activities.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@JvmField
val viewModelModule = module {

    viewModel {
        MainViewModel()
    }
}
