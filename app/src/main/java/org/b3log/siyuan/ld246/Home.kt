package org.b3log.siyuan.ld246

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Token
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kongzue.dialogx.dialogs.InputDialog
import com.kongzue.dialogx.dialogs.PopNotification
import com.kongzue.dialogx.dialogs.PopTip
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.b3log.siyuan.CascadeMaterialTheme
import org.b3log.siyuan.S
import org.b3log.siyuan.Us
import org.b3log.siyuan.compose.SelectableHtmlText
import org.b3log.siyuan.compose.components.CommonTopAppBar
import org.b3log.siyuan.ld246.api.ApiServiceNotification
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeActivity : ComponentActivity() {
    val TAG = "ld246-HomeActivity"
    var mmkv: MMKV = MMKV.defaultMMKV()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置沉浸式通知栏
        window.setDecorFitsSystemWindows(false)
        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            insets
        }
        setContent {
            CascadeMaterialTheme {
                UI(intent)
            }
        }

    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
    @Composable
    private fun UI(intent: Intent?) {
        val TAG = "MainPro-MyUI"
        val uri = intent?.data
        val Lcc = LocalContext.current
        val inspectionMode = LocalInspectionMode.current // 获取当前是否处于预览模式// 获取窗口尺寸
        val coroutineScope = rememberCoroutineScope()
        val fileName = uri?.let { Us.getFileName(Lcc, it) }
        val fileSize = uri?.let { Us.getFileSize(Lcc, it) }
        val mimeType = intent?.data?.let { Us.getMimeType(Lcc, it) } ?: ""
        val fileType = fileName?.let { Us.getFileMIMEType(mimeType, it) }
            ?: run { Us.getFileMIMEType(mimeType) }
        val isLandscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）

        var isMenuVisible by rememberSaveable { mutableStateOf(false) }
        var itemCount by remember { mutableStateOf(15) }
        val state = rememberPullToRefreshState()


// 创建Retrofit实例
        val retrofit = Retrofit.Builder()
            .baseUrl("https://${S.HOST_ld246}/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

// 创建API服务实例
        val apiService = retrofit.create(ApiServiceNotification::class.java)
        val viewmodel = NotificationsViewModel()
        if (state.isRefreshing) {
            LaunchedEffect(true) {
                delay(500) // 避免接口请求频繁
                viewmodel.fetchNotifications(state, apiService)
            }
        }
// 启动时自动获取通知
        LaunchedEffect(Unit) {
            viewmodel.fetchNotifications(state, apiService)
        }
        Scaffold(
            topBar = {
                CommonTopAppBar(
                    "汐洛链滴社区客户端",
                    uri,
                    additionalMenuItem = { AddDropdownMenu() }) {
                    // 将Context对象安全地转换为Activity
                    if (Lcc is Activity) {
                        Lcc.finish() // 结束活动
                    }
                }
            }, modifier = Modifier
                .background(Color.Gray)
                .nestedScroll(state.nestedScrollConnection)
        ) {
            Box(
                Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                NotificationsScreen(viewModel = viewmodel, Lcc)
                if (state.isRefreshing) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                } else {
                    LinearProgressIndicator(progress = { state.progress }, Modifier.fillMaxWidth())
                }
            }
        }
    }


    @Composable
    fun AddDropdownMenu() {
        DropdownMenuItem(
            text = { Text("设置TOKEN") },
            leadingIcon = { Icon(Icons.TwoTone.Token, contentDescription = null) },
            onClick = {
                val deToken = Us.getDecryptedToken(mmkv, S.KEY_TOKEN_ld246, S.KEY_AES_TOKEN_ld246)
                InputDialog(
                    "输入TOKEN",
                    "可在社区 设置 - 账号 中找到 API Token，固定以 'token ' 开头",
                    "确定",
                    "取消",
                    deToken?.let { deToken } ?: run { "token " }
                )
                    .setCancelable(false)
                    .setOkButton { baseDialog, v, inputStr ->
                        // 生成AES密钥
                        val aesKey = Us.generateAesKey()
                        // 注意：这里需要将SecretKey转换为可以存储的格式，例如转换为字节数组然后进行Base64编码
                        val encodedKey = Base64.encodeToString(aesKey.encoded, Base64.DEFAULT)
                        // 加密Token
                        val encryptedToken = Us.encryptAes(inputStr, aesKey)
                        // 将加密后的Token存储到MMKV中
                        mmkv.encode(S.KEY_AES_TOKEN_ld246, encodedKey)
                        mmkv.encode(S.KEY_TOKEN_ld246, encryptedToken)
                        PopNotification.show(
                            "TOKEN已更新（${
                                Us.displayTokenLimiter(
                                    inputStr,
                                    "token ".length + 4,
                                    4
                                )
                            }）",
                            "请手动刷新界面，通常为下拉刷新"
                        ).noAutoDismiss()
                        false
                    }
                    .show()
            },
        )
    }

    @Composable
    fun NotificationsScreen(viewModel: NotificationsViewModel, Lcc: Context) {
        // 观察LiveData并更新状态
        val notifications = viewModel.notifications.observeAsState(listOf()).value
        Log.d("NotificationsList", notifications.toString())
        if (notifications.isNullOrEmpty()) {
            // 显示空数据状态的占位符
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(13) { index ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            NotificationsList(notifications, Lcc)
        }
    }

    @Composable
    fun NotificationsList(notifications: List<回帖消息Response_Notification>, Lcc: Context) {
        LazyColumn {
            item {
                notifications.forEach { notification ->
                    NotificationCard(notification, Lcc)
                }
            }
        }
    }

    @Composable
    fun NotificationCard(notification: 回帖消息Response_Notification, Lcc: Context) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            colors = if (notification.hasRead) {
                // 如果已读
                CardDefaults.cardColors(
                    containerColor = S.C.Card_bgColor_green1.current,
                    contentColor = Color.White
                )
            } else {
                // 如果未读
                CardDefaults.cardColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            }
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(notification.authorAvatarURL)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Author Avatar",
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = notification.authorName,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(start = 8.dp) // 添加一些 padding 以增加间隔
                    )
                }
                Text(text = notification.title, fontSize = 15.sp, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold)
                SelectableHtmlText(notification.content)
            }
        }
    }


    inner class NotificationsViewModel : ViewModel() {
        private val _notifications = MutableLiveData<List<回帖消息Response_Notification>?>()
        val notifications: MutableLiveData<List<回帖消息Response_Notification>?> = _notifications

        @OptIn(ExperimentalMaterial3Api::class)
        fun fetchNotifications(state: PullToRefreshState, apiService: ApiServiceNotification) {
            viewModelScope.launch {
                try {
                    _notifications.postValue(null) // 先重置
                    // 执行网络请求
                    apiService.apiV2NotificationsCommentedGet(
                        "1",
                        Us.getDecryptedToken(mmkv, S.KEY_TOKEN_ld246, S.KEY_AES_TOKEN_ld246),
                        "Sillot-anroid/0.35"
                    ).enqueue(object :
                        Callback<回帖消息Response> {
                        override fun onResponse(
                            call: Call<回帖消息Response>,
                            response: Response<回帖消息Response>
                        ) {
                            if (response.isSuccessful) {
                                Log.d("onResponse", response.body().toString())
                                _notifications.postValue(response.body()?.data?.commentedNotifications)
                                PopTip.show("<(￣︶￣)↗[GO!]");
                                apiService.apiV2NotificationsMakeRead(
                                    "commented",
                                    Us.getDecryptedToken(
                                        mmkv,
                                        S.KEY_TOKEN_ld246,
                                        S.KEY_AES_TOKEN_ld246
                                    ),
                                    "Sillot-anroid/0.35"
                                )
                            } else {
                                // 处理错误响应
                                val rcode = response.code()
                                when (rcode) {
                                    401 -> PopNotification.show(
                                        "更新失败",
                                        "TOKEN为空或者错误，请在右上角设置 TOKEN 后下拉刷新"
                                    ).noAutoDismiss()

                                    403 -> PopNotification.show("更新失败", "权限不足")
                                        .noAutoDismiss()

                                    else -> PopNotification.show(
                                        "onResponse失败",
                                        response.toString()
                                    ).noAutoDismiss()
                                }
                            }
                            if (state.isRefreshing) {
                                state.endRefresh()
                            }
                        }

                        override fun onFailure(call: Call<回帖消息Response>, t: Throwable) {
                            // 处理异常
                            Log.e("onFailure", t.toString())
                            if (state.isRefreshing) {
                                state.endRefresh()
                            }
                            PopNotification.show(call.toString(), t.toString()).noAutoDismiss()
                        }
                    })
                } catch (e: Exception) {
                    // 处理错误
                    Log.e("catch viewModelScope.launch", e.toString())
                    PopNotification.show("任务失败", e.toString()).noAutoDismiss()
                } finally {
                    // 此处执行则不会等待 onResponse
                }
            }
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        UI(null)
    }

}
