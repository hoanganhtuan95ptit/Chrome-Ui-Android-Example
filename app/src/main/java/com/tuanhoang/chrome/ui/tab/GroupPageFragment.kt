package com.tuanhoang.chrome.ui.tab

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.one.coreapp.ui.base.fragments.BaseViewBindingFragment
import com.tuanhoang.chrome.entities.GroupPage
import kotlinx.coroutines.launch

abstract class GroupPageFragment<T : ViewBinding>(@LayoutRes val layoutId: Int = 0) : BaseViewBindingFragment<T>(layoutId), PageView {

    abstract val groupPage: GroupPage


    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {

            (parentFragment as TabView).onPageShow(groupPage).join()

            onViewReady(view)
        }
    }

    override fun provideGroupPage(): GroupPage? {

        return groupPage
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()

        (parentFragment as TabView).onPageHide(groupPage)
    }

    open fun onViewReady(view: View) {

    }
}