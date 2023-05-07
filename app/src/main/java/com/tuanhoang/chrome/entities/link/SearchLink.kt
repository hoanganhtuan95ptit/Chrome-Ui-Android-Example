package com.tuanhoang.chrome.entities.link

import com.tuanhoang.chrome.entities.Link
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class SearchLink(

    override var id: String = UUID.randomUUID().toString(),

    override var url: String = "",
    override var name: String = "",

    var type: SearchLinkType = SearchLinkType.NORMAL
) : Link(id, url, name)

enum class SearchLinkType {
    NORMAL, SEARCH_GOOGLE, SEARCH_YOUTUBE, SEARCH_TWITTER
}