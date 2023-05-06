package com.tuanhoang.chrome.ui.tab.home.adapter

import android.view.View
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.one.adapter.ViewItemAdapter
import com.one.adapter.ViewItemCloneable
import com.one.coreapp.utils.extentions.*
import com.tuanhoang.chrome.databinding.ItemLinkResultBinding
import com.tuanhoang.chrome.entities.Link
import java.net.URL

class ResultLinkAdapter(onItemClick: (View, ResultLinkViewItem) -> Unit) : ViewItemAdapter<ResultLinkViewItem, ItemLinkResultBinding>(onItemClick) {

    override fun bind(binding: ItemLinkResultBinding, viewType: Int, position: Int, item: ResultLinkViewItem) {
        super.bind(binding, viewType, position, item)

        refreshLogo(binding, item)
        refreshName(binding, item)
        refreshTitle(binding, item)
        refreshImage(binding, item)
    }

    private fun refreshLogo(binding: ItemLinkResultBinding, item: ResultLinkViewItem) {
        binding.ivLogo.setImage(item.logo)
    }

    private fun refreshName(binding: ItemLinkResultBinding, item: ResultLinkViewItem) {
        binding.tvName.setText(item.name)
    }

    private fun refreshTitle(binding: ItemLinkResultBinding, item: ResultLinkViewItem) {
        binding.tvTitle.setText(item.title)
    }

    private fun refreshImage(binding: ItemLinkResultBinding, item: ResultLinkViewItem) {
        binding.ivImage.setImage(item.image, CenterCrop())
    }
}

data class ResultLinkViewItem(
    val data: Link,

    var logo: Image<*> = emptyImage(),
    var name: Text<*> = emptyText(),

    var title: Text<*> = emptyText(),
    var image: Image<*> = emptyImage()
) : ViewItemCloneable {

    override fun clone() = copy()

    fun refresh() = apply {

        logo = "https://www.google.com/s2/favicons?sz=128&domain=${URL(data.url).host}".toImage()
        name = data.name.toText()

        title = data.title.toText()
        image = data.image.toImage()
    }

    override fun areItemsTheSame(): List<Any> = listOf(
        data
    )

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
    )
}