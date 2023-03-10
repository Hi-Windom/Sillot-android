package org.b3log.siyuan.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock.sleep
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class InitActivity: Activity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            var mContext: Context = applicationContext
            // 创建一个权限列表，把需要使用而没用授权的的权限存放在这里

            // 创建一个权限列表，把需要使用而没用授权的的权限存放在这里
            val permissionList: MutableList<String> = ArrayList()

            // 判断权限是否已经授予，没有就把该权限添加到列表中
            arrayListOf(
                Manifest.permission.ACCEPT_HANDOVER,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
                Manifest.permission.ACCESS_MEDIA_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE, // 允许获取网络信息状态
                Manifest.permission.ACCESS_NOTIFICATION_POLICY,
                Manifest.permission.ACCESS_WIFI_STATE, // 允许获取当前WiFi接入的状态以及WLAN热点的信息
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.ANSWER_PHONE_CALLS,
                Manifest.permission.BLUETOOTH, // 允许程序连接到已配对的蓝牙设备
                Manifest.permission.BLUETOOTH_ADMIN, // 允许应用程序发现和配对蓝牙设备
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.BROADCAST_STICKY,
                Manifest.permission.CALL_COMPANION_APP,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.CAMERA, // 允许程序访问摄像头进行拍照
                Manifest.permission.CHANGE_NETWORK_STATE, // 允许程序改变网络连接状态
                Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
                Manifest.permission.CHANGE_WIFI_STATE, // 允许程序改变Wi-Fi连接状态
                Manifest.permission.EXPAND_STATUS_BAR, // 允许程序扩展或收缩状态栏
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.GET_PACKAGE_SIZE,
                Manifest.permission.INSTALL_SHORTCUT, // 允许应用程序创建快捷方式
                Manifest.permission.INTERNET,  // 允许程序访问网络连接
                Manifest.permission.KILL_BACKGROUND_PROCESSES,
                Manifest.permission.NFC,
                Manifest.permission.READ_CALENDAR, // 允许程序读取用户日历数据
                Manifest.permission.RECEIVE_BOOT_COMPLETED,
                Manifest.permission.RECORD_AUDIO, // 允许程序录制音频，通过手机或耳机的麦克
                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, // 忽略电池优化
                Manifest.permission.REQUEST_INSTALL_PACKAGES,
                Manifest.permission.REQUEST_PASSWORD_COMPLEXITY,
                Manifest.permission.SET_ALARM, // 允许程序设置闹铃提醒
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                Manifest.permission.WRITE_CALENDAR, // 允许一个程序写入但不读取用户日历数据
                Manifest.permission.WAKE_LOCK, // 允许程序在手机屏幕关闭后后台进程仍然运行（唤醒锁定）
                Manifest.permission.VIBRATE, // 允许访问振动设备
            ).forEach {
                println(it)
                if (ContextCompat.checkSelfPermission(mContext, it)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionList.add(it)
                }
            }

            // android 11 / api 30
            arrayListOf(
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            ).forEach {
                println(it)
                if (ContextCompat.checkSelfPermission(mContext, it)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionList.add(it)
                }
            }

            // android 12 / api 31
//        arrayListOf(
//            Manifest.permission.ACCESS_BLOBS_ACROSS_USERS,
//            Manifest.permission.BIND_COMPANION_DEVICE_SERVICE,
//            Manifest.permission.BIND_COMPANION_DEVICE_SERVICE,
//            Manifest.permission.BLUETOOTH_ADVERTISE,
//            Manifest.permission.BLUETOOTH_CONNECT,
//            Manifest.permission.BLUETOOTH_SCAN,
//            Manifest.permission.REQUEST_COMPANION_START_FOREGROUND_SERVICES_FROM_BACKGROUND,
//            Manifest.permission.START_FOREGROUND_SERVICES_FROM_BACKGROUND,
//            Manifest.permission.UPDATE_PACKAGES_WITHOUT_USER_ACTION,
//        ).forEach {
//                if (ContextCompat.checkSelfPermission(mContext, it)
//                    != PackageManager.PERMISSION_GRANTED
//                ) {
//                    permissionList.add(it)
//                }
//            }

            // system app only, can not use in any api level

//           arrayListOf(
//           Manifest.permission.ACCESS_CHECKIN_PROPERTIES,
//            Manifest.permission.ACCOUNT_MANAGER,
//            Manifest.permission.BATTERY_STATS,
//            Manifest.permission.BIND_ACCESSIBILITY_SERVICE,
//            Manifest.permission.BIND_*,
//            Manifest.permission.BLUETOOTH_PRIVILEGED,
//            Manifest.permission.BROADCAST_PACKAGE_REMOVED,
//            Manifest.permission.BROADCAST_SMS,
//            Manifest.permission.BROADCAST_WAP_PUSH,
//            Manifest.permission.CALL_PRIVILEGED,
//            Manifest.permission.CAPTURE_AUDIO_OUTPUT,
//            Manifest.permission.CHANGE_COMPONENT_ENABLED_STATE,
//            Manifest.permission.CHANGE_CONFIGURATION,
//            Manifest.permission.CLEAR_APP_CACHE,
//            Manifest.permission.CONTROL_LOCATION_UPDATES,
//            Manifest.permission.DELETE_CACHE_FILES,
//            Manifest.permission.DUMP,
//            Manifest.permission.GLOBAL_SEARCH,
//            Manifest.permission.INSTALL_LOCATION_PROVIDER,
//            Manifest.permission.INSTALL_PACKAGES,
//            Manifest.permission.INSTANT_APP_FOREGROUND_SERVICE,
//            Manifest.permission.MEDIA_CONTENT_CONTROL,
//            Manifest.permission.MODIFY_PHONE_STATE,
//            Manifest.permission.STATUS_BAR,
//            Manifest.permission.WRITE_SETTINGS,
//            Manifest.permission.BIND_CONTROLS,
//            Manifest.permission.BIND_QUICK_ACCESS_WALLET_SERVICE,
//            Manifest.permission.QUERY_ALL_PACKAGES,
//        )


        // 如果列表为空，就是全部权限都获取了，不用再次获取了。不为空就去申请权限
        if (permissionList.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionList.toTypedArray(), 1002
            )
        } else {
            Toast.makeText(mContext, "你好，汐洛", Toast.LENGTH_LONG).show()
        }

        // important
        finish()


            val battery = Intent("sc.windom.sillot.intent.permission.Battery")
            battery.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(battery)
    }

}