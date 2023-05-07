package com.tuanhoang.chrome.ui.tab.home

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.one.adapter.MultiAdapter
import com.one.coreapp.utils.autoCleared
import com.one.coreapp.utils.extentions.getOrEmpty
import com.one.coreapp.utils.extentions.resumeActive
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.koin.getViewModel

class HomeFragment : PageFragment<FragmentHomeBinding, HomeViewModel>() {


    private val mainViewModel: MainViewModel by lazy {

        getKoin().getViewModel(requireActivity(), MainViewModel::class)
    }


    override val page: Page by lazy {

        val page = requireArguments().getParcelable<Page>(PARAM_GROUP_PAGE)!!

        mainViewModel.tabList.getOrEmpty().flatMap { it.pages.values }.find { it.id == page.id } ?: page
    }


    private var scrollY = 0

    private var adapter by autoCleared<MultiAdapter>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (parentFragment as? TabView)?.onPageLogo(LOGO_PAGE_DEFAULT)
        (parentFragment as? TabView)?.onPageTitle(getString(R.string.title_home))
    }

    override fun onViewReady(view: View) {
        super.onViewReady(view)

        setupRecyclerView()

        observeData()
    }

    override fun onPause() {
        super.onPause()

        page.scrollY = scrollY
    }

    override fun updateVerticalOffset(verticalOffset: Int) {

        page.verticalOffset = verticalOffset
    }

    private fun setupRecyclerView() {

        val binding = binding ?: return

        val groupLinkAdapter = GroupLinkAdapter { _, link ->

            offerNavEvent(WebEvent(url = link.url))
        }

        val resultLinkAdapter = ResultLinkAdapter { _, item ->

            offerNavEvent(WebEvent(url = item.data.url))
        }

        adapter = MultiAdapter(groupLinkAdapter, resultLinkAdapter).apply {

            setRecyclerView(binding.recyclerView)
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                scrollY += dy
            }
        })
    }

    private fun observeData() = with(viewModel) {

        viewItemListDisplay.observe(viewLifecycleOwner) {

            viewLifecycleOwner.lifecycleScope.launch {

                suspendCancellableCoroutine<Boolean> { a ->

                    adapter?.submitList(it) {
                        a.resumeActive(true)
                    }
                }

                binding!!.recyclerView.scrollBy(0, page.scrollY)
            }
        }
    }

    companion object {

        fun newInstance(page: Page?) = HomeFragment().apply {

            arguments = bundleOf(PARAM_GROUP_PAGE to page)
        }
    }
}

class OverviewEvent(val page: Page? = null) : NavigationEvent()