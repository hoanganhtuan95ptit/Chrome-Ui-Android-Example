package com.tuanhoang.chrome.ui.activities.adapter

import android.view.View
import com.one.adapter.ViewItemAdapter
import com.one.adapter.ViewItemCloneable
import com.one.coreapp.utils.extentions.*
import com.tuanhoang.chrome.databinding.ItemTabBinding
import com.tuanhoang.chrome.entities.Tab

class TabAdapter(onItemClick: (View, TabViewItem) -> Unit) : ViewItemAdapter<TabViewItem, ItemTabBinding>(onItemClick) {

    override fun bind(binding: ItemTabBinding, viewType: Int, position: Int, item: TabViewItem, payloads: MutableList<Any>) {
        super.bind(binding, viewType, position, item, payloads)

        if (payloads.contains(PAYLOAD_LOGO)) {
            refreshLogo(binding, item)
        }

        if (payloads.contains(PAYLOAD_TITLE)) {
            refreshTitle(binding, item)
        }

        if (payloads.contains(PAYLOAD_IMAGE)) {
            refreshImage(binding, item)
        }

        if (payloads.contains(PAYLOAD_SELECTED)) {
            refreshSelected(binding, item)
        }
    }

    override fun bind(binding: ItemTabBinding, viewType: Int, position: Int, item: TabViewItem) {
        super.bind(binding, viewType, position, item)

        refreshLogo(binding, item)
        refreshTitle(binding, item)
        refreshImage(binding, item)
        refreshSelected(binding, item)
    }

    private fun refreshLogo(binding: ItemTabBinding, item: TabViewItem) {
        binding.ivLogo.setImage(item.logo)
    }

    private fun refreshTitle(binding: ItemTabBinding, item: TabViewItem) {
        binding.tvTitle.setText(item.title)
    }

    private fun refreshImage(binding: ItemTabBinding, item: TabViewItem) {
        binding.ivTab.setImage(item.image)
    }

    private fun refreshSelected(binding: ItemTabBinding, item: TabViewItem) {
        binding.root.isSelected = item.isSelect
        binding.tvTitle.isSelected = item.isSelect
        binding.ivClose.isSelected = item.isSelect
    }
}

data class TabViewItem(
    val data: Tab,

    var logo: Image<*> = emptyImage(),

    var title: Text<*> = emptyText(),

    var image: Image<*> = emptyImage(),

    var isSelect: Boolean = false,
) : ViewItemCloneable {

    override fun clone() = copy()

    fun refresh() = apply {

        logo = data.logo.toImage()

        title = data.title.toText()

        image = data.image.toImage()

        isSelect = data.isCurrent
    }

    override fun areItemsTheSame(): List<Any> = listOf(
        data
    )

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        logo to PAYLOAD_LOGO,
        title to PAYLOAD_TITLE,
        image to PAYLOAD_IMAGE,

        isSelect to PAYLOAD_SELECTED
    )
}

private const val PAYLOAD_LOGO = "PAYLOAD_LOGO"
private const val PAYLOAD_TITLE = "PAYLOAD_TITLE"
private const val PAYLOAD_IMAGE = "PAYLOAD_IMAGE"
private const val PAYLOAD_SELECTED = "PAYLOAD_SELECTED"