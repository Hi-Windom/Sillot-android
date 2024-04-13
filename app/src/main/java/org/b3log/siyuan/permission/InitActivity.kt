package org.b3log.siyuan.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kongzue.dialogx.dialogs.PopTip
import kotlin.system.exitProcess

@Deprecated("之前不会在当前活动申请权限的时候凑合用的")
class InitActivity: Activity() {

    var works = 0
    // 创建一个权限列表，把需要使用而没用授权的的权限存放在这里
    var permissionList: MutableList<String> = ArrayList()
    fun onApplyButtonClick(view: View) {
        if(works == 0){
            finish()
        } else {
            doApply()
        }
    }
    fun onCloseButtonClick(view: View) {
        finish()
    }


    // 请求码应该在整个应用中是全局唯一的，但是处理权限请求结果应该是在申请权限的活动中进行。
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        when(requestCode){
            1002 -> {
                if(grantResults.isNotEmpty()){
                    grantResults.forEachIndexed { _, it ->
                       if(it == PackageManager.PERMISSION_GRANTED)
                        works --
                    }

                    if(works == 0){

                        finish() // 完成当前 activity

                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // 点击空白处将中断权限申请
//        finish()

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("InitActivity", "create InitActivity activity")
        super.onCreate(savedInstanceState)
        val contentViewId = intent.getIntExtra("contentViewId", 0)
        setContentView(contentViewId)
//        window.setBackgroundDrawable(null)


        val mContext: Context = applicationContext
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
        if (Build.VERSION.SDK_INT >= 33) {
            Ps.useAPI33.forEach {
                if (ContextCompat.checkSelfPermission(mContext, it)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionList.add(it)
                    works++
                } else {

                    println(it)
                }
            }
        }
        doApply()

    }

    override fun onDestroy() {
        Log.i("InitActivity", "destroy InitActivity activity")
        super.onDestroy()
    }

    private fun doApply() {



        // 如果列表为空，就是全部权限都获取了，不用再次获取了。不为空就去申请权限
        // 调用后系统会显示一个请求用户授权的提示对话框，App不能配置和修改这个对话框，如果需要提示用户这个权限相关的信息或说明，需要在调用 requestPermissions() 之前处理
        // 由于该方法是异步的，所以无返回值，当用户处理完授权操作时，会回调Activity或者Fragment的onRequestPermissionsResult()方法。
        // int requestCode: 会在回调onRequestPermissionsResult()时返回，用来判断是哪个授权申请的回调。
        println(permissionList)
        if (permissionList.isNotEmpty()) {
            permissionList.forEach {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, it)) {
                    // 用户拒绝过这个权限了，应该提示用户，为什么需要这个权限。
                    val mContext: Context = applicationContext
                    PopTip.show("必要权限被拒绝申请，请手动授权！")
                    //打开本应用信息界面
                    val intent = Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                    finish()
                } else {
                }
            }

            // 申请授权。
            ActivityCompat.requestPermissions(
                this,
                permissionList.toTypedArray(), 1002
            )
        } else {
//            finish()
            exitProcess(-1)
        }
    }

}