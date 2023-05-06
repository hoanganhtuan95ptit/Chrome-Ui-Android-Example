package com.tuanhoang.chrome.ui.tab.web

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import com.one.coreapp.utils.extentions.getOrEmpty
import com.one.coreapp.utils.extentions.setVisible
import com.one.navigation.NavigationEvent
import com.tuanhoang.chrome.LOGO_PAGE_DEFAULT
import com.tuanhoang.chrome.PARAM_GROUP_PAGE
import com.tuanhoang.chrome.databinding.FragmentWebBinding
import com.tuanhoang.chrome.entities.Page
import com.tuanhoang.chrome.ui.activities.MainViewModel
import com.tuanhoang.chrome.ui.tab.GroupPageFragment
import com.tuanhoang.chrome.ui.tab.TabView
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.koin.getViewModel
import java.net.URL

class WebFragment : GroupPageFragment<FragmentWebBinding, MainViewModel>() {


    override val page: Page by lazy {

        val page = requireArguments().getParcelable<Page>(PARAM_GROUP_PAGE)!!

        viewModel.tabList.getOrEmpty().flatMap { it.pages.values }.find { it.id == page.id } ?: page
    }


    override val viewModel: MainViewModel by lazy {
        getKoin().getViewModel(requireActivity(), MainViewModel::class)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        ViewCompat.setNestedScrollingEnabled(binding!!.root, false)

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onViewReady(view: View) {
        super.onViewReady(view)

        setupWeb()
    }

    override fun onBackPressed(): Boolean {

        val binding = binding ?: return false

        return if (binding.webView.canGoBack()) {

            binding.webView.goBack()

            true
        } else {

            false
        }
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

        val bundle = bundleOf()
        binding.webView.saveState(bundle)
        viewModel.updatePage(page, bundle.getByteArray(WEBVIEW_CHROMIUM_STATE))
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

        webView.settings.userAgentString = webView.settings.userAgentString + "Chrome(Platform=Android&AppVersion=${info?.versionName})"

        webView.setOnScrollChangeListener { _, _, scrollY, _, _ ->

            page.scrollY = scrollY
        }

        webView.webChromeClient = object : WebChromeClient() {


            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)

                this@WebFragment.binding ?: return

                (parentFragment as? TabView)?.onPageTitle(title ?: return)
            }

            override fun onProgressChanged(webview: WebView, newProgress: Int) {

                this@WebFragment.binding ?: return

                if (newProgress == 100) {
                    binding.swipeRefresh.isRefreshing = false
                } else {
                    binding.progressBar.progress = newProgress
                }

                binding.progressBar.setVisible(newProgress != 100 && !binding.swipeRefresh.isRefreshing)
            }
        }

        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

                this@WebFragment.binding ?: return

                val logo = kotlin.runCatching {
                    "https://www.google.com/s2/favicons?sz=128&domain=${URL(url).host}"
                }.getOrElse {
                    LOGO_PAGE_DEFAULT
                }

                (parentFragment as? TabView)?.onPageLogo(logo)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }

        binding.swipeRefresh.setOnRefreshListener {

            binding.webView.reload()
        }

        if (page.byte == null) {

            binding.webView.loadUrl(page.url)
        } else {

            binding.webView.restoreState(bundleOf(WEBVIEW_CHROMIUM_STATE to page.byte))
        }
    }

    companion object {

        private const val WEBVIEW_CHROMIUM_STATE = "WEBVIEW_CHROMIUM_STATE"

        fun newInstance(page: Page?) = WebFragment().apply {

            arguments = bundleOf(PARAM_GROUP_PAGE to page)
        }
    }
}

class WebEvent(val page: Page? = null, val url: String? = null) : NavigationEvent()