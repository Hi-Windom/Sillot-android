/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/8 上午11:38
 * updated: 2024/7/8 上午11:38
 */

package sc.windom.gibbet.MG

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import sc.windom.sofill.S
import sc.windom.namespace.SillotMatrix.R


/**
 * description: 前台通知管理类
 * @author: Kay
 * @date: 2021/1/25 11:28
 */
object ForegroundPushManager {

    //显示通知
    @SuppressLint("MissingPermission")
    fun showNotification(context: Context){
        val notification = createForegroundNotification(context)
        NotificationManagerCompat.from(context).notify(S.SILLOT_GIBBET_notificationId, notification)
    }

    //隐藏通知
    fun stopNotification(context: Context){
        NotificationManagerCompat.from(context).cancel(S.SILLOT_GIBBET_notificationId)
    }

    /**
     * 创建服务通知
     */
    private fun createForegroundNotification(context: Context): Notification {
        val notificationManager = NotificationManagerCompat.from(context)

        // Android8.0以上的系统，新建消息通道
        val chan = NotificationChannel(S.SILLOT_GIBBET_NOTIFICATION_CHANNEL_ID, S.SILLOT_GIBBET_NOTIFICATION_CHANEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        chan.description = "汐洛绞架后台保活（一般没什么卵用）"
        chan.enableLights(false) // 呼吸灯
        chan.setSound(null, null) // 提示音
        chan.enableVibration(true) // 震动
        notificationManager.createNotificationChannel(chan)
        val builder = NotificationCompat.Builder(context, S.SILLOT_GIBBET_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.icon) //通知小图标
            .setContentTitle("❤️ 来自汐洛绞架 ") //通知标题
            .setContentText("点击返回活动") //通知内容
            .setAutoCancel(true) //点击通知栏关闭通知
            .setOngoing(true) //不能清除通知
            .setPriority(NotificationManager.IMPORTANCE_HIGH) // 通知类别，适用“勿扰模式”
            .setCategory(NotificationCompat.CATEGORY_MESSAGE) // 通知类别，"勿扰模式"时系统会决定要不要显示你的通知
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 屏幕可见性，适用“锁屏状态”
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setSilent(true) // 静默通知 https://github.com/Hi-Windom/Sillot-android/issues/80
        //设定启动的内容
        val activityIntent = Intent(Intent.ACTION_MAIN)
        activityIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        activityIntent.component = ComponentName(context, S.ACTIVITY.URIMainActivity)
        activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 123, activityIntent, PendingIntent.FLAG_IMMUTABLE)

//        val pendingIntent = PendingIntent.getActivity(context, 1, activityIntent, PendingIntent.FLAG_CANCEL_CURRENT )
        builder.setContentIntent(pendingIntent)
        //创建通知并返回
        return builder.build()
    }
}