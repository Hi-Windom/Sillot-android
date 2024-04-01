package org.b3log.siyuan

object Ss {

    const val DefaultHTTPPort = 58131
    const val initCrashReportID = "26ae2b5fb4"
    const val initBaiduPushID = "6Ef26UV3UyM5b7NyiwiGAnM0"


    const val XLQTFW_notificationId = 58131

    // 通知频道ID
    const val XLQTFW_notificationChannelId = "sillot_notification_channel_id_58131" // 🦢 汐洛前台通知服务
    const val SY_NOTIFICATION_CHANNEL_ID = "sillot_notification_channel_id_6806"  // 📚 思源内核服务
    const val FloatingWindowService_NOTIFICATION_CHANNEL_ID = "sillot_notification_channel_id_100001"

    const val URIMainActivity = "org.b3log.siyuan.MainActivity"

    // REQUEST CODE
    const val REQUEST_SELECT_FILE = 100
    const val REQUEST_CAMERA = 101
    const val REQUEST_LOCATION = 1002
    const val VIDEO_PICK_REQUEST_CODE = 10001
    const val REQUEST_IGNORE_BATTERY_OPTIMIZATIONS_AND_REBOOT = 10002 // 申请电源优化无限制权限后重启，部分系统没有该权限
    const val REQUEST_IGNORE_BATTERY_OPTIMIZATIONS = 10002 // 申请电源优化无限制权限，部分系统没有该权限
    const val REQUEST_ExternalStorageManager = 10003
}