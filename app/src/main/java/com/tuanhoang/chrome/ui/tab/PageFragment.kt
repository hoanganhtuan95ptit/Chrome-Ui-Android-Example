package com.tuanhoang.chrome.ui.tab

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.one.coreapp.ui.base.fragments.BaseViewModelFragment
import com.tuanhoang.chrome.entities.Page
import kotlinx.coroutines.launch

abstract class PageFragment<T : ViewBinding, VM : ViewModel>(@LayoutRes val layoutId: Int = 0) : BaseViewModelFragment<T, VM>(layoutId), PageView {

    abstract val page: Page


    override var isSupportTransition: Boolean = false


    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {

            (parentFragment as TabView).onPageShow(page).join()

            onViewReady(view)
        }
    }

    open fun onViewReady(view: View) {

    }

    override fun provideGroupPage(): Page? {

        return page
    }

    override fun onResume() {
        super.onResume()
        Log.d("tuanha", "onResume: ${this.javaClass.simpleName}")
    }

    override fun onPause() {
        super.onPause()
        Log.d("tuanha", "onPause: ${this.javaClass.simpleName}")
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()

        (parentFragment as TabView).onPageHide(page)
    }

}