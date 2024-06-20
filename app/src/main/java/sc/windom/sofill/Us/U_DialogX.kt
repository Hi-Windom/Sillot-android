package sc.windom.sofill.Us

import android.content.Context
import com.kongzue.dialogx.dialogs.PopNotification
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener
import sc.windom.sofill.S
import sc.windom.sofill.U.genSpannableColorfulString
import sc.windom.sofill.U.isSystemDarkMode


object U_DialogX {
    private val TAG = "sc.windom.sofill.U.DialogX"

    /**
     * 根据上下文识别系统暗黑模式并生成白字
     */
    private fun Context.genCSCSDA(text: CharSequence): CharSequence {
        val isDarkMode = isSystemDarkMode(this)
        return if (isDarkMode) genSpannableColorfulString(text, S.COLORINT.LightWhite) else text
    }

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
    @Deprecated("之前没注意可以通过 DialogX.globalTheme = DialogX.THEME.AUTO 全局适配，这里保留作为多参数工具函数参考")
    fun PopNoteShow(context: Context, vararg params: Any): PopNotification {
        val isDarkMode = isSystemDarkMode(context)
        val builder = PopNotification.build()

        when (params.size) {
            1 -> {
                val title = params[0] as String
                builder.setTitle(context.genCSCSDA(title))
            }
            2 -> {
                val p1 = params.getOrNull(0)
                val p2 = params.getOrNull(1)
                when {
                    p1 is String && p2 is CharSequence -> {
                        val title = p1
                        val message = p2
                        builder.setTitle(context.genCSCSDA(title))
                            .setMessage(context.genCSCSDA(message))
                    }
                    p1 is Int && p2 is CharSequence -> {
                        val iconResId = p1
                        val title = p2
                        builder.setIconResId(iconResId)
                            .setTitle(context.genCSCSDA(title))
                    }
                    else -> return builder.setTitle("Invalid parameter combination.").show()
                }
            }
            3 -> {
                val iconResId = params[0] as Int
                val title = params[1] as String
                val message = params[2] as String
                builder.setIconResId(iconResId)
                    .setTitle(context.genCSCSDA(title))
                    .setMessage(context.genCSCSDA(message))
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

    /**
     *  com.kongzue.dialogx.dialogs.PopTip.show 的简单封装，用于支持暗黑模式的自动识别
     *  @param context 上下文，一般是 activity
     *  @param params 约定好的1-4个参数，使用逗号分隔。注意类型规范，没有类型检查。
     *
     *  - 参数1个：message
     *  - 参数2个：iconResId, message 或者 message, iconResId
     *  - 参数3个：message, cbText, cb
     *  - 参数4个：message, cbText, cb, iconResId
     *  - 参数类型补充：CharSequence是一个接口，String类实现了这个接口。 message 为 CharSequence。iconResId 为 Int 类型。cb 为无参数 Runnable
     *
     *  PopTipShow().noAutoDismiss() 是支持的
     *  按钮背景色目前没找到 DialogX 设置，因此暗黑模式显示有点违和
     */
    @JvmStatic
    @Deprecated("之前没注意可以通过 DialogX.globalTheme = DialogX.THEME.AUTO 全局适配，这里保留作为多参数工具函数参考")
    fun PopTipShow(context: Context, vararg params: Any): PopTip {
        val isDarkMode = isSystemDarkMode(context)
        val builder = PopTip.build()

        when (params.size) {
            1 -> {
                val message = params[0] as String
                builder.setMessage(context.genCSCSDA(message))
            }
            2 -> {
                val p1 = params.getOrNull(0)
                val p2 = params.getOrNull(1)
                when {
                    p1 is CharSequence && p2 is Int -> {
                        val message = p1
                        val iconResId = p2
                        builder.setIconResId(iconResId)
                            .setMessage(context.genCSCSDA(message))
                    }
                    p1 is Int && p2 is CharSequence -> {
                        val iconResId = p1
                        val message = p2
                        builder.setIconResId(iconResId)
                            .setMessage(context.genCSCSDA(message))
                    }
                    else -> return builder.setMessage("Invalid parameter combination.").show()
                }
            }
            3 -> {
                val message = params[0] as String
                val cbText = params[1] as String
                val cb = params[2] as Runnable
                builder.setMessage(context.genCSCSDA(message))
                    .setButton(cbText)
                    .onButtonClickListener =
                    OnDialogButtonClickListener { dialog, v ->
                        dialog.dismiss()
                        cb.run()
                        false
                    }
            }
            4 -> {
                val message = params[0] as String
                val cbText = params[1] as String
                val cb = params[2] as Runnable
                val iconResId = params[3] as Int
                builder.setIconResId(iconResId)
                    .setMessage(context.genCSCSDA(message))
                    .setButton(cbText)
                    .onButtonClickListener =
                    OnDialogButtonClickListener { dialog, v ->
                        dialog.dismiss()
                        cb.run()
                        false
                    }
            }
            else -> builder.setMessage("Invalid parameters for PopNoteShow.")
        }

        if (isDarkMode)  builder.setBackgroundColor(S.COLORINT.DarkDeep)

        return builder.show().apply {
            if (params.contains(PopTip::noAutoDismiss)) {
                builder.show().noAutoDismiss()
            } else {
                builder.show()
            }
        }
    }
}