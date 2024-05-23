package org.b3log.siyuan.ld246.utils

import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class AuthorizedWebViewClient(private val accessToken: String) : WebViewClient() {
    private val client = OkHttpClient()
    private val TAG = "AuthorizedWebViewClient"

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val url = request.url.toString()
        if (request.method == "GET" && url.startsWith("http")) {
            val requestBuilder = Request.Builder().url(url)
                .addHeader("Authorization", "token $accessToken")
            try {
                val response = client.newCall(requestBuilder.build()).execute()
                val contentType = response.header("Content-Type") ?: "text/html"
                val contentEncoding = response.header("Content-Encoding")
                return WebResourceResponse(contentType, contentEncoding, response.body?.byteStream())
            } catch (e: IOException) {
                Log.e(TAG, e.toString())
            }
        }
        return super.shouldInterceptRequest(view, request)
    }
}