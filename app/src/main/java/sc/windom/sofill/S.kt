/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/7 下午6:35
 * updated: 2024/7/7 下午6:35
 */

@file:Suppress("CompositionLocalNaming", "CompositionLocalNaming")
package sc.windom.sofill

import sc.windom.sofill.Ss.S_API
import sc.windom.sofill.Ss.S_Activity
import sc.windom.sofill.Ss.S_ColorInt
import sc.windom.sofill.Ss.S_ColorStringHex
import sc.windom.sofill.Ss.S_Compose
import sc.windom.sofill.Ss.S_DEBUG
import sc.windom.sofill.Ss.S_Events
import sc.windom.sofill.Ss.S_File
import sc.windom.sofill.Ss.S_Intent
import sc.windom.sofill.Ss.S_REQUEST_CODE
import sc.windom.sofill.Ss.S_Uri
import sc.windom.sofill.Ss.S_Webview
import sc.windom.sofill.Ss.S_packageName

object S {
    const val gitRepoUrl = "https://github.com/Hi-Windom/Sillot-android"
    const val emailAdress = "694357845@qq.com"
    const val QQ = "694357845"
    const val TTK = "AsyncTTk" // 抖音

    const val DefaultHTTPPort = 58131
    const val initCrashReportID = "26ae2b5fb4"
    const val initBaiduPushID = "6Ef26UV3UyM5b7NyiwiGAnM0"

    const val KEY_TOKEN_ld246 = "TOKEN_ld246"
    const val KEY_AES_TOKEN_ld246 = "AES_KEY_TOKEN_ld246"
    const val KEY_TOKEN_Sillot_Gibbet = "TOKEN_Sillot_Gibbet"
    const val KEY_AES_TOKEN_Sillot_Gibbet = "AES_KEY_TOKEN_Sillot_Gibbet"

    const val HOST_ld246 = "ld246.com"


    const val SILLOT_GIBBET_notificationId = 58131
    const val USB_AUDIO_EXCLUSIVE_notificationId = 7654321

    // 用户可见的通道名称
    const val SILLOT_GIBBET_NOTIFICATION_CHANEL_NAME = "🦢 汐洛绞架通知服务"
    const val SILLOT_MUSIC_PLAYER_NOTIFICATION_CHANEL_NAME = "🦢 汐洛音乐播放服务"

    // 通知频道ID
    const val SILLOT_GIBBET_NOTIFICATION_CHANNEL_ID = "sillot_notification_channel_id_58131$SILLOT_GIBBET_notificationId"
    const val SILLOT_MUSIC_PLAYER_NOTIFICATION_CHANNEL_ID = "sillot_notification_channel_id_$USB_AUDIO_EXCLUSIVE_notificationId"
    const val SY_NOTIFICATION_CHANNEL_ID = "sillot_notification_channel_id_6806"  // 📚 思源内核服务
    const val FloatingWindowService_NOTIFICATION_CHANNEL_ID = "sillot_notification_channel_id_100001"

    @JvmStatic
    val ACTIVITY = S_Activity

    // REQUEST CODE
    @JvmStatic
    val REQUEST_CODE = S_REQUEST_CODE

    @JvmStatic
    val API = S_API

    @JvmStatic
    val ColorStringHex = S_ColorStringHex

    @JvmStatic
    val EVENTS = S_Events

    @JvmStatic
    val INTENT = S_Intent

    @JvmStatic
    val COLORINT = S_ColorInt

    // compose 组件常量
    @JvmStatic
    val C = S_Compose

    @JvmStatic
    val DEBUG = S_DEBUG

    @JvmStatic
    val AppQueryIDs = S_packageName

    @JvmStatic
    val WEBVIEW = S_Webview

    @JvmStatic
    val URI = S_Uri

    @JvmStatic
    val FILE = S_File

}