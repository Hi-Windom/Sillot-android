/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/21 17:00
 * updated: 2024/7/21 17:00
 */

package sc.windom.sofill.android.webview

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.webkit.WebView
import sc.windom.sofill.U.isLightColor
import sc.windom.sofill.Us.U_Layout.statusBarHeight


/**
 * 推荐在四处调用，将良好的沉浸式状态栏体验带到 webView：
 * 在 [WebViewLayoutManager] 的 onConfigurationChangedCallback、 onLayoutChangedCallback，
 * 在 [android.webkit.WebChromeClient] 的 onPageFinished 中调用.
 * 在 [android.app.Activity] 的 onForeground 中调用
 *
 * `webView.rootView.setBackgroundColor(statusBarBelowColor)` 与 [WebViewLayoutManager] 进行联动（键盘弹出时延时重置布局并不总是理想，有时候还是会漏出布局底色露馅，
 * 一般来说 statusBarBelowColor 与网页背景色是一致的，因此不容易被发现）。
 * @suppress 不可能一直监听状态栏下方区域颜色是否变化，这样的开销没必要。因此如果切换主题不触发页面重载，即不经过onPageFinished，可以通过其他方式手动触发一下，问题不大。
 * 或者可以通过 JSAndroid 调用触发
 * @param isRoot webview 是否是根布局。无论是否是根布局我们都假设 webview 顶部与状态栏无缝衔接。当沉浸式异常时可以尝试设置为 true
 *
 */
@JvmOverloads // 自动生成带有默认参数的重载函数供 java 使用
fun applySystemThemeToWebView(
    activity: Activity,
    webView: WebView,
    isRoot: Boolean = false,
) {
    Handler(Looper.getMainLooper()).postDelayed({
        val statusBarBelowColor = activity.setStatusBarColorFromBelowStatusBar(webView, isRoot)
        statusBarBelowColor?.let {
            if (isRoot) webView.setBackgroundColor(it)
            else webView.rootView.setBackgroundColor(it)
        }
    }, 0) // 延时只有调试的时候需要
}

private fun Activity.setStatusBarIconColorAccordingToColor(color: Int) {
    // 根据状态栏颜色决定状态栏图标颜色（深色或浅色）
    val lightIcons = color.isLightColor()
    window.insetsController?.setSystemBarsAppearance(
        if (lightIcons) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
    )
}

private fun Activity.setStatusBarColorFromBelowStatusBar(view: View, isRoot: Boolean): Int? {
    // 获取状态栏下方的颜色
    val statusBarBelowColor = getDominantColorFromBelowStatusBar(view, isRoot)
    // 设置状态栏颜色
    if (statusBarBelowColor != null) {
        window.statusBarColor = statusBarBelowColor
    }
    // 根据状态栏颜色调整图标颜色（深色或浅色）
    statusBarBelowColor?.let { setStatusBarIconColorAccordingToColor(it) }
    return statusBarBelowColor
}

private fun getDominantColorFromBelowStatusBar(view: View, isRoot: Boolean): Int? {
    val _view = if (isRoot) view else view.rootView
    // 获取状态栏高度，如果view或其rootView为null，则返回null
    val statusBarHeight = _view?.statusBarHeight ?: return null

    // 获取状态栏下方的视图，如果获取失败，则返回null
    val decorView = _view as? ViewGroup ?: return null
    val statusBarBelowView = decorView.findViewById<View>(android.R.id.content) ?: return null

    // 获取状态栏下方视图的位图
    statusBarBelowView.isDrawingCacheEnabled = true
    statusBarBelowView.buildDrawingCache(true)
    val bitmap = statusBarBelowView.drawingCache ?: return null

    // 获取状态栏下方的颜色
    val dominantColor = bitmap.getPixel(0, statusBarHeight)

    // 清理缓存
    statusBarBelowView.isDrawingCacheEnabled = false
    statusBarBelowView.destroyDrawingCache()

    return dominantColor
}
