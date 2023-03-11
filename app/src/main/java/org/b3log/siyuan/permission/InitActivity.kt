package org.b3log.siyuan.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class InitActivity: Activity() {

    var works = 0

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        when(requestCode){
            1002 -> {
                if(grantResults.isNotEmpty()){
                    grantResults.forEachIndexed { _, it ->
                       if( it != PackageManager.PERMISSION_DENIED)
                        works --
                    }

                    if(works == 0){
                        //打开本应用信息界面
//                        val intent = Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                        intent.data = Uri.parse("package:$packageName")
//                        startActivity(intent)




                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        finish() // 完成当前 activity
        val battery = Intent("sc.windom.sillot.intent.permission.Battery")
        battery.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(battery)

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var mContext: Context = applicationContext
        org.b3log.siyuan.andapi.Toast.Show(mContext,"你好，汐洛")
        // 创建一个权限列表，把需要使用而没用授权的的权限存放在这里

        // 创建一个权限列表，把需要使用而没用授权的的权限存放在这里
        val permissionList: MutableList<String> = ArrayList()

        // 判断权限是否已经授予，没有就把该权限添加到列表中
        Ps.PG_Core.forEach {
            if (ContextCompat.checkSelfPermission(mContext, it)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionList.add(it)
                works++
            } else {

                println(it)
            }
        }



        // 如果列表为空，就是全部权限都获取了，不用再次获取了。不为空就去申请权限
        // 调用后系统会显示一个请求用户授权的提示对话框，App不能配置和修改这个对话框，如果需要提示用户这个权限相关的信息或说明，需要在调用 requestPermissions() 之前处理
        // 由于该方法是异步的，所以无返回值，当用户处理完授权操作时，会回调Activity或者Fragment的onRequestPermissionsResult()方法。
        // int requestCode: 会在回调onRequestPermissionsResult()时返回，用来判断是哪个授权申请的回调。
        println(permissionList)
        if (permissionList.isNotEmpty()) {
                    permissionList.forEach {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, it)) {
                            // 用户拒绝过这个权限了，应该提示用户，为什么需要这个权限。

                        } else {
                        }
                    }

            // 申请授权。
            ActivityCompat.requestPermissions(
                this,
                permissionList.toTypedArray(), 1002
            )
        } else {
//            Toast.makeText(mContext, "你好，汐洛", Toast.LENGTH_LONG).show()
            finish()
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


        // 由于requestPermissions方法是异步的，移到回调完成finish
//                finish()
//        do {
//            sleep(200)
//        } while (works != 0)
//        finish()
    }

}