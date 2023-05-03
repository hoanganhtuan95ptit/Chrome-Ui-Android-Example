package com.tuanhoang.chrome.ui.tab.home

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import com.one.navigation.NavigationEvent
import com.tuanhoang.chrome.PARAM_GROUP_PAGE
import com.tuanhoang.chrome.databinding.FragmentHomeBinding
import com.tuanhoang.chrome.entities.GroupPage
import com.tuanhoang.chrome.ui.tab.GroupPageFragment

class HomeFragment : GroupPageFragment<FragmentHomeBinding>() {

    override val groupPage: GroupPage by lazy {

        requireArguments().getParcelable(PARAM_GROUP_PAGE)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        ViewCompat.setNestedScrollingEnabled(binding!!.nestedScrollView, true)

        super.onViewCreated(view, savedInstanceState)


        if (groupPage.scrollY > 0) binding!!.nestedScrollView.doOnPreDraw {

            binding!!.nestedScrollView.scrollY = groupPage.scrollY
        }

        binding!!.nestedScrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->

            groupPage.scrollY = scrollY
        }
    }

    override fun updateVerticalOffset(verticalOffset: Int) {

        groupPage.verticalOffset = verticalOffset
    }

    companion object {

        fun newInstance(groupPage: GroupPage?) = HomeFragment().apply {

            arguments = bundleOf(PARAM_GROUP_PAGE to groupPage)
        }
    }
}

class OverviewEvent(val groupPage: GroupPage? = null) : NavigationEvent()