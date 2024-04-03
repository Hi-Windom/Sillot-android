package org.b3log.siyuan.permission

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi


object Ps {
    @JvmField // java能够正常调用
    val useLocation = mutableListOf(
        Manifest.permission.ACCESS_COARSE_LOCATION, // 允许应用访问大概位置。
        Manifest.permission.ACCESS_FINE_LOCATION, // 允许应用访问精确位置。
        Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,  // 允许应用程序访问其他位置提供程序命令。
    )
    @JvmField // java能够正常调用
    val useNet = mutableListOf(
        Manifest.permission.ACCESS_NETWORK_STATE, // 允许获取网络信息状态
        Manifest.permission.ACCESS_WIFI_STATE, // 允许获取当前WiFi接入的状态以及WLAN热点的信息
        Manifest.permission.BLUETOOTH, // 允许程序连接到已配对的蓝牙设备
        Manifest.permission.BLUETOOTH_ADMIN, // 允许应用程序发现和配对蓝牙设备
        Manifest.permission.CHANGE_NETWORK_STATE, // 允许程序改变网络连接状态
        Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,  // 允许应用程序进入Wi-Fi组播模式。
        Manifest.permission.CHANGE_WIFI_STATE, // 允许程序改变Wi-Fi连接状态
        Manifest.permission.INTERNET,  // 允许程序访问网络连接
    )
    @JvmField // java能够正常调用
    val useNotification = mutableListOf(
        Manifest.permission.ACCESS_NOTIFICATION_POLICY, // 希望访问通知策略的应用程序的标记权限。
    )
    @JvmField // java能够正常调用
    val useMedia = mutableListOf(
        Manifest.permission.CAMERA, // 允许程序访问摄像头进行拍照
        Manifest.permission.RECORD_AUDIO, // 允许程序录制音频，通过手机或耳机的麦克
    )
    @JvmField // java能够正常调用
    val useSensor = mutableListOf(
        Manifest.permission.ACTIVITY_RECOGNITION,  // 允许应用程序识别身体活动。
        Manifest.permission.BODY_SENSORS, // 允许应用访问来自传感器的数据，用户可使用传感器来测量他/她体内发生的事情，例如心律。
        Manifest.permission.NFC, // 允许应用程序通过NFC执行I / O操作。
        Manifest.permission.VIBRATE, // 允许访问振动设备
    )
    @JvmField // java能够正常调用
    val useCom = mutableListOf(
        Manifest.permission.WAKE_LOCK, // 允许程序在手机屏幕关闭后后台进程仍然运行（唤醒锁定）? / 允许使用PowerManager WakeLocks保持处理器休眠或屏幕变暗?
        Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND, // 允许伴随应用在后台运行。
        Manifest.permission.REQUEST_COMPANION_USE_DATA_IN_BACKGROUND, // 允许同伴应用在后台使用数据。
        Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, // 忽略电池优化,应用程序必须拥有才能使用的许可 Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS。
        Manifest.permission.KILL_BACKGROUND_PROCESSES,  // 允许应用程序调用 ActivityManager.killBackgroundProcesses(String)。
        Manifest.permission.FOREGROUND_SERVICE, // 允许使用常规应用程序Service.startForeground。
        Manifest.permission.BROADCAST_STICKY, // 允许应用程序广播粘性意图。
        Manifest.permission.CALL_COMPANION_APP, // 运行关联启动。
    )
    @JvmField // java能够正常调用
    val useData = mutableListOf(
        Manifest.permission.GET_PACKAGE_SIZE, // 允许应用程序找出任何包使用的空间。
        Manifest.permission.READ_EXTERNAL_STORAGE, // 允许应用程序读取非当前应用文件夹
        Manifest.permission.WRITE_EXTERNAL_STORAGE, // 允许应用程序写入非当前应用文件夹
    )
    @JvmField // java能够正常调用
    val useConvenient = mutableListOf(
        Manifest.permission.EXPAND_STATUS_BAR, // 允许程序扩展或收缩状态栏
        Manifest.permission.INSTALL_SHORTCUT, // 允许应用程序创建快捷方式
        Manifest.permission.READ_CALENDAR, // 允许程序读取用户日历数据
        Manifest.permission.WRITE_CALENDAR, // 允许一个程序写入但不读取用户日历数据
        Manifest.permission.SET_ALARM, // 允许程序设置闹铃提醒? / 允许应用程序广播意图为用户设置警报?
        Manifest.permission.RECEIVE_BOOT_COMPLETED, // 允许应用程序接收 Intent.ACTION_BOOT_COMPLETED系统完成引导后广播的消息。
    )

    @JvmField // java能够正常调用
    val usePhone = mutableListOf(
        Manifest.permission.READ_PHONE_STATE, // 允许读取电话状态。
        Manifest.permission.READ_CALL_LOG, // 允许应用程序读取通话记录。
        Manifest.permission.WRITE_CALL_LOG, // 允许应用程序写入通话记录。
        Manifest.permission.ADD_VOICEMAIL, // 允许应用程序添加语音邮件系统。
        Manifest.permission.USE_SIP, // 允许应用程序使用SIP视频服务。
        Manifest.permission.ANSWER_PHONE_CALLS, // 允许该应用接听来电。
        Manifest.permission.CALL_PHONE, // 允许应用程序在不通过Dialer用户界面的情况下发起电话呼叫，以使用户确认呼叫。
        Manifest.permission.READ_CONTACTS, // 允许应用程序读取用户的联系人数据。
        Manifest.permission.WRITE_CONTACTS,  // 允许应用程序写入用户的联系人数据。
        Manifest.permission.GET_ACCOUNTS // 允许访问帐户服务中的帐户列表。
    )
    @JvmField // java能够正常调用
    val useSMS = mutableListOf( // 该组权限共通
        Manifest.permission.READ_SMS, // 允许应用程序读取短信消息。
        Manifest.permission.RECEIVE_SMS, // 允许应用程序接收短信消息。
        Manifest.permission.SEND_SMS, // 允许应用程序发送短信消息。
        Manifest.permission.RECEIVE_WAP_PUSH // 允许应用程序接收WAP PUSH消息。
    )
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @JvmField // java能够正常调用
    val useAPI33 = mutableListOf(
// android 13 / api 33
        Manifest.permission.POST_NOTIFICATIONS, // 发送通知
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_AUDIO,
    )
    @JvmField // java能够正常调用
    val PG_Core = useNotification + useNet + useData + useMedia + useConvenient + usePhone + useLocation + useSensor // 核心权限组，每次启动都要检查
    @JvmField // java能够正常调用
    val PG_unCore = useSMS + useCom // 非核心权限组，仅安装后首次启动集中申请

// 以下内容省略前缀 Manifest.permission.
    // system app only, can not use in any api level
// ACCESS_CHECKIN_PROPERTIES,
// ACCOUNT_MANAGER,
// BATTERY_STATS,
// BIND_ACCESSIBILITY_SERVICE,
// BIND_*,
// BLUETOOTH_PRIVILEGED,
// BROADCAST_PACKAGE_REMOVED,
// BROADCAST_SMS,
// BROADCAST_WAP_PUSH,
// CALL_PRIVILEGED,
// CAPTURE_AUDIO_OUTPUT,
// CHANGE_COMPONENT_ENABLED_STATE,
// CHANGE_CONFIGURATION,
// CLEAR_APP_CACHE,
// CONTROL_LOCATION_UPDATES,
// DELETE_CACHE_FILES,
// DUMP,
// GLOBAL_SEARCH,
// INSTALL_LOCATION_PROVIDER,
// INSTALL_PACKAGES,
// INSTANT_APP_FOREGROUND_SERVICE,
// MEDIA_CONTENT_CONTROL,
// MODIFY_PHONE_STATE,
// STATUS_BAR,
// WRITE_SETTINGS,
// BIND_CONTROLS,
// BIND_QUICK_ACCESS_WALLET_SERVICE,
// QUERY_ALL_PACKAGES,

//    特殊权限
// MANAGE_DOCUMENTS
// MANAGE_EXTERNAL_STORAGE 自 Android 11 开始，MANAGE_EXTERNAL_STORAGE 权限变得更加受限，仅允许系统应用或者通过特殊配置的应用才能获得此权限。对于大多数应用来说，建议使用更精细的文件访问权限，如 READ_EXTERNAL_STORAGE 和 WRITE_EXTERNAL_STORAGE。
// REQUEST_INSTALL_PACKAGES, // 允许应用程序请求安装软件包。。通常不需要直接请求此权限，而是在请求安装应用程序时，系统会自动弹出权限请求对话框。
// ACCESS_MEDIA_LOCATION, // 允许应用程序访问媒体文件的位置信息，包括照片和视频等媒体文件的地理位置数据。暂时不知道怎么处理。
// ACCESS_BACKGROUND_LOCATION, // 允许应用访问后台位置。需要先请求前台位置访问权限，然后再请求后台位置访问权限。目前暂时没有申请的必要。
// SYSTEM_ALERT_WINDOW, // 悬浮窗权限，需要专门的申请判断。
// DISABLE_KEYGUARD, // 锁屏显示，暂时不知道怎么处理。
// RECEIVE_MMS, // 允许应用程序接收彩信消息。没什么用。
}