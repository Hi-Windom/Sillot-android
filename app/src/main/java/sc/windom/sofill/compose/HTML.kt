package sc.windom.sofill.compose

import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.RelativeSizeSpan
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import sc.windom.sofill.U
import org.xml.sax.XMLReader
import java.util.Stack

@Composable
fun HtmlText(html: String, modifier: Modifier = Modifier) {
    // 最简单的封装
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                // 设置MovementMethod以使链接可点击
                movementMethod = LinkMovementMethod.getInstance()
            }
        },
        update = { it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT) }
    )
}

@Composable
fun SelectableHtmlText(html: String, modifier: Modifier = Modifier) {
    // 简单处理
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            TextView(context).apply {
                // 允许长按复制文本，需放在前面
                setTextIsSelectable(true)
                // 设置MovementMethod以使链接可点击，需放在后面
                // 尝试过自定义处理逻辑，结果替换个链接都费劲
                movementMethod = LinkMovementMethod.getInstance()
                textSize = 17f // 设置全局字体大小
                // setTextColor() // 设置字体颜色
                // setLinkTextColor() // 设置链接颜色
            }
        },
        update = { textView ->
            val _Html = U.parseAndDecodeUrl(
                html,
                """['"]https://ld246.com/forward\?goto=([^'"]*)['"]""".toRegex()
            )
//            Log.i("HTML", _Html)
            textView.text =
                HtmlCompat.fromHtml(_Html, HtmlCompat.FROM_HTML_MODE_COMPACT, null, MyTagHandler())
        }
    )
}


/**
 * 当 HTML 解析器遇到一个它不知道如何解释的标签时，这个方法将被调用。HTML 和 BODY 例外，这两个可以被处理
 *
 */
class MyTagHandler : Html.TagHandler {
    private val spans = Stack<Int>()

    override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader) {
        if (opening) {
            // 对于开始标签，记录当前output的长度
            spans.push(output.length)
        } else {
            // 对于结束标签，获取对应的开始位置，并应用样式
            if (spans.isNotEmpty()) {
                val start = spans.pop()
                val end = output.length

                // 确保start和end是有效的
                if (start >= 0 && end >= 0 && start < end) {
                    when (tag) {
                        "sillot" -> output.setSpan(
                            RelativeSizeSpan(3f),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        "siyuan" -> {
                        }
                        // 其他标签...
                    }
                }
            }
        }
    }
}
