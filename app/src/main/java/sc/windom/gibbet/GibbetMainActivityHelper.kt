/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/17 13:18
 * updated: 2024/8/17 13:18
 */

package sc.windom.gibbet

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.view.DragEvent
import android.view.KeyEvent
import android.webkit.WebView
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.tencent.bugly.crashreport.BuglyLog
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mobile.Mobile
import sc.windom.potter.producer.MainPro
import sc.windom.sofill.Ss.QQ
import sc.windom.sofill.Ss.QQMail
import sc.windom.sofill.Ss.REQUEST_CAMERA
import sc.windom.sofill.Ss.抖音
import sc.windom.sofill.U
import sc.windom.sofill.Us.U_DEBUG
import sc.windom.sofill.Us.U_FuckOtherApp
import sc.windom.sofill.Us.U_Phone
import sc.windom.sofill.Us.U_Pro.ContentInsertCallback
import sc.windom.sofill.Us.U_Pro.onDragSend2Producer
import sc.windom.sofill.Us.U_Pro.onDragWebView
import sc.windom.sofill.Us.U_Thread.runOnUiThread
import sc.windom.sofill.Us.addFlagsForMatrixModel
import sc.windom.sofill.android.HWs
import sc.windom.sofill.dataClass.IInsertBlockNextRequest
import sc.windom.sofill.pioneer.mmkvGibbet
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
                        Json.encodeToString(payload)
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

fun Activity.handleKeyEvent(event: KeyEvent): Boolean {
    val TAG = "handleKeyEvent"
    if (event.action == KeyEvent.ACTION_DOWN) {
        BuglyLog.w(TAG, event.keyCode.toString())
        if (event.keyCode == KeyEvent.KEYCODE_ESCAPE) { // getKeyCode 的数字只能拿来和 KeyEvent 里面的对比，不然没有意义
            // 处理ESC键按下事件，并不能阻止输入法对ESC的响应，只有输入法退出了才轮到这里。
            // 除非设置 WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM，那键盘需要自己处理了
            return true  // 返回true表示事件已被处理，不再传递
        }
    }
    // 事件未被处理
    return false
}

fun Activity.androidFeedback() {
    val menuOptions = arrayOf("电子邮件", "QQ", "抖音")
    BottomMenu.show(menuOptions)
        .setMessage("请选择反馈渠道")
        .setOnMenuItemClickListener { dialog, text, index ->
            when (text) {
                "电子邮件" -> {
                    U_FuckOtherApp.sendEmail(packageManager, QQMail, "汐洛安卓反馈", U_DEBUG.getDeviceInfoString())
                }
                "QQ" -> {
                    U_FuckOtherApp.launchQQAndCopyToClipboard(this, QQ, "开发者 QQ 号已复制")
                }
                "抖音" -> {
                    U_FuckOtherApp.launchTikTopAndCopyToClipboard(this, 抖音, "开发者抖音号已复制")
                }
            }
            false
        }
}

fun setSillotGibbetCheckInState(webView: WebView): Boolean {
    val TAG = "setSillotGibbetCheckInState"
    var result = false
    runOnUiThread {
        val webViewUrl = webView.url
        result = webViewUrl?.contains("/check-auth?") == true
        mmkvGibbet.putString("AppCheckInState", if (result) "lockScreen" else "unlockScreen")
        BuglyLog.d(TAG, "exit() AppCheckInState -> ${if (result) "lockScreen" else "unlockScreen"}")
    }
    return result
}

fun handleOnBack(applicationContext: Context, webView: WebView, exitTime: Long): Long {
    // 在这里处理后退逻辑
    if (U_Phone.isPad(applicationContext)) {
        if (System.currentTimeMillis() - exitTime > 2000) {
            PopTip.show("再按一次退出汐洛绞架")
        } else {
            HWs.instance?.vibratorWaveform(
                applicationContext,
                longArrayOf(0, 30, 25, 40, 25, 10),
                intArrayOf(2, 4, 3, 2, 2, 2),
                -1
            )
            webView.evaluateJavascript("javascript:window.location.href = 'siyuan://api/system/exit';") { }
        }
    } else {
        webView.evaluateJavascript("javascript:window.goBack ? window.goBack() : window.history.back()") { }
    }
    HWs.instance?.vibratorWaveform(
        applicationContext,
        longArrayOf(0, 30, 25, 40, 25),
        intArrayOf(9, 2, 1, 7, 2),
        -1
    )
    return System.currentTimeMillis()
}

fun Activity.coldRestart(cl: Class<out Activity>, webView: WebView) {
    BuglyLog.w("coldRestart", "coldRestart() invoked")
    setSillotGibbetCheckInState(webView)
    // 从任务列表中移除，禁止放在 onDestroy
    finishAndRemoveTask() // 这个方法用于结束当前活动，并从任务栈中移除整个任务。这意味着，当前活动所在的任务中所有的活动都会被结束，并且任务本身也会被移除。如果这个任务是最顶层的任务，那么用户将返回到主屏幕。
    val intent = Intent(this, cl)
    intent.addFlagsForMatrixModel()
    startActivity(intent)
    android.os.Process.killProcess(android.os.Process.myPid()) // 暂时无法解决杀死其他任务栈的冲突，不加这句无法重启内核
}

fun Activity.openCamera(): Uri? {
    var photoUri: Uri? = null
    val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    val packageManager: PackageManager = getPackageManager()
    if (captureIntent.resolveActivity(packageManager) != null) {
        photoUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
        photoUri?.let {
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, it)
            captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(captureIntent, REQUEST_CAMERA)
        }
    }
    return photoUri
}
