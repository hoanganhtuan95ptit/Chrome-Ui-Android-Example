package com.tuanhoang.chrome.ui.tab.search

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.one.adapter.ViewItemCloneable
import com.one.coreapp.ui.base.viewmodels.BaseViewModel
import com.one.coreapp.utils.extentions.combineSources
import com.one.coreapp.utils.extentions.getOrEmpty
import com.one.coreapp.utils.extentions.postDifferentValue
import com.tuanhoang.chrome.entities.link.SearchLink
import com.tuanhoang.chrome.entities.link.SearchLinkType
import com.tuanhoang.chrome.ui.tab.search.adapter.SearchViewItem

class SearchViewModel : BaseViewModel() {

    @VisibleForTesting
    val query: LiveData<String> = MediatorLiveData<String>().apply {

        value = "test"
    }

    @VisibleForTesting
    val viewItemList: LiveData<List<ViewItemCloneable>> = combineSources(query) {

        val query = query.getOrEmpty()

        val list = arrayListOf<ViewItemCloneable>()

        SearchLink(id = "GOOGLE", url = "https://www.google.com/search?q=$query", query, SearchLinkType.SEARCH_GOOGLE).let {

            SearchViewItem(it).refresh()
        }.let {

            list.add(it)
        }

        SearchLink(id = "YOUTUBE", url = "https://www.youtube.com/results?search_query=$query", query, SearchLinkType.SEARCH_YOUTUBE).let {

            SearchViewItem(it).refresh()
        }.let {

            list.add(it)
        }

        postValue(list)
    }

    val viewItemListDisplay: LiveData<List<ViewItemCloneable>> = combineSources(viewItemList) {

        viewItemList.getOrEmpty().map {
            it.clone()
        }.let {
            postDifferentValue(it)
        }
    }

    fun updateQuery(query: String) {

        this.query.postDifferentValue(query.trim().lowercase())
    }
}