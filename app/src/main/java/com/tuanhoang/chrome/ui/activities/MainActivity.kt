package com.tuanhoang.chrome.ui.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.view.doOnPreDraw
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.one.adapter.MultiAdapter
import com.one.coreapp.ui.base.activities.BaseViewModelActivity
import com.one.coreapp.utils.extentions.*
import com.one.navigation.Navigation
import com.one.navigation.NavigationEvent
import com.tuanhoang.chrome.R
import com.tuanhoang.chrome.databinding.ActivityMainBinding
import com.tuanhoang.chrome.entities.Tab
import com.tuanhoang.chrome.ui.activities.adapter.TabAdapter
import com.tuanhoang.chrome.ui.activities.adapter.TabViewItem
import com.tuanhoang.chrome.ui.tab.TabFragment
import com.tuanhoang.chrome.ui.tab.TabView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

class MainActivity : BaseViewModelActivity<ActivityMainBinding, MainViewModel>(), Navigation {


    private var adapter: MultiAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        setupAddTab()
        setupStatusBar()
        setupRecyclerView()

        observeData()

        lifecycleScope.launchWhenResumed {

            openSingleTab(viewModel.getTab(""), false)
        }
    }

    private fun setupAddTab() {

        val binding = binding ?: return

        binding.ivAdd.setDebouncedClickListener {

            openSingleTab(viewModel.getTab(""))
        }
    }

    private fun setupStatusBar() {

        val binding = binding ?: return

        val updateUi: (WindowInsets) -> Unit = {

            val statusHeight = it.getStatusBar()
            val navigationHeight = it.getNavigationBar()

            if (statusHeight > 0) binding.recTab.updatePadding(top = statusHeight)
            if (navigationHeight > 0) binding.vBackgroundAction.updateMargin(bottom = navigationHeight)
        }

        window.decorView.setOnApplyWindowInsetsListener { _, insets ->

            updateUi.invoke(insets)
            insets
        }

        binding.root.doOnPreDraw {

            updateUi.invoke(binding.root.rootWindowInsets)
        }
    }

    private fun setupRecyclerView() {

        val binding = binding ?: return

        val tabAdapter = TabAdapter { _, tabViewItem ->

            openSingleTab(tabViewItem.data)
        }

        adapter = MultiAdapter(tabAdapter).apply {

            binding.recTab.adapter = this
            binding.recTab.layoutManager = GridLayoutManager(this@MainActivity, 2)
        }
    }

    private fun observeData() = with(viewModel){

        tabViewItemListDisplay.observe(this@MainActivity) {

            val binding = binding ?: return@observe

            lifecycleScope.launch {

                if (it.size > (adapter?.itemCount ?: 0)) delay(350)

                suspendCancellableCoroutine<Boolean> { a ->

                    adapter?.submitList(it) {
                        a.resumeActive(true)
                    }
                }

                binding.recTab.scrollToPosition(it.indexOfLast { (it as? TabViewItem)?.data?.isCurrent == true }.takeIf { it >= 0 } ?: (it.size - 1))
            }
        }
    }

    override fun onNavigationEvent(event: NavigationEvent): Boolean {

//        if (event is AssetEvent) {
//
//            supportFragmentManager.beginTransaction().replace(R.id.frame_content, AssetFragment.newInstance(), "").addToBackStack("").commitAllowingStateLoss()
//            return true
//        }

        return super.onNavigationEvent(event)
    }

    fun openMultiTab() {

        val binding = binding ?: return


        val fragment = supportFragmentManager.fragments.find { it is TabView } ?: return
        fragment.onPause()


        val tabViewItemListDisplay = viewModel.tabViewItemListDisplay.getOrEmpty()

        val indexSelect = tabViewItemListDisplay.indexOfLast { (it as? TabViewItem)?.data?.isCurrent == true }


        val layoutManager = binding.recTab.layoutManager as GridLayoutManager

        var viewItemSelect = layoutManager.findViewByPosition(indexSelect)

        viewItemSelect = viewItemSelect ?: binding.ivAdd


        binding.root.show(binding.frameContent, viewItemSelect)
        binding.frameContent.visibility = View.GONE
    }

    private fun openSingleTab(tab: Tab, anim: Boolean = true) = lifecycleScope.launch {

        val binding = binding ?: return@launch


        supportFragmentManager.beginTransaction().replace(R.id.frame_content, TabFragment.newInstance(tabId = tab.id)).commitAllowingStateLoss()


        val layoutManager = binding.recTab.layoutManager as GridLayoutManager


        val tabViewItemListDisplay = viewModel.tabViewItemListDisplay.getOrEmpty()

        val indexSelect = tabViewItemListDisplay.indexOfLast { (it as? TabViewItem)?.data?.id == tab.id }

        val viewItemSelect = layoutManager.findViewByPosition(indexSelect) ?: binding.ivAdd


        if (anim) binding.root.show(viewItemSelect, binding.frameContent)


        viewItemSelect.visibility = View.VISIBLE
        binding.frameContent.visibility = View.VISIBLE


        val fragment = supportFragmentManager.fragments.find { it is TabView } ?: return@launch
        fragment.onResume()
    }
}