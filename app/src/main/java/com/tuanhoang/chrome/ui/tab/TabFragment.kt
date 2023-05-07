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
import androidx.core.view.marginStart
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


    private lateinit var tab: Tab


    private var animation: Boolean = true

    private var pageTypeCurrent: GroupPageType? = null

    private var verticalOffsetCurrent = 0


    private val mainViewModel: MainViewModel by lazy {
        getKoin().getViewModel(requireActivity(), MainViewModel::class)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tab = mainViewModel.getTab(arguments?.getString(PARAM_TAB_ID) ?: "")

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
        val navigationHeight = insets.getNavigationBar()

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

            binding.tvViewTab.setText(tabNumber)
            binding.tvViewTab1.setText(tabNumber)
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


        updateStateSupend(animation, pageTypeNew, verticalOffset)
    }

    @SuppressLint("Recycle")
    private suspend fun updateStateSupend(animation: Boolean, pageTypeNew: GroupPageType, verticalOffset: Int = 0) = suspendCancellableCoroutine<Boolean> { continuation ->

        val binding = binding ?: return@suspendCancellableCoroutine

        val param = binding.appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = param.behavior as? AppBarLayout.Behavior ?: return@suspendCancellableCoroutine


        val list = arrayListOf<PropertyValuesHolder>()


        val verticalOffsetNew = if (pageTypeNew != GroupPageType.HOME) -binding.appBarLayout.totalScrollRange else verticalOffset


        if (verticalOffsetCurrent != verticalOffsetNew) PropertyValuesHolder.ofInt(Offset, verticalOffsetCurrent, verticalOffsetNew).let {

            list.add(it)
        }

        val marginEndNew = if (pageTypeNew == GroupPageType.SEARCH || (pageTypeNew == GroupPageType.HOME && verticalOffsetNew == -binding.appBarLayout.totalScrollRange)) {
            DP_16
        } else if (pageTypeNew == GroupPageType.HOME) {
            DP_24
        } else {
            DP_96
        }

        if (binding.edtSearch.marginEnd != marginEndNew) PropertyValuesHolder.ofInt(marginEnd, binding.edtSearch.marginEnd, marginEndNew).let {

            list.add(it)
        }

        val marginStartNew = if (pageTypeNew == GroupPageType.SEARCH || (pageTypeNew == GroupPageType.HOME && verticalOffsetNew == -binding.appBarLayout.totalScrollRange)) {
            DP_16
        } else if (pageTypeNew == GroupPageType.HOME) {
            DP_24
        } else {
            DP_56
        }

        if (binding.edtSearch.marginEnd != marginEndNew) PropertyValuesHolder.ofInt(marginStart, binding.edtSearch.marginStart, marginStartNew).let {

            list.add(it)
        }


        val ivHomeTranslationXNew = if (pageTypeNew != GroupPageType.NORMAL) {
            -DP_48.toFloat()
        } else {
            DP_0.toFloat()
        }

        if (binding.ivHome.translationX != ivHomeTranslationXNew) PropertyValuesHolder.ofFloat(ivHomeTranslationX, binding.ivHome.translationX, ivHomeTranslationXNew).let {

            list.add(it)
        }


//        val ivAddTabTranslationXNew = if (pageTypeNew != GroupPageType.NORMAL) {
//            DP_128.toFloat()
//        } else {
//            DP_0.toFloat()
//        }
//
//        if (binding.ivAddTab.translationX != ivHomeTranslationXNew) PropertyValuesHolder.ofFloat(ivAddTabTranslationX, binding.ivAddTab.translationX, ivAddTabTranslationXNew).let {
//
//            list.add(it)
//        }


        val ivViewTabTranslationXNew = if (pageTypeNew != GroupPageType.NORMAL) {
            DP_96.toFloat()
        } else {
            DP_0.toFloat()
        }

        if (binding.tvViewTab.translationX != ivViewTabTranslationXNew) PropertyValuesHolder.ofFloat(ivViewTabTranslationX, binding.tvViewTab.translationX, ivViewTabTranslationXNew).let {

            list.add(it)
        }


        val ivMenuProfileTranslationXNew = if (pageTypeNew != GroupPageType.NORMAL) {
            DP_96.toFloat()
        } else {
            DP_0.toFloat()
        }

        if (binding.ivAccount.translationX != ivMenuProfileTranslationXNew) PropertyValuesHolder.ofFloat(ivMenuProfileTranslationX, binding.ivAccount.translationX, ivMenuProfileTranslationXNew).let {

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

            binding.edtSearch.updateMarginHorizontal(animator.getAnimatedValue(marginStart) as? Int, animator.getAnimatedValue(marginEnd) as? Int)

            (animator.getAnimatedValue(ivHomeTranslationX) as? Float)?.let {
                binding.ivHome.translationX = it
            }

//            (animator.getAnimatedValue(ivAddTabTranslationX) as? Float)?.let {
//                binding.ivAddTab.translationX = it
//            }

            (animator.getAnimatedValue(ivViewTabTranslationX) as? Float)?.let {
                binding.tvViewTab.translationX = it
                binding.vBackgroundTab.translationX = it
            }

            (animator.getAnimatedValue(ivMenuProfileTranslationX) as? Float)?.let {
                binding.ivAccount.translationX = it
            }

            (animator.getAnimatedValue(Offset) as? Int)?.let {
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
                CollapsingImageBehavior2()
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
        private val DP_128 = DP_4 * 32
        private val DP_136 = DP_4 * 34

        private const val Offset = "Offset"
        private const val marginEnd = "marginEnd"
        private const val marginStart = "marginStart"
        private const val ivHomeTranslationX = "ivHomeTranslationX"
        private const val ivAddTabTranslationX = "ivAddTabTranslationX"
        private const val ivViewTabTranslationX = "ivViewTabTranslationX"
        private const val ivMenuProfileTranslationX = "ivMenuProfileTranslationX"

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

class CollapsingImageBehavior(context: Context?, attrs: AttributeSet?) : CoordinatorLayout.Behavior<View>(context, attrs) {

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {

        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {

        child.translationY = (dependency.bottom - parent.paddingTop - child.height).toFloat()

        return true
    }
}


class CollapsingImageBehavior2 : CoordinatorLayout.Behavior<View>() {

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {

        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {

        child.translationY = (dependency.bottom - parent.paddingTop).toFloat()

        return true
    }
}