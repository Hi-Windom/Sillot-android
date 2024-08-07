package sc.windom.sofill.Us

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.util.Log
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager
import com.kongzue.dialogx.dialogs.PopNotification
import sc.windom.sofill.S
import sc.windom.sofill.U
import splitties.systemservices.inputMethodManager
import kotlin.math.pow
import kotlin.math.sqrt

object U_Phone {
    private val TAG = "Us.U_Phone"

    /**
     * 并不会直接导致高刷率的生效，它只是在获取支持的显示模式中寻找高刷率最大的模式，并将其设置为首选模式。
     */
    @JvmStatic
    fun Activity.setPreferredDisplayMode() {
        val display = this.display ?: return
        val modes = display.supportedModes
        var preferredMode = modes[0]

        for (mode in modes) {
            // Log.d(TAG, "supported mode: $mode")
            if (mode.refreshRate > preferredMode.refreshRate && mode.physicalWidth >= preferredMode.physicalWidth) {
                preferredMode = mode
            }
        }

        Log.d(TAG, "apply preferredMode mode: $preferredMode to ${this.javaClass.simpleName}")
        val params = window.attributes
        params.preferredDisplayModeId = preferredMode.modeId
        window.attributes = params
    }
    @JvmStatic
    fun inputMethodList(): MutableList<InputMethodInfo> {
        return inputMethodManager.inputMethodList
    }

    @JvmStatic
    fun enabledInputMethodList(): MutableList<InputMethodInfo> {
        return inputMethodManager.enabledInputMethodList
    }

    @JvmStatic
    fun showInputMethodPicker() {
        inputMethodManager.showInputMethodPicker()
    }

    @JvmStatic
    fun showInputMethodPicker(context: Context) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showInputMethodPicker()
    }

    @JvmStatic
    fun isOriginOS(applicationContext: Context): Boolean {
        val packageManager = applicationContext.packageManager
        val miuiPackageName = S.AppQueryIDs.变形器
        return try {
            packageManager.getPackageInfo(miuiPackageName, PackageManager.GET_META_DATA)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    @JvmStatic
    fun isMIUI(applicationContext: Context): Boolean {
        val packageManager = applicationContext.packageManager
        val miuiPackageName = S.AppQueryIDs.小米相册
        return try {
            packageManager.getPackageInfo(miuiPackageName, PackageManager.GET_META_DATA)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    @JvmStatic
    fun isLargeScreenMachine(context: Context): Boolean {
        // 获取屏幕的方向
        val screenLayout = context.resources.configuration.screenLayout
        // 获取屏幕尺寸的掩码
        val sizeMask = Configuration.SCREENLAYOUT_SIZE_MASK
        // 获取屏幕尺寸的值
        val screenSize = screenLayout and sizeMask

        // 如果屏幕尺寸是超大屏或者巨屏，则可能是平板电脑
        return screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE ||
                screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    @JvmStatic
    fun isPad(context: Context): Boolean { // Converted from Utils.java
        val metrics = context.resources.displayMetrics
        val widthInches = metrics.widthPixels / metrics.xdpi
        val heightInches = metrics.heightPixels / metrics.ydpi
        val diagonalInches = sqrt(
            widthInches.toDouble().pow(2.0) + heightInches.toDouble()
                .pow(2.0)
        )
        return diagonalInches >= 7
    }

    /**
     * 是否为竖屏
     */
    @JvmStatic
    fun Context.isPortrait(): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    /**
     * 是否为横屏
     */
    @JvmStatic
    fun Context.isLandscape(): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    /**
     * 支持在非 UI 线程中调用。使用自有全屏判断函数，比 com.blankj.utilcode.util.ScreenUtils.isFullScreen 准确
     * @see [Activity.toggleFullScreen]
     */
    @JvmStatic
    fun toggleFullScreen(activity: Activity) {
        activity.runOnUiThread {
            try {
                val isFullScreen = isFullScreen(activity)
                activity.toggleFullScreen(!isFullScreen)
            } catch (e: Exception) {
                PopNotification.show("Error toggling full screen", e.toString())
            }
        }
    }
    /**
     * 设计为结合 compose rememberSavable 使用
     */
    fun Activity.toggleFullScreen(fullScreen: Boolean) {
        val windowInsetsController = window.insetsController ?: return

        if (fullScreen) {
            enterFullScreen(windowInsetsController)
        } else {
            exitFullScreen(windowInsetsController)
        }
    }

    @JvmStatic
    fun isFullScreen(activity: Activity): Boolean {
        val windowInsets = activity.window.decorView.rootWindowInsets
        val insets = windowInsets?.getInsets(WindowInsets.Type.systemBars())
        // 如果系统栏的 insets 都是 0，那么可以认为应用是全屏的
        if (insets != null) {
            return insets.top == 0 && insets.bottom == 0 && insets.left == 0 && insets.right == 0
        }
        return false
    }

    private fun Activity.enterFullScreen(windowInsetsController: WindowInsetsController) {
        val params = window.attributes
        params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        window.attributes = params
        windowInsetsController.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        hideSystemBars(windowInsetsController)
    }

    private fun Activity.exitFullScreen(windowInsetsController: WindowInsetsController) {
        val params = window.attributes
        params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
        window.attributes = params
        windowInsetsController.systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
        showSystemBars(windowInsetsController)
    }

    private fun Activity.hideSystemBars(windowInsetsController: WindowInsetsController) {
        windowInsetsController.hide(WindowInsets.Type.statusBars())
        windowInsetsController.hide(WindowInsets.Type.systemBars())
        windowInsetsController.hide(WindowInsets.Type.navigationBars())
        windowInsetsController.hide(WindowInsets.Type.captionBar())
        windowInsetsController.hide(WindowInsets.Type.systemGestures())
    }

    private fun Activity.showSystemBars(windowInsetsController: WindowInsetsController) {
        windowInsetsController.show(WindowInsets.Type.statusBars())
        windowInsetsController.show(WindowInsets.Type.systemBars())
        windowInsetsController.show(WindowInsets.Type.navigationBars())
        windowInsetsController.show(WindowInsets.Type.captionBar())
        windowInsetsController.show(WindowInsets.Type.systemGestures())
    }

}