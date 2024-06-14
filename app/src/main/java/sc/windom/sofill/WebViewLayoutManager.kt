package sc.windom.sofill

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowInsets
import android.view.WindowManager
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.blankj.utilcode.util.BarUtils
import com.tencent.bugly.crashreport.BuglyLog
import com.tencent.mmkv.MMKV
import sc.windom.sofill.U.applySystemThemeToWebView
import sc.windom.sofill.U.isInSpecialMode

/**
 * Android small window mode soft keyboard black occlusion [siyuan-note/siyuan-android#7](https://github.com/siyuan-note/siyuan-android/pull/7)
 *
 * 优化注册工具栏显示/隐藏跟随软键盘状态 [Hi-Windom/Sillot-android#84](https://github.com/Hi-Windom/Sillot-android/issues/84)
 *
 * 基于 [Yingyi](https://ld246.com/member/shuoying) 的 AndroidBug5497Workaround 改进，将原软键盘监听集成，统一调整布局。MIUI小窗底部有小白条，已进行抬高处理。
 * 本方案不使用 com.blankj.utilcode.util 的 KeyboardUtils.fixAndroidBug5497 和 KeyboardUtils.registerSoftInputChangedListener，
 * 因为该库不兼容小窗模式、不识别悬浮键盘。本方案使用 WindowInsets 的 isVisible(Type.ime()) 方法来检查输入法是否可见（Android 11+）。
 * 如果在清单文件的 activity 节点声明了 android:windowSoftInputMode="adjustResize"，则普通键盘弹起时无需重新布局，
 * 但有个小缺陷：系统自动重新布局是与输入法弹起同时进行的，会导致用户能短暂看到布局底色。因此，推荐声明为 android:windowSoftInputMode="adjustPan"
 * 本方案会自动调整布局，这样不受系统自动重新布局影响，可以通过指定 delayResetLayoutWhenImeShow （默认值 0 ） 来避免用户短暂看到布局底色。
 * 此外，推荐进一步声明为 android:windowSoftInputMode="stateHidden|adjustPan"，stateHidden 与 isImeVisible = false 初始化保持一致性。
 * @constructor - 在 Java 中（this指代当前activity）：
 * ```java
 * WebViewLayoutManager.assistActivity(this, webView).setDelayResetLayoutWhenImeShow(200);
 * ```
 * @sample WebViewLayoutManager.assistActivity
 * TODO: 平板端测试有一定延迟，后续优化
 * @author https://ld246.com/member/soltus, GLM-4
 */
@SuppressLint("WrongConstant")
class WebViewLayoutManager private constructor(
    private val activity: Activity,
    private val webView: WebView
) {
    private val TAG = "WebViewLayoutManager"
    private var mmkv: MMKV = MMKV.defaultMMKV()
    var autoWebViewDarkMode: Boolean = false
    var delayResetLayoutWhenImeShow: Long =
        0 // 默认与 android:windowSoftInputMode="adjustResize" 行为保持一致，推荐赋值为 200
    var JSonImeShow = ""
    var JSonImeHide = ""
    var JSonImeShow0Height = ""
    var JSonImeHide0Height = ""
    private var currentOrientation: Int = -1
    private var isImeVisible = false // 支持悬浮键盘
    private var imeHeight = 0 // 悬浮键盘的值为 0
    private var lastLayoutWidth = 0
    private var lastLayoutHeight = 0
    private val view: View // Activity的内容视图

    init {
        autoWebViewDarkMode = mmkv.getBoolean("autoWebViewDarkMode", false)
        applySystemThemeToWebView(activity, webView, autoWebViewDarkMode)
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
                restLayout("监听布局变化")
            }
        }
        // 监听配置变化
        activity.registerComponentCallbacks(object : ComponentCallbacks {
            override fun onConfigurationChanged(newConfig: Configuration) {
                autoWebViewDarkMode = mmkv.getBoolean("autoWebViewDarkMode", false)
                applySystemThemeToWebView(activity, webView, autoWebViewDarkMode)
                Log.w(
                    TAG,
                    "新配置是否横屏: ${newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE}, " +
                            "旧配置是否横屏: ${currentOrientation == Configuration.ORIENTATION_LANDSCAPE}"
                )
                logInfo()
                if (newConfig.orientation != currentOrientation) {
                    currentOrientation = newConfig.orientation
                    restLayout("监听配置编号-屏幕方向已改变")
                } else {
                    restLayout("监听配置变化")
                }
            }

            override fun onLowMemory() {
                TODO("Not yet implemented")
            }
        })
    }

    /**
     * 重置布局
     * @param traker 跟踪调用者
     */
    private fun restLayout(traker: String) {
        val newHight =
            this.getRootViewHeight() - BarUtils.getNavBarHeight() / 2 + this.navigationBarHeight / 2
        if (true || view.height != newHight && view.height + this.imeHeight != newHight) {
//            logInfo()
            Log.w(
                TAG,
                "restLayout@$traker, view.height: ${view.height}, newHight: $newHight, isImeVisible: ${this.isImeVisible}, imeHeight: ${this.imeHeight}, isInSpecialMode(): ${activity.isInSpecialMode()}"
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
        val rect = this.visibleRect
        val rootViewHeight = this.getRootViewHeight()
        val rootViewWidth = this.getRootViewWidth()
        val display = this.displayMetrics
        val navigationBarHeight = this.navigationBarHeight
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
            "navigationBarHeight: $navigationBarHeight, NavBarHeight: ${BarUtils.getNavBarHeight()}, StatusBarHeight: ${BarUtils.getStatusBarHeight()}" +
                    ", rootViewHeight: $rootViewHeight, display.heightPixels: ${display.heightPixels}"
                    + "\n------------------------------------------------\n"
        )
    }

    /**
     * 计算可用高度
     */
    private fun computeUsableHeight(): Int {
        val rect = visibleRect
        return rect.height()
    }

    /**
     * 计算可用宽度
     */
    private fun computeUsableWidth(): Int {
        val rect = visibleRect
        return rect.width()
    }

    /**
     * 获取视图的可见区域
     */
    private val visibleRect: Rect
        get() {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            return rect
        }

    /**
     * 获取根视图的高度
     */
    private fun getRootViewHeight(): Int {
        return view.rootView.height
    }

    /**
     * 获取根视图的宽度
     */
    private fun getRootViewWidth(): Int {
        return view.rootView.width
    }

    /**
     * 获取显示度量
     */
    private val displayMetrics: DisplayMetrics
        get() {
            val displayMetrics = DisplayMetrics()
            // 获取WindowManager服务
            val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            // 获取当前窗口度量
            val windowMetrics = windowManager.currentWindowMetrics
            // 获取窗口的边界
            val bounds = windowMetrics.bounds
            // 设置显示度量到窗口的大小
            displayMetrics.widthPixels = bounds.width()
            displayMetrics.heightPixels = bounds.height()
            return displayMetrics
        }

    /**
     * 获取导航栏高度
     */
    @get:SuppressLint("DiscouragedApi", "InternalInsetResource")
    private val navigationBarHeight: Int
        get() {
            val context = view.context
            val hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey()
            if (!hasMenuKey) {
                val resourceId =
                    context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
                return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
            } else {
                return 0
            }
        }


    companion object {
        @JvmStatic
        fun assistActivity(activity: Activity, webView: WebView): WebViewLayoutManager {
            return WebViewLayoutManager(activity, webView)
        }
    }
}
