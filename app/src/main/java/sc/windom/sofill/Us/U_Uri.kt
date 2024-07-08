/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/9 上午7:00
 * updated: 2024/7/9 上午7:00
 */

package sc.windom.sofill.Us

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import com.blankj.utilcode.util.ActivityUtils.startActivity
import com.kongzue.dialogx.dialogs.PopNotification
import sc.windom.sofill.compose.WebViewActivity
import java.util.Locale

object U_Uri {
    /**
     * 请求意图，来自特殊URL协议
     * @param _url 原始URL
     * @param real_url 真实URL，为空等于 _url
     * @return 返回请求是否成功，不含结果。结果在 Activity 查收
     */
    fun Activity.askIntentForSUS(_url: String, real_url: String = _url): Boolean {
        val TAG = "askIntentForSUS"
        Log.d(TAG, "try to startActivityForResult by $_url")
        PopNotification.show(
            "处理授权回调注意事项",
            "必须先将汐洛设置为默认浏览器（获得授权后即可恢复默认浏览器）。"
        ).noAutoDismiss()
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(real_url))
            ActivityCompat.startActivityForResult(this, intent, 1, null)
            true
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            false
        }
    }


    fun openUrl(url: String, noBrowser: Boolean = false) {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (noBrowser) {
            i.addFlags(Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER)
        }
        try {
            startActivity(i)
        } catch (e: Exception) {
            PopNotification.show(e.message, e.stackTrace.toString()).noAutoDismiss()
        }
    }

    /**
     * 从 Android 12 开始，经过验证的链接现在会自动在相应的应用中打开，以获得更简化、更快速的用户体验。
     * 谷歌还更改了未经Android应用链接验证或用户手动批准的链接的默认处理方式。
     * 谷歌表示，Android 12将始终在默认浏览器中打开此类未经验证的链接，而不是向您显示应用程序选择对话框。
     *
     */
    @JvmStatic
    fun openURLUseDefaultApp(url: String) {
        val uri = Uri.parse(url)
        if (!uri.scheme.isNullOrEmpty() && uri.scheme?.lowercase(Locale.getDefault())
                ?.startsWith("http") == true
        ) {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    @JvmStatic
    fun openURLUseSB(context: Context, url: String) {
        val uri = Uri.parse(url)
        val webIntent = Intent(context, WebViewActivity::class.java);
        webIntent.setData(uri)
        webIntent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
                    or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                    or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        );
        webIntent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
        startActivity(webIntent);
    }

}