/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/12 23:06
 * updated: 2024/8/12 23:06
 */

package org.b3log.ld246

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.Base64
import android.view.MotionEvent
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Album
import androidx.compose.material.icons.twotone.Article
import androidx.compose.material.icons.twotone.Attribution
import androidx.compose.material.icons.twotone.CenterFocusWeak
import androidx.compose.material.icons.twotone.Navigation
import androidx.compose.material.icons.twotone.OpenInBrowser
import androidx.compose.material.icons.twotone.Person
import androidx.compose.material.icons.twotone.Quickreply
import androidx.compose.material.icons.twotone.Reply
import androidx.compose.material.icons.twotone.SafetyCheck
import androidx.compose.material.icons.twotone.Swipe
import androidx.compose.material.icons.twotone.TextFields
import androidx.compose.material.icons.twotone.Token
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kongzue.dialogx.dialogs.InputDialog
import com.kongzue.dialogx.dialogs.PopNotification
import com.kongzue.dialogx.dialogs.PopTip
import com.tencent.bugly.crashreport.BuglyLog
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import sc.windom.sillot.MatrixModel
import sc.windom.sofill.S
import sc.windom.sofill.Ss.S_Uri
import sc.windom.sofill.Ss.S_Webview
import sc.windom.sofill.U
import sc.windom.sofill.Us.U_Uri
import sc.windom.sofill.Us.thisSourceFilePath
import sc.windom.sofill.android.HWs
import sc.windom.sofill.android.webview.WebPoolsPro
import sc.windom.sofill.annotations.SillotActivity
import sc.windom.sofill.annotations.SillotActivityType
import sc.windom.sofill.api.MyRetrofit.createRetrofit
import sc.windom.sofill.api.ld246.ApiServiceNotification
import sc.windom.sofill.compose.FullScreenWebView
import sc.windom.sofill.compose.MyTagHandler
import sc.windom.sofill.compose.components.CommonTopAppBar
import sc.windom.sofill.compose.partialCom.DdMenuI
import sc.windom.sofill.compose.partialCom.NetworkAware
import sc.windom.sofill.compose.theme.CascadeMaterialTheme
import sc.windom.sofill.dataClass.ld246_Response
import sc.windom.sofill.dataClass.ld246_Response_Data_Notification
import sc.windom.sofill.dataClass.ld246_User
import sc.windom.sofill.pioneer.getSavedValue
import sc.windom.sofill.pioneer.mmkv
import sc.windom.sofill.pioneer.rememberSaveableMMKV
import sc.windom.sofill.pioneer.rememberSerializableMMKV
import sc.windom.sofill.pioneer.savableStateFlowMMKV
import sc.windom.sofill.pioneer.saveValue

/**
 * 设计目标：android:launchMode="singleInstancePerTask"
 */
@SillotActivity(SillotActivityType.Main)
@SillotActivity(SillotActivityType.Launcher)
@SillotActivity(SillotActivityType.UseVisible)
class HomeActivity : MatrixModel() {
    private val TAG = "Home.kt"
    private val srcPath = thisSourceFilePath(TAG)
    private lateinit var thisActivity: Activity
    override fun getMatrixModel(): String {
        return "链滴流云"
    }
    private var exitTime: Long = 0
    private var openUrlExternal: Boolean =
        mmkv.getSavedValue("${S.AppQueryIDs.汐洛}_@openUrlExternal", false) // 全局同步配置
    private val titles_icons = listOf(
        Icons.TwoTone.Article,
        Icons.TwoTone.Quickreply,
        Icons.TwoTone.Reply,
        Icons.TwoTone.Attribution,
        Icons.TwoTone.CenterFocusWeak,
        Icons.TwoTone.Album
    )
    private val mapEmpty = mutableMapOf<String, List<ld246_Response_Data_Notification>?>().apply {
        S.API.ld246_notification_type.associateWithTo(this) { emptyList() }
    }
    var map: MutableMap<String, List<ld246_Response_Data_Notification>?> = mapEmpty
    private var job: Job? = null
    private var retrofit: Retrofit? = null
    private var apiService: ApiServiceNotification? = null
    private var FullScreenWebView_url: MutableState<String?> = mutableStateOf(null)

    override fun onSaveInstanceState(outState: Bundle) {
        BuglyLog.d(TAG, "outState: $outState")
        if (outState.isEmpty) return // avoid crash
        super.onSaveInstanceState(outState)
        // 可添加额外需要保存可序列化的数据
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        BuglyLog.i(TAG, "onNewIntent() invoked")
        intoWorks(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BuglyLog.i(TAG, "onCreate() invoked")
        // 设置沉浸式通知栏
        window.setDecorFitsSystemWindows(false)
        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            insets
        }
        thisActivity = this

        // 创建Retrofit实例
        retrofit = createRetrofit("https://${S_Uri.HOST_ld246}/")
        // 创建API服务实例
        apiService = retrofit?.create(ApiServiceNotification::class.java)
        // 获取OnBackPressedDispatcher
        val onBackPressedDispatcher = onBackPressedDispatcher
        // 设置OnBackPressedCallback
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 在这里处理后退逻辑
                if (System.currentTimeMillis() - exitTime > 2000) {
                    PopTip.show("再按一次结束当前活动")
                    exitTime = System.currentTimeMillis()
                } else {
                    HWs.instance?.vibratorWaveform(
                        applicationContext,
                        longArrayOf(0, 30, 25, 40, 25, 10),
                        intArrayOf(2, 4, 3, 2, 2, 2),
                        -1
                    )
                    try {
                        Thread.sleep(200)
                    } catch (e: Exception) {
                        PopNotification.show(e.cause.toString(), e.toString())
                    }
                    BuglyLog.w(TAG, "再见")
                    finish()
//                        exitProcess(0)
                }
                HWs.instance?.vibratorWaveform(
                    applicationContext,
                    longArrayOf(0, 30, 25, 40, 25),
                    intArrayOf(9, 2, 1, 7, 2),
                    -1
                )
            }
        })

        intoWorks(intent)

    }

    private fun intoWorks(intent: Intent?) {
        val uri = intent?.data
        val scheme = uri?.scheme
        val host = uri?.host
        BuglyLog.d(TAG, "scheme: $scheme, host:$host")
        setContent {
            CascadeMaterialTheme {
                UI(intent)
            }
        }
        if (uri != null) {
            if (
                S_Uri.isUriMatched(uri, S_Uri.case_ld246_1)
                || S_Uri.isUriMatched(uri, S_Uri.case_ld246_2)
                || S_Uri.isUriMatched(uri, S_Uri.case_github_1)
                || S_Uri.isUriMatched(uri, S_Uri.case_mqq_1) // 拉起QQ授权
                || uri.scheme?.startsWith("http") == true
            ) {
                FullScreenWebView_url.value = uri.toString()
            }
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
    @Composable
    private fun UI(intent: Intent?) {
        val uri = intent?.data
        val Lcc = LocalContext.current
        val token = rememberSaveable {
            mutableStateOf(
                U.getDecryptedToken(
                    mmkv,
                    S.KEY_TOKEN_ld246,
                    S.KEY_AES_TOKEN_ld246
                )
            )
        }
        val currentTab = rememberSaveableMMKV(mmkv, "${srcPath}_@currentTab", "用户")
        val isShowBottomText = rememberSaveableMMKV(mmkv, "${srcPath}_@isShowBottomText", false)
        val pullToRefreshState = rememberPullToRefreshState()
        val isMenuVisible = rememberSaveable { mutableStateOf(false) }
        val userPageData = rememberSerializableMMKV(mmkv, "${srcPath}_@userPageData", ld246_User())
        val showFullScreenWebView = rememberSaveable { mutableStateOf(false) }
        val viewmodel = remember {
            NotificationsViewModel(
                token,
                currentTab,
                pullToRefreshState,
                userPageData,
                apiService
            )
        }
        val notificationsState = viewmodel.notificationsState.collectAsState(
            // initial = listOf() // 由于使用了 savableStateFlowMMKV ，这里不提供初始值
        )
        DisposableEffect(viewmodel) {
            onDispose {
                // 在这里释放资源
                job?.cancel()
            }
        }

        LaunchedEffect(Unit) {
            snapshotFlow { currentTab.value } // 创建一个Flow，它在每次currentTab.value变化时发出（启动时也会执行一次）.
                // snapshotFlow 与其他 Flow 的主要区别在于它是如何检测状态变化的。snapshotFlow 使用 Compose 的状态对象（如 State、MutableState 等）来检测变化，
                // 并且它是通过 Compose 的重组机制来实现的。这意味着 snapshotFlow 只在 Compose 的重组过程中检测状态变化，而不是在每次状态值发生变化时。
                .conflate() // 当新值到来时，如果上一个值还没被处理，就忽略它
                .collectLatest { // collectLatest会取消当前正在进行的操作，并开始新的操作
                    BuglyLog.d(TAG, "collectLatest currentTab.value: ${currentTab.value}")
                    pullToRefreshState.startRefresh()
                }
        }
        LaunchedEffect(pullToRefreshState.isRefreshing) {
            // pullToRefreshState 无法用 snapshotFlow 直接捕获
            val isRefreshing = pullToRefreshState.isRefreshing
            BuglyLog.d(TAG, "collectLatest pullToRefreshState.isRefreshing: $isRefreshing")
            if (isRefreshing) {
                if (currentTab.value == "用户") {
                    viewmodel.updateUserPage()
                } else {
                    viewmodel.fetchNotificationV2()
                }
            }
        }


        LaunchedEffect(FullScreenWebView_url.value) {
            if (!FullScreenWebView_url.value.isNullOrBlank()) {
                BuglyLog.d(TAG, "new FullScreenWebView_url -> ${FullScreenWebView_url.value}")
                showFullScreenWebView.value = true
            }
        }

        // 不是最佳实践，但是先凑合
        // 试试 ModalBottomSheet（已经有过实践）或者 BottomDrawer（非模态，应该可以实现最小化收起）
        if (!FullScreenWebView_url.value.isNullOrBlank() && showFullScreenWebView.value) {
            FullScreenWebView(
                thisActivity,
                FullScreenWebView_url.value!!,
                WebPoolsPro.key_NoActivity_Shared
            ) {
                showFullScreenWebView.value = false
            }
        } else {
            Scaffold(
                topBar = {
                    CommonTopAppBar(
                        "汐洛链滴社区客户端",
                        srcPath,
                        uri,
                        isMenuVisible,
                        additionalMenuItem = {
                            AddDropdownMenu(onDismiss = {
                                isMenuVisible.value = false
                            }, isShowBottomText, token, pullToRefreshState)
                        }) {
                        // 将Context对象安全地转换为Activity
                        if (Lcc is Activity) {
                            Lcc.finish() // 结束活动
                        }
                    }
                }, modifier = Modifier
                    .imePadding()
                    .background(Color.Gray)
                    .nestedScroll(pullToRefreshState.nestedScrollConnection)
            ) {
                Box(
                    Modifier
                        .padding(it)
                        .fillMaxSize()
                ) {
                    NetworkAware()
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (currentTab.value == "用户") {
                            if (userPageData.value.userName.isNullOrBlank() == true) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column {
                                        Text(
                                            text = "未登录",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 24.sp
                                        )
                                        Text(
                                            text = "请先配置 API Token",
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            } else {
                                UserPage(userPageData.value, ::_openURL)
                            }
                        } else {
                            notificationsState.value?.let { it1 -> NotificationsScreen(it1[currentTab.value]) }
                        }

                        SecondaryTextTabs(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(top = 31.dp),
                            currentTab,
                            isShowBottomText
                        )
                    }
                    if (pullToRefreshState.isRefreshing) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    } else {
                        LinearProgressIndicator(
                            progress = { pullToRefreshState.progress },
                            Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun AddDropdownMenu(
        onDismiss: () -> Unit,
        isShowBottomText: MutableState<Boolean>,
        token: MutableState<String?>,
        pullToRefreshState: PullToRefreshState
    ) {
        DdMenuI(
            text = { Text("手动刷新") },
            icon = { Icon(Icons.TwoTone.Swipe, contentDescription = null) },
            cb = {
                onDismiss()
                pullToRefreshState.startRefresh()
            }
        )
        DdMenuI(
            text = {
                if (isShowBottomText.value) {
                    Text("图标底部导航")
                } else {
                    Text("文字底部导航")
                }
            },
            icon = if (isShowBottomText.value) {
                { Icon(Icons.TwoTone.Navigation, contentDescription = null) }
            } else {
                { Icon(Icons.TwoTone.TextFields, contentDescription = null) }
            },
            cb = {
                onDismiss()
                isShowBottomText.value = !isShowBottomText.value
            }
        )
        DdMenuI(
            text = {
                if (openUrlExternal) {
                    Text("应用内打开链接")
                } else {
                    Text("浏览器打开链接")
                }
            },
            icon = {
                if (openUrlExternal) {
                    Icon(Icons.TwoTone.SafetyCheck, contentDescription = null)
                } else {
                    Icon(Icons.TwoTone.OpenInBrowser, contentDescription = null)
                }
            },
            cb = {
                onDismiss()
                openUrlExternal = !openUrlExternal
                mmkv.saveValue("${S.AppQueryIDs.汐洛}_@openUrlExternal", openUrlExternal)
            }
        )
        DdMenuI(
            text = { Text("链滴 API TOKEN") },
            icon = { Icon(Icons.TwoTone.Token, contentDescription = null) },
            cb = {
                onDismiss()
                val deToken = U.getDecryptedToken(mmkv, S.KEY_TOKEN_ld246, S.KEY_AES_TOKEN_ld246)
                InputDialog(
                    "🛸 API TOKEN",
                    "可在社区 设置 - 账号 中找到 API Token，固定以 'token ' 开头\n\n温馨提示：应用存储 Token 时进行了一定的处理，且不会传输到网络，但用户仍需注意防止 Token 泄露！建议使用前先阅读源代码",
                    "确定",
                    "取消",
                    deToken?.let { deToken } ?: run { "token " }
                )
                    .setCancelable(false)
                    .setOkButton { baseDialog, v, inputStr ->
                        token.value = inputStr
                        // 生成AES密钥
                        val aesKey = U.generateAesKey()
                        // 注意：这里需要将SecretKey转换为可以存储的格式，例如转换为字节数组然后进行Base64编码
                        val encodedKey = Base64.encodeToString(aesKey.encoded, Base64.DEFAULT)
                        // 加密Token
                        val encryptedToken = U.encryptAes(inputStr, aesKey)
                        // 将加密后的Token存储到MMKV中
                        mmkv.encode(S.KEY_AES_TOKEN_ld246, encodedKey)
                        mmkv.encode(S.KEY_TOKEN_ld246, encryptedToken)
                        pullToRefreshState.startRefresh()
                        PopNotification.show(
                            "TOKEN已更新（${
                                U.displayTokenLimiter(
                                    inputStr,
                                    "token ".length + 4,
                                    4
                                )
                            }）"
                        ).noAutoDismiss()
                        false
                    }
                    .show(thisActivity)
            },
        )
    }

    @Composable
    private fun SecondaryTextTabs(
        modifier: Modifier,
        currentTab: MutableState<String>,
        isShowBottomText: MutableState<Boolean>,
    ) {
        // REF https://www.composables.com/material3/tabrow
        val state by rememberSaveable { mutableIntStateOf(S.API.ld246_notification_type.size) }
        val selectedContentColor = S.C.btn_bgColor_pink.current    // 选中时文字颜色
        val unselectedContentColor = Color.Gray // 未选中时文字颜色

        Column(modifier = modifier) {
            TabRow(
                selectedTabIndex = state,
                containerColor = MaterialTheme.colorScheme.surface, // 设置TabRow的背景颜色
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[state])
                            .height(0.dp), // 设置选中指示器的高度 （切换动画有卡顿感，干脆隐藏了）
                        color = selectedContentColor // 设置选中指示器的颜色
                    )
                }
            ) {
                Tab(
                    selected = currentTab.value == "用户",
                    onClick = {
                        currentTab.value = "用户"
                    },
                    selectedContentColor = selectedContentColor,
                    unselectedContentColor = unselectedContentColor,
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.height(46.dp),
                        ) {
                            if (!isShowBottomText.value) {
                                Icon(
                                    imageVector = Icons.TwoTone.Person,
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    contentDescription = "user"
                                )
                            } else {
                                Text(
                                    text = "用户",
                                    maxLines = 2,
                                    modifier = Modifier.fillMaxSize(),
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                )
                S.API.ld246_notification_type.forEachIndexed { index, title ->
                    Tab(
                        selected = currentTab.value == title,
                        onClick = {
                            currentTab.value = title
                        },
                        selectedContentColor = selectedContentColor,
                        unselectedContentColor = unselectedContentColor,
                        text = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.height(46.dp),
                            ) {
                                if (!isShowBottomText.value) {
                                    Icon(
                                        imageVector = titles_icons[index],
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        contentDescription = title
                                    )
                                } else {
                                    Text(
                                        text = title,
                                        maxLines = 2,
                                        modifier = Modifier.fillMaxSize(),
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun NotificationsScreen(v: List<ld246_Response_Data_Notification>?) {
        if (v.isNullOrEmpty()) {
            // 显示空数据状态的占位符
            LazyColumn(
                modifier = Modifier.padding(
                    bottom = 70.dp, // 避免被底栏遮住
                    top = 5.dp, // 顶部有进度条，留空更美观
                )
            ) {
                items(13) { index ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp, top = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0x31F1F2F3),
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
            LazyColumn(
                modifier = Modifier.padding(
                    bottom = 70.dp, // 避免被底栏遮住
                    top = 5.dp, // 顶部有进度条，留空更美观
                )
            ) {
                item {
                    v.forEach { notification ->
                        NotificationCard(notification)
                    }
                }
            }
        }
    }

    @Composable
    private fun NotificationCard(notification: ld246_Response_Data_Notification) {
        val uriHandler = LocalUriHandler.current
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp, top = 2.dp),
            colors = if (notification.hasRead != true) {
                // 如果未读
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // 如果已读
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            }
        ) {
            Column(modifier = Modifier
                .padding(10.dp)
                .clickable {
                    // SelectableHtmlText 需要响应内容的点击事件，因此打开文章得扩大到整个卡片。
                    _openURL(
                        "https://${S_Uri.HOST_ld246}/article/${notification.dataId}",
                        uriHandler
                    )
                }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            _openURL(
                                "https://${S_Uri.HOST_ld246}/member/${notification.authorName}",
                                uriHandler
                            )
                        }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(notification.authorAvatarURL)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Author Avatar",
                        modifier = Modifier.size(26.dp)
                    )
                    notification.authorName?.let {
                        Text(
                            text = it,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(start = 8.dp) // 添加一些 padding 以增加间隔
                        )
                    }
                }
                notification.title?.let {
                    Text(
                        text = it,
                        fontSize = 17.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                _openURL(
                                    "https://${S_Uri.HOST_ld246}/article/${notification.dataId}",
                                    uriHandler
                                )
                            }
                    )
                }

                notification.content?.let {
                    SelectableUrlHandleHtmlText(
                        it,
                        MaterialTheme.colorScheme.onBackground.toArgb()
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("StaticFieldLeak")
    private inner class NotificationsViewModel(
        _token: MutableState<String?>,
        _currentTab: MutableState<String>,
        _pullToRefreshState: PullToRefreshState,
        _userPageData: MutableState<ld246_User>,
        _apiService: ApiServiceNotification?,
    ) : ViewModel() {
        val TAG = "NotificationsViewModel"
        val token = _token
        val currentTab = _currentTab
        val pullToRefreshState = _pullToRefreshState
        val userPageData = _userPageData
        val apiService = _apiService
        private val _notificationsState = savableStateFlowMMKV(
            mmkv,
            "${srcPath}@NotificationsViewModel",
            map.toMap()
        ) {
            it?.let { map = it.toMutableMap() }
        }
        val notificationsState: StateFlow<Map<String, List<ld246_Response_Data_Notification>?>?> =
            _notificationsState

        init {
            BuglyLog.d(
                TAG,
                "init with currentTab ${currentTab.value} (if you see this log when swith tab , check the viewmodel binding way)"
            )
            if (apiService == null) {
                PopNotification.show("初始化异常", "apiService is null")
            }
            pullToRefreshState.startRefresh()
        }

        private fun _onError(call: Call<ld246_Response>, t: Throwable) {
            // 处理异常
            PopNotification.show(call.toString(), t.toString())
                .noAutoDismiss()
            viewModelScope.launch {
                pullToRefreshState.endRefresh()
            }
        }


        private fun handle_ld246_Response(response: Response<ld246_Response>) {
            viewModelScope.launch {
                pullToRefreshState.endRefresh()
            }
            val message = "Error Response: ${response.message()}"
            when (response.code()) {
                200 ->
                    PopTip.show("<(￣︶￣)↗[${response.code()}]")

                401 -> PopNotification.show(
                    message,
                    "TOKEN为空或者错误，请在右上角设置 TOKEN 后下拉刷新"
                ).noAutoDismiss()

                403 -> PopNotification.show(message, "权限不足").noAutoDismiss()
                else -> PopNotification.show(
                    " ￣へ￣ [${response.code()}]",
                    response.toString()
                )
                    .noAutoDismiss()
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        fun updateUserPage() {
            BuglyLog.d(TAG, "updateUserPage -> old userPageData: ${userPageData.value}")
            val caller = apiService?.apiV2UserGet(token.value, S_Webview.UA_edge_android)
            caller?.enqueue(object : Callback<ld246_Response> {
                override fun onResponse(
                    p0: Call<ld246_Response>,
                    response: Response<ld246_Response>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.data?.user?.let { userPageData.value = it }
                    }
                    handle_ld246_Response(response)
                }

                override fun onFailure(p0: Call<ld246_Response>, p1: Throwable) {
                    _onError(p0, p1)
                }
            })
        }


        @OptIn(ExperimentalMaterial3Api::class)
        fun fetchAllNotifications() {
            viewModelScope.launch {
                val calls = mutableListOf<Call<ld246_Response>>()
                apiService?.let {
                    calls.add(
                        it.apiV2NotificationsCommentedGet(
                            1,
                            token.value,
                            S_Webview.UA_edge_android
                        )
                    )
                    calls.add(
                        it.apiV2NotificationsComment2edGet(
                            1,
                            token.value,
                            S_Webview.UA_edge_android
                        )
                    )
                    calls.add(
                        it.apiV2NotificationsReplyGet(
                            1,
                            token.value,
                            S_Webview.UA_edge_android
                        )
                    )
                    calls.add(
                        it.apiV2NotificationsAtGet(
                            1,
                            token.value,
                            S_Webview.UA_edge_android
                        )
                    )
                    calls.add(
                        it.apiV2NotificationsFollowingGet(
                            1,
                            token.value,
                            S_Webview.UA_edge_android
                        )
                    )
                    calls.add(
                        it.apiV2NotificationsPointGet(
                            1,
                            token.value,
                            S_Webview.UA_edge_android
                        )
                    )
                }
                calls.forEach { caller ->
//                    delay(100) // 避免接口请求频繁
                    caller.enqueue(object : Callback<ld246_Response> {
                        override fun onResponse(
                            call: Call<ld246_Response>,
                            response: Response<ld246_Response>
                        ) {
                            if (response.isSuccessful && response.body() != null) {
                                val data = response.body()?.data
                                try {
                                    when (call) {
                                        calls[0] -> map["回帖"] =
                                            data?.commentedNotifications

                                        calls[1] -> map["评论"] =
                                            data?.comment2edNotifications

                                        calls[2] -> map["回复"] = data?.replyNotifications
                                        calls[3] -> map["提及"] = data?.atNotifications
                                        calls[4] -> map["关注"] =
                                            data?.followingNotifications

                                        calls[5] -> map["积分"] = data?.pointNotifications
                                    }
                                } catch (e: Exception) {
                                    PopNotification.show(
                                        e.cause.toString(),
                                        e.toString()
                                    )
                                        .noAutoDismiss()
                                }
                            } else {
                                // 仅失败提醒
                                handle_ld246_Response(response)
                            }
                            viewModelScope.launch {
                                _notificationsState.emit(map.toMap())
                                pullToRefreshState.endRefresh()
                            }
                        }

                        override fun onFailure(call: Call<ld246_Response>, t: Throwable) {
                            _onError(caller, t)
                        }
                    })
                }
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        fun fetchNotificationV2() {
            job = viewModelScope.launch {
                try {
                    if (map.values.any { it.isNullOrEmpty() }) {
                        fetchAllNotifications()
                    } else if (pullToRefreshState.isRefreshing) {
                        // 执行当前tab的请求
                        val caller: Call<ld246_Response> = when (currentTab.value) {
                            "回帖" -> apiService?.apiV2NotificationsCommentedGet(
                                1,
                                token.value,
                                S_Webview.UA_edge_android
                            )

                            "评论" -> apiService?.apiV2NotificationsComment2edGet(
                                1,
                                token.value,
                                S_Webview.UA_edge_android
                            )

                            "回复" -> apiService?.apiV2NotificationsReplyGet(
                                1,
                                token.value,
                                S_Webview.UA_edge_android
                            )

                            "提及" -> apiService?.apiV2NotificationsAtGet(
                                1,
                                token.value,
                                S_Webview.UA_edge_android
                            )

                            "关注" -> apiService?.apiV2NotificationsFollowingGet(
                                1,
                                token.value,
                                S_Webview.UA_edge_android
                            )

                            "积分" -> apiService?.apiV2NotificationsPointGet(
                                1,
                                token.value,
                                S_Webview.UA_edge_android
                            )

                            else -> null
                        } ?: return@launch
                        caller.enqueue(object : Callback<ld246_Response> {
                            override fun onResponse(
                                call: Call<ld246_Response>,
                                response: Response<ld246_Response>
                            ) {
                                // 处理响应
                                if (response.isSuccessful && response.body() != null) {
                                    val data = response.body()?.data
                                    try {

                                        val notifications = when (currentTab.value) {
                                            "回帖" -> data?.commentedNotifications
                                            "评论" -> data?.comment2edNotifications
                                            "回复" -> data?.replyNotifications
                                            "提及" -> data?.atNotifications
                                            "关注" -> data?.followingNotifications
                                            "积分" -> data?.pointNotifications
                                            else -> null
                                        }
                                        map[currentTab.value] = notifications
                                    } catch (e: Exception) {
                                        PopNotification.show(
                                            e.cause.toString(),
                                            e.toString()
                                        )
                                            .noAutoDismiss()
                                    }
                                }
                                viewModelScope.launch {
                                    _notificationsState.emit(map.toMap())
                                    handle_ld246_Response(response)
                                }
                            }

                            override fun onFailure(call: Call<ld246_Response>, t: Throwable) {
                                _onError(caller, t)
                            }
                        })
                    } else {
                        // 不请求只更新显示TAB对于数据，一般是点击TAB的时候
                        viewModelScope.launch {
                            _notificationsState.emit(map.toMap())
                            pullToRefreshState.endRefresh()
                        }
                    }
                } catch (e: Exception) {
                    // 处理错误
                    PopNotification.show("任务失败", e.toString()).noAutoDismiss()
                } finally {
                    // 此处执行则不会等待
                }
            }
        }
    }


    @Preview(showBackground = true)
    @Composable
    private fun DefaultPreview() {
        UI(null)
    }

    private fun _openURL(url: String, uriHandler: UriHandler? = null) {
        if (openUrlExternal) {
            if (uriHandler != null) {
                uriHandler.openUri(url)
            } else {
                U_Uri.openUrl(url)
            }
        } else {
            FullScreenWebView_url.value = url
        }
    }

    /**
     * 完全体（CustomLinkMovementMethod），因为需要共享一些数据不好抽离，基础版在 [sc.windom.sofill.compose.SelectableHtmlText]
     */
    @Composable
    private fun SelectableUrlHandleHtmlText(
        html: String,
        textColorInt: Int,
        modifier: Modifier = Modifier
    ) {
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { context ->
                TextView(context).apply {
                    // 允许长按复制文本，需放在前面
                    setTextIsSelectable(true)
                    // 设置MovementMethod以使链接可点击，需放在后面
                    // 尝试过自定义处理逻辑，结果替换个链接都费劲
                    movementMethod = LinkMovementMethod.getInstance()
                    textSize = 15f // 设置全局字体大小
                    setTextColor(textColorInt) // 设置字体颜色
                    // setLinkTextColor() // 设置链接颜色
                }
            },
            update = { textView ->
                // 设置自定义的MovementMethod
                textView.movementMethod = CustomLinkMovementMethod()
                val _Html = U.parseAndDecodeUrl(
                    html,
                    """['"]https://ld246.com/forward\?goto=([^'"]*)['"]""".toRegex()
                )
//            BuglyLog.i("HTML", _Html)
                textView.text = HtmlCompat.fromHtml(
                    _Html,
                    HtmlCompat.FROM_HTML_MODE_COMPACT,
                    null,
                    MyTagHandler()
                )
            }
        )
    }

    private inner class CustomLinkMovementMethod : LinkMovementMethod() {
        override fun onTouchEvent(
            widget: TextView,
            buffer: Spannable,
            event: MotionEvent
        ): Boolean {
            val action = event.action

            if (action == MotionEvent.ACTION_UP) {
                val x = event.x.toInt()
                val y = event.y.toInt()

                val layout = widget.layout
                val line = layout.getLineForVertical(y)
                val off = layout.getOffsetForHorizontal(line, x.toFloat())

                val links = buffer.getSpans(off, off, ClickableSpan::class.java)

                if (links.isNotEmpty()) {
                    // 这里执行您的自定义逻辑，例如：
                    // 获取链接的URL
                    val urlSpan = links[0] as URLSpan
                    val url = urlSpan.url
                    _openURL(url)
                    return true
                }
            }

            return super.onTouchEvent(widget, buffer, event)
        }
    }
}
