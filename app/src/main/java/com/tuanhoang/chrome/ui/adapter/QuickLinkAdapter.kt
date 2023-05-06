package com.tuanhoang.chrome.ui.adapter

import android.view.View
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.one.adapter.ViewItemAdapter
import com.one.adapter.ViewItemCloneable
import com.one.coreapp.utils.extentions.*
import com.tuanhoang.chrome.databinding.ItemLinkQuickBinding
import com.tuanhoang.chrome.entities.Link
import java.net.URL

class QuickLinkAdapter(onItemClick: (View, QuickLinkViewItem) -> Unit) : ViewItemAdapter<QuickLinkViewItem, ItemLinkQuickBinding>(onItemClick) {

    override fun bind(binding: ItemLinkQuickBinding, viewType: Int, position: Int, item: QuickLinkViewItem, payloads: MutableList<Any>) {
        super.bind(binding, viewType, position, item, payloads)

        if (payloads.contains(PAYLOAD_LOGO)) {
            refreshLogo(binding, item)
        }

        if (payloads.contains(PAYLOAD_TITLE)) {
            refreshTitle(binding, item)
        }
    }

    override fun bind(binding: ItemLinkQuickBinding, viewType: Int, position: Int, item: QuickLinkViewItem) {
        super.bind(binding, viewType, position, item)

        refreshLogo(binding, item)
        refreshTitle(binding, item)
    }

    private fun refreshLogo(binding: ItemLinkQuickBinding, item: QuickLinkViewItem) {
        binding.ivLogo.setImage(item.logo, CenterCrop(), CircleCrop())
    }

    private fun refreshTitle(binding: ItemLinkQuickBinding, item: QuickLinkViewItem) {
        binding.tvName.setText(item.title)
    }

}

data class QuickLinkViewItem(
    val data: Link,

    var logo: Image<*> = emptyImage(),

    var title: Text<*> = emptyText(),
) : ViewItemCloneable {

    override fun clone() = copy()

    fun refresh() = apply {

        logo = "https://www.google.com/s2/favicons?sz=128&domain=${URL(data.url).host}".toImage()

        title = data.name.toText()
    }

    override fun areItemsTheSame(): List<Any> = listOf(
        data
    )

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        logo to PAYLOAD_LOGO,
        title to PAYLOAD_TITLE,
    )
}

private const val PAYLOAD_LOGO = "PAYLOAD_LOGO"
private const val PAYLOAD_TITLE = "PAYLOAD_TITLE"
