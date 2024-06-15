package sc.windom.sofill.android.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tencent.bugly.crashreport.BuglyLog
import sc.windom.sofill.U.isInSpecialMode
import sc.windom.sofill.Us.U_Layout.displayMetrics
import sc.windom.sofill.Us.U_Layout.getRootViewHeight
import sc.windom.sofill.Us.U_Layout.navigationBarHeight
import sc.windom.sofill.Us.U_Layout.statusBarHeight
import sc.windom.sofill.Us.U_Layout.visibleRect

/**
 * Android small window mode soft keyboard black occlusion [siyuan-note/siyuan-android#7](https://github.com/siyuan-note/siyuan-android/pull/7)
 *
 * 优化注册工具栏显示/隐藏跟随软键盘状态 [Hi-Windom/Sillot-android#84](https://github.com/Hi-Windom/Sillot-android/issues/84)
 *
 * 基于 [Yingyi](https://ld246.com/member/shuoying) 的 AndroidBug5497Workaround 改进，将原软键盘监听集成，统一调整布局。
 * 本方案不使用 com.blankj.utilcode.util 的 KeyboardUtils.fixAndroidBug5497、 KeyboardUtils.registerSoftInputChangedListener 和 BarUtils.getNavBarHeight()，
 * 因为这些方法对安卓12+来说都已经过时，不兼容小窗模式、不识别悬浮键盘，getNavBarHeight获取的值错误等。本方案使用 WindowInsets 的 isVisible(Type.ime()) 方法来检查输入法是否可见（Android 11+）。
 * 如果在清单文件的 activity 节点声明了 android:windowSoftInputMode="adjustResize"，则普通键盘弹起时无需重新布局，
 * 但有个小缺陷：系统自动重新布局是与输入法弹起同时进行的，会导致用户能短暂看到布局底色。因此，推荐声明为 android:windowSoftInputMode="adjustPan"
 * 本方案会自动调整布局，这样不受系统自动重新布局影响，可以通过指定 delayResetLayoutWhenImeShow （默认值 0 ） 来避免用户短暂看到布局底色。
 * 如果需要支持安卓11以下设备，可以考虑在代码中根据API级别动态设置 getWindow().setSoftInputMode 替代在清单文件中声明，并使用其他支持安卓11以下设备的方案替代本方案
 * 此外，推荐进一步声明为 android:windowSoftInputMode="stateHidden|adjustPan"，stateHidden 与 isImeVisible = false 初始化保持一致性。
 *
 * 测试场景覆盖以下状态的排列组合：
 * - 设备与系统：[Xiaomi HyperOS Phone @Android14，vivo OriginOS4 Phone @Android14，Lenovo ZUI14 Pad @Android12]
 * - 布局模式：[普通，小窗，多窗口]
 * - 导航方式：[全面屏手势，经典导航键]
 * - 屏幕方向：[竖屏，横屏]
 * - 特殊操作：[无，改变屏幕方向]
 *
 * @since v0.35
 * @suppress 前端是否提供了键盘工具条，如果没有一般不需要赋值 JSonIme* ，不过建议保留 delayResetLayoutWhenImeShow 提供更好的视觉效果。
 * 如果手机端键盘工具条有而平板端没有，请自行判断设备。
 * @constructor - 在 Java 中（this指代当前activity）：
 * ```java
 * WebViewLayoutManager.assistActivity(this, webView).setDelayResetLayoutWhenImeShow(200);
 * ```
 * @sample WebViewLayoutManager.assistActivity
 * @author https://ld246.com/member/soltus, GLM-4
 * @property delayResetLayoutWhenImeShow 收窄布局延时执行时间。键盘弹起到最后高度需要一个过程，因此收窄布局应当延时执行（不包括小窗和多窗口模式），延时多久没有标准，推荐赋值为 200
 * @property JSonImeShow 键盘显示时执行的JavaScript代码（注意不支持 Optional Chaining 等写法）
 * @property JSonImeHide 键盘显示时执行的JavaScript代码（注意不支持 Optional Chaining 等写法）
 * @property JSonImeShow0Height 键盘显示时执行的JavaScript代码（注意不支持 Optional Chaining 等写法）
 * @property JSonImeHide0Height 键盘显示时执行的JavaScript代码（注意不支持 Optional Chaining 等写法）
 * @property softInputMode 覆盖清单中声明，默认值为 SOFT_INPUT_ADJUST_PAN，
 * @property onConfigurationChangedCallback 配置发生变化时的回调函数，如果赋值该项则不应在 activity 中重写 onConfigurationChanged 方法，否则回调无效。示例：
 * ```java
 * webViewLayoutManager.setOnConfigurationChangedCallback((newConfig)->{
 *   Log.w(TAG, "新配置屏幕方向: " + newConfig.orientation);
 *   return null; // java 中调用必须 return null
 * });
 * ```
 * 可以动态设置，例如 SOFT_INPUT_ADJUST_RESIZE ，注意同步修改 delayResetLayoutWhenImeShow
 */
@SuppressLint("WrongConstant", "ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.S)
class WebViewLayoutManager private constructor(
    private val activity: Activity,
    private val webView: WebView
) {
    private val TAG = "WebViewLayoutManager"
    var delayResetLayoutWhenImeShow: Long =
        0 // 默认与 android:windowSoftInputMode="adjustResize" 行为保持一致
    var JSonImeShow = ""
    var JSonImeHide = ""
    var JSonImeShow0Height = ""
    var JSonImeHide0Height = ""
    var softInputMode =
        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
    var onConfigurationChangedCallback: ((Configuration) -> Unit)? = null
    private var isImeVisible = false // 支持悬浮键盘
    private var imeHeight = 0 // 悬浮键盘的值为 0
    private var lastLayoutWidth = 0
    private var lastLayoutHeight = 0
    private val view: View // Activity的内容视图

    init {
        val frameLayout = activity.findViewById<FrameLayout>(android.R.id.content)
        this.view = frameLayout.getChildAt(0)
        ViewCompat.setOnApplyWindowInsetsListener(this.view) { v: View?, insets: WindowInsetsCompat ->
            this.isImeVisible = insets.isVisible(WindowInsets.Type.ime())
            this.imeHeight = insets.getInsets(WindowInsets.Type.ime()).bottom
            BuglyLog.w(TAG, "isImeVisible: ${this.isImeVisible}, imeHeight: ${this.imeHeight}")
            restLayout("WindowInsets")
            insets
        }

        // 本方案使用 isVisible(Type.ime()) 替代 KeyboardUtils.registerSoftInputChangedListener
        // 后已经可以做到准确识别键盘是否显示和键盘高度，但仍需要通过监听布局变化以识别小窗模式和多窗口模式
        frameLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val currentWidth = frameLayout.width
            val currentHeight = frameLayout.height

            // 为了避免循环调用，因此需要检查布局的宽度和高度是否发生了变化
            if (currentWidth != this.lastLayoutWidth || currentHeight != this.lastLayoutHeight) {
                this.lastLayoutWidth = currentWidth
                this.lastLayoutHeight = currentHeight
                activity.window.setSoftInputMode(this.softInputMode)
                restLayout("监听布局变化")
            }
        }
        // 监听配置变化。如果 onConfigurationChangedCallback 不为空则不应在 activity 中重写 onConfigurationChanged 方法，否则回调无效。
        onConfigurationChangedCallback?.let {
            activity.registerComponentCallbacks(object : ComponentCallbacks {
                override fun onConfigurationChanged(newConfig: Configuration) {
                    it.invoke(newConfig)
                }

                override fun onLowMemory() {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    /**
     * 重置布局
     * @param traker 跟踪调用者
     */
    private fun restLayout(traker: String) {
        val newHight =
            view.getRootViewHeight() - view.navigationBarHeight // 兼容经典导航键、小米系统小窗底部小白条，o(￣ヘ￣o#)
        // logInfo()
        Log.w(
            TAG,
            "restLayout@$traker, view.height: ${view.height}, newHight: $newHight, " +
                    "isImeVisible: ${this.isImeVisible}, imeHeight: ${this.imeHeight}, isInSpecialMode(): ${activity.isInSpecialMode()}"
        )
        if (this.isImeVisible) {
            // 键盘弹起到最后高度需要一个过程，因此收窄布局应当延时执行（不包括小窗和多窗口模式），延时多久没有标准
            // （如果声明了 android:windowSoftInputMode="adjustResize" 则无效，因为系统已经自动调整了布局）
            Handler(Looper.getMainLooper()).postDelayed({
                if (this.imeHeight == 0 || activity.isInSpecialMode()) {
                    webView.evaluateJavascript(this.JSonImeShow0Height, null)
                } else {
                    webView.evaluateJavascript(this.JSonImeShow, null)
                }
                view.layoutParams.height =
                    newHight - if (activity.isInSpecialMode()) 0 else this.imeHeight
                webView.layoutParams.height = -1 // 这里不能改，必须MATCH_PARENT
                view.requestLayout()
                webView.requestLayout()
            }, if (activity.isInSpecialMode()) 0 else this.delayResetLayoutWhenImeShow)
        } else {
            // 填充布局应当立即执行
            if (this.imeHeight == 0 || activity.isInSpecialMode()) {
                webView.evaluateJavascript(this.JSonImeHide0Height, null)
            } else {
                webView.evaluateJavascript(this.JSonImeHide, null)
            }
            view.layoutParams.height = newHight
            webView.layoutParams.height = -1 // 这里不能改，必须MATCH_PARENT
            view.requestLayout()
            webView.requestLayout()
        }
    }


    /**
     * 打印日志信息
     *
     * view.layoutParams.height：
     * 这个属性指的是视图的布局参数中的高度设置。
     * 它决定了视图在布局过程中应该占用多大的高度空间。
     * LayoutParams类是视图的一个属性，用于定义如何将视图放置在其父视图容器中。
     * height可以设置为具体的像素值（如LayoutParams.MATCH_PARENT，LayoutParams.WRAP_CONTENT或具体的数值），
     * 或者是一个基于父容器的比例（如LayoutParams.FILL_PARENT，这已在新版本中不推荐使用）。
     *
     * view.height：
     * 这个属性指的是视图在布局完成后实际的高度。
     * 它是在布局过程结束后，系统根据view.layoutParams.height和其他因素（如视图的内容、父容器的限制等）计算出的实际高度值。
     * 这个值是只读的，开发者在代码中不能直接设置view.height，它是由布局系统确定的。
     *
     * view.bottom：
     * view.bottom是视图底边的坐标，相对于其父视图的坐标空间。它是视图顶部坐标加上视图高度的结果，即view.bottom = view.top + view.height。
     * view.bottom通常用于确定视图在屏幕上的位置，特别是在计算视图之间的间隔或确定触摸事件的坐标时。
     *
     * height = bottom - top
     */
    private fun logInfo() {
        val rect = view.visibleRect
        val rootViewHeight = view.getRootViewHeight()
        val display = activity.displayMetrics
        val navigationBarHeight = view.navigationBarHeight
        Log.d(
            TAG,
            "rect.top: ${rect.top} | view.top: ${view.top} | webView.top: ${webView.top}"
        )
        Log.d(
            TAG,
            "rect.height: ${rect.height()} | view.height: ${view.height} | webView.height: ${webView.height}"
        )
        Log.d(
            TAG,
            "rect.bottom: ${rect.bottom} | view.bottom: ${view.bottom} | webView.bottom: ${webView.bottom}"
        )
        Log.d(
            TAG,
            "view.layoutParams.height: ${view.layoutParams.height}, webView.layoutParams.height: ${webView.layoutParams.height}"
        )
        Log.d(
            TAG,
            "navigationBarHeight: $navigationBarHeight, StatusBarHeight: ${view.statusBarHeight}" +
                    ", rootViewHeight: $rootViewHeight, display.heightPixels: ${display.heightPixels}"
                    + "\n------------------------------------------------\n"
        )
    }


    companion object {
        @JvmStatic
        fun assistActivity(activity: Activity, webView: WebView): WebViewLayoutManager {
            return WebViewLayoutManager(activity, webView)
        }
    }
}
