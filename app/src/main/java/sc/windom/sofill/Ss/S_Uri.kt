package sc.windom.sofill.Ss

import android.net.Uri

object S_Uri {

    data class UriMatchPattern(val scheme: String, val host: String)

    fun isUriMatched(uri: Uri?, pattern: UriMatchPattern): Boolean {
        return uri?.scheme == pattern.scheme && uri.host == pattern.host
    }

    val case_ld246_1 = UriMatchPattern("https", "ssl.ptlogin2.qq.com") // 目前必须将默认打开方式浏览器设置为汐洛，否则QQ回调进不来
    val case_ld246_2 = UriMatchPattern("https", "ld246.com")
    val case_github_1 = UriMatchPattern("https", "github.com")
    val case_mqq_1 = UriMatchPattern("wtloginmqq", "ptlogin")
}