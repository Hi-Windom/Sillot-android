package org.b3log.siyuan

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.media.AudioManager
import android.os.Build
import android.util.Log

// 由于USBDeviceReceiver是在AndroidManifest.xml中静态注册的，您不需要在onResume和onPause方法中注册和注销接收器。系统会自动处理这些事件。
class USBDeviceReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "USBDeviceReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        when (intent.action) {
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                }
                device?.let {
                    if (isUsbAudioDevice(device)) {
                        Log.d(TAG, "USB Audio device attached: ${device.productName} v${device.version}\n${device.deviceName} (${device.deviceId}) ${device.manufacturerName}${device.productId}")
                        requestAudioFocus(context, audioManager)
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
                        Log.d(TAG, "USB Audio device detached: ${device.productName} v${device.version}\n${device.deviceName} (${device.deviceId}) ${device.manufacturerName}${device.productId}")
                        abandonAudioFocus(audioManager)
                    }
                }
            }
        }
    }

    private fun isUsbAudioDevice(device: UsbDevice): Boolean {
        // Check if the USB device is an audio device
        // You may need to check device characteristics to determine if it's an audio device
        // This is just a placeholder
        return true
    }

    private fun requestAudioFocus(context: Context, audioManager: AudioManager): Boolean {
        // 请求USB音频独占
        val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
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
        }

        val result = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus(audioManager: AudioManager) {
        // 使用AudioManager的abandonAudioFocus方法放弃音频焦点
        audioManager.abandonAudioFocus(null)
    }
}

