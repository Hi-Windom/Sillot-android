package org.b3log.siyuan

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.launch
import org.b3log.siyuan.producer.getFileName

class USB_AUDIO_EXCLUSIVE : AppCompatActivity() {
    private lateinit var usbManager: UsbManager
    private lateinit var audioManager: AudioManager
    // 创建一个AudioFocusRequest构建器
    private val audioFocusRequestBuilder = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)

    companion object {
        private const val TAG = "USB_AUDIO_EXCLUSIVE"
        private const val USB_PERMISSION_REQUEST_CODE = 10109
    }



    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyUI_USB_AUDIO_EXCLUSIVE(this)
        }
        // 设置沉浸式通知栏
        window.setDecorFitsSystemWindows(false)
        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            insets
        }
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(usbReceiver, filter)
        // Check for existing USB devices
        checkForExistingDevices()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(usbReceiver)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            USB_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, handle accordingly
                } else {
                    // Permission denied, handle accordingly
                }
            }
        }
    }

    private fun requestPermission(device: UsbDevice) {
        val permissionIntent = PendingIntent.getBroadcast(
            this,
            USB_PERMISSION_REQUEST_CODE,
            Intent("com.example.USB_PERMISSION"),
            PendingIntent.FLAG_IMMUTABLE
        )
        usbManager.requestPermission(device, permissionIntent)
    }

    private val usbReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action ?: return
            when (action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    }
                    device?.let {
                        if (isUsbAudioDevice(device)) {
                            // 禁止获取：device.serialNumber
                            Log.d(TAG, "USB Audio device attached: ${device.productName} v${device.version}\n${device.deviceName} (${device.deviceId}) ${device.manufacturerName} ${device.productId}")
                            requestAudioFocus()
                        }
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    }
                    device?.let {
                        if (isUsbAudioDevice(device)) {
                            Log.d(TAG, "USB Audio device detached: ${device.productName} v${device.version}\n${device.deviceName} (${device.deviceId}) ${device.manufacturerName} ${device.productId}")
                            abandonAudioFocus()
                        }
                    }
                }
            }
        }
    }

    private fun checkForExistingDevices() {
        val deviceList: HashMap<String, UsbDevice>? = usbManager.deviceList
        deviceList?.values?.forEach { device ->
            if (isUsbAudioDevice(device)) {
                Log.d(TAG, "Found existing USB audio device: ${device.productName} v${device.version}\n${device.deviceName} (${device.deviceId}) ${device.manufacturerName} ${device.productId}")
                requestPermission(device)
            }
        }
    }

    private fun isUsbAudioDevice(device: UsbDevice): Boolean {
        // Check if the USB device is an audio device
        // You may need to check device characteristics to determine if it's an audio device
        // This is just a placeholder
        return true
    }

    private fun requestAudioFocus(): Boolean { // 请求USB音频独占
        // 使用构建器创建AudioFocusRequest对象
        val audioFocusRequest = audioFocusRequestBuilder.setOnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    Log.d(TAG, "恢复播放或继续播放")
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    Log.d(TAG, "暂停播放并释放媒体资源")
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    Log.d(TAG, "暂停播放，但不清除媒体资源，因为可能会很快再次获得焦点")
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    Log.d(TAG, "降低音量，这通常用于过渡性的干扰，如通知")
                }
            }
        }.build()
        // 通过AudioManager请求音频焦点
        val result = audioManager.requestAudioFocus(audioFocusRequest)
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }
    private fun abandonAudioFocus() {
        // 使用AudioManager的abandonAudioFocusRequest方法放弃音频焦点
        audioManager.abandonAudioFocusRequest(audioFocusRequestBuilder.build())
    }

}


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyUI_USB_AUDIO_EXCLUSIVE(activity: USB_AUDIO_EXCLUSIVE) {
    val coroutineScope = rememberCoroutineScope() // 用于启动协程的作用域
    var isServiceRunning by remember { mutableStateOf(false) }

    // 定义格式化时间的函数
    fun formatTime(milliseconds: Int): String {
        val totalSeconds = milliseconds / 1000
        val hours = if (milliseconds < 10000) 0 else totalSeconds / 3600
        val minutes = if (milliseconds < 10000) totalSeconds / 60 else (totalSeconds % 3600) / 60
        val seconds = if (milliseconds < 10000) totalSeconds % 60 else totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
    fun calculateTimeRatio(currentTime: Double, totalTime: Double): Float {
        // 将 Double 类型的 currentTime 转换为 Float 类型
        val currentTimeFloat = currentTime.toFloat()
        val totalTimeFloat = totalTime.toFloat()

        // 检查 totalTime 是否为 0 以避免除以 0 的错误
        return if (totalTime > 0.0f) currentTimeFloat / totalTimeFloat else 0.0f
    }
    fun timeStringToDouble(timeString: String): Double {
        val parts = timeString.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size != 3) {
            throw IllegalArgumentException("Time string must be in the format of HH:MM:SS")
        }

        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()
        val seconds = parts[2].toInt()

        val totalSeconds = hours * 3600 + minutes * 60 + seconds
        return totalSeconds.toDouble()
    }

    // 在这里停止 MusicService
    fun stopMusicService() {
        val serviceIntent = Intent(activity, MusicService::class.java)
        activity.stopService(serviceIntent)
        isServiceRunning = false
    }

    // 在这里移除通知栏
    fun removeNotification() {
        NotificationManagerCompat.from(activity).cancel(Ss.USB_AUDIO_EXCLUSIVE_notificationId)
    }

    val uri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var progress by remember { mutableFloatStateOf(0f) }
    val isPlaying = remember { mutableStateOf(false) } // 需要传递对象，因此不适合用 by
    val isStopped = remember { mutableStateOf(false) }
    var currentTime = remember { mutableStateOf("00:00:00") }
    var totalTime = remember { mutableStateOf("00:00:00") }
    val musicPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            fileName = getFileName(activity, uri) ?: ""
            isPlaying.value = true
            isStopped.value = false
            // 启动 MusicService 并传递音乐文件的 URI
            val serviceIntent = Intent(activity, MusicService::class.java).apply {
                action = MusicService.ACTION_PLAY
                data = uri
                putExtra("fileName", fileName)
            }
            activity.startService(serviceIntent)
        }
    }

    fun togglePlaySkipNext() {
        coroutineScope.launch {
            val serviceIntent = Intent(activity, MusicService::class.java).apply {
                action = MusicService.ACTION_SkipNext
                data = uri
            }
            activity.startService(serviceIntent)
            isStopped.value = true
        }
    }
    fun togglePlaySkipPrevious() {
        coroutineScope.launch {
            val serviceIntent = Intent(activity, MusicService::class.java).apply {
                action = MusicService.ACTION_SkipPrevious
                data = uri
            }
            activity.startService(serviceIntent)
            isStopped.value = true
        }
    }
    fun togglePlayPause() {
        coroutineScope.launch {
            isStopped.value = false
            if (isPlaying.value) {
                // 启动 MusicService 并传递音乐文件的 URI
                val serviceIntent = Intent(activity, MusicService::class.java).apply {
                    action = MusicService.ACTION_PAUSE
                    data = uri
                }
                activity.startService(serviceIntent)
            } else {
                if (isStopped.value) {
                    uri?.let {
                        // 启动 MusicService 并传递音乐文件的 URI
                        val serviceIntent = Intent(activity, MusicService::class.java).apply {
                            data = uri
                            action = MusicService.ACTION_PLAY
                        }
                        activity.startService(serviceIntent)
                    }
                } else {
                    // 启动 MusicService 并传递音乐文件的 URI
                    val serviceIntent = Intent(activity, MusicService::class.java).apply {
                        action = MusicService.ACTION_RESUME
                        data = uri
                    }
                    activity.startService(serviceIntent)
                }
            }
            isPlaying.value = !isPlaying.value
        }
    }


    // 在这里停止 MusicService 并移除通知
    DisposableEffect(Unit) {
        onDispose {
            stopMusicService()
            removeNotification()
        }
    }


    LaunchedEffect(Unit) {
        val filter = IntentFilter(MusicService.ACTION_MEDIA_STATUS_CHANGED)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == MusicService.ACTION_MEDIA_STATUS_CHANGED) {
                    isPlaying.value = intent.getBooleanExtra(MusicService.EXTRA_MEDIA_PLAYING, false)
                    currentTime.value = formatTime(intent.getIntExtra(MusicService.EXTRA_MEDIA_CURRENT_POSITION, 0))
                    totalTime.value = formatTime(intent.getIntExtra(MusicService.EXTRA_MEDIA_DURATION, 0))
                }
            }
        }

        activity.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED) // 这里需要 EXPORTED
    }


    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    modifier = Modifier
                                        .clickable(onClick = {
                                            activity.finish() // 结束活动
                                        })
                                        .padding(end = 8.dp)
                                )
                                Text(
                                    text = "汐洛音频播放器",
                                    fontSize = 18.sp
                                )
                            }
                        }, modifier = Modifier.background(Color.Blue)
                    )
                }, modifier = Modifier.background(Color.Gray)
            ) {}
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ChooseMusicButton(musicPickerLauncher)
                if (fileName.isNotEmpty()) {
                    Column (
                        modifier = Modifier.fillMaxHeight(.7f).fillMaxWidth(),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
//                        Image()
                        Text(
                            text = "正在播放：\n${fileName}",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PlayPauseControl(isPlaying, ::togglePlayPause,::togglePlaySkipPrevious, ::togglePlaySkipNext)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center)  {
                            Text(
                                text = currentTime.value,
                                fontSize = 14.sp,
                                modifier = Modifier
                            )
                            Slider(
                                value = calculateTimeRatio(timeStringToDouble(currentTime.value), timeStringToDouble(totalTime.value)),
                                enabled = false, // 能力有限，请通过通知栏操作
                                onValueChange = {  },
                                valueRange = 0f..1f,
                                modifier = Modifier.fillMaxWidth(.7f)
                            )
                            Text(
                                text = totalTime.value,
                                fontSize = 14.sp,
                                modifier = Modifier
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun PlayPauseControl(isPlaying: MutableState<Boolean>, togglePlayPause: () -> Unit, togglePlaySkipPrevious: () -> Unit, togglePlaySkipNext: () -> Unit) {
    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = togglePlaySkipPrevious,
            enabled = false,
            modifier = Modifier.size(72.dp)
        ) {
            val tint = MaterialTheme.colorScheme.primary

            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "上一首",
                tint = tint,
                modifier = Modifier.size(72.dp)
            )
        }
        IconButton(
            onClick = togglePlayPause,
            modifier = Modifier.size(72.dp)
        ) {
            val tint = MaterialTheme.colorScheme.primary

            Icon(
                imageVector = if (isPlaying.value) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying.value) "Pause" else "Play",
                tint = tint,
                modifier = Modifier.size(72.dp)
            )
        }
        IconButton(
            onClick = togglePlaySkipNext,
            enabled = false,
            modifier = Modifier.size(72.dp)
        ) {
            val tint = MaterialTheme.colorScheme.primary

            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "下一首",
                tint = tint,
                modifier = Modifier.size(72.dp)
            )
        }
    }
}


@Composable
fun ChooseMusicButton(launcher: ActivityResultLauncher<String>) {
    Button(
        onClick = {
            // 选择音乐文件
            launcher.launch("audio/*")
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Choose Music",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}



@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val activity = USB_AUDIO_EXCLUSIVE()
    MyUI_USB_AUDIO_EXCLUSIVE(activity)
}