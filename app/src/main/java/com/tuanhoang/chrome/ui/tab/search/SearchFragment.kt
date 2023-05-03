package com.tuanhoang.chrome.ui.tab.search

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import com.one.navigation.NavigationEvent
import com.one.navigation.offerNavEvent
import com.tuanhoang.chrome.databinding.FragmentSearchBinding
import com.tuanhoang.chrome.entities.GroupPage
import com.tuanhoang.chrome.entities.GroupPageType
import com.tuanhoang.chrome.ui.tab.GroupPageFragment
import com.tuanhoang.chrome.ui.tab.TabView
import com.tuanhoang.chrome.ui.tab.web.WebEvent

class SearchFragment : GroupPageFragment<FragmentSearchBinding>() {

    override val groupPage: GroupPage by lazy {

        GroupPage(type = GroupPageType.SEARCH)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        ViewCompat.setNestedScrollingEnabled(binding!!.nestedScrollView, false)

        super.onViewCreated(view, savedInstanceState)

        if (groupPage.scrollY > 0) binding!!.nestedScrollView.doOnPreDraw {

            binding!!.nestedScrollView.scrollY = groupPage.scrollY
        }


        val binding = binding ?: return

        binding.tv1.setOnClickListener {

            offerNavEvent(WebEvent(url = "https://www.google.com.vn/search?q=test"))
        }

        binding.tv2.setOnClickListener {

//            offerNavEvent(AssetEvent())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        (parentFragment as TabView).onPageRemove(groupPage)
    }
}

class SearchEvent() : NavigationEvent()