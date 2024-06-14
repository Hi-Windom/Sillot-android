package sc.windom.sofill.Us

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.DragEvent
import android.webkit.WebView
import android.widget.Toast
import com.blankj.utilcode.util.ActivityUtils.startActivity
import com.kongzue.dialogx.dialogs.PopTip
import sc.windom.sofill.S

object U_FuckOtherApp {

    @JvmStatic
    fun setOnDragListenerForWebView(webView: WebView, targetActivityClass: Class<out Activity>) {
        webView.setOnDragListener { view, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val clipData = event.clipData
                    if (clipData != null && clipData.itemCount > 0) {
                        val item = clipData.getItemAt(0)
                        val intent = Intent(view.context, targetActivityClass)
                        when {
                            item.uri != null -> {
                                intent.data = item.uri
                                intent.action = event.action.toString()
                            }
                            item.text != null -> {
                                val text = item.text.toString()
                                intent.putExtra("text_data", text)
                                intent.action = Intent.ACTION_SEND
                                intent.type = "text/plain"
                            }
                        }
                        view.context.startActivity(intent)
                    }
                }
            }
            true
        }
    }

    /**
     * @param packageManager
     * @param recipient 收件人
     * @param subject 邮件主题
     * @param body 邮件正文
     */
    fun sendEmail(
        packageManager: PackageManager,
        recipient: String,
        subject: String?,
        body: String?
    ) {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.setData(Uri.parse("mailto:")) // only email apps should handle this

        // 设置收件人
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        // 设置邮件主题
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        // 设置邮件正文
        emailIntent.putExtra(Intent.EXTRA_TEXT, body)
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        } else {
            PopTip.show("No email client found")
        }
    }

    /**
     * @param context 能够获取到 applicationContext 的都可以，传入 applicationContext 也没问题
     * @param qqNumber QQ号
     * @param tip 提示
     */
    fun launchQQAndCopyToClipboard(context: Context, qqNumber: CharSequence, tip: CharSequence) {
        // 将QQ号复制到剪贴板
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", qqNumber)
        clipboard.setPrimaryClip(clip)

        val intent: Intent? = context.packageManager.getLaunchIntentForPackage(S.AppQueryIDs.QQ)

        if (intent != null) {
            startActivity(intent)
            Toast.makeText(context.applicationContext, tip, Toast.LENGTH_SHORT).show() // 启动后弹出，否则会被吞掉。因为跳转了，使用 applicationContext 才能显示
        } else {
            PopTip.show("QQ 未安装")
        }
    }

    /**
     * @param context 能够获取到 applicationContext 的都可以，传入 applicationContext 也没问题
     * @param TTA 抖音号
     * @param tip 提示
     */
    fun launchTikTopAndCopyToClipboard(context: Context, TTA: CharSequence, tip: CharSequence) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", TTA)
        clipboard.setPrimaryClip(clip)

        val intent: Intent? =
            context.packageManager.getLaunchIntentForPackage(S.AppQueryIDs.抖音)

        if (intent != null) {
            startActivity(intent)
            Toast.makeText(context.applicationContext, tip, Toast.LENGTH_SHORT).show() // 启动后弹出，否则会被吞掉。因为跳转了，使用 applicationContext 才能显示
        } else {
            PopTip.show("抖音未安装")
        }
    }
}