package com.tuanhoang.chrome.ui.activities

import android.view.View
import com.one.adapter.ViewItemAdapter
import com.one.adapter.ViewItemCloneable
import com.one.coreapp.utils.extentions.*
import com.tuanhoang.chrome.databinding.ItemTabBinding
import com.tuanhoang.chrome.entities.Tab

class TabAdapter(onItemClick: (View, TabViewItem) -> Unit) : ViewItemAdapter<TabViewItem, ItemTabBinding>(onItemClick) {

    override fun bind(binding: ItemTabBinding, viewType: Int, position: Int, item: TabViewItem) {
        super.bind(binding, viewType, position, item)

        binding.ivTab.setImage(item.image)

        binding.tvTitle.setText(item.name)
    }
}

data class TabViewItem(
    val data: Tab,

    var name: Text<*> = emptyText(),

    var image: Image<*> = emptyImage(),
) : ViewItemCloneable {

    override fun clone() = copy()

    fun refresh() = apply {

        name = data.name.toText()

        image = data.image.toImage()
    }

    override fun areItemsTheSame(): List<Any> = listOf(
        data
    )
}