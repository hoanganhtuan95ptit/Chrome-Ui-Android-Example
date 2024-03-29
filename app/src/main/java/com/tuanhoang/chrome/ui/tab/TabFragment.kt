package com.tuanhoang.chrome.ui.tab

import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Range
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.core.view.marginEnd
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.AppBarLayout
import com.one.coreapp.ui.base.fragments.BaseFragment
import com.one.coreapp.ui.base.fragments.BaseViewBindingFragment
import com.one.coreapp.utils.extentions.*
import com.one.navigation.Navigation
import com.one.navigation.NavigationEvent
import com.tuanhoang.chrome.PARAM_TAB_ID
import com.tuanhoang.chrome.R
import com.tuanhoang.chrome.databinding.FragmentTabBinding
import com.tuanhoang.chrome.entities.GroupPageType
import com.tuanhoang.chrome.entities.Page
import com.tuanhoang.chrome.entities.Tab
import com.tuanhoang.chrome.ui.activities.MainActivity
import com.tuanhoang.chrome.ui.activities.MainViewModel
import com.tuanhoang.chrome.ui.tab.home.HomeFragment
import com.tuanhoang.chrome.ui.tab.home.OverviewEvent
import com.tuanhoang.chrome.ui.tab.search.SearchEvent
import com.tuanhoang.chrome.ui.tab.search.SearchFragment
import com.tuanhoang.chrome.ui.tab.web.WebEvent
import com.tuanhoang.chrome.ui.tab.web.WebFragment
import com.tuanhoang.chrome.utils.ext.getBitmap
import com.tuanhoang.chrome.utils.ext.setDrag
import com.tuanhoang.chrome.utils.ext.updateMarginHorizontal
import kotlinx.coroutines.*
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.koin.getViewModel
import kotlin.math.absoluteValue
import kotlin.math.max


class TabFragment : BaseViewBindingFragment<FragmentTabBinding>(), Navigation, TabView {


    private var animation: Boolean = true

    private var pageTypeCurrent: GroupPageType? = null

    private var verticalOffsetCurrent = 0


    private val tab: Tab by lazy {
        mainViewModel.getTab(arguments?.getString(PARAM_TAB_ID) ?: "")
    }

    private val mainViewModel: MainViewModel by lazy {
        getKoin().getViewModel(requireActivity(), MainViewModel::class)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        animation = tab.pages.size <= 0

        setupTab()
        setupHome()
        setupSearch()
        setupStatusBar()
        setupAppBarLayout()

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {

            val groupPages = tab.pages.values.toList()
            openGroupPage(groupPages.getOrNull(groupPages.lastIndex))
        }

        observeMainData()
    }

    override fun onResume() {
        super.onResume()

        val fragmentLast = childFragmentManager.fragments.lastOrNull()

        if (fragmentLast !is PageView) return

        fragmentLast.onResume()
    }

    override fun onPause() {
        super.onPause()

        val binding = binding ?: return


        mainViewModel.updateTab(tab, binding.frameContent.getBitmap())


        val fragmentLast = childFragmentManager.fragments.lastOrNull()

        if (fragmentLast !is PageView) return

        fragmentLast.onPause()
    }

    override fun onBackPressed(): Boolean {

        val fragmentList = childFragmentManager.fragments.apply { reverse() }


        if (fragmentList.any { (it as? BaseFragment)?.onBackPressed() == true }) {

            return true
        }


        val groupPages = tab.pages.values.toList()


        return if (groupPages.size > 1) {

            openGroupPage(groupPages.getOrNull(groupPages.lastIndex - 1))

            true
        } else {

            false
        }
    }

    override fun onNavigationEvent(event: NavigationEvent): Boolean {

        if (event is WebEvent) {

            val page = event.page ?: event.url?.let {

                Page(type = GroupPageType.NORMAL, url = it)
            }

            addFragment(WebFragment.newInstance(page))

            return true
        }

        if (event is OverviewEvent) {

            val page = event.page ?: Page(type = GroupPageType.HOME)

            addFragment(HomeFragment.newInstance(page))

            return true
        }

        if (event is SearchEvent) {

            addFragment(SearchFragment())

            return true
        }

        return super.onNavigationEvent(event)
    }

    override fun onPageLogo(logo: String) {

        mainViewModel.updateTab(tab = tab, logo = logo)
    }

    override fun onPageTitle(title: String) {

        mainViewModel.updateTab(tab = tab, title = title)
    }

    override fun onPageShow(page: Page) = viewLifecycleOwner.lifecycleScope.launch {

        tab.addLast(page)

        updateState(animation, pageTypeNew = page.type, verticalOffset = page.verticalOffset).join()
    }

    override fun onPageHide(page: Page) = viewLifecycleOwner.lifecycleScope.launch {

    }

    override fun onPageRemove(page: Page) = viewLifecycleOwner.lifecycleScope.launch {

        val binding = binding ?: return@launch

        tab.remove(page)

        if (page.type == GroupPageType.SEARCH) {

            val inputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            val view = requireActivity().currentFocus ?: View(activity)
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

            binding.edtSearch.clearFocus()
        }
    }

    private fun setupTab() {

        val binding = binding ?: return

        binding.tvViewTab1.setDebouncedClickListener {

            (activity as? MainActivity)?.openMultiTab()
        }

        binding.tvViewTab.setDebouncedClickListener {

            (activity as? MainActivity)?.openMultiTab()
        }
    }

    private fun setupHome() {

        val binding = binding ?: return

        binding.ivHome.setDebouncedClickListener {

            offerNavEvent(OverviewEvent(null))
        }
    }

    private fun setupSearch() {

        val binding = binding ?: return

        binding.edtSearch.addTextChangedListener {

            childFragmentManager.fragments.filterIsInstance<PageView>().lastOrNull()?.updateQuery(it.toString())
        }

        binding.edtSearch.setOnFocusChangeListener { _, b ->

            if (b) viewLifecycleOwner.lifecycleScope.launch {

                updateState(animation, pageTypeNew = GroupPageType.SEARCH).join()
                offerNavEvent(SearchEvent())
            }
        }
    }

    private fun setupStatusBar() = requireActivity().window.decorView.setOnApplyWindowInsetsListener { _, insets ->

        val binding = binding ?: return@setOnApplyWindowInsetsListener insets

        val statusHeight = insets.getStatusBar()

        if (statusHeight > 0) binding.coordinatorLayout.updatePadding(top = statusHeight)
        if (statusHeight > 0) binding.frameContent.resize(height = requireActivity().window.decorView.height - statusHeight - 56.toPx())

        insets
    }

    private fun setupAppBarLayout() {

        val binding = binding ?: return

        binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->


            if (verticalOffsetCurrent == verticalOffset) return@OnOffsetChangedListener
            verticalOffsetCurrent = verticalOffset


            val fragmentLast = childFragmentManager.fragments.last()


            if (pageTypeCurrent == GroupPageType.HOME && fragmentLast is PageView) {

                fragmentLast.updateVerticalOffset(verticalOffset)
            }


            if (pageTypeCurrent != GroupPageType.HOME) {

                return@OnOffsetChangedListener
            }


            val percent = verticalOffset.absoluteValue * 100 / binding.appBarLayout.totalScrollRange.absoluteValue

            val percentRange = Range(90, 100)


            val marginHorizontalRang = Range(16.toPx(), 24.toPx())

            val marginHorizontal = marginHorizontalRang.upper - (marginHorizontalRang.upper - marginHorizontalRang.lower) * max(0, percent - percentRange.lower) / (percentRange.upper - percentRange.lower)

            binding.edtSearch.updateMarginHorizontal(marginHorizontal)
        })
    }

    private fun observeMainData() = with(mainViewModel) {

        tabList.observe(viewLifecycleOwner) {

            val binding = binding ?: return@observe

            val tabNumber = if (it.size > 10) "9+" else "${it.size}"

            binding.tvViewTab.text = tabNumber
            binding.tvViewTab1.text = tabNumber
        }
    }

    private fun addFragment(fragment: Fragment) {

        childFragmentManager.beginTransaction().replace(R.id.frame_content, fragment, fragment.javaClass.name).commitAllowingStateLoss()
    }

    private fun openGroupPage(page: Page?) {

        if (page == null || page.type == GroupPageType.HOME) {

            offerNavEvent(OverviewEvent(page))
        } else if (page.type == GroupPageType.NORMAL) {

            offerNavEvent(WebEvent(page))
        }
    }

    private suspend fun updateState(animation: Boolean, pageTypeNew: GroupPageType, verticalOffset: Int = 0) = launch(actionName = "updateState") {

        val binding = binding ?: return@launch


        this@TabFragment.animation = true
        this@TabFragment.pageTypeCurrent = pageTypeNew


        suspendCancellableCoroutine<Boolean> { continuation ->

            binding.appBarLayout.post {
                continuation.resumeActive(true)
            }
        }


        while (isActive) {

            if (binding.appBarLayout.totalScrollRange != 0) break
            delay(100)
        }


        updateStateSuspend(animation, pageTypeNew, verticalOffset)
    }

    @SuppressLint("Recycle")
    private suspend fun updateStateSuspend(animation: Boolean, pageTypeNew: GroupPageType, verticalOffset: Int = 0) = suspendCancellableCoroutine<Boolean> { continuation ->

        val binding = binding ?: return@suspendCancellableCoroutine

        val param = binding.appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = param.behavior as? AppBarLayout.Behavior ?: return@suspendCancellableCoroutine


        val list = arrayListOf<PropertyValuesHolder>()


        val verticalOffsetNew = if (pageTypeNew != GroupPageType.HOME) -binding.appBarLayout.totalScrollRange else verticalOffset


        if (verticalOffsetCurrent != verticalOffsetNew) PropertyValuesHolder.ofInt(OFFSET, verticalOffsetCurrent, verticalOffsetNew).let {

            list.add(it)
        }


        val searchMarginEndNew = if (pageTypeNew == GroupPageType.SEARCH || (pageTypeNew == GroupPageType.HOME && verticalOffsetNew == -binding.appBarLayout.totalScrollRange)) {
            DP_16
        } else if (pageTypeNew == GroupPageType.HOME) {
            DP_24
        } else {
            DP_96
        }

        val searchMarginEndCurrent = binding.edtSearch.marginEnd

        if (searchMarginEndCurrent != searchMarginEndNew) PropertyValuesHolder.ofInt(SEARCH_MARGIN_END, searchMarginEndCurrent, searchMarginEndNew).let {

            list.add(it)
        }


        val searchMarginStartNew = if (pageTypeNew == GroupPageType.SEARCH || (pageTypeNew == GroupPageType.HOME && verticalOffsetNew == -binding.appBarLayout.totalScrollRange)) {
            DP_16
        } else if (pageTypeNew == GroupPageType.HOME) {
            DP_24
        } else {
            DP_56
        }

        val searchMarginStartCurrent = binding.edtSearch.marginEnd

        if (searchMarginStartCurrent != searchMarginEndNew) PropertyValuesHolder.ofInt(SEARCH_MARGIN_START, searchMarginStartCurrent, searchMarginStartNew).let {

            list.add(it)
        }


        val homeTranslationXNew = if (pageTypeNew != GroupPageType.NORMAL) {
            -DP_48.toFloat()
        } else {
            DP_0.toFloat()
        }

        val homeTranslationXCurrent = binding.ivHome.translationX

        if (homeTranslationXCurrent != homeTranslationXNew) PropertyValuesHolder.ofFloat(HOME_TRANSACTION_X, homeTranslationXCurrent, homeTranslationXNew).let {

            list.add(it)
        }


        val tabTranslationXNew = if (pageTypeNew != GroupPageType.NORMAL) {
            DP_96.toFloat()
        } else {
            DP_0.toFloat()
        }

        val tabTranslationXCurrent = binding.tvViewTab.translationX

        if (tabTranslationXCurrent != tabTranslationXNew) PropertyValuesHolder.ofFloat(TAB_TRANSACTION_X, tabTranslationXCurrent, tabTranslationXNew).let {

            list.add(it)
        }


        val profileTranslationXNew = if (pageTypeNew != GroupPageType.NORMAL) {
            DP_96.toFloat()
        } else {
            DP_0.toFloat()
        }

        val profileTranslationXCurrent = binding.ivAccount.translationX

        if (profileTranslationXCurrent != profileTranslationXNew) PropertyValuesHolder.ofFloat(PROFILE_TRANSACTION_X, profileTranslationXCurrent, profileTranslationXNew).let {

            list.add(it)
        }


        if (list.isEmpty()) {

            binding.appBarLayout.setDrag(behavior, pageTypeNew == GroupPageType.HOME)

            continuation.resumeActive(true)

            return@suspendCancellableCoroutine
        }


        val animator = list.animation(duration = if (animation) 350 else 0, onStart = {

            binding.appBarLayout.setDrag(behavior, true)
        }, onUpdate = { animator ->

            binding.edtSearch.updateMarginHorizontal(animator.getAnimatedValue(SEARCH_MARGIN_START) as? Int, animator.getAnimatedValue(SEARCH_MARGIN_END) as? Int)

            (animator.getAnimatedValue(HOME_TRANSACTION_X) as? Float)?.let {
                binding.ivHome.translationX = it
            }

            (animator.getAnimatedValue(TAB_TRANSACTION_X) as? Float)?.let {
                binding.tvViewTab.translationX = it
                binding.vBackgroundTab.translationX = it
            }

            (animator.getAnimatedValue(PROFILE_TRANSACTION_X) as? Float)?.let {
                binding.ivAccount.translationX = it
            }

            (animator.getAnimatedValue(OFFSET) as? Int)?.let {
                behavior.topAndBottomOffset = it
                binding.appBarLayout.requestLayout()
            }
        }, onEnd = {

            binding.appBarLayout.setDrag(behavior, pageTypeNew == GroupPageType.HOME)


            val layoutParams = binding.frameContent.layoutParams as CoordinatorLayout.LayoutParams

            layoutParams.behavior = if (pageTypeNew == GroupPageType.HOME) {
                binding.frameContent.translationY = 0f
                AppBarLayout.ScrollingViewBehavior()
            } else {
                binding.frameContent.translationY = (binding.appBarLayout.bottom - binding.coordinatorLayout.paddingTop).toFloat()
                ContentCollapsingBehavior()
            }

            binding.frameContent.requestLayout()


            continuation.resumeActive(true)
        })

        continuation.invokeOnCancellation {

            animator.cancel()
        }
    }

    companion object {

        private const val DP_0 = 0

        private val DP_4 = 4.toPx()
        private val DP_16 = DP_4 * 4
        private val DP_24 = DP_4 * 6
        private val DP_48 = DP_4 * 12
        private val DP_56 = DP_4 * 14
        private val DP_96 = DP_4 * 24

        private const val OFFSET = "OFFSET"

        private const val SEARCH_MARGIN_END = "SEARCH_MARGIN_END"
        private const val SEARCH_MARGIN_START = "SEARCH_MARGIN_START"

        private const val TAB_TRANSACTION_X = "TAB_TRANSACTION_X"
        private const val HOME_TRANSACTION_X = "HOME_TRANSACTION_X"
        private const val PROFILE_TRANSACTION_X = "PROFILE_TRANSACTION_X"

        fun newInstance(tabId: String) = TabFragment().apply {

            arguments = bundleOf(PARAM_TAB_ID to tabId)
        }
    }
}

interface TabView {

    fun onPageLogo(logo: String)

    fun onPageTitle(title: String)


    fun onPageShow(page: Page): Job

    fun onPageHide(page: Page): Job

    fun onPageRemove(page: Page): Job
}

interface PageView {

    fun updateQuery(query: String) {

    }

    fun provideGroupPage(): Page? {

        return null
    }

    fun updateVerticalOffset(verticalOffset: Int) {

    }
}

class SearchCollapsingBehavior(context: Context?, attrs: AttributeSet?) : CoordinatorLayout.Behavior<View>(context, attrs) {

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {

        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {

        child.translationY = (dependency.bottom - parent.paddingTop - child.height).toFloat()

        return true
    }
}


class ContentCollapsingBehavior : CoordinatorLayout.Behavior<View>() {

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {

        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {

        child.translationY = (dependency.bottom - parent.paddingTop).toFloat()

        return true
    }
}