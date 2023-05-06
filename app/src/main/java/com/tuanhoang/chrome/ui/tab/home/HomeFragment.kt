package com.tuanhoang.chrome.ui.tab.home

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import com.one.adapter.MultiAdapter
import com.one.coreapp.utils.autoCleared
import com.one.navigation.NavigationEvent
import com.one.navigation.offerNavEvent
import com.tuanhoang.chrome.LOGO_PAGE_DEFAULT
import com.tuanhoang.chrome.PARAM_GROUP_PAGE
import com.tuanhoang.chrome.R
import com.tuanhoang.chrome.databinding.FragmentHomeBinding
import com.tuanhoang.chrome.entities.Page
import com.tuanhoang.chrome.ui.activities.MainViewModel
import com.tuanhoang.chrome.ui.adapter.GroupLinkAdapter
import com.tuanhoang.chrome.ui.tab.PageFragment
import com.tuanhoang.chrome.ui.tab.TabView
import com.tuanhoang.chrome.ui.tab.home.adapter.ResultLinkAdapter
import com.tuanhoang.chrome.ui.tab.web.WebEvent
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.koin.getViewModel

class HomeFragment : PageFragment<FragmentHomeBinding, HomeViewModel>() {

    override val page: Page by lazy {

        requireArguments().getParcelable(PARAM_GROUP_PAGE)!!
    }


    private var adapter by autoCleared<MultiAdapter>()


    private val mainViewModel: MainViewModel by lazy {

        getKoin().getViewModel(requireActivity(), MainViewModel::class)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        observeData()

        (parentFragment as? TabView)?.onPageLogo(LOGO_PAGE_DEFAULT)
        (parentFragment as? TabView)?.onPageTitle(getString(R.string.title_home))
    }

    override fun updateVerticalOffset(verticalOffset: Int) {

        page.verticalOffset = verticalOffset
    }

    private fun setupRecyclerView() {

        val binding = binding ?: return

        val groupLinkAdapter = GroupLinkAdapter { view, link ->

            offerNavEvent(WebEvent(url = link.url))
        }

        val resultLinkAdapter = ResultLinkAdapter { view, item ->

            offerNavEvent(WebEvent(url = item.data.url))
        }

        adapter = MultiAdapter(groupLinkAdapter, resultLinkAdapter).apply {

            setRecyclerView(binding.recHome)
        }

        binding.recHome.setOnScrollChangeListener { _, _, scrollY, _, _ ->

            page.scrollY = scrollY
        }
    }

    private fun observeData() = with(viewModel) {

        viewItemListDisplay.observe(viewLifecycleOwner) {

            Log.d("tuanha", "observeData: ${it.map { it.javaClass.simpleName }}")
            adapter?.submitList(it)
//            binding!!.recyclerView.scrollY = page.scrollY
        }
    }

    companion object {

        fun newInstance(page: Page?) = HomeFragment().apply {

            arguments = bundleOf(PARAM_GROUP_PAGE to page)
        }
    }
}

class OverviewEvent(val page: Page? = null) : NavigationEvent()