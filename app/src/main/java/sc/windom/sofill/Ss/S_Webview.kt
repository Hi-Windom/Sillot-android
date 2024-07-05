package sc.windom.sofill.Ss

object S_Webview {
    val UA_edge_android = "Mozilla/5.0 (Linux; Android 12; K) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/120.0.0.0 " +
            "Mobile Safari/537.36 " +
            "EdgA/120.0.0.0 " // edge 浏览器安卓UA

    val UA_win10 = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"

    val searchEngines = mapOf(
        "必应" to "www.bing.com/?q=", // 一般都会重定向到国内版
        "必应中国" to "cn.bing.com/?q=",
        "秘塔AI搜索" to "metaso.cn/?q=",
        "DuckDuckGo（需要科学上网）" to "duckduckgo.com/?q=",
//  需要登录      "Yandex" to "yandex.ee/?q=",
    )
}