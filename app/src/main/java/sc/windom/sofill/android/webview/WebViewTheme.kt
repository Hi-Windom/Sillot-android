package sc.windom.sofill.android.webview

import android.app.Activity
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsetsController
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import org.b3log.siyuan.R
import sc.windom.sofill.U.DialogX
import sc.windom.sofill.U.isLightColor
import sc.windom.sofill.Us.U_Layout.statusBarHeight


/**
 * 推荐在四处调用，将良好的沉浸式状态栏体验带到webview：
 * 在 WebViewLayoutManager 的 onConfigurationChangedCallback、 onLayoutChangedCallback 和 onWindowInsetsListenerCallback 中调用，
 * webViewd 的 WebViewClient 的 onPageFinished 中调用.
 *
 * `webView.rootView.setBackgroundColor(statusBarBelowColor)` 与 [WebViewLayoutManager] 进行联动（键盘弹出时延时重置布局并不总是理想，有时候还是会漏出布局底色露馅，
 * 一般来说 statusBarBelowColor 与网页背景色是一致的，因此不容易被发现）。
 * @suppress 不可能一直监听状态栏下方区域颜色是否变化，这样的开销没必要。因此如果切换主题不触发页面重载，即不经过onPageFinished，可以通过其他方式手动触发一下，问题不大。
 * 或者可以通过 JSAndroid 调用触发
 *
 * @param forceWebViewFollowSystemDarkMode 决定是否自动在 webview 中强制跟随系统通过算法处理暗黑模式。如果前端已经有暗黑模式配置，此项应为 false（默认值）
 *
 */
fun applySystemThemeToWebView(
    activity: Activity,
    webView: WebView,
    forceWebViewFollowSystemDarkMode: Boolean
) {
    val currentNightMode: Int =
        activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
        // 系统处于暗色模式
        if (forceWebViewFollowSystemDarkMode) {
            DialogX.PopNoteShow(
                activity,
                R.drawable.icon,
                "系统深色模式",
                "已开启 WebView 自动明暗（实验性功能）"
            )
            webView.evaluateJavascript(
                "javascript:document.documentElement.setAttribute('data-theme-mode', 'dark')",
                null
            )
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) // 向下兼容代码，可以删除
            if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.getSettings(), true)
            }
        }
    } else {
        // 系统处于亮色模式
        if (forceWebViewFollowSystemDarkMode) {
            DialogX.PopNoteShow(
                activity,
                R.drawable.icon,
                "系统明亮模式",
                "已开启 WebView 自动明暗（实验性功能）"
            )
            webView.evaluateJavascript(
                "javascript:document.documentElement.setAttribute('data-theme-mode', 'light')",
                null
            )
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // 向下兼容代码，可以删除
            if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.getSettings(), false)
            }
        }
    }
    Handler(Looper.getMainLooper()).postDelayed({
        val statusBarBelowColor = activity.setStatusBarColorFromBelowStatusBar(webView)
        webView.rootView.setBackgroundColor(statusBarBelowColor)
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

private fun Activity.setStatusBarColorFromBelowStatusBar(view: View): Int {
    // 获取状态栏下方的颜色
    val statusBarBelowColor = getDominantColorFromBelowStatusBar(view)
    // 设置状态栏颜色
    window.statusBarColor = statusBarBelowColor
    // 根据状态栏颜色调整图标颜色（深色或浅色）
    setStatusBarIconColorAccordingToColor(statusBarBelowColor)
    return statusBarBelowColor
}

private fun getDominantColorFromBelowStatusBar(view: View): Int {
    // 获取状态栏高度
    val statusBarHeight = view.statusBarHeight

    // 获取状态栏下方的视图
    val statusBarBelowView = view.rootView.findViewById<View>(android.R.id.content)

    // 获取状态栏下方视图的位图
    statusBarBelowView.isDrawingCacheEnabled = true
    statusBarBelowView.buildDrawingCache(true)
    val bitmap = statusBarBelowView.drawingCache

    // 获取状态栏下方的颜色
    val dominantColor = bitmap.getPixel(0, statusBarHeight)

    // 清理缓存
    statusBarBelowView.isDrawingCacheEnabled = false
    statusBarBelowView.destroyDrawingCache()

    return dominantColor
}
