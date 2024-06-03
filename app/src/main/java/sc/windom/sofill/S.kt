@file:Suppress("CompositionLocalNaming", "CompositionLocalNaming")
package sc.windom.sofill

import android.net.Uri
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

object S {

    const val packageName = "sc.windom.sillot"
    const val siyuanPackage = "org.b3log.siyuan"
    const val gitRepoUrl = "https://github.com/Hi-Windom/Sillot-android"
    const val emailAdress = "694357845@qq.com"
    const val QQ = "694357845"
    const val TTK = "AsyncTTk" // 抖音
    const val workspaceParentDir = "/storage/emulated/0/Android/data/$packageName/files"

    const val DefaultHTTPPort = 58131
    const val initCrashReportID = "26ae2b5fb4"
    const val initBaiduPushID = "6Ef26UV3UyM5b7NyiwiGAnM0"

    const val KEY_TOKEN_ld246 = "TOKEN_ld246"
    const val KEY_AES_TOKEN_ld246 = "AES_KEY_TOKEN_ld246"
    const val KEY_TOKEN_Sillot_Gibbet = "TOKEN_Sillot_Gibbet"
    const val KEY_AES_TOKEN_Sillot_Gibbet = "AES_KEY_TOKEN_Sillot_Gibbet"

    const val HOST_ld246 = "ld246.com"


    const val XLQTFW_notificationId = 58131
    const val USB_AUDIO_EXCLUSIVE_notificationId = 7654321

    // 通知频道ID
    const val XLQTFW_notificationChannelId = "sillot_notification_channel_id_58131$XLQTFW_notificationId" // 🦢 汐洛前台通知服务
    const val SILLOT_MUSIC_PLAYER_NOTIFICATION_CHANNEL_ID = "sillot_notification_channel_id_$USB_AUDIO_EXCLUSIVE_notificationId" // 🦢 汐洛音乐播放服务
    const val SY_NOTIFICATION_CHANNEL_ID = "sillot_notification_channel_id_6806"  // 📚 思源内核服务
    const val FloatingWindowService_NOTIFICATION_CHANNEL_ID = "sillot_notification_channel_id_100001"

    const val URIMainActivity = "$siyuanPackage.MainActivity"

    // REQUEST CODE
    const val REQUEST_SELECT_FILE = 100
    const val REQUEST_CAMERA = 101
    const val REQUEST_LOCATION = 1002
    const val VIDEO_PICK_REQUEST_CODE = 10001
    const val REQUEST_IGNORE_BATTERY_OPTIMIZATIONS_AND_REBOOT = 10002 // 申请电源优化无限制权限后重启，部分系统没有该权限
    const val REQUEST_IGNORE_BATTERY_OPTIMIZATIONS = 10002 // 申请电源优化无限制权限，部分系统没有该权限
    const val REQUEST_CODE_MANAGE_STORAGE = 10003
    const val REQUEST_OVERLAY = 10004 // 悬浮窗
    const val REQUEST_POST_NOTIFICATIONS = 10005 // 读取通知
    const val REQUEST_CODE_INSTALL_PERMISSION = 10006 // 安装应用
    const val REQUEST_ACCESS_FINE_LOCATION = 10007 // 精确位置


    object API {
        val ld246_notification_type = listOf("回帖", "评论", "回复", "提及", "关注", "积分")
        val ld246_notification_type_EN = listOf("commented", "comment2ed", "reply", "at", "following", "point")
    }

    object ColorStringHex {
        val bgColor_light = "#1e1e1e"
    }

    // compose 组件常量
    object C {
        // sp（缩放无关像素）：用于字体大小，会根据用户的字体大小偏好进行缩放。
        // dp（密度无关像素）：用于布局尺寸和位置，不可用于字体。
        // 抽取出来作为全局可复用默认值（不是所谓的全局变量），在预览模式下可用CompositionLocalProvider单独调整。应当仅用于常量，理由如下：
        // CompositionLocal 设计用于在 Compose 的树结构中传递数据，而不是用于存储可变状态。CompositionLocal 的值在 Compose 的重组过程中保持不变，除非您显式地使用 CompositionLocalProvider 更新它们
        val Thumbnail_Height = compositionLocalOf { 150 }
        val Thumbnail_Height_IMG = compositionLocalOf { 250 }
        val Button_Width = compositionLocalOf { 300 }
        val btn_lspace = compositionLocalOf { 0.1.em }
        val btn_PaddingTopH = compositionLocalOf { 3.dp }
        val btn_PaddingTopV = compositionLocalOf { 6.dp }
        val btn_TextFontsizeH = compositionLocalOf { 14.sp }
        val btn_TextFontsizeV = compositionLocalOf { 18.sp }
        val btn_Color1 = compositionLocalOf { Color.White }
        val btn_bgColor_black = compositionLocalOf { Color.Black }
        val btn_bgColor_pink = compositionLocalOf { Color(0xFFE91E63) }
        val btn_bgColor1 = compositionLocalOf { Color(0xFF2196F3) }
        val btn_bgColor2 = compositionLocalOf { Color(0xFF1976D2) }
        val btn_bgColor3 = compositionLocalOf { Color(0xFF2391B5) }
        val btn_bgColor4 = compositionLocalOf { Color(0xFF237A58) }
        val btnText1 = compositionLocalOf { "分享" }
        val btnText2 = compositionLocalOf { "复制到剪贴板" }
        val btnText3 = compositionLocalOf { "保存到指定文件夹" }
        val btnText4 = compositionLocalOf { "存入工作空间级资源目录" }
        val btnText5 = compositionLocalOf { "保存至汐洛绞架" }
        val btnTextAudio1 = compositionLocalOf { "播放（旧）" }
        val btnTextVideo1 = compositionLocalOf { "播放" }
        val btnText5Apk1 = compositionLocalOf { "通过第三方安装" }
        val btnText5Apk2 = compositionLocalOf { "通过汐洛安装" }
        val btnTextMagnet1 = compositionLocalOf { "第三方打开" }
        val small_iconSize = compositionLocalOf { 24.dp }
        val Card_bgColor_green1 = compositionLocalOf { Color(0x58237A58) }
        val Card_bgColor_gold1 = compositionLocalOf { Color(0x58997758) }
        val Card_bgColor_red1 = compositionLocalOf { Color(0xC8791020) }
    }

    data class UriMatchPattern(val scheme: String, val host: String)

    fun isUriMatched(uri: Uri?, pattern: UriMatchPattern): Boolean {
        return uri?.scheme == pattern.scheme && uri.host == pattern.host
    }
    val case_ld246_1 = UriMatchPattern("https", "ssl.ptlogin2.qq.com") // 目前必须将默认打开方式浏览器设置为汐洛，否则QQ回调进不来
    val case_ld246_2 = UriMatchPattern("https", "ld246.com")
    val case_github_1 = UriMatchPattern("https", "github.com")
}