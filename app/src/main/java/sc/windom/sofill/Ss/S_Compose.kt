/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/8 上午4:18
 * updated: 2024/7/8 上午4:18
 */

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
    val btnText5 = compositionLocalOf { "保存至汐洛绞架（TOKEN）" }
    val btnText6 = compositionLocalOf { "保存至汐洛绞架（免密）" }
    val btnTextOpenByThirdParty = compositionLocalOf { "第三方打开" }
    val btnTextAudio1 = compositionLocalOf { "播放（旧）" }
    val btnTextVideo1 = compositionLocalOf { "播放" }
    val btnText5Apk1 = compositionLocalOf { "通过第三方安装" }
    val btnText5Apk2 = compositionLocalOf { "通过汐洛安装" }
    val small_iconSize = compositionLocalOf { 24.dp }
    val Card_bgColor_green1 = compositionLocalOf { Color(0x58237A58) }
    val Card_bgColor_gold1 = compositionLocalOf { Color(0x58997758) }
    val Card_bgColor_red1 = compositionLocalOf { Color(0xCF791020) }
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

    /**
     * lnco 主题色的 Material Compose 适配
     *
     * 0xF0123456 <-> #123456F0
     */
    object lnco {
        object light {
            val __b3_theme_primary = 0xffe6c829
            val __b3_theme_primary_light = 0xe0e6c829
            val __b3_theme_primary_lighter = 0xbae6c829
            val __b3_theme_primary_lightest = 0x85e6c829
            val __b3_theme_secondary = 0xffa48c8e
            val __b3_theme_background = 0xfa402020
            val __b3_theme_background_light = 0xd1402020
            val __b3_theme_surface = 0xff422424
            val __b3_theme_surface_light = 0xc7422424
            val __b3_theme_surface_lighter = 0xad422424
            val __b3_theme_error = 0xf792123c
            val __b3_theme_on_primary = 0xff000000
            val __b3_theme_on_secondary = 0xff00ab7c
            val __b3_theme_on_background = 0xffdffcff
            val __b3_theme_on_surface = 0xffd2cee5
            val __b3_theme_on_surface_light = 0xf090fbf5
            val __b3_theme_on_error = 0xffd7e0e0
        }
        object dark {
            val __b3_theme_primary = 0xffffa300
            val __b3_theme_primary_light = 0xb8ffa300
            val __b3_theme_primary_lighter = 0x94ffa300
            val __b3_theme_primary_lightest = 0x4fffa300
            val __b3_theme_secondary = 0xffa48c8e
            val __b3_theme_background = 0xff000000
            val __b3_theme_background_light = 0xb8000000
            val __b3_theme_surface = 0xe0000807
            val __b3_theme_surface_light = 0x94000807
            val __b3_theme_surface_lighter = 0x4f000807
            val __b3_theme_error = 0xff820020
            val __b3_theme_on_primary = 0xff000000
            val __b3_theme_on_secondary = 0xff00ab7c
            val __b3_theme_on_background = 0xffb3d8c9
            val __b3_theme_on_surface = 0x9600eee7
            val __b3_theme_on_surface_light = 0xFF8F9D69
            val __b3_theme_on_error = 0xff00e2e6
        }
    }
}