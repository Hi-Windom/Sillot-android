/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/15 上午11:01
 * updated: 2024/7/15 上午11:01
 */

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

    const val gitRepoUrl = "https://github.com/Hi-Windom/Sillot-android"
    const val HOST_ld246 = "ld246.com"
    const val HOST_Sillot_docs = "sillot.db.sc.cn"
    const val URL_Sillot_docs_uprade_webview = "https://${HOST_Sillot_docs}/依赖更新/安卓升级webview/"
}