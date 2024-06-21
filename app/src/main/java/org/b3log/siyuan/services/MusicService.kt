package org.b3log.siyuan.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.b3log.siyuan.R
import sc.windom.sofill.S
import java.io.IOException
import androidx.media.app.NotificationCompat as MediaNotificationCompat


@Deprecated("正在转向 media3 : Media3 库会使用播放器的状态自动更新媒体会话。因此，您无需手动处理从玩家到会话的映射。这与传统方法有所不同，在传统方法中，您需要独立于播放器本身创建和维护 PlaybackStateCompat，例如用于指明任何错误。")
class MusicService : LifecycleService(), MediaPlayer.OnCompletionListener,
    MediaPlayer.OnSeekCompleteListener {
    val TAG = "MusicService"
    // 只有上一首、下一首、播放暂停这三个会直接在媒体控制器上显示为按钮
    val COMMON_CONTROL_ACTIONS = PlaybackStateCompat.ACTION_PLAY_PAUSE or // 备注: 允许播放和暂停（不用再重复声明单独的二者）
            // 允许跳转至指定位置（进度条交互）
            PlaybackStateCompat.ACTION_SEEK_TO or
            // 允许跳转到下一首
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
            // 允许跳转到上一首
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
            // 允许停止，不推荐，逻辑复杂还没什么用
//            PlaybackStateCompat.ACTION_STOP or
            // 允许设置随机播放模式
            PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE or
            // 允许启用或禁用字幕（歌词）
            PlaybackStateCompat.ACTION_SET_CAPTIONING_ENABLED or
            // 允许设置重复模式（如单曲循环、全部循环等）
            PlaybackStateCompat.ACTION_SET_REPEAT_MODE or
            // 允许快进
            PlaybackStateCompat.ACTION_FAST_FORWARD or
            // 允许快退
            PlaybackStateCompat.ACTION_REWIND or
            // 允许跳转到播放队列中的特定项目
            PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: NotificationManager
    private lateinit var mediaController: MediaControllerCompat

    companion object {
        const val ACTION_PLAY = "sc.windom.sillot.action.PLAY"
        const val ACTION_START = "sc.windom.sillot.action.START"
        const val ACTION_PAUSE = "sc.windom.sillot.action.PAUSE"
        const val ACTION_RESUME = "sc.windom.sillot.action.RESUME"
        const val ACTION_NEXT = "sc.windom.sillot.action.NEXT"
        const val ACTION_PREVIOUS = "sc.windom.sillot.action.PREVIOUS"
        const val ACTION_SEEKTO = "sc.windom.sillot.action.SEEKTO"
        const val ACTION_SkipPrevious = "sc.windom.sillot.action.SkipPrevious"
        const val ACTION_SkipNext = "sc.windom.sillot.action.SkipNext"
        const val ACTION_MEDIA_STATUS_CHANGED = "sc.windom.sillot.action.MEDIA_STATUS_CHANGED"
        const val EXTRA_MEDIA_PLAYING = "sc.windom.sillot.extra.MEDIA_PLAYING"
        const val EXTRA_MEDIA_CURRENT_POSITION = "sc.windom.sillot.extra.MEDIA_CURRENT_POSITION"
        const val EXTRA_MEDIA_DURATION = "sc.windom.sillot.extra.MEDIA_DURATION"
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return LocalBinder()
    }

    inner class LocalBinder : Binder() {
        fun getService(): MusicService = this@MusicService // 不要删除
    }
    private val scope = CoroutineScope(Dispatchers.Main)


    private fun startSendingMediaStatusUpdates(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    sendMediaStatusBroadcast(mediaPlayer)
                    delay(490) // 每xxx毫秒发送一次
                }
            }
        }
    }

    private fun stopSendingMediaStatusUpdates() {
        scope.coroutineContext.cancelChildren() // 取消所有协程任务
    }


    private var mediaDuration: Int = 0

    // 在 MediaPlayer 成功准备后更新 mediaDuration
    private fun updateMediaDuration(mediaPlayer: MediaPlayer) {
        mediaDuration = mediaPlayer.duration
    }

    private fun sendMediaStatusBroadcast(mediaPlayer: MediaPlayer) {
        val intent = Intent(ACTION_MEDIA_STATUS_CHANGED)
        intent.putExtra(EXTRA_MEDIA_PLAYING, mediaPlayer.isPlaying)
        intent.putExtra(EXTRA_MEDIA_CURRENT_POSITION, mediaPlayer.currentPosition)
        intent.putExtra(EXTRA_MEDIA_DURATION, mediaDuration)
        sendBroadcast(intent)
    }


    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener(this)
        mediaPlayer.setOnSeekCompleteListener(this)
        mediaPlayer.setOnPreparedListener {
            Log.w(TAG," setOnPreparedListener() invoked")

            val playbackState = PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, // 更新播放状态
                    mediaPlayer.currentPosition.toLong(), 1f)
                .setActions(COMMON_CONTROL_ACTIONS)
                .build()
            mediaSession.setPlaybackState(playbackState)
            mediaPlayer.start()
            updateMediaDuration(it)
            sendMediaStatusBroadcast(mediaPlayer)
        }

        createNotificationChannel(
            S.SILLOT_MUSIC_PLAYER_NOTIFICATION_CHANNEL_ID,
            S.SILLOT_MUSIC_PLAYER_NOTIFICATION_CHANEL_NAME
        )
        mediaSession = MediaSessionCompat(this, "MusicService")
        // 创建 MediaController
        mediaController = MediaControllerCompat(this, mediaSession)

        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                Log.w(TAG," onPlay() in mediaSession.setCallback")
                super.onPlay()

                if (!mediaPlayer.isPlaying) {
                    Log.d(TAG," onPlay() in mediaSession.setCallback !mediaPlayer.isPlaying")
                    mediaPlayer.start()
//                    mediaController.transportControls.play()
                } else {
                    Log.d(TAG," onPlay() in mediaSession.setCallback mediaPlayer.isPlaying")
                }
                // 不要放在前面，不然 mediaPlayer.currentPosition 位置可能不对
                val playbackState = PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, // 更新播放状态
                        mediaPlayer.currentPosition.toLong(), 1f)
                    .setActions(COMMON_CONTROL_ACTIONS)
                    .build()
                mediaSession.setPlaybackState(playbackState)
                sendMediaStatusBroadcast(mediaPlayer)
            }

            override fun onPause() {
                Log.w(TAG," onPause() in mediaSession.setCallback")
                super.onPause()
                mediaPlayer.pause()
                // 更新 MediaController 的播放状态
                val playbackState = PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PAUSED,
                        mediaPlayer.currentPosition.toLong(), 1f)
                    .setActions(COMMON_CONTROL_ACTIONS)
                    .build()
                mediaSession.setPlaybackState(playbackState)
//                mediaController.transportControls.pause() // 这句代码会让 onPause 不停被调用，禁止
                sendMediaStatusBroadcast(mediaPlayer)
            }

            override fun onStop() {
                Log.w(TAG," onStop() in mediaSession.setCallback")
                super.onStop()
                mediaPlayer.stop()
                val playbackState = PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_STOPPED, // 更新播放状态
                        mediaPlayer.currentPosition.toLong(), 1f)
                    .setActions(COMMON_CONTROL_ACTIONS)
                    .build()
                mediaSession.setPlaybackState(playbackState)
//                mediaController.transportControls.stop() // 这句代码会让 onStop 不停被调用，禁止
                sendMediaStatusBroadcast(mediaPlayer)
            }

            override fun onSeekTo(pos: Long) {
                Log.w(TAG," onSeekTo() in mediaSession.setCallback")
                super.onSeekTo(pos)
                mediaPlayer.seekTo(pos.toInt())
                val state = if (mediaPlayer.isPlaying) {
                    PlaybackStateCompat.STATE_PLAYING
                } else {
                    PlaybackStateCompat.STATE_PAUSED
                }
                val playbackState = PlaybackStateCompat.Builder()
                    .setState(state, // 更新播放状态
                        pos, 1f) // 更新播放位置
                    .setActions(COMMON_CONTROL_ACTIONS)
                    .build()
                mediaSession.setPlaybackState(playbackState)
                sendMediaStatusBroadcast(mediaPlayer)
            }

            // 实现其他 MediaSession 回调
        })
        startSendingMediaStatusUpdates(this)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        intent?.let {
            when (it.action) {
                ACTION_PLAY -> {
                    val uri = intent.data
                    if (uri != null) {
                        try {
                            val playbackState = PlaybackStateCompat.Builder()
                                .setState(PlaybackStateCompat.STATE_CONNECTING,
                                    mediaPlayer.currentPosition.toLong(), 1f)
                                .setActions(COMMON_CONTROL_ACTIONS)
                                .build()
                            mediaSession.setPlaybackState(playbackState)
                            // 设置数据源、准备和开始播放
                            mediaPlayer.reset()
                            mediaPlayer.setDataSource(applicationContext, uri)
                            mediaPlayer.prepare() // 重写方法已经实现自动启动
                            // 设置单曲循环
                            mediaPlayer.isLooping = false // 通常在 MediaPlayer.prepare() 方法之后立即调用 setLooping
                            startForeground(S.USB_AUDIO_EXCLUSIVE_notificationId, createAudioPlaybackNotification())

                            // 设置媒体会话的音频时长和文件名
                            mediaSession.setMetadata(
                                MediaMetadataCompat.Builder()
                                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, intent.getStringExtra("fileName"))
                                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "汐洛音频播放器") // 不是自己提供的音频服务，各种乱七八糟的文件名根本无法分割歌手名和歌曲名
                                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "未知专辑")
                                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                                        mediaPlayer.duration.toLong()
                                    )
                                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(resources,
                                        R.drawable.icon
                                    )) // 专辑图片
                                    .build()
                            )
//                            mediaController.transportControls.play()
                            mediaSession.setActive(true) // 影响 MediaSessionCompat 的状态，使其从活跃状态变为非活跃状态。当 MediaSessionCompat 处于非活跃状态时，它将不会响应用户的媒体控制操作，如播放、暂停、停止等。

                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {

                    }
                }
                ACTION_PAUSE -> {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                        val playbackState = PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_PAUSED,
                                mediaPlayer.currentPosition.toLong(), 1f)
                            .setActions(COMMON_CONTROL_ACTIONS)
                            .build()
                        mediaSession.setPlaybackState(playbackState)
                        sendMediaStatusBroadcast(mediaPlayer)
                    } else {

                    }
                }
                ACTION_RESUME -> {
                    if (!mediaPlayer.isPlaying) {
                        mediaPlayer.start()
                        val playbackState = PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_PLAYING,
                                mediaPlayer.currentPosition.toLong(), 1f)
                            .setActions(COMMON_CONTROL_ACTIONS)
                            .build()
                        mediaSession.setPlaybackState(playbackState)
                        sendMediaStatusBroadcast(mediaPlayer)
                    } else {

                    }
                }
                ACTION_SEEKTO -> {
                    intent.getStringExtra("seekto")?.let { it1 -> Log.e("ACTION_SEEKTO", it1) }
                }
                ACTION_SkipNext -> {
                    intent.getStringExtra("SkipNext")?.let { it1 -> Log.e("ACTION_SkipNext", it1) }
                }
                ACTION_SkipPrevious -> {
                    intent.getStringExtra("SkipPrevious")?.let { it1 -> Log.e("ACTION_SkipPrevious", it1) }
                }
                // 其他操作...
                else -> {}
            }
        }
        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        stopSendingMediaStatusUpdates()
        mediaSession.setMetadata(null)
        mediaSession.setActive(false)
        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaController.transportControls.stop()
        mediaSession.release()
        mediaPlayer.release()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        Log.w(TAG, "onCompletion() invoked")
        try {
            // 检查 mediaPlayer 是否为 null 或处于错误状态
            if (mp != null) {
                val playbackState = PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_STOPPED,
                        mediaPlayer.currentPosition.toLong(), 1f)
                    .setActions(COMMON_CONTROL_ACTIONS)
                    .build()
                mediaSession.setPlaybackState(playbackState)
                sendMediaStatusBroadcast(mediaPlayer)
            } else {
                Log.e(TAG, "MediaPlayer is null in onCompletion()")
            }
        } catch (e: Exception) {
            // 捕获并处理异常
            Log.e(TAG, "Exception in onCompletion()", e)
        }
    }


    override fun onSeekComplete(mp: MediaPlayer?) {
        Log.w(TAG," onSeekComplete() invoked")
    }

    private fun createAudioPlaybackNotification(): Notification  {
        val builder = NotificationCompat.Builder(this, S.SILLOT_MUSIC_PLAYER_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("未知歌曲")
            .setContentText("未知艺术家")
            .setSmallIcon(R.drawable.icon)
//            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_music))
            .setStyle(MediaNotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken)) // 不设置就是普通通知了
            .setShowWhen(false)
            .setOngoing(true)

        return builder.build()
    }


    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "汐洛音频播放器"
        }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        return channelId
    }
}
