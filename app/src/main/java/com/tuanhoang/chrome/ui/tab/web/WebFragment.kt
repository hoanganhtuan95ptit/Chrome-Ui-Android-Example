package com.tuanhoang.chrome.ui.tab.web

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import com.one.coreapp.utils.extentions.setVisible
import com.one.navigation.NavigationEvent
import com.tuanhoang.chrome.PARAM_GROUP_PAGE
import com.tuanhoang.chrome.databinding.FragmentWebBinding
import com.tuanhoang.chrome.entities.GroupPage
import com.tuanhoang.chrome.ui.tab.GroupPageFragment

class WebFragment : GroupPageFragment<FragmentWebBinding>() {

    override val groupPage: GroupPage by lazy {

        requireArguments().getParcelable(PARAM_GROUP_PAGE)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        ViewCompat.setNestedScrollingEnabled(binding!!.root, false)

        super.onViewCreated(view, savedInstanceState)

        binding!!.webView.setOnScrollChangeListener { _, _, scrollY, _, _ ->

            groupPage.scrollY = scrollY
        }
    }

    override fun onViewReady(view: View) {
        super.onViewReady(view)

        if (groupPage.scrollY > 0) binding!!.webView.doOnPreDraw {

            binding!!.webView.scrollY = groupPage.scrollY
        }

        setupWeb()
    }

    override fun onResume() {
        super.onResume()

        val binding = binding ?: return

        binding.webView.onResume()
    }

    override fun onPause() {
        super.onPause()

        val binding = binding ?: return

        binding.webView.onPause()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWeb() {

        val binding = binding ?: return

        val webView = binding.webView

        val info = try {
            requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        } catch (e: Throwable) {
            null
        }

        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.domStorageEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.settings.userAgentString = webView.settings.userAgentString + "Krystal(Platform=Android&AppVersion=${info?.versionName})"

        webView.webChromeClient = object : WebChromeClient() {

            override fun onProgressChanged(webview: WebView, newProgress: Int) {

                val binding = this@WebFragment.binding ?: return

                if (newProgress == 100) {
                    binding.swipeRefresh.isRefreshing = false
                } else {
                    binding.progressBar.progress = newProgress
                }

                binding.progressBar.setVisible(newProgress != 100 && !binding.swipeRefresh.isRefreshing)
            }
        }

        webView.webViewClient = object : WebViewClient() {

        }

        binding.swipeRefresh.setOnRefreshListener {

            binding.webView.reload()
        }

        binding.webView.loadUrl(groupPage.pages.values.lastOrNull()?.url ?: "https://www.google.com")
    }

    companion object {

        fun newInstance(groupPage: GroupPage?) = WebFragment().apply {

            arguments = bundleOf(PARAM_GROUP_PAGE to groupPage)
        }
    }
}

class WebEvent(val groupPage: GroupPage? = null, val url: String? = null) : NavigationEvent()