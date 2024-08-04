/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/5 01:07
 * updated: 2024/8/5 01:07
 */

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
import sc.windom.sofill.U.isInSpecialMode
import sc.windom.sofill.Us.adjustLayoutMarginForSystemBars
import sc.windom.sofill.Us.displayMetrics
import sc.windom.sofill.Us.navigationBarHeightH
import sc.windom.sofill.Us.navigationBarHeightV
import sc.windom.sofill.Us.statusBarHeight
import sc.windom.sofill.Us.visibleRect
import splitties.systemservices.inputMethodManager


/**
 * # Android 12+ 最好用最完善的 WebView 布局托管方案
 *
 * ```
 * 可与 flowus obsidian 等软件的效果媲美. 耗时两个月打磨优化, 唯一缺点是太过完美 (bushi
 * ```
 *
 * 本方案使用 WindowInsets 的 isVisible(Type.ime()) 方法来检查输入法是否可见（Android 11+）。
 *
 * ```
 * 本方案不使用 com.blankj.utilcode.util 的 KeyboardUtils.fixAndroidBug5497、 KeyboardUtils.registerSoftInputChangedListener 和 BarUtils.getNavBarHeight()，
 * 因为这些方法对 Android 12+ 来说都已经过时，不兼容小窗模式、不识别悬浮键盘，getNavBarHeight获取的值错误等。
 * ```
 *
 * 如果在清单文件的 activity 节点声明了 android:windowSoftInputMode="adjustResize"，则普通键盘弹起时无需重新布局，
 *
 * ```
 * 但有个小缺陷：系统自动重新布局是与输入法弹起同时进行的，会导致用户能短暂看到布局底色。因此，推荐声明为 android:windowSoftInputMode="adjustPan", 由本方案接管重新布局。
 * ```
 *
 * 本方案会自动调整布局，这样不受系统自动重新布局影响，可以通过指定 [delayResetLayoutWhenImeShow] （默认值 0 ） 来避免用户短暂看到布局底色。
 * 此外，推荐进一步声明为 android:windowSoftInputMode="stateHidden|adjustPan"，stateHidden 与 [isImeVisible] = false 初始化保持一致性。
 *
 * ```
 * 如果需要支持安卓11以下设备，可以考虑在代码中根据API级别动态设置 getWindow().setSoftInputMode 替代在清单文件中声明，并使用其他支持安卓11以下设备的方案替代本方案。
 * ```
 *
 * 测试场景覆盖以下状态的排列组合：
 * - 设备与系统：[Xiaomi HyperOS Phone @Android14，vivo OriginOS4 Phone @Android14，Lenovo ZUI14 Pad @Android12]
 * - 布局模式：[普通，小窗，多窗口]
 * - 导航方式：[全面屏手势隐藏导航条，全面屏手势显示导航条, 经典导航键]
 * - 屏幕方向：[竖屏，左横屏，右横屏]
 * - 非 ime 的特殊操作：[无，改变屏幕方向, 调整小窗大小，切换多窗口位置 (上下可能存在不同, 左右不影响)]
 * - ime 的特殊操作：[无，悬浮键盘与非悬浮键盘之间切换，平板端使用蓝牙键盘模式，键盘调节（例如修改高度）]
 * - 输入法：[Jovi输入法Pro，QQ输入法，百度输入法，微信输入法，搜狗输入法，讯飞输入法，雨燕输入法]
 *
 * 已知问题：
 * - 微信输入法切换屏幕方向后会收起软键盘，初步判断事微信输入法的问题
 *
 * 参考引用：
 * - [Yingyi](https://ld246.com/member/shuoying) 的 AndroidBug5497Workaround 。
 *
 * @since v0.35
 * @suppress
 * - <1> Compose 中使用需要移除 `Modifier.imePadding()` ，否则布局调整冲突。
 * Compose 中 `Modifier.imePadding()` 只能适配键盘而不会调整 webview 布局，实测无法解决汐洛绞架伺服页面等依赖 vh 的布局，因此仍需托管。
 * - <2> 前端是否提供了键盘工具条，这将关系到`JSonIme*`，见 [JSonImeShow] | [JSonImeHide] | [JSonImeShow0Height] | [JSonImeHide0Height] 。
 * - 如果没有一般不需要赋值 `JSonIme*` ，不过仍建议尝试合适的 [delayResetLayoutWhenImeShow] 提供更好的视觉效果。
 * - 如果有，最好在 `JSonIme*` 中第一行JS语句通过变量等方式锁定或解锁，因为一次键盘弹出或收起会多次调用 [restLayout] 是很正常的，参考JS代码：
 * ```js
 * window.Sillot.android.LockKeyboardToolbar=true;
 * hideKeyboardToolbar();
 * showKeyboardToolbar();
 * ```
 * ```js
 * window.Sillot.android.LockKeyboardToolbar=false;
 * hideKeyboardToolbar();
 * ```
 * - <3> 如果手机端键盘工具条有而平板端没有，请自行判断设备。
 * @constructor
 * - 在 Java 中：
 * ```java
 * WebViewLayoutManager.assistActivity(activity, webView).setDelayResetLayoutWhenImeShow(200);
 * ```
 * - 在 Kotlin 中：
 * ```kotlin
 * WebViewLayoutManager.assistActivity(activity, webView).delayResetLayoutWhenImeShow = 200
 * ```
 * @sample WebViewLayoutManager.assistActivity
 * @author <a href="https://github.com/Soltus">Soltus</a>, GLM-4
 * @see [applySystemThemeToWebView]
 * @property delayResetLayoutWhenImeShow
 * @property JSonImeShow
 * @property JSonImeHide
 * @property JSonImeShow0Height
 * @property JSonImeHide0Height
 * @property softInputMode
 * @property onConfigurationChangedCallback
 * @property onLayoutChangedCallback
 * @property onImeInsetsCallback
 * @property callIdCounter 只读
 * @param activity 活动，通过 [WebViewLayoutManager.assistActivity] 指定
 * @param webView WebView，通过 [WebViewLayoutManager.assistActivity] 指定
 * @param inCompose 是否 Compose 布局，通过 [WebViewLayoutManager.assistActivity] 指定
 */
//  [Android small window mode soft keyboard black occlusion](https://github.com/siyuan-note/siyuan-android/pull/7)
//  [优化注册工具栏显示/隐藏跟随软键盘状态](https://github.com/Hi-Windom/Sillot-android/issues/84)
@SuppressLint("WrongConstant", "ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.S)
class WebViewLayoutManager private constructor(
    private val activity: Activity,
    private val webView: WebView,
    private val inCompose: Boolean
) {
    private val TAG = "WebViewLayoutManager"

    /**
     * 收窄布局延时执行时间。键盘弹起到最后高度需要一个过程，因此收窄布局应当延时执行（不包括小窗和多窗口模式），
     *  - 延时多久没有标准，考虑到不同机型和输入法的表现差异，不宜设置太小，否则会导致键盘弹起时短暂看到布局底色，推荐赋值范围 `131-186`。
     *  - 如果已经在 [onImeInsetsCallback] 中调用了 [applySystemThemeToWebView] 填充背景底色，则可以不覆盖该属性值（默认为0）或者设置为一个较小的值。
     */
    @JvmField
    var delayResetLayoutWhenImeShow: Long =
        0 // 默认与 android:windowSoftInputMode="adjustResize" 行为保持一致

    /**
     * 键盘显示时执行的JavaScript代码
     * @suppress 不支持 Optional Chaining （例如 `window?.siyuan`） 等写法
     */
    @JvmField
    var JSonImeShow = ""

    /**
     * 键盘收起时执行的JavaScript代码
     * @suppress 不支持 Optional Chaining （例如 `window?.siyuan`） 等写法
     */
    @JvmField
    var JSonImeHide = ""

    /**
     * 键盘显示且0高度（比如悬浮键盘）时执行的JavaScript代码
     * @suppress 不支持 Optional Chaining （例如 `window?.siyuan`） 等写法
     */
    @JvmField
    var JSonImeShow0Height = ""

    /**
     * 键盘收起且0高度（比如悬浮键盘）时执行的JavaScript代码
     * @suppress 不支持 Optional Chaining （例如 `window?.siyuan`） 等写法
     */
    @JvmField
    var JSonImeHide0Height = ""

    /**
     * 覆盖清单中声明，默认值为 [android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN],
     * 可以动态设置，例如  [android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE] ，注意同步修改 [delayResetLayoutWhenImeShow]
     */
    @JvmField
    var softInputMode =
        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN

    /**
     * ## 配置发生变化时的回调函数
     *
     * 如果赋值该项则不应在 activity 中重写 `onConfigurationChanged` 方法，否则回调无效。
     */
    @JvmField
    var onConfigurationChangedCallback: ((Configuration) -> Unit)? = null

    /**
     * ## 布局发生变化时的回调函数
     *
     * - 在 Java 中：
     * ```java
     * webViewLayoutManager.setOnConfigurationChangedCallback((newConfig)->{
     *   Log.w(TAG, "新配置屏幕方向: " + newConfig.orientation);
     *   return null; // java 中调用必须 return null
     * });
     * ```
     * - 在 Kotlin 中：
     * ```kotlin
     * webViewLayoutManager.onConfigurationChangedCallback = { newConfig ->
     *   Log.w(TAG, "新配置屏幕方向: " + newConfig.orientation)
     * }
     * ```
     */
    @JvmField
    var onLayoutChangedCallback: ((frameLayout: FrameLayout) -> Unit)? = null

    /**
     * ## 软键盘显示隐藏时的回调函数
     *
     * 推荐 [applySystemThemeToWebView] , 可填充背景底色
     *
     *
     */
    @JvmField
    var onImeInsetsCallback: ((insets: WindowInsetsCompat) -> Unit)? = null

    /**
     * 支持悬浮键盘, 不支持小窗模式
     * insets监听中判断新旧值可以避免两次调用 restLayout ，但是不能这么做，原因见 [restLayout]
     */
    private var isImeVisible = false

    /**
     * 记录上次的状态，用于识别从悬浮键盘切换
     */
    private var currentImeVisible = false

    /**
     * 不等于实际键盘高度， 悬浮键盘的值为 0
     *
     * insets监听中判断新旧值可以避免两次调用 restLayout ，但是不能这么做，原因见 [restLayout]
     */
    private var imeHeight = 0

    /**
     * 记录上次的状态，用于识别从悬浮键盘切换
     */
    private var currentImeHeight = 0
    private var lastLayoutWidth = 0
    private var lastLayoutHeight = 0

    /**
     * 保留属性, 还没用到
     */
    private var lastStatusBarHeight = 0

    /**
     * 竖屏状态下导航条高度
     */
    private var lastNavigationBarHeightV = 0

    /**
     * 横屏状态下导航条高度
     */
    private var lastNavigationBarHeightH = 0

    /**
     * 绑定的 activity 的内容视图
     */
    private val view: View
    private val TRAKER_INSETS = "WindowInsets"
    private val TRAKER_LAYOUT = "监听布局变化"

    /**
     * 用于记录 restLayout 被调用次数, 在 restLayout 中自增作为唯一的标识符
     */
    private var _callIdCounter = 0

    /**
     * 公共的只读属性，返回当前实例的 [restLayout] 被调用次数
     */
    val callIdCounter: Int
        get() = synchronized(this) {
            _callIdCounter
        }

    init {
        val frameLayout = activity.findViewById<FrameLayout>(android.R.id.content)
        this.view = frameLayout.getChildAt(0)
        ViewCompat.setOnApplyWindowInsetsListener(this.view) { v: View?, insets: WindowInsetsCompat ->
            if (this.imeHeight != insets.getInsets(WindowInsets.Type.ime()).bottom) {
                // 此监听器触发条件非常宽松，因此判断 Ime 相关（不支持小窗模式和悬浮键盘）以免死循环发生。
                onImeInsetsCallback?.invoke(insets)
            }
            this.isImeVisible = insets.isVisible(WindowInsets.Type.ime()) // 不支持小窗模式
            this.imeHeight = insets.getInsets(WindowInsets.Type.ime()).bottom
            restLayout(this.TRAKER_INSETS)
            insets
        }

        // 本方案使用 isVisible(Type.ime()) 替代 KeyboardUtils.registerSoftInputChangedListener
        // 后已经可以做到准确识别键盘是否显示和键盘高度，但仍需要通过监听布局变化以识别小窗模式和多窗口模式
        frameLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val currentWidth = frameLayout.width
            val currentHeight = frameLayout.height
            this.lastStatusBarHeight = frameLayout.statusBarHeight
            this.lastNavigationBarHeightV = frameLayout.navigationBarHeightV
            this.lastNavigationBarHeightH = frameLayout.navigationBarHeightH

            // 为了避免循环调用，因此需要检查布局的宽度和高度是否发生了变化
            if (currentWidth != this.lastLayoutWidth || currentHeight != this.lastLayoutHeight) {
                this.lastLayoutWidth = currentWidth
                this.lastLayoutHeight = currentHeight
                activity.window.setSoftInputMode(this.softInputMode)
                restLayout(this.TRAKER_LAYOUT)
                onLayoutChangedCallback?.invoke(frameLayout)
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
        val display = activity.displayMetrics
        Log.d(
            TAG,
            "init done. StatusBarHeight: ${view.statusBarHeight}" +
                    ", display.widthPixels: ${display.widthPixels}, display.heightPixels: ${display.heightPixels}"
        )
    }

    /**
     * 重置布局。requestLayout() 会触发 OnApplyWindowInset，不过不必担心死循环，函数内已判断仅当需要重置布局时才重置布局。
     * 而且，同一个 ime inset 两次调用 requestLayout() 是必要的，因为要支持悬浮键盘和小窗模式。
     * @param tracker 跟踪调用者
     */
    private fun restLayout(tracker: String) {
        // 使用 let 函数来确保 this 的正确性（指向当前 Class ）, 注意：里面通过 it 访问变量并不是局部的，期间会受到全局变化影响
        this.let {
            synchronized(this) { // 每个实例都有自己的锁
                it._callIdCounter++
                val callId = it._callIdCounter
                if (webView.parent == null) {
                    Log.w(
                        TAG,
                        "[${callId}] restLayout@$tracker, webView.parent == null"
                    )
                    if (view.layoutParams != null) {
                        // 重置默认布局
                        view.layoutParams.height = view.rootView.height
                        view.adjustLayoutMarginForSystemBars()
                        view.requestLayout()
                    }
                    return
                }
                if (webView.layoutParams == null) {
                    Log.w(
                        TAG,
                        "[${callId}] restLayout@$tracker, webView.layoutParams == null -> break restLayout()"
                    )
                    return
                }
                val nochange = (tracker == it.TRAKER_INSETS) && (it.currentImeVisible == it.isImeVisible) && (it.currentImeHeight == it.imeHeight)

                /**
                 * 兼容经典导航键、小米系统小窗底部小白条、实体键盘
                 */
                val newHight =
                    view.rootView.height - if (it.imeHeight == 0) view.navigationBarHeightV else 0

                /**
                 * 小窗和多窗口模式
                 */
                val isInSpecialMode_lock =
                    it.imeHeight == 0 && activity.isInSpecialMode()

                /**
                 * 从悬浮键盘切换至非悬浮键盘
                 */
                val fromFloating2Normal_lock =
                    it.currentImeVisible && it.currentImeHeight == 0 && it.imeHeight != 0

                /**
                 * 从非悬浮键盘切换至悬浮键盘
                 */
                val fromNormal2Floating_lock =
                    it.currentImeVisible && it.currentImeHeight != 0 && it.imeHeight == 0

                /**
                 * 小米系统上会遇到
                 */
                val ImeHequalsNavBarH = it.imeHeight == view.navigationBarHeightV
                val isIme0H = it.imeHeight == 0 || ImeHequalsNavBarH
                Log.w(
                    TAG,
                    "[${callId}] [ restLayout @$tracker ] currentImeVisible: ${it.currentImeVisible}, currentImeHeight: ${it.currentImeHeight}, " +
                            "isImeVisible: ${it.isImeVisible}, imeHeight:${it.imeHeight}, isInSpecialMode: ${activity.isInSpecialMode()}, " +
                            "从悬浮键盘切换至非悬浮键盘: $fromFloating2Normal_lock, 从非悬浮键盘切换至悬浮键盘: $fromNormal2Floating_lock"
                )
//                logInfo()

                it.currentImeVisible = it.isImeVisible
                it.currentImeHeight = it.imeHeight

                if (it.isImeVisible) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        inputMethodManager.currentInputMethodInfo?.let { ime ->
                            // it.packageName + it.serviceName = it.id
                            Log.i(
                                TAG,
                                "[${callId}] [ ime ] packageName: ${ime.packageName}, serviceName: ${ime.serviceName}"
                            )
                        }
                    }
                    // 键盘弹起到最后高度需要一个过程，因此收窄布局应当延时执行（不包括小窗和多窗口模式），延时多久没有标准
                    // （如果声明了 android:windowSoftInputMode="adjustResize" 则无效，因为系统已经自动调整了布局）
                    val delayTime: Long = if (it.currentImeVisible || isInSpecialMode_lock ||
                        fromFloating2Normal_lock || fromNormal2Floating_lock) 0 else it.delayResetLayoutWhenImeShow
                    if (nochange) {
                        Log.i(
                            TAG,
                            "[${callId}] requestLayout skip, nochange"
                        )
                    } else {
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                view.layoutParams.height =
                                    newHight - if (isInSpecialMode_lock || fromNormal2Floating_lock) 0 else it.imeHeight
                                view.adjustLayoutMarginForSystemBars() // 调整布局边距，兼容传统虚拟导航键
                                webView.layoutParams.height = -1 // 这里不能改，必须MATCH_PARENT
                                webView.layoutParams.width = -1
                                view.requestLayout() // 触发 view 及其所有子视图（包括 webView ）的布局重新计算过程
                                Log.i(
                                    TAG,
                                    "[${callId}] requestLayout done, new height of view: ${view.layoutParams.height}"
                                )
                            },
                            delayTime
                        )
                    }
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            if (isIme0H) {
                                webView.evaluateJavascript(it.JSonImeShow0Height, null)
                            } else {
                                webView.evaluateJavascript(it.JSonImeShow, null)
                            }
                        },
                        delayTime + 20
                    )
                } else {
                    if (isIme0H) {
                        webView.evaluateJavascript(it.JSonImeHide0Height, null)
                    } else {
                        webView.evaluateJavascript(it.JSonImeHide, null)
                    }
                    if (nochange) {
                        Log.i(
                            TAG,
                            "[${callId}] skip requestLayout skip, nochange"
                        )
                    } else {
                        // 填充布局应当立即执行
                        view.layoutParams.height = newHight
                        view.adjustLayoutMarginForSystemBars() // 调整布局边距，兼容传统虚拟导航键
                        webView.layoutParams.height = -1 // 这里不能改，必须MATCH_PARENT
                        webView.layoutParams.width = -1
                        view.requestLayout() // 触发 view 及其所有子视图（包括 webView ）的布局重新计算过程
                        Log.i(
                            TAG,
                            "[${callId}] requestLayout done, new height of view: ${view.layoutParams.height}"
                        )
                    }
                }
            }
        }
    }

    /**
     * 打印日志信息，调试时使用
     */
    private fun logInfo() {
        val rect = view.visibleRect
        Log.d(
            TAG,
            "\n------------------------------------------------\n" +
                    "[Top] rect: ${rect.top} | rootView: ${view.rootView.top} | view: ${view.top} | webView: ${webView.top}"
        )
        Log.d(
            TAG,
            "[Height] rect: ${rect.height()} | rootView: ${view.rootView.height} | view: ${view.height} | webView: ${webView.height}"
        )
        Log.d(
            TAG,
            "[Bottom] rect: ${rect.bottom} | rootView: ${view.rootView.bottom} | view: ${view.bottom} | webView: ${webView.bottom}"
        )
        Log.d(
            TAG,
            "[Width] rect: ${rect.width()} | rootView: ${view.rootView.width} | view: ${view.width} | webView: ${webView.width}"
        )
        Log.d(
            TAG,
            "[Right] rect: ${rect.right} | rootView: ${view.rootView.right} | view: ${view.right} | webView: ${webView.right}"
        )
        Log.d(
            TAG,
            "[Left] rect: ${rect.left} | rootView: ${view.rootView.left} | view: ${view.left} | webView: ${webView.left}"
        )
        Log.d(
            TAG,
            "view.navigationBarHeightV: ${view.navigationBarHeightV}, lastNavigationBarHeightV: ${this.lastNavigationBarHeightV}, " +
                    "view.navigationBarHeightH: ${view.navigationBarHeightH}, lastNavigationBarHeightH: ${this.lastNavigationBarHeightH}"
                    + "\n------------------------------------------------\n"
        )
    }


    companion object {
        /**
         * 绑定使用
         * @param activity 活动
         * @param webView WebView
         * @param inCompose 是否 Compose 布局。[WebViewLayoutManager] 不能与 `Modifier.imePadding()` 同时使用！`inCompose` 实际上没有发挥作用，保留字段后续可能有用
         */
        @JvmStatic
        @JvmOverloads // 自动生成带有默认参数的重载函数供 java 使用
        fun assistActivity(
            activity: Activity,
            webView: WebView,
            inCompose: Boolean = false
        ): WebViewLayoutManager {
            return WebViewLayoutManager(activity, webView, inCompose)
        }
    }
}
