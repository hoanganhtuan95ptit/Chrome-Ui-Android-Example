package com.tuanhoang.chrome.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Tab(

    val id: String = UUID.randomUUID().toString(),

    var logo: String = "",
    var title: String = "",
    var image: String = "",

    var isCurrent: Boolean = false,

    var pages: LinkedHashMap<String, Page> = LinkedHashMap()
) : Parcelable {

    fun addLast(page: Page) {

        while (pages.containsKey(page.id)) remove(pages.values.lastOrNull() ?: return)

        pages[page.id] = page
    }

    fun remove(page: Page) {

        pages.remove(page.id)
    }
}

@Parcelize
data class Page(

    val id: String = UUID.randomUUID().toString(),

    var url: String = "",

    var scrollY: Int = -1,

    var verticalOffset: Int = -1,

    var type: GroupPageType = GroupPageType.HOME,
) : Parcelable {

    var byte: ByteArray? = null
}

enum class GroupPageType {

    HOME, SEARCH, NORMAL
}
