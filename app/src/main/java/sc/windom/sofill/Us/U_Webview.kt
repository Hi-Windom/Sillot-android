/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/7 06:24
 * updated: 2024/8/7 06:24
 */

package sc.windom.sofill.Us

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.View
import android.webkit.JsResult
import android.webkit.ValueCallback
import android.webkit.WebSettings
import android.webkit.WebView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kongzue.dialogx.dialogs.PopNotification
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener
import sc.windom.sofill.Ss.S_Uri
import sc.windom.sofill.Ss.S_Webview
import sc.windom.sofill.Ss.S_packageName
import sc.windom.sofill.U.compareVersions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object U_Webview {
}

@JvmStatic
fun Context.getWebViewVer(): String {
    var webViewVer = ""
    val packageManager = this.packageManager
    val webViewPackageInfo = packageManager.getPackageInfo(S_packageName.GoogleWebview, 0)
    webViewVer = webViewPackageInfo.versionName
    Log.w("U_Webview", webViewVer)
    return webViewVer
}

@JvmStatic
fun Activity.checkWebViewVer(minVer: String) {
    val webViewVer = getWebViewVer()
    val result = compareVersions(webViewVer, minVer)
    Log.w("U_Webview", "checkWebViewVer result : $result")
    if (result < 0) {
        PopNotification.show("系统 WebView 版本 $webViewVer 太低, 请升级至 $minVer+").noAutoDismiss().onPopNotificationClickListener =
            object : OnDialogButtonClickListener<PopNotification?> {
                override fun onClick(
                    p0: PopNotification?,
                    p1: View?
                ): Boolean {
                    U_Uri.openURLUseDefaultApp(S_Uri.URL_Sillot_docs_uprade_webview)
                    return true
                }
            }
    }
}

/**
 * 注入 VConsole 方便调试
 */
fun WebView.injectVConsole(resultCallback: ValueCallback<String?>? = null) {
    val js = """
        let script = document.createElement('script');
        script.type = 'text/javascript';
        script.src = 'https://unpkg.com/vconsole@latest/dist/vconsole.min.js';
        document.head.appendChild(script);
        script.onload = function() {
            var vConsole = new window.VConsole();
            vConsole.showSwitch();
        };
"""
    this.evaluateJavascript(js, resultCallback)
}

/**
 * 注入 Eruda 方便调试
 */
fun WebView.injectEruda(resultCallback: ValueCallback<String?>? = null) {
    val js = """
                    (function () {
                        if (window.eruda) return;
                        var define;
                        if (window.define) {
                            define = window.define;
                            window.define = null;
                        }
                        var script = document.createElement('script'); 
                        script.src = '//cdn.jsdelivr.net/npm/eruda'; 
                        document.body.appendChild(script); 
                        script.onload = function () { 
                            eruda.init();
                            if (define) {
                                window.define = define;
                            }
                        }
                    })();
                """
    this.evaluateJavascript(js, resultCallback)
}

/**
 * 修复QQ授权登录界面“一键登录”按钮不显示的问题。**这是临时性修复，问题根源需要去解决，多半是 webview 布局出现了问题（肉眼观察不出来）**
 * @see [sc.windom.sofill.android.webview.WebViewLayoutManager]
 */
fun WebView.fixQQAppLaunchButton(resultCallback: ValueCallback<String?>? = null) {
    val js = """
        let e = document.querySelector("#onekey");
        if (e) { e.style.position = "relative"; }
"""
    this.evaluateJavascript(js, resultCallback)
}

@SuppressLint("SetJavaScriptEnabled")
@JvmStatic
@JvmOverloads
fun WebSettings.applyDefault(
    webViewTextZoom: Int = 100,
    ua: String = S_Webview.UA_edge_android
): WebSettings {
    apply {
        javaScriptEnabled = true
        allowFileAccess = true
        allowContentAccess = true
        cacheMode = WebSettings.LOAD_NO_CACHE
        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW // 允许Http和Https混合
        textZoom = webViewTextZoom
        useWideViewPort = true
        loadWithOverviewMode =
            false // 设置 WebView 是否以概览模式加载页面，即按宽度缩小内容以适应屏幕。设为 true 实测发现 github 等页面会有个不美观的抽搐过程
        userAgentString = ua
        /**
         * 这个设置具有全局效果，会影响到一个进程中的所有 WebView 实例；
         * 如果在页面已经开始加载之后才去修改这个设置，WebView 实例会忽略对此设置的更改
         */
        databaseEnabled = true
        domStorageEnabled = true
        /**
         * `window.open()` 允许 JavaScript 打开一个新窗口，默认情况下会拦截此调用。
         */
        javaScriptCanOpenWindowsAutomatically = true
    }
    return this
}

@JvmStatic
fun Activity.showJSAlert(
    view: WebView,
    url: String?,
    message: String?,
    result: JsResult?
) {
    val date = Date()
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    val formattedDate = sdf.format(date)

    MaterialAlertDialogBuilder(this)
        .setTitle("[WebChromeClient] onJsAlert from WebView")
        .setMessage(
            """
        
        -------------------------------
        $message
        -------------------------------
        
        * ${view.title}
        * $formattedDate
        * $url
        """.trimIndent()
        )
        .setPositiveButton("OK") { dialog: DialogInterface?, which: Int -> result!!.confirm() }
        .setCancelable(false)
        .show()
}