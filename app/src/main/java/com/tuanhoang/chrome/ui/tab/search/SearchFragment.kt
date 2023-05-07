package com.tuanhoang.chrome.ui.tab.search

import android.os.Bundle
import android.view.View
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
import com.tuanhoang.chrome.databinding.FragmentSearchBinding
import com.tuanhoang.chrome.entities.GroupPageType
import com.tuanhoang.chrome.entities.Page
import com.tuanhoang.chrome.ui.activities.MainViewModel
import com.tuanhoang.chrome.ui.tab.PageFragment
import com.tuanhoang.chrome.ui.tab.TabView
import com.tuanhoang.chrome.ui.tab.search.adapter.SearchAdapter
import com.tuanhoang.chrome.ui.tab.web.WebEvent
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.koin.getViewModel

class SearchFragment : PageFragment<FragmentSearchBinding, SearchViewModel>() {


    private val mainViewModel: MainViewModel by lazy {

        getKoin().getViewModel(requireActivity(), MainViewModel::class)
    }


    override val page: Page by lazy {

        Page(type = GroupPageType.SEARCH)
    }


    private var scrollY = 0

    private var adapter by autoCleared<MultiAdapter>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (parentFragment as? TabView)?.onPageLogo(LOGO_PAGE_DEFAULT)
        (parentFragment as? TabView)?.onPageTitle(getString(R.string.title_search))
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

    override fun onDestroyView() {
        super.onDestroyView()

        (parentFragment as TabView).onPageRemove(page)
    }

    override fun updateQuery(query: String) {

        viewModel.updateQuery(query)
    }

    private fun setupRecyclerView() {

        val binding = binding ?: return

        val searchAdapter = SearchAdapter { view, item ->

            offerNavEvent(WebEvent(url = item.data.url))
        }

        adapter = MultiAdapter(searchAdapter).apply {

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

}

class SearchEvent() : NavigationEvent()