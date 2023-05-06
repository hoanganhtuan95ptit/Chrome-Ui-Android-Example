package com.tuanhoang.chrome.ui.tab.search

import android.os.Bundle
import android.util.Log
import android.view.View
import com.one.adapter.MultiAdapter
import com.one.coreapp.utils.autoCleared
import com.one.navigation.NavigationEvent
import com.one.navigation.offerNavEvent
import com.tuanhoang.chrome.LOGO_PAGE_DEFAULT
import com.tuanhoang.chrome.R
import com.tuanhoang.chrome.databinding.FragmentSearchBinding
import com.tuanhoang.chrome.entities.GroupPageType
import com.tuanhoang.chrome.entities.Page
import com.tuanhoang.chrome.ui.activities.MainViewModel
import com.tuanhoang.chrome.ui.tab.PageFragment
import com.tuanhoang.chrome.ui.tab.TabView
import com.tuanhoang.chrome.ui.tab.search.adapter.SearchAdapter
import com.tuanhoang.chrome.ui.tab.web.WebEvent
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.koin.getViewModel

class SearchFragment : PageFragment<FragmentSearchBinding, SearchViewModel>() {

    override val page: Page by lazy {

        Page(type = GroupPageType.SEARCH)
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
        (parentFragment as? TabView)?.onPageTitle(getString(R.string.title_search))
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

        binding.recyclerView.setOnScrollChangeListener { _, _, scrollY, _, _ ->

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

}

class SearchEvent() : NavigationEvent()