package com.tuanhoang.chrome.entities.link

import com.tuanhoang.chrome.entities.Link
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
class ResultLink(

    override var id: String = UUID.randomUUID().toString(),

    override var url: String = "",
    override var name: String = "",

    var title: String = "",
    var image: String = "",
) : Link(id, url, name)