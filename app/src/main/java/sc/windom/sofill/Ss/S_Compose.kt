package sc.windom.sofill.Ss

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * compose 组件常量
 */
object S_Compose {
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
    val btnTextOpenByThirdParty = compositionLocalOf { "第三方打开" }
    val btnTextAudio1 = compositionLocalOf { "播放（旧）" }
    val btnTextVideo1 = compositionLocalOf { "播放" }
    val btnText5Apk1 = compositionLocalOf { "通过第三方安装" }
    val btnText5Apk2 = compositionLocalOf { "通过汐洛安装" }
    val small_iconSize = compositionLocalOf { 24.dp }
    val Card_bgColor_green1 = compositionLocalOf { Color(0x58237A58) }
    val Card_bgColor_gold1 = compositionLocalOf { Color(0x58997758) }
    val Card_bgColor_red1 = compositionLocalOf { Color(0xC8791020) }
    val text_fontSize_sss = compositionLocalOf { 8.sp }
    val text_lineHeight_sss = compositionLocalOf { 10.sp }
    val text_fontSize_ss = compositionLocalOf { 10.sp }
    val text_lineHeight_ss = compositionLocalOf { 12.sp }
    val text_fontSize_s = compositionLocalOf { 12.sp }
    val text_lineHeight_s = compositionLocalOf { 14.sp }
    val text_fontSize_m = compositionLocalOf { 14.sp }
    val text_lineHeight_m = compositionLocalOf { 16.sp }
    val text_fontSize_x = compositionLocalOf { 16.sp }
    val text_lineHeight_x = compositionLocalOf { 18.sp }
    val text_fontSize_xx = compositionLocalOf { 18.sp }
    val text_lineHeight_xx = compositionLocalOf { 20.sp }
    val text_fontSize_xxx = compositionLocalOf { 20.sp }
    val text_lineHeight_xxx = compositionLocalOf { 22.sp }
    val text_fontSize_xxxx = compositionLocalOf { 22.sp }
    val text_lineHeight_xxxx = compositionLocalOf { 24.sp }
    val text_fontSize_xxxxx = compositionLocalOf { 24.sp }
    val text_lineHeight_xxxxx = compositionLocalOf { 26.sp }
}