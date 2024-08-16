/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/15 00:28
 * updated: 2024/8/15 00:28
 */

package sc.windom.sofill.android.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowInsets
import android.view.WindowManager
import android.webkit.WebView
import androidx.annotation.RequiresApi
import sc.windom.sofill.U.isInSpecialMode
import sc.windom.sofill.Us.adjustLayoutMarginForSystemBars
import sc.windom.sofill.Us.displayMetrics
import sc.windom.sofill.Us.getSystemBarInsets
import sc.windom.sofill.Us.navigationBarHeight
import sc.windom.sofill.Us.statusBarHeight
import sc.windom.sofill.Us.visibleRect
import sc.windom.sofill.base.Debuggable
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
 * - 微信输入法切换屏幕方向后会收起软键盘，初步判断是微信输入法的问题, 因为即使不重新布局也是一样的
 * - 无法有效检测到小窗模式下键盘是否显示, 因为安卓不开放 `ImeTracker` 接口
 *
 * 参考引用：
 * - [Yingyi](https://ld246.com/member/shuoying) 的 AndroidBug5497Workaround 。
 *
 * ### 在 Compose 中使用
 *
 *  [WebViewLayoutManager] 现在只参与 `webview` 的重新布局, 因此可以与 Compose 布局同时使用了,
 *
 * 在根节点添加以下代码:
 *  ```kotlin
 *  modifier = Modifier.imePadding().navigationBarsPadding().statusBarsPadding()
 *  ```
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
 * @see [applyViewColorToSystemBar]
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
 */
//  [Android small window mode soft keyboard black occlusion](https://github.com/siyuan-note/siyuan-android/pull/7)
//  [优化注册工具栏显示/隐藏跟随软键盘状态](https://github.com/Hi-Windom/Sillot-android/issues/84)
@SuppressLint("WrongConstant", "ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.S)
class WebViewLayoutManager private constructor(
    private val activity: Activity,
    private val webView: WebView,
    private val monitor: View,
) : Debuggable("WebViewLayoutManager") {

    /**
     * 避免作用域污染
     */
    private var thisIns: WebViewLayoutManager

    /**
     * 收窄布局延时执行时间。键盘弹起到最后高度需要一个过程，因此收窄布局应当延时执行（不包括小窗和多窗口模式），
     *  - 延时多久没有标准，考虑到不同机型和输入法的表现差异，不宜设置太小，否则会导致键盘弹起时短暂看到布局底色，推荐赋值范围 `131-186`。
     *  - 如果已经在 [onImeInsetsCallback] 中调用了 [applyViewColorToSystemBar] 填充背景底色，则可以不覆盖该属性值（默认为0）或者设置为一个较小的值。
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
     *
     * @suppress 不推荐 [applyViewColorToSystemBar] , 因为 [edgeToEdge] 已经包含
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
     *
     * @suppress 不推荐 [applyViewColorToSystemBar] , 因为 [edgeToEdge] 已经包含
     */
    @JvmField
    var onLayoutChangedCallback: ((monitor: View) -> Unit)? = null

    /**
     * ## 软键盘显示隐藏时的回调函数
     *
     * @suppress 不推荐 [applyViewColorToSystemBar] , 因为 [edgeToEdge] 已经包含
     *
     */
    @JvmField
    var onImeInsetsCallback: ((insets: WindowInsets) -> Unit)? = null

    /**
     * ### activity.window 焦点改变的回调, 比如前后台切换
     *
     * - 传参 `hasFocus` 表示是否可见(具有焦点), 注意 window 可见性不等于 `View.hasFocus()`
     * - 首次调用可能发生在失去焦点后, 即默认可见时可能不会调用 (因系统而异, 比如小米系统默认可见会调用, 而vivo系统不会) ,
     * 因此回调代码可能需要手动先执行一次
     */
    @JvmField
    var windowChangeFocusCallback: ((hasFocus: Boolean) -> Unit)? = null


    /**
     * # 启用沉浸式状态栏与导航栏
     *
     * #### 启用前请检查 [applyViewColorToSystemBar] 的不兼容声明
     */
    @JvmField
    var edgeToEdge: Boolean = false

    /**
     * TODO: 额外修剪的高度, 适用于有其他布局挤占了 webview 的空间
     */
    @JvmField
    var cutHeight = 0

    /**
     * 支持悬浮键盘, 不支持小窗模式 (目前没有其他办法)
     *
     * insets监听中判断新旧值可以避免两次调用 restLayout ，但是不能这么做，原因见 [restLayout]
     */
    private var isImeVisible = false

    /**
     * 记录上次的状态，用于识别从悬浮键盘切换
     */
    private var currentImeVisible = false

    /**
     * # 不等于实际软键盘高度
     *
     * 1. 小窗模式中的值为 0
     * 2. 由于使用 `insets.getInsets(WindowInsets.Type.ime()).bottom` 赋值, 普通模式下可能等于底部导航条高度
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
     * 上一次的屏幕方向, 支持四个角度的方向, 而不是简单的横竖屏
     */
    private var lastOrientation: Int? = 0

    /**
     * 导航条高度, 适配屏幕方向变化
     */
    private var lastNavigationBarHeight = 0

    /**
     * 状态栏高度
     *
     */
    private var lastStatusBarHeight = 0

    /**
     * 托管布局的内容视图, 应当是 [webView] 的容器视图, 通常是直系 `webView.parent`, compose 中动态布局充满不确定性, 可能是 `webView.parent.parent...`,
     * 可以通过 [setViewOnGlobalLayout] 在视图就绪的时候修改
     */
    var view: View
    var setViewOnGlobalLayout: (() -> Unit)? = null
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
        get() = synchronized(thisIns) {
            _callIdCounter
        }

    init {
        this.view = activity.window.decorView // 初始化 view
        monitor.setOnApplyWindowInsetsListener { v, insets ->
            bug("OnApplyWindowInsets")
            if (imeHeight != insets.getInsets(WindowInsets.Type.ime()).bottom) {
                // 此监听器触发条件非常宽松，因此判断 Ime 相关（不支持小窗模式和悬浮键盘）以免死循环发生。
                onImeInsetsCallback?.invoke(insets)
            }
            isImeVisible = insets.isVisible(WindowInsets.Type.ime()) // 不支持小窗模式
            imeHeight = insets.getInsets(WindowInsets.Type.ime()).bottom
            // 似乎更稳定
            lastStatusBarHeight = view.visibleRect.top
//            view.statusBarHeight.let {
//                lastStatusBarHeight = it
//            }
            restLayout(TRAKER_INSETS)
            if (edgeToEdge) applyViewColorToSystemBar(
                activity,
                webView,
                "edgeToEdge (OnApplyWindowInsets)"
            )
            insets
        }

        // 本方案使用 isVisible(Type.ime()) 替代 KeyboardUtils.registerSoftInputChangedListener
        // 后已经可以做到准确识别键盘是否显示和键盘高度，但仍需要通过监听布局变化以识别小窗模式和多窗口模式
        monitor.viewTreeObserver.addOnGlobalLayoutListener {
            bug("OnGlobalLayout")
            if (edgeToEdge) applyViewColorToSystemBar(
                activity,
                webView,
                "edgeToEdge (OnGlobalLayout)",
            )
            setViewOnGlobalLayout?.invoke()
            val currentWidth = monitor.width
            val currentHeight = monitor.height
            val currentOrientation = activity.display?.rotation // 获取当前屏幕方向
            lastNavigationBarHeight = monitor.navigationBarHeight(currentOrientation)
            // 为了避免循环调用，因此需要检查布局的宽度和高度是否发生了变化
            if (
                currentOrientation != lastOrientation ||
                currentWidth != lastLayoutWidth ||
                currentHeight != lastLayoutHeight
            ) {
                lastLayoutWidth = currentWidth
                lastLayoutHeight = currentHeight
                lastOrientation = currentOrientation
                activity.window.setSoftInputMode(softInputMode)
                restLayout(TRAKER_LAYOUT)
                onLayoutChangedCallback?.invoke(monitor)
            }
        }
        monitor.viewTreeObserver.addOnWindowFocusChangeListener(object :
            ViewTreeObserver.OnWindowFocusChangeListener {
            /**
             * 可见性变化, 比如拉一下状态栏
             */
            override fun onWindowFocusChanged(hasFocus: Boolean) {
                bug(
                    "onWindowFocusChanged ->  webView.hasFocus: ${webView.hasFocus()} hasWindowFocus: $hasFocus"
                )
                if (hasFocus && edgeToEdge) {
                    applyViewColorToSystemBar(
                        activity,
                        webView,
                        "edgeToEdge (onWindowFocusChanged)"
                    )
                }
                windowChangeFocusCallback?.invoke(hasFocus)
            }
        })
        /**
         * 目前来看没用
         */
        monitor.viewTreeObserver.addOnWindowAttachListener(object :
            ViewTreeObserver.OnWindowAttachListener {
            override fun onWindowAttached() {
                bug("onWindowAttached")
            }

            override fun onWindowDetached() {
                bug("onWindowDetached")
            }
        })
        /**
         * 目前来看没用
         */
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            monitor.viewTreeObserver.addOnWindowVisibilityChangeListener(object :
                ViewTreeObserver.OnWindowVisibilityChangeListener {
                override fun onWindowVisibilityChanged(visibility: Int) {
                    bug("[API 34 Preview] onWindowVisibilityChanged -> $visibility")
                }
            })
        }
        // 监听配置变化。如果 onConfigurationChangedCallback 不为空则不应在 activity 中重写 onConfigurationChanged 方法，否则回调无效。
        onConfigurationChangedCallback?.let {
            activity.registerComponentCallbacks(object : ComponentCallbacks {
                override fun onConfigurationChanged(newConfig: Configuration) {
                    log("onConfigurationChanged -> $newConfig")
                    it.invoke(newConfig)
                }

                override fun onLowMemory() {
                    log("onLowMemory")
                }
            })
        }
        val display = activity.displayMetrics
        bug(
            "init done. StatusBarHeight: ${webView.statusBarHeight}" +
                    ", display.widthPixels: ${display.widthPixels}, display.heightPixels: ${display.heightPixels}"
        )
        // 初始化屏幕方向
        this.lastOrientation = activity.display?.rotation
        // 初始化 thisIns 在 init 块最后
        thisIns = this
    }

    /**
     * 重置布局。requestLayout() 会触发 OnApplyWindowInset，不过不必担心死循环，函数内已判断仅当需要重置布局时才重置布局。
     * 而且，同一个 ime inset 两次调用 requestLayout() 是必要的，因为要支持悬浮键盘和小窗模式。
     * @param tracker 跟踪调用者
     */
    // 函数内 thisIns 可以省略，但是阅读起来不够直观
    private fun restLayout(tracker: String) {
        synchronized(thisIns) { // 每个实例都有自己的锁
            _callIdCounter++
            val callId = _callIdCounter
            if (webView.parent == null) {
                warn(
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
                warn(
                    "[${callId}] restLayout@$tracker, webView.layoutParams == null -> break restLayout()"
                )
                return
            }

            /**
             * 小窗和多窗口模式
             */
            val isInSpecialMode_lock =
                imeHeight == 0 && activity.isInSpecialMode()

            /**
             * 从悬浮键盘切换至非悬浮键盘, 不支持小窗模式
             */
            val fromFloating2Normal_lock =
                currentImeVisible && isImeVisible
                        && imeHeight != 0 && imeHeight != lastNavigationBarHeight
                        && currentImeHeight == lastNavigationBarHeight

            /**
             * 从非悬浮键盘切换至悬浮键盘, 不支持小窗模式
             */
            val fromNormal2Floating_lock =
                currentImeVisible && isImeVisible
                        && currentImeHeight != 0 && currentImeHeight != lastNavigationBarHeight
                        && imeHeight == lastNavigationBarHeight

            /**
             * 键盘 0 高, 普通模式下未呼出键盘, 也可能是小窗模式或者悬浮输入法
             */
            val isIme0H =
                isInSpecialMode_lock || imeHeight == lastNavigationBarHeight


            /**
             * # 大道至简
             *
             * 兼容经典导航键、小米系统小窗底部小白条、实体键盘
             */
            val newHeight = view.visibleRect.height() - cutHeight

//                view.rootView.height -
//                    (if (needFitNavigationBarHeight_lock) lastNavigationBarHeight else 0) -
//                    (if (isImeVisible) imeHeight else 0)
            bug("[${callId}] [ restLayout @$tracker ] cutHeight: $cutHeight newHeight: -> $newHeight <- " +
                    "lastStatusBarHeight: ${lastStatusBarHeight}: , " +
                    "imeHeight: ${imeHeight}")

            /**
             * nochange 判断太难维护, 而且降低频次对性能感知不强, 反而不够丝滑
             */
            var nochange = (tracker == TRAKER_INSETS) &&
                    (currentImeVisible == isImeVisible) && (currentImeHeight == imeHeight)
                    && callId > 1 // 首次调用无法判断
                    && !isInSpecialMode_lock && !fromFloating2Normal_lock && !fromNormal2Floating_lock

            warn(
                "[${callId}] [ restLayout @$tracker ] " +
                        "currentImeVisible: ${currentImeVisible}, currentImeHeight: ${currentImeHeight}, " +
                        "isImeVisible: ${isImeVisible}, imeHeight:${imeHeight}, " +
                        "isInSpecialMode: ${activity.isInSpecialMode()}, " +
                        "从悬浮键盘切换至非悬浮键盘: $fromFloating2Normal_lock, " +
                        "从非悬浮键盘切换至悬浮键盘: $fromNormal2Floating_lock"
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                inputMethodManager.currentInputMethodInfo?.let { ime ->
                    // ime.packageName + ime.serviceName = ime.id
                    log(
                        "OnApplyWindowInsets [ ime ] packageName: ${ime.packageName}, serviceName: ${ime.serviceName}"
                    )
                }
            }
            inputMethodManager.lastInputMethodSubtype
            logInfo(callId) // before restLayout

            currentImeVisible = isImeVisible
            currentImeHeight = imeHeight

            view.setPadding(0, 0, 0, 0)
            if (isImeVisible) {
                // 键盘弹起到最后高度需要一个过程，因此收窄布局应当延时执行（不包括小窗和多窗口模式），延时多久没有标准
                // （如果声明了 android:windowSoftInputMode="adjustResize" 则无效，因为系统已经自动调整了布局）
                val delayTime: Long = if (
                    fromFloating2Normal_lock || fromNormal2Floating_lock
                ) 0 else delayResetLayoutWhenImeShow
                if (nochange) {
                    log(
                        "[${callId}] [ restLayout @$tracker ] requestLayout skip, nochange"
                    )
                } else {
                    Handler(Looper.getMainLooper()).post(
                        {
                            webView.layoutParams.height = newHeight
                            webView.requestLayout()
                            log(
                                "[${callId}] [ restLayout @$tracker ] requestLayout done, " +
                                        "view.height: ${view.height}, webView.height: ${webView.height}"
                            )
                        }
                    )
                }
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        if (isIme0H) {
                            webView.evaluateJavascript(JSonImeShow0Height, null)
                        } else {
                            webView.evaluateJavascript(JSonImeShow, null)
                        }
                    },
                    delayTime
                )
            } else {
                if (nochange) {
                    log(
                        "[${callId}] [ restLayout @$tracker ] requestLayout skip, nochange"
                    )
                } else {
                    // 填充布局应当立即执行
                    webView.layoutParams.height = newHeight
                    webView.requestLayout()
                    log(
                        "[${callId}] [ restLayout @$tracker ] requestLayout done, " +
                                "view.height: ${view.height}, webView.height: ${webView.height}"
                    )
                }
                if (isIme0H) {
                    webView.evaluateJavascript(JSonImeHide0Height, null)
                } else {
                    webView.evaluateJavascript(JSonImeHide, null)
                }
            }
        }
    }

    /**
     * 打印日志信息，调试时使用
     */
    private fun logInfo(callId: Int) {
        if (debugLevel <= DebugLevel.MINIMAL) return
        try {
            val rect = view.visibleRect
            val deco = activity.window.decorView
            val SBI = view.getSystemBarInsets()
            val p1 = webView.parent as? ViewGroup
            val p2 = webView.parent.parent as? ViewGroup
            val p3 = webView.parent.parent.parent as? ViewGroup
            val p4 = webView.parent.parent.parent.parent as? ViewGroup
//            bug(
//                "\n------------------------------------------------\n" +
//                        "[${callId}] [hash] view: ${view.hashCode()} webView: ${webView.hashCode()} " +
//                        "rootView: ${view.rootView.hashCode()} decorView: ${deco.hashCode()}"
//            )
            bug(
                "[${callId}] [uniqueDrawingId] view: ${view.uniqueDrawingId} webView: ${webView.uniqueDrawingId} " +
                        "rootView: ${view.rootView.uniqueDrawingId} decorView: ${deco.uniqueDrawingId}"
            )
            bug(
                "[${callId}] [javaClass] view: ${view.javaClass.name} " +
                        "parent*N: [${p1?.javaClass?.simpleName} -> ${p2?.javaClass?.simpleName}" +
                        " -> ${p3?.javaClass?.simpleName} -> ${p4?.javaClass?.simpleName}]"
            )
            bug(
                "[${callId}] [SBI] " +
                        "Left: ${SBI.left} " +
                        "Top: ${SBI.top} " +
                        "Right: ${SBI.right} " +
                        "Bottom: ${SBI.bottom}"
            )
            bug(
                "[${callId}] [Height] rect: ${rect.height()} view: ${view.height} " +
                        "webView: ${webView.height} parent*N: [${p1?.height} ${p2?.height} ${p3?.height} ${p4?.height}] decorView: ${deco.height}"
            )
            bug(
                "[${callId}] [Width] rect: ${rect.width()} view: ${view.width} " +
                        "webView: ${webView.width} parent*N: [${p1?.width} ${p2?.width} ${p3?.width} ${p4?.width}] decorView: ${deco.width}"
            )
        } catch (e: Exception) {
            bug(e.stackTraceToString())
        }
    }


    companion object {
        /**
         * 绑定使用
         * @param activity 活动
         * @param webView WebView
         * @param monitor 监听布局的视图
         */
        @JvmStatic
        @JvmOverloads // 自动生成带有默认参数的重载函数供 java 使用
        fun assistActivity(
            activity: Activity,
            webView: WebView,
            monitor: View,
        ): WebViewLayoutManager {
            return WebViewLayoutManager(activity, webView, monitor)
        }
    }
}
