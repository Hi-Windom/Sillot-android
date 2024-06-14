package sc.windom.sofill.compose

import android.annotation.SuppressLint
import android.net.Uri
import android.view.DragEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import sc.windom.sofill.U
import java.io.File

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun SelectableText(text: String, modifier: Modifier = Modifier, style: TextStyle = TextStyle()) {
    // 支持长按选择操作
    SelectionContainer {
        Text(
            text = text,
            modifier = modifier,
            style = style
                .copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Normal
                )
        )
    }
}

@Composable
fun DragAndDropArea(
    onDragEnd: (Uri?, CharSequence?) -> Unit,
    onDragStart: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var offsetX = 0f
    var offsetY = 0f
    val Lcc = LocalContext.current
    // 用于存储临时文件的缓存目录
    val cacheDir = remember { File(Lcc.cacheDir, "temp") }
    if (!cacheDir.exists()) {
        cacheDir.mkdirs()
    }
    var draggedData: Uri? = null
    var draggedText: CharSequence? = null
    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragCancel = {},
                    onDragStart = {
                        onDragStart()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // 在这里，我们无法直接获取拖拽数据，因此需要在 AndroidView 中处理
                    },
                    onDragEnd = {
                        // 拖拽结束时的处理
                        onDragEnd(draggedData, draggedText)
                        // 尝试获取拖拽进来的数据
//                        val data = LocalClipboardManager.current.dragAndDropPayload
//                        val uri = result?.getContentUri()
//                        val text = result?.getText()
//                        if (uri != null) {
//                            // 处理URI数据
//                            val cachedFile = U.FileUtils.saveUriToCache(Lcc, uri, cacheDir)
//                            onDragEnd(cachedFile?.toUri(), null)
//                        } else if (text != null) {
//                            // 处理文本数据
//                            onDragEnd(null, text)
//                        }
                    }
                )
            }
    ) {
        AndroidView(
            { context ->
                FrameLayout(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    setOnDragListener { view, event ->
                        when (event.action) {
                            DragEvent.ACTION_DROP -> {
                                val clipData = event.clipData
                                if (clipData != null && clipData.itemCount > 0) {
                                    val item = clipData.getItemAt(0)
                                    val uri = item.uri
                                    val text = item.text
                                    if (uri != null) {
                                        // 处理URI数据
                                        val cachedFile = U.FileUtils.saveUriToCache(Lcc, uri, cacheDir)
                                        onDragEnd(cachedFile?.toUri(), null)
                                    } else if (text != null) {
                                        // 处理文本数据
                                        onDragEnd(null, text)
                                    }
                                    if (uri != null) {
                                        // 将 URI 数据保存到缓存目录
                                        draggedData = uri
                                    }
                                }
                                true
                            }
                            else -> false
                        }
                    }
                }
            },
            modifier = Modifier.matchParentSize()
        )

        // 这里可以放置其他 Composable 内容
        content()
    }
}

