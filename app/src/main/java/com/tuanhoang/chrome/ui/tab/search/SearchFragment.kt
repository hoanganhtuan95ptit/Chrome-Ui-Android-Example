package com.tuanhoang.chrome.ui.tab.search

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import com.one.navigation.NavigationEvent
import com.one.navigation.offerNavEvent
import com.tuanhoang.chrome.LOGO_PAGE_DEFAULT
import com.tuanhoang.chrome.R
import com.tuanhoang.chrome.databinding.FragmentSearchBinding
import com.tuanhoang.chrome.entities.Page
import com.tuanhoang.chrome.entities.GroupPageType
import com.tuanhoang.chrome.ui.tab.GroupPageFragment
import com.tuanhoang.chrome.ui.tab.TabView
import com.tuanhoang.chrome.ui.tab.web.WebEvent

class SearchFragment : GroupPageFragment<FragmentSearchBinding, SearchViewModel>() {

    override val page: Page by lazy {

        Page(type = GroupPageType.SEARCH)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        ViewCompat.setNestedScrollingEnabled(binding!!.nestedScrollView, false)

        super.onViewCreated(view, savedInstanceState)

        if (page.scrollY > 0) binding!!.nestedScrollView.doOnPreDraw {

            binding!!.nestedScrollView.scrollY = page.scrollY
        }


        val binding = binding ?: return

        binding.tv1.setOnClickListener {

            offerNavEvent(WebEvent(url = "https://www.google.com.vn/search?q=test"))
        }

        binding.tv2.setOnClickListener {

//            offerNavEvent(AssetEvent())
        }

        (parentFragment as? TabView)?.onPageLogo(LOGO_PAGE_DEFAULT)
        (parentFragment as? TabView)?.onPageTitle(getString(R.string.title_search))
    }

    override fun onDestroyView() {
        super.onDestroyView()

        (parentFragment as TabView).onPageRemove(page)
    }

    override fun updateQuery(query: String) {

//        viewModel.update
    }
}

class SearchEvent() : NavigationEvent()