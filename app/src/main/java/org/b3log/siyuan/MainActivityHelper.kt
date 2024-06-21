package org.b3log.siyuan

import android.view.DragEvent
import android.webkit.WebView
import kotlinx.serialization.encodeToString
import mobile.Mobile
import sc.windom.potter.producer.MainPro
import sc.windom.sofill.U
import sc.windom.sofill.Us.U_Pro.ContentInsertCallback
import sc.windom.sofill.Us.U_Pro.onDragSend2Producer
import sc.windom.sofill.Us.U_Pro.onDragWebView
import sc.windom.sofill.dataClass.IInsertBlockNextRequest
import java.io.File


/**
 * 文本插入webview光标处，文件转发到中转站。
 * 图文混排无法解决，这是官方API限制。测试文章 https://www.bilibili.com/read/mobile?id=22096084
 * @see [sc.windom.sofill.Us.U_Pro.onDragWebView]
 */
fun WebView.onDragInsertIntoWebView(event: DragEvent) {
    val TAG = "View.onDragInsertIntoWebView"
    this.onDragWebView(event, object : ContentInsertCallback {
        override fun onStringContentInsert(content: String, webView: WebView) {
            // 获取 ID，用于锚定插入位置
            webView.evaluateJavascript(
                "(function() { " +
                        "var range = window.getSelection().getRangeAt(0);" +
                        "var parentElement = range.startContainer.parentElement;" +
                        "while (parentElement && !parentElement.hasAttribute('data-node-id')) {" +
                        "  parentElement = parentElement.parentElement;" +
                        "}" +
                        "return parentElement ? parentElement.getAttribute('data-node-id') : null;" +
                        "})()"
            ) { value ->
                // 这里处理返回的 data-node-id 值
                // Log.d(TAG, "data-node-id: $value")
                val md = U.DOSC.text2Markdown(content)
                value?.let {
                    val payload = IInsertBlockNextRequest(
                        Data = md,
                        DataType = "markdown",
                        PreviousID = it.replace("\"", "")
                    )
                    val paramsJSON =
                        kotlinx.serialization.json.Json.encodeToString(payload)
                    // Log.i(TAG, paramsJSON)
                    Mobile.insertBlockNext(paramsJSON)
                }
            }
        }

        override fun onFileContentInsert(file: File, webView: WebView) {
            // 如果是文件，转发处理
            webView.onDragSend2Producer(event, MainPro::class.java)
        }

        override fun onDefault(event: DragEvent, webView: WebView) {
            // 其他情况，转发处理
            webView.onDragSend2Producer(event, MainPro::class.java)
        }
    })
}
