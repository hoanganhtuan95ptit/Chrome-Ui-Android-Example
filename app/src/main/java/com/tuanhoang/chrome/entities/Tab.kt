package com.tuanhoang.chrome.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Tab(

    val id: String = UUID.randomUUID().toString(),

    var url: String = "",
    val name: String = "",
    var image: String = "",

    var isCurrent: Boolean = false,

    var groupPages: LinkedHashMap<String, GroupPage> = LinkedHashMap()
) : Parcelable {

    fun addLast(groupPage: GroupPage) {

        while (groupPages.containsKey(groupPage.id)) remove(groupPages.values.lastOrNull() ?: return)

        groupPages[groupPage.id] = groupPage
    }

    fun remove(groupPage: GroupPage) {

        groupPages.remove(groupPage.id)
    }
}

@Parcelize
data class GroupPage(

    val id: String = UUID.randomUUID().toString(),

    var type: GroupPageType = GroupPageType.HOME,

    val pages: LinkedHashMap<String, Page> = Page().let { linkedMapOf(it.id to it) }
) : Parcelable {

    var scrollY: Int
        get() = pages.values.last().scrollY
        set(value) {
            pages.values.last().scrollY = value
        }

    var verticalOffset: Int
        get() = pages.values.last().verticalOffset
        set(value) {
            pages.values.last().verticalOffset = value
        }
}

enum class GroupPageType {

    HOME, SEARCH, NORMAL
}

@Parcelize
data class Page(

    val id: String = UUID.randomUUID().toString(),

    var url: String = "",

    var scrollY: Int = -1,
    var verticalOffset: Int = -1,
) : Parcelable
