package com.tuanhoang.chrome.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
open class Link(
    open var id: String = UUID.randomUUID().toString(),
    open var url: String = "",
    open var name: String = "",
) : Parcelable {
}
