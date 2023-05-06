package com.tuanhoang.chrome.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.one.adapter.MultiAdapter
import com.one.adapter.ViewItemAdapter
import com.one.adapter.ViewItemCloneable
import com.tuanhoang.chrome.databinding.ItemLinkGroupBinding
import com.tuanhoang.chrome.entities.Link

class GroupLinkAdapter(private val onItemClick: (View, Link) -> Unit) : ViewItemAdapter<GroupLinkViewItem, ItemLinkGroupBinding>() {

    override fun createViewItem(parent: ViewGroup, viewType: Int): ItemLinkGroupBinding {

        val binding = super.createViewItem(parent, viewType)

        val quickLinkAdapter = QuickLinkAdapter { view, quickLinkViewItem ->

            onItemClick.invoke(view, quickLinkViewItem.data)
        }

        MultiAdapter(quickLinkAdapter).apply {

            binding.recyclerView.adapter = this
            binding.recyclerView.layoutManager = GridLayoutManager(binding.root.context, 4)
            binding.recyclerView.isNestedScrollingEnabled = false
        }

        return binding
    }

    override fun bind(binding: ItemLinkGroupBinding, viewType: Int, position: Int, item: GroupLinkViewItem, payloads: MutableList<Any>) {
        super.bind(binding, viewType, position, item, payloads)

        if (payloads.contains(PAYLOAD_DATA)) {
            refreshData(binding, item)
        }
    }

    override fun bind(binding: ItemLinkGroupBinding, viewType: Int, position: Int, item: GroupLinkViewItem) {
        super.bind(binding, viewType, position, item)

        refreshData(binding, item)
    }

    private fun refreshData(binding: ItemLinkGroupBinding, item: GroupLinkViewItem) {

        (binding.recyclerView.adapter as? MultiAdapter)?.submitList(item.viewItemList)
    }
}

data class GroupLinkViewItem(
    val data: List<Link>,

    var viewItemList: List<ViewItemCloneable> = emptyList()
) : ViewItemCloneable {

    override fun clone() = copy(viewItemList = viewItemList.map { it.clone() })

    fun refresh() = apply {

        viewItemList = data.map {
            QuickLinkViewItem(it).refresh()
        }
    }

    override fun areItemsTheSame(): List<Any> = listOf(
        "GroupLinkViewItem"
    )

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        viewItemList.flatMap { viewItemCloneable -> viewItemCloneable.getContentsCompare().map { it.first } } to PAYLOAD_DATA,
    )
}

private const val PAYLOAD_DATA = "PAYLOAD_DATA"
