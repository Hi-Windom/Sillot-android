package sc.windom.sofill.Us

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import com.blankj.utilcode.util.ScreenUtils
import sc.windom.sofill.U

object U_Phone {

    /**
     * 是否为竖屏
     */
    fun Context.isPortrait(): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    /**
     * 是否为横屏
     */
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
                U.DialogX.PopNoteShow(activity, "Error toggling full screen", e)
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