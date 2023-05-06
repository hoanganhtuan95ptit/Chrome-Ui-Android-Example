package com.tuanhoang.chrome.utils

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebSettings
import android.webkit.WebView

class WebManager(private val context: Context) {

    private val idAndWeb: LinkedHashMap<String, WebView> = linkedMapOf()

    private val versionName by lazy {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun getWeb(id: String): WebView {

        val webView = idAndWeb[id] ?: WebView(context).apply {

            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.domStorageEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            settings.userAgentString = settings.userAgentString + "Krystal(Platform=Android&AppVersion=${versionName})"

            idAndWeb[id] = this
        }

        return webView
    }
}