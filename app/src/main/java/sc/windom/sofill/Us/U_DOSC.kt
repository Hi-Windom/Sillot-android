package sc.windom.sofill.Us

import com.tencent.bugly.crashreport.BuglyLog
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.renderer.markdown.MarkdownRenderer
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist

object U_DOSC {
    private val TAG = "sc.windom.sofill.Us.U_DOSC"
    fun isMarkdown(text: String): Boolean {
        // 检查文本中是否包含Markdown特有的语法特征
        val containsMarkdownSyntax =
            text.contains(Regex("^(#{1,6})\\s|\\*\\s|_\\s|[\\[]\\([^\\)]+\\)[\\]]"))
        // 检查文本的开始和末尾是否是HTML标签
        val startsWithHtmlTag = Regex("^<[a-zA-Z]").find(text) != null
        val endsWithHtmlTag = Regex("[a-zA-Z]>$").find(text) != null

        // 如果文本包含Markdown语法，并且开始和末尾不是HTML标签，则判定为Markdown
        return containsMarkdownSyntax && !startsWithHtmlTag && !endsWithHtmlTag
    }

    fun checkContentFormat(text: String): String {
        // 首先检查文本是否包含Markdown特有的语法
        if (isMarkdown(text)) {
            return "Markdown"
        } else {
            // 创建一个只允许特定HTML标签的Safelist
            val safelist = Safelist.relaxed()
            // 使用Jsoup尝试解析文本，并保留允许的HTML标签
            val cleanText = Jsoup.clean(text, safelist)
            if (cleanText != text) {
                // 文本包含HTML标签，因此是HTML内容
                return "HTML"
            }
        }
        // 如果没有HTML标签，也不是Markdown，则返回其他
        return "Other"
    }

    fun processHtml(html: String): String {
        // 对HTML进行校验和清理
        val safeHtml = sanitizeHtml(html)
        // 处理安全的HTML文本
        BuglyLog.d(TAG, "HTML: $safeHtml")
        return safeHtml
    }

    fun processMarkdown(markdown: String): String {
        val validMarkdown = validateMarkdown2HTML(markdown)
        BuglyLog.d(TAG, "validateMarkdown2HTML: $validMarkdown")
        return validMarkdown
    }

    fun text2Markdown(text: String): String {
        // 使用CommonMark解析器解析Markdown
        val parser = Parser.builder().build()
        val renderer = MarkdownRenderer.builder().build()
        val document: Node = parser.parse(text)
        val validMarkdown = renderer.render(document)
        BuglyLog.d(TAG, "validMarkdown: $validMarkdown")
        return validMarkdown
    }

    fun processPlainText(text: String): String {
        // 处理普通文本
        BuglyLog.d(TAG, text)
        return text
    }

    fun sanitizeHtml(html: String): String {
        // 使用Jsoup的Safelist清理HTML，只允许安全的标签和属性
        val whitelist = Safelist.relaxed()
        return Jsoup.clean(html, whitelist)
    }

    fun validateMarkdown2HTML(markdown: String): String {
        // 使用CommonMark解析器解析Markdown
        val parser = Parser.builder().build()
        val renderer = HtmlRenderer.builder().build()

        try {
            // 解析Markdown文本
            val document: Node = parser.parse(markdown)
            // 渲染Markdown为HTML
            val html: String = renderer.render(document)
            // 如果没有异常，返回渲染后的HTML
            return html
        } catch (e: Exception) {
            // 如果解析过程中发生异常，返回错误信息
            return "Error: ${e.message}"
        }
    }
}