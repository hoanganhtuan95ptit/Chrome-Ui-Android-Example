package com.tuanhoang.chrome.ui.activities

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.one.adapter.ViewItemCloneable
import com.one.coreapp.App
import com.one.coreapp.ui.base.viewmodels.BaseViewModel
import com.one.coreapp.utils.FileUtils
import com.one.coreapp.utils.extentions.combineSources
import com.one.coreapp.utils.extentions.getOrEmpty
import com.one.coreapp.utils.extentions.postDifferentValue
import com.one.coreapp.utils.extentions.postValue
import com.tuanhoang.chrome.entities.Page
import com.tuanhoang.chrome.entities.Tab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainViewModel : BaseViewModel() {

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

    fun updatePage(page: Page, byteArray: ByteArray?) = viewModelScope.launch(handler + Dispatchers.IO) {

        page.byte = byteArray

        val pageCache = tabList.getOrEmpty().flatMap { it.pages.values }.find { it.id == page.id } ?: return@launch

        pageCache.byte = byteArray

        Unit
    }

    fun updateTab(tab: Tab, bitmap: Bitmap) = viewModelScope.launch(handler + Dispatchers.IO) {

        val tabCache = tabList.getOrEmpty().find { it.id == tab.id } ?: let {

            bitmap.recycle()
            return@launch
        }

        val file = FileUtils.createFile(App.shared, true, "image", tabCache.id) ?: let {

            bitmap.recycle()
            return@launch
        }

        FileUtils.save(bitmap, file)

        bitmap.recycle()


        tabCache.image = file.absolutePath


        tabList.postValue(tabList.getOrEmpty())
    }

    fun updateTab(tab: Tab, logo: String? = null, title: String? = null) = viewModelScope.launch(handler + Dispatchers.IO) {

        val tabCache = tabList.getOrEmpty().find { it.id == tab.id } ?: let {

            return@launch
        }

        if (logo != null) {
            tabCache.logo = logo
        }

        if (title != null) {
            tabCache.title = title
        }

        Log.d("tuanha", "updateTab: title:$title logo:$logo ")

        tabList.postValue(tabList.getOrEmpty())
    }
}