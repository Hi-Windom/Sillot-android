package org.b3log.siyuan.compose

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext


@Composable
fun loadAppThumbnail(): ImageBitmap? { // 这里获取到的是自己的应用图标
    val context = LocalContext.current
    val packageManager = context.packageManager
    val packageName = context.packageName

    var thumbnail: ImageBitmap? = null

    val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
    val drawable = applicationInfo.loadIcon(packageManager)
    thumbnail = convertDrawableToImageBitmap(drawable)

    return thumbnail
}


@Composable
fun convertDrawableToImageBitmap(drawable: Drawable): ImageBitmap? {
    return try {
        val bitmap = (drawable as BitmapDrawable).bitmap
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}