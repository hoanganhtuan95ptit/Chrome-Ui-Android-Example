package com.tuanhoang.chrome.ui.activities

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.one.adapter.ViewItemCloneable
import com.one.coreapp.ui.base.viewmodels.BaseViewModel
import com.one.coreapp.utils.extentions.combineSources
import com.one.coreapp.utils.extentions.getOrEmpty
import com.one.coreapp.utils.extentions.postDifferentValue
import com.tuanhoang.chrome.entities.Tab

class MainViewModel : BaseViewModel() {

    @VisibleForTesting
    val tabList: LiveData<List<Tab>> = MediatorLiveData<List<Tab>>().apply {

        value = listOf()
    }

    @VisibleForTesting
    val tabViewItemList: LiveData<List<ViewItemCloneable>> = combineSources(tabList) {

        tabList.getOrEmpty().map {

            TabViewItem(it).refresh()
        }.let {

            postValue(it)
        }
    }

    val tabViewItemListDisplay: LiveData<List<ViewItemCloneable>> = combineSources(tabViewItemList) {

        tabViewItemList.getOrEmpty().map {

            it.clone()
        }.let {

            postDifferentValue(it)
        }
    }

    fun getTab(tabId: String?): Tab {


        val tabList = this.tabList.getOrEmpty().toMutableList()


        var tab: Tab? = null

        tabList.forEach {

            it.isCurrent = it.id.equals(tabId, true)

            if (it.isCurrent) tab = it
        }

        tab = tab ?: Tab(isCurrent = true).apply {

            tabList.add(this)
        }


        this.tabList.postDifferentValue(tabList)

        return tab!!
    }
}