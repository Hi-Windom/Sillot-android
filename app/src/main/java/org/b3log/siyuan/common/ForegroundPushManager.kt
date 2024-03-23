package org.b3log.siyuan.common

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.b3log.siyuan.Ss
import org.b3log.siyuan.R


/**
 * description: 前台通知管理类
 * @author: Kay
 * @date: 2021/1/25 11:28
 */
object ForegroundPushManager {

    val notificationId = Ss.XLQTFW_notificationId

    val notificationChannelId = Ss.XLQTFW_notificationChannelId

    //显示通知
    @SuppressLint("MissingPermission")
    fun showNotification(context: Context){
        var notification = createForegroundNotification(context)
        NotificationManagerCompat.from(context).notify(notificationId, notification!!)
    }

    //隐藏通知
    fun stopNotification(context: Context){
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    /**
     * 创建服务通知
     */
    private fun createForegroundNotification(context: Context): Notification? {
        val notificationManager = NotificationManagerCompat.from(context)

        // Android8.0以上的系统，新建消息通道
        //用户可见的通道名称
        val channelName = "🦢 汐洛前台通知服务"
        //通道的重要程度
        val importance = NotificationManager.IMPORTANCE_HIGH
        val chan = NotificationChannel(notificationChannelId, channelName, importance)
        chan.description = "汐洛后台保活（一般没什么卵用）"
        chan.enableLights(false) // 呼吸灯
        chan.setSound(null, null) // 提示音
        chan.enableVibration(true) // 震动
        notificationManager.createNotificationChannel(chan)
        val builder = NotificationCompat.Builder(context, notificationChannelId)
        .setSmallIcon(R.drawable.icon) //通知小图标
        .setContentTitle("Sillot ❤️") //通知标题
        .setContentText("服务正在运行中") //通知内容
        .setAutoCancel(true) //点击通知栏关闭通知
        .setOngoing(true) //不能清除通知
        .setPriority(importance) // 通知类别，适用“勿扰模式”
        .setCategory(NotificationCompat.CATEGORY_MESSAGE) // 通知类别，"勿扰模式"时系统会决定要不要显示你的通知
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 屏幕可见性，适用“锁屏状态”
        .setWhen(System.currentTimeMillis())
        .setShowWhen(true)
        //设定启动的内容
        val activityIntent = Intent(Intent.ACTION_MAIN)
        activityIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        activityIntent.component = ComponentName(context, Ss.URIMainActivity)
        activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        val pendingIntent: PendingIntent
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(context, 123, activityIntent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(context, 123, activityIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        }

//        val pendingIntent = PendingIntent.getActivity(context, 1, activityIntent, PendingIntent.FLAG_CANCEL_CURRENT )
        builder.setContentIntent(pendingIntent)
        //创建通知并返回
        return builder.build()
    }
}