package com.tuanhoang.chrome.ui.tab.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.one.adapter.ViewItemCloneable
import com.one.coreapp.ui.base.viewmodels.BaseViewModel
import com.one.coreapp.utils.extentions.combineSources
import com.one.coreapp.utils.extentions.getOrEmpty
import com.one.coreapp.utils.extentions.postDifferentValue

class SearchViewModel : BaseViewModel() {

    val query: LiveData<String> = MediatorLiveData()

    val viewItemList: LiveData<List<ViewItemCloneable>> = combineSources(query) {


    }

    val viewItemListDisplay: LiveData<List<ViewItemCloneable>> = combineSources(viewItemList) {

        viewItemList.getOrEmpty().map {
            it.clone()
        }.let {
            postDifferentValue(it)
        }
    }

    fun updateQuery(query: String) {

        this.query.postDifferentValue(query)
    }
}