package com.tuanhoang.chrome.ui.tab.search.adapter

import android.view.View
import com.one.adapter.ViewItemAdapter
import com.one.adapter.ViewItemCloneable
import com.one.coreapp.utils.extentions.*
import com.tuanhoang.chrome.R
import com.tuanhoang.chrome.databinding.ItemSearchBinding
import com.tuanhoang.chrome.entities.link.SearchLink
import com.tuanhoang.chrome.entities.link.SearchLinkType
import java.net.URL

class SearchAdapter(onItemClick: (View, SearchViewItem) -> Unit) : ViewItemAdapter<SearchViewItem, ItemSearchBinding>(onItemClick) {

    override fun bind(binding: ItemSearchBinding, viewType: Int, position: Int, item: SearchViewItem, payloads: MutableList<Any>) {
        super.bind(binding, viewType, position, item, payloads)

        if (payloads.contains(PAYLOAD_NAME)) {
            refreshName(binding, item)
        }
    }

    override fun bind(binding: ItemSearchBinding, viewType: Int, position: Int, item: SearchViewItem) {
        super.bind(binding, viewType, position, item)

        refreshLogo(binding, item)
        refreshName(binding, item)
    }

    private fun refreshLogo(binding: ItemSearchBinding, item: SearchViewItem) {
        binding.ivLogo.setImage(item.logo)
    }

    private fun refreshName(binding: ItemSearchBinding, item: SearchViewItem) {
        binding.tvTitle.setText(item.name)
    }
}

data class SearchViewItem(
    val data: SearchLink,

    var logo: Image<*> = emptyImage(),

    var name: Text<*> = emptyText(),
) : ViewItemCloneable {

    override fun clone() = copy()

    fun refresh() = apply {

        logo = if (data.type == SearchLinkType.NORMAL) {
            emptyImage()
        } else {
            "https://www.google.com/s2/favicons?sz=128&domain=${URL(data.url).host}".toImage()
        }

        name = if (data.type == SearchLinkType.SEARCH_GOOGLE) {
            TextRes(R.string.search_with_google, data.name.toText())
        } else if (data.type == SearchLinkType.SEARCH_YOUTUBE) {
            TextRes(R.string.search_with_youtube, data.name.toText())
        } else {
            data.name.toText()
        }
    }

    override fun areItemsTheSame(): List<Any> = listOf(
        data.id
    )

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        name to PAYLOAD_NAME
    )
}

private const val PAYLOAD_NAME = "PAYLOAD_NAME"