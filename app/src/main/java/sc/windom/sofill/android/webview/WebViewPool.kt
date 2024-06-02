package sc.windom.sofill.android.webview

import android.content.Context
import android.webkit.WebView

class WebViewPool private constructor() {
    companion object {
        private var instance: WebViewPool? = null

        fun getInstance(): WebViewPool {
            if (instance == null) {
                instance = WebViewPool()
            }
            return instance!!
        }
    }

    private val webViews = mutableListOf<WebView>()

    fun getWebView(context: Context): WebView {
        // 从池中获取WebView，如果池中没有，则创建一个新的
        return if (webViews.isEmpty()) {
            WebView(context).apply { webViews.add(this) }
        } else {
            webViews.removeAt(0)
        }
    }

    fun releaseWebView(webView: WebView) {
        // 将WebView放回池中
        webViews.add(webView)
    }
}
