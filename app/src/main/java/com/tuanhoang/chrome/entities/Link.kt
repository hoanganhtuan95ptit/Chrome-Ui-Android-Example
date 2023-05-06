package com.tuanhoang.chrome.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Link(
    var url: String = "",
    var name: String = "",

    var title: String = "",
    var image: String = "",

    var type: LinkType = LinkType.NORMAL
) : Parcelable {
}

enum class LinkType {
    NORMAL
}