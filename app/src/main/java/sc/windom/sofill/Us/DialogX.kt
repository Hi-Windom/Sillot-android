package sc.windom.sofill.Us

import android.content.Context
import com.kongzue.dialogx.dialogs.PopNotification
import sc.windom.sofill.S
import sc.windom.sofill.U.genSpannableColorfulString
import sc.windom.sofill.U.isSystemDarkMode

object U_DialogX {
    private val TAG = "sc.windom.sofill.U.DialogX"

    /**
     *  com.kongzue.dialogx.dialogs.PopNotification.show 的简单封装，用于支持暗黑模式的自动识别
     *  @param context 上下文，一般是 activity
     *  @param params 约定好的1-3个参数，使用逗号分隔。注意类型规范，没有类型检查。
     *
     *  - 参数1个：title
     *  - 参数2个：title, message 或者 iconResId, title
     *  - 参数3个：iconResId, title, message
     *  - 参数类型补充：CharSequence是一个接口，String类实现了这个接口。title 和 message 为 CharSequence。iconResId 为 Int 类型。
     *
     *  PopNoteShow().noAutoDismiss() 是支持的
     */
    @JvmStatic
    fun PopNoteShow(context: Context, vararg params: Any): PopNotification {
        val isDarkMode = isSystemDarkMode(context)
        val builder = PopNotification.build()

        when (params.size) {
            1 -> {
                val title = params[0] as String
                builder.setTitle(if (isDarkMode) genSpannableColorfulString(title, S.COLORINT.LightWhite) else title)
            }
            2 -> {
                val p1 = params.getOrNull(0)
                val p2 = params.getOrNull(1)
                when {
                    p1 is String && p2 is CharSequence -> {
                        val title = p1
                        val message = p2
                        builder.setTitle(if (isDarkMode) genSpannableColorfulString(title, S.COLORINT.LightWhite) else title)
                            .setMessage(if (isDarkMode) genSpannableColorfulString(message, S.COLORINT.LightWhite) else message)
                    }
                    p1 is Int && p2 is CharSequence -> {
                        val iconResId = p1
                        val title = p2
                        builder.setIconResId(iconResId)
                            .setTitle(if (isDarkMode) genSpannableColorfulString(title, S.COLORINT.LightWhite) else title)
                    }
                    else -> return builder.setTitle("Invalid parameter combination.").show()
                }
            }
            3 -> {
                val iconResId = params[0] as Int
                val title = params[1] as String
                val message = params[2] as String
                builder.setIconResId(iconResId)
                    .setTitle(if (isDarkMode) genSpannableColorfulString(title, S.COLORINT.LightWhite) else title)
                    .setMessage(if (isDarkMode) genSpannableColorfulString(message, S.COLORINT.LightWhite) else message)
            }
            else -> builder.setTitle("Invalid parameters for PopNoteShow.").setMessage(params.toString())
        }

        if (isDarkMode)  builder.setBackgroundColor(S.COLORINT.DarkDeep)

        return builder.show().apply {
            if (params.contains(PopNotification::noAutoDismiss)) {
                builder.show().noAutoDismiss()
            } else {
                builder.show()
            }
        }
    }
}