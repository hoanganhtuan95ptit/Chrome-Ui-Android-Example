package com.tuanhoang.chrome.di

import com.tuanhoang.chrome.ui.activities.MainViewModel
import com.tuanhoang.chrome.ui.tab.home.HomeViewModel
import com.tuanhoang.chrome.ui.tab.search.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@JvmField
val viewModelModule = module {

    viewModel {
        MainViewModel()
    }

    viewModel {
        HomeViewModel()
    }

    viewModel {
        SearchViewModel()
    }
}
