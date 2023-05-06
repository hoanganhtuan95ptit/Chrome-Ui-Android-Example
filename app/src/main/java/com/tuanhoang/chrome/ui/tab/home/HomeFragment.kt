package com.tuanhoang.chrome.ui.tab.home

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import com.one.navigation.NavigationEvent
import com.tuanhoang.chrome.LOGO_PAGE_DEFAULT
import com.tuanhoang.chrome.PARAM_GROUP_PAGE
import com.tuanhoang.chrome.R
import com.tuanhoang.chrome.databinding.FragmentHomeBinding
import com.tuanhoang.chrome.entities.Page
import com.tuanhoang.chrome.ui.activities.MainViewModel
import com.tuanhoang.chrome.ui.tab.GroupPageFragment
import com.tuanhoang.chrome.ui.tab.TabView
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.koin.getViewModel

class HomeFragment : GroupPageFragment<FragmentHomeBinding, MainViewModel>() {

    override val page: Page by lazy {

        requireArguments().getParcelable(PARAM_GROUP_PAGE)!!
    }


    override val viewModel: MainViewModel by lazy {
        getKoin().getViewModel(requireActivity(), MainViewModel::class)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        ViewCompat.setNestedScrollingEnabled(binding!!.nestedScrollView, true)

        super.onViewCreated(view, savedInstanceState)


        if (page.scrollY > 0) binding!!.nestedScrollView.doOnPreDraw {

            binding!!.nestedScrollView.scrollY = page.scrollY
        }

        binding!!.nestedScrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->

            page.scrollY = scrollY
        }

        (parentFragment as? TabView)?.onPageLogo(LOGO_PAGE_DEFAULT)
        (parentFragment as? TabView)?.onPageTitle(getString(R.string.title_home))
    }

    override fun updateVerticalOffset(verticalOffset: Int) {

        page.verticalOffset = verticalOffset
    }

    companion object {

        fun newInstance(page: Page?) = HomeFragment().apply {

            arguments = bundleOf(PARAM_GROUP_PAGE to page)
        }
    }
}

class OverviewEvent(val page: Page? = null) : NavigationEvent()