package com.tuanhoang.chrome.ui.tab.home

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import com.one.adapter.ViewItemCloneable
import com.one.coreapp.ui.base.viewmodels.BaseViewModel
import com.one.coreapp.utils.extentions.combineSources
import com.one.coreapp.utils.extentions.getOrEmpty
import com.one.coreapp.utils.extentions.liveData
import com.one.coreapp.utils.extentions.postDifferentValue
import com.tuanhoang.chrome.entities.Link
import com.tuanhoang.chrome.ui.adapter.GroupLinkViewItem
import com.tuanhoang.chrome.ui.tab.home.adapter.ResultLinkViewItem

class HomeViewModel : BaseViewModel() {

    @VisibleForTesting
    val viewItemList: LiveData<List<ViewItemCloneable>> = liveData {

        val list = arrayListOf<ViewItemCloneable>()

        listOf(
            Link(url = "https://www.reddit.com/", name = "Reddit"),
            Link(url = "https://github.com/", name = "Github"),
            Link(url = "https://twitter.com/", name = "Twitter"),
            Link(url = "https://www.facebook.com/", name = "Facebook"),
            Link(url = "https://www.google.com/", name = "Google"),
            Link(url = "https://www.youtube.com/", name = "Youtube"),
            Link(url = "https://www.instagram.com/", name = "Instagram"),
            Link(url = "https://www.wikipedia.org/", name = "Wikipedia")
        ).let {

            GroupLinkViewItem(it).refresh()
        }.let {

            list.add(it)
        }

        listOf(
            Link(
                url = "https://github.com/",
                name = "Github",
                image = "https://github.blog/wp-content/uploads/2021/12/GitHub-code-search_banner.png?resize=1200%2C630",
                title = "Today, we are rolling out a technology preview for GitHub code search, the next iteration for search, discovery, and navigation on GitHub."
            ),
            Link(
                url = "https://github.com/",
                name = "Github",
                image = "https://github.blog/wp-content/uploads/2021/12/GitHub-code-search_banner.png?resize=1200%2C630",
                title = "Today, we are rolling out a technology preview for GitHub code search, the next iteration for search, discovery, and navigation on GitHub."
            ),
            Link(
                url = "https://github.com/",
                name = "Github",
                image = "https://github.blog/wp-content/uploads/2021/12/GitHub-code-search_banner.png?resize=1200%2C630",
                title = "Today, we are rolling out a technology preview for GitHub code search, the next iteration for search, discovery, and navigation on GitHub."
            ),
            Link(
                url = "https://github.com/",
                name = "Github",
                image = "https://github.blog/wp-content/uploads/2021/12/GitHub-code-search_banner.png?resize=1200%2C630",
                title = "Today, we are rolling out a technology preview for GitHub code search, the next iteration for search, discovery, and navigation on GitHub."
            ),
            Link(
                url = "https://github.com/",
                name = "Github",
                image = "https://github.blog/wp-content/uploads/2021/12/GitHub-code-search_banner.png?resize=1200%2C630",
                title = "Today, we are rolling out a technology preview for GitHub code search, the next iteration for search, discovery, and navigation on GitHub."
            ),
        ).map {

            ResultLinkViewItem(it).refresh()
        }.let {

            list.addAll(it)
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
}