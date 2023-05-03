package com.tuanhoang.chrome.ui.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.one.adapter.MultiAdapter
import com.one.coreapp.ui.base.activities.BaseViewModelActivity
import com.one.coreapp.utils.extentions.getOrEmpty
import com.one.coreapp.utils.extentions.resumeActive
import com.one.coreapp.utils.extentions.setDebouncedClickListener
import com.one.coreapp.utils.extentions.show
import com.one.navigation.Navigation
import com.one.navigation.NavigationEvent
import com.tuanhoang.chrome.R
import com.tuanhoang.chrome.databinding.ActivityMainBinding
import com.tuanhoang.chrome.entities.Tab
import com.tuanhoang.chrome.ui.tab.TabFragment
import com.tuanhoang.chrome.ui.tab.TabView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

class MainActivity : BaseViewModelActivity<ActivityMainBinding, MainViewModel>(), Navigation {


    private var adapter: MultiAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = binding ?: return

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT


        lifecycleScope.launchWhenResumed {

            openSingleTab(viewModel.getTab(""))
        }


        binding.ivAdd.setDebouncedClickListener {

            openSingleTab(viewModel.getTab(""))
        }


        val tabAdapter = TabAdapter { view, tabViewItem ->

            openSingleTab(tabViewItem.data)
        }


        adapter = MultiAdapter(tabAdapter).apply {

            binding.recTab.adapter = this
            binding.recTab.layoutManager = GridLayoutManager(this@MainActivity, 2)
        }

        viewModel.tabViewItemListDisplay.observe(this) {

            lifecycleScope.launch {

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

    fun openSingleTab(tab: Tab) = lifecycleScope.launch {

        val binding = binding ?: return@launch


        supportFragmentManager.beginTransaction().replace(R.id.frame_content, TabFragment.newInstance(tabId = tab.id)).commitAllowingStateLoss()


        val layoutManager = binding.recTab.layoutManager as GridLayoutManager


        var viewItemSelect: View? = null

        while (viewItemSelect == null && isActive) {

            val tabViewItemListDisplay = viewModel.tabViewItemListDisplay.getOrEmpty()

            val indexSelect = tabViewItemListDisplay.indexOfLast { (it as? TabViewItem)?.data?.id == tab.id }

            viewItemSelect = layoutManager.findViewByPosition(indexSelect)

            delay(10)
        }

        viewItemSelect = viewItemSelect ?: binding.ivAdd


        binding.root.show(viewItemSelect, binding.frameContent)
        binding.frameContent.visibility = View.VISIBLE


        val fragment = supportFragmentManager.fragments.find { it is TabView } ?: return@launch
        fragment.onResume()
    }
}