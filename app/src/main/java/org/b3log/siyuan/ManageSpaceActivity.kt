package org.b3log.siyuan

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Transition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sc.windom.sofill.S
import sc.windom.sofill.U
import sc.windom.sofill.Us.U_FileUtils.sizeInBytes
import sc.windom.sofill.Us.U_FileUtils.usertDir
import sc.windom.sofill.Us.U_Safe
import sc.windom.sofill.compose.components.CommonTopAppBar
import sc.windom.sofill.compose.theme.CascadeMaterialTheme
import java.io.File
import kotlin.math.ln
import kotlin.math.pow

// TODO: 添加更多可清理内容
class ManageSpaceActivity : AppCompatActivity() {
    //     将"清除数据"项变为"管理空间"，自定义数据清除    https://github.com/Hi-Windom/Sillot-android/issues/49
    val TAG = "ManageSpaceActivity.kt"
    private lateinit var thisActivity: AppCompatActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        thisActivity = this
        // 设置沉浸式通知栏
        window.setDecorFitsSystemWindows(false)
        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            insets
        }
        setContent {
            CascadeMaterialTheme {
                WaitUI()
            }
        }
        U.SAFE.checkBiometric(thisActivity, ::navigateToMainScreen, ::showRetryDialog, ::showBiometricErrorDialog)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) //设置竖屏锁定
    }
    private fun navigateToMainScreen() {
        // 认证成功，导航到主屏幕
        setContent {
            CascadeMaterialTheme {
                UI(intent, TAG)
            }
        }
    }

    private fun showRetryDialog() {
        // 显示重试对话框
        setContent {
            CascadeMaterialTheme {
                WaitUI()
            }
        }
        U.SAFE.checkBiometric(thisActivity, ::navigateToMainScreen, ::showRetryDialog, ::showBiometricErrorDialog)
    }

    private fun showBiometricErrorDialog(errString: CharSequence, state: Int) {
        // 生物识别错误
        setContent {
            CascadeMaterialTheme {
                ErrorUI(errString, state)
            }
        }

    }


    @Composable
    private fun WaitUI() {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.fillMaxSize(.8f))
        }
    }


    @Composable
    private fun ErrorUI(message: CharSequence, state: Int) {
        val t = when (state) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                "应用可以进行生物识别技术进行身份验证。"
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                "该设备上没有搭载可用的生物特征功能。"
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                "生物识别功能当前不可用。"
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                "用户没有录入生物识别数据。"
            }

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                "设备需要安全更新才能使用生物识别功能。"
            }

            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                "设备不支持生物识别功能。"
            }

            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                "生物识别功能的当前状态未知。"
            }

            else -> {
                "未知原因"
            }
        }
        val isMenuVisible = rememberSaveable { mutableStateOf(false) }
        Scaffold(
            topBar = {
                CommonTopAppBar("当前需要认证", TAG, null, isMenuVisible) {
                    thisActivity.finish()
                }
            },
            bottomBar = {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        U.SAFE.checkBiometric(thisActivity, ::navigateToMainScreen, ::showRetryDialog, ::showBiometricErrorDialog)
                    },
                ) {
                    Text("重试认证")
                }
            }) {
            Box(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                ) {
                    Text(
                        text = message.toString(),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = S.C.text_fontSize_xxxx.current,
                        lineHeight = S.C.text_lineHeight_xxxx.current,
                        maxLines = 6,
                        overflow = TextOverflow.Ellipsis, // 如果文本仍然太长，则显示省略号
                    )
                    ErrorText(t)
                    ErrorText("基于用户数据安全考虑，您必须认证成功才能继续。")
                }
            }
        }
    }

    @Composable
    private fun ErrorText(text: String, ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.error,
            fontSize = S.C.text_fontSize_xxxx.current,
            lineHeight = S.C.text_lineHeight_xxxx.current,
        )
    }
}



@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState")
@Composable
private fun UI(intent: Intent?, TAG: String) {
    val uri = intent?.data
    val Lcc = LocalContext.current
    val isMenuVisible = rememberSaveable { mutableStateOf(false) }
    val filesList = mutableStateOf<List<File>>(emptyList())
    val selectedFiles = rememberSaveable { mutableSetOf<String>() }
    val selectedFilesCount = rememberSaveable { mutableIntStateOf(0) } // 跟踪选中的文件数量
    var isCleaning by remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(true) }
    var refreshFilesList by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = isCleaning, key2 = refreshFilesList) {
        isLoading.value = true
        // 获取所有缓存目录的文件列表
        val cacheDirs = listOf(
            Lcc.filesDir, // /data/user/$userId/$packageName/files
            Lcc.cacheDir, // /data/data/$packageName/cache
            Lcc.getExternalFilesDir(null), // /storage/emulated/$userId/Android/data/$packageName/files
            File(Lcc.filesDir.parent, "app_webview"), // /data/data/$packageName/app_webview
        )

        val _filesList = cacheDirs.flatMap { dir ->
            dir?.listFiles()?.toList() ?: emptyList()
        }.sortedByDescending { it.sizeInBytes } // 按大小降序排序
        filesList.value = _filesList
        isLoading.value = false
    }

    Scaffold(
        topBar = {
            CommonTopAppBar("汐洛存储清理助手", TAG, uri, isMenuVisible) {
                // 将Context对象安全地转换为Activity
                if (Lcc is Activity) {
                    Lcc.finish() // 结束活动
                }
            }
        },
        bottomBar = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    isCleaning = true
                    coroutineScope.launch {
                        launchCleaningProcess(selectedFiles) {
                            isCleaning = false
                            selectedFiles.clear()
                            selectedFilesCount.intValue = 0 // 清理后重置计数器
                        }
                    }
                },
                enabled = selectedFilesCount.intValue > 0 && !isCleaning // selectedFiles.isNotEmpty() 判断根本行不通，见鬼
            ) {
                Text("清理选中项目")
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                refreshFilesList = !refreshFilesList
            }) {
                Icon(Icons.Filled.Refresh, contentDescription = "刷新")
            }
        }
    ) {
        if (isLoading.value || isCleaning) {
            Box(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.fillMaxSize(.8f))
            }
        } else if (filesList.value.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("已经很干净啦~")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(it)) {
                items(filesList.value) { file ->
                    val isExternalFilesDir = file.path.startsWith(Lcc.getExternalFilesDir(null)?.path ?: "")
                    val isChecked = rememberSaveable(file.absolutePath) {
                        mutableStateOf(selectedFiles.contains(file.absolutePath))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked.value,
                            onCheckedChange = { checked ->
                                isChecked.value = checked
                                if (checked) {
                                    selectedFiles.add(file.absolutePath)
                                    selectedFilesCount.intValue++
                                } else {
                                    selectedFiles.remove(file.absolutePath)
                                    selectedFilesCount.intValue--
                                }
                            }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = file.name,
                                fontSize = 16.sp,
                                color = if (isExternalFilesDir) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                text = file.absolutePath,
                                color = Color.Gray,
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis, // 如果文本仍然太长，则显示省略号
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Text(
                            text = U.FileUtils.getFileOrFolderSize(file),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

suspend fun launchCleaningProcess(selectedFiles: Set<String>, onComplete: () -> Unit) {
    withContext(Dispatchers.IO) {
        selectedFiles.forEach { filePath ->
            val file = File(filePath)
            file.deleteRecursively()
        }
        withContext(Dispatchers.Main) {
            onComplete()
        }
    }
}
