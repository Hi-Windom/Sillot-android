package org.b3log.siyuan.producer

import android.app.Activity
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import org.b3log.siyuan.Ss
import org.b3log.siyuan.videoPlayer.SimplePlayer

class MainPro : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val uri = intent.data

        if (uri != null) {
            handleMedia(uri)
        } else {
            // 处理其他类型文件
        }

        finish()
    }

    private fun handleMedia(uri: Uri) {
        val videoPath = if (uri.scheme == "file") {
            // 本地文件
            uri.path ?: ""
        } else {
            // URL
            uri.toString()
        }

        val intent = Intent(this, SimplePlayer::class.java)
        intent.putExtra("videoPath", videoPath)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Ss.VIDEO_PICK_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedVideoUri = data?.data
            selectedVideoUri?.let {
                val videoPath = getRealPathFromURI(it) // 获取真实路径
                val intent = Intent(this, SimplePlayer::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK) // 独立窗口，不影响主窗口
                intent.putExtra("videoPath", videoPath)
                startActivity(intent)
            }
        }
    }

    private fun getRealPathFromURI(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.let {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            val path = cursor.getString(columnIndex)
            cursor.close()
            return path
        }
        return ""
    }
}
