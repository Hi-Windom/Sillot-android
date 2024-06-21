@file:Suppress("CompositionLocalNaming", "CompositionLocalNaming")

package sc.windom.potter.producer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Base64
import android.util.Size
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Token
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.InputDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBottomMenuButtonClickListener
import com.tencent.bugly.crashreport.BuglyLog
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import mobile.Mobile
import org.b3log.siyuan.App
import org.b3log.siyuan.R
import org.b3log.ld246.HomeActivity
import org.b3log.siyuan.services.BootService
import sc.windom.sofill.S
import sc.windom.sofill.U
import sc.windom.sofill.Us.Toast
import sc.windom.sofill.Us.U_FileUtils.workspaceParentDir
import sc.windom.sofill.android.webview.WebPoolsPro
import sc.windom.sofill.android.webview.WebPoolsPro.Companion.instance
import sc.windom.sofill.api.MyRetrofit.createRetrofit
import sc.windom.sofill.api.siyuan.SiyuanNoteAPI
import sc.windom.sofill.api.siyuan.siyuan
import sc.windom.sofill.compose.ApkButtons
import sc.windom.sofill.compose.AudioButtons
import sc.windom.sofill.compose.LockScreenOrientation
import sc.windom.sofill.compose.MagnetButtons
import sc.windom.sofill.compose.SelectableHtmlText
import sc.windom.sofill.compose.SelectableText
import sc.windom.sofill.compose.VideoButtons
import sc.windom.sofill.compose.components.CommonTopAppBar
import sc.windom.sofill.compose.components.WaitUI
import sc.windom.sofill.compose.partialCom.DdMenuI
import sc.windom.sofill.compose.partialCom.NetworkAware
import sc.windom.sofill.compose.theme.CascadeMaterialTheme
import sc.windom.sofill.dataClass.ICreateDocWithMdRequest
import sc.windom.sofill.dataClass.INotebook
import sc.windom.sofill.dataClass.IPayload
import java.io.IOException
import java.util.Date
import java.util.Objects


// TODO: 多选文件打开的处理
// TODO: 文件覆盖提醒
// TODO: 缓存清理
// TODO: 如果是 workspaceParentDir 目录下的文件支持删除
// TODO: 文件被删除时处理异常
class MainPro : ComponentActivity() {
    val TAG = "producer/MainPro.kt"
    private var mmkv: MMKV = MMKV.defaultMMKV()
    private lateinit var thisActivity: Activity
    private var in2_intent: Intent? = null
    private var webView: WebView? = null
    private var created = mutableStateOf(false)
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        BuglyLog.i(TAG, "onNewIntent() invoked. @ $intent")
        init(intent)
//        setIntent(intent) // 更新当前 Intent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BuglyLog.i(TAG, "onCreate() invoked. @ $intent")
        setContent {
            CascadeMaterialTheme {
                WaitUI()
            }
        }
        if (!created.value) {
            init(intent)
        }
    }

    override fun onDestroy() {
        BuglyLog.w(TAG, "onDestroy() invoked")
        super.onDestroy()
        // 解绑服务
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }

    private var bootService: BootService? = null
    private var serviceBound = false

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as BootService.LocalBinder
            bootService = binder.getService()
            serviceBound = true
            App.bootService = bootService!!
            // 服务绑定后，执行依赖于bootService的代码
            performActionWithService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bootService = null
            serviceBound = false
        }
    }

    private fun init(intent: Intent?) {
        thisActivity = this
        in2_intent = intent
        if (intent == null) {
            return
        }
        val scheme = in2_intent?.data?.scheme
        val host = in2_intent?.data?.host
        BuglyLog.d(
            TAG,
            "scheme: $scheme, host: $host, action: ${in2_intent?.action}, type: ${in2_intent?.type}"
        )

        if (S.isUriMatched(in2_intent?.data, S.case_ld246_1) || S.isUriMatched(
                in2_intent?.data, S.case_ld246_2
            ) || S.isUriMatched(in2_intent?.data, S.case_github_1)
        ) {
            // 转发处理
            val homeIntent = Intent(this, HomeActivity::class.java)
            homeIntent.data = in2_intent?.data // 将URI数据传递给HomeActivity
            startActivity(homeIntent)
            finish() // 不需要返回MainPro，在这里结束它
        } else if (in2_intent?.data != null && in2_intent?.data?.scheme.isNullOrEmpty() || listOf(
                "http", "https", "siyuan"
            ).contains(
                in2_intent?.data?.scheme
            )
        ) {
            // 转发处理
            // 这里需要转发的思路是：此处判断作为兜底十有八九用户已经将汐洛设置为默认浏览器，转发给系统会导致死循环，系统认为没有应用能够处理，汐洛会闪退
            val homeIntent = Intent(this, HomeActivity::class.java)
            homeIntent.data = in2_intent?.data // 将URI数据传递给HomeActivity
            startActivity(homeIntent)
            finish() // 不需要返回MainPro，在这里结束它
        } else {
            // ...
        }

        // 设置沉浸式通知栏
        window.setDecorFitsSystemWindows(false)
        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            insets
        }

        created.value = true
        setContent {
            CascadeMaterialTheme {
                MyUI(TAG)
            }
        }

        if (bootService == null) {
            webView = Objects.requireNonNull<WebPoolsPro?>(instance).createWebView(
                this, "Sillot-MainPro"
            )
            // 绑定服务
            val intent = Intent(applicationContext, BootService::class.java)
            intent.putExtra(S.INTENT.EXTRA_WEB_VIEW_KEY, "Sillot-MainPro")
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        } else {
            performActionWithService()
        }
    }

    fun performActionWithService() {
        BuglyLog.w(TAG, "performActionWithService() invoked")
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
    @Composable
    fun MyUI(TAG: String) {
        val inspectionMode = LocalInspectionMode.current // 获取当前是否处于预览模式// 获取窗口尺寸
        val coroutineScope = rememberCoroutineScope()
        val targetIntent = rememberSaveable { mutableStateOf(in2_intent) }
        var head_title by rememberSaveable { mutableStateOf("汐洛中转站") }
        val token: MutableState<String?> = rememberSaveable {
            mutableStateOf(
                null
            )
        }
        val fileName: MutableState<String> = rememberSaveable {
            mutableStateOf("")
        }
        val fileSize: MutableState<String> = rememberSaveable {
            mutableStateOf("")
        }
        val mimeType: MutableState<String> = rememberSaveable {
            mutableStateOf("")
        }
        val fileType: MutableState<String> = rememberSaveable {
            mutableStateOf("")
        }
        val isLandscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）

        val isMenuVisible = rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(in2_intent) {
            targetIntent.value = in2_intent
        }

        LaunchedEffect(key1 = in2_intent) {
            head_title = if (in2_intent?.action == Intent.ACTION_SEND) {
                "汐洛受赏中转站"
            } else if (in2_intent?.data != null && fileSize.value.isNotEmpty() && mimeType.value.isNotEmpty()) {
                "汐洛文件中转站"
            } else if (!in2_intent?.data?.scheme.isNullOrEmpty() && in2_intent?.type.isNullOrEmpty()) {
                "汐洛链接中转站"
            } else {
                "汐洛中转站"
            }
            BuglyLog.i(TAG, "$head_title @ $in2_intent")
            token.value = U.getDecryptedToken(
                mmkv, S.KEY_TOKEN_Sillot_Gibbet, S.KEY_AES_TOKEN_Sillot_Gibbet
            )
            when (in2_intent?.action) {
                Intent.ACTION_SEND -> {
                    // 直接拖拽到 MainPro 就是 ACTION_SEND
                    in2_intent?.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
                        // 处理文本内容，这个下面已经有处理了
                    }

                    // 处理分享的文件
                    in2_intent?.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { sharedFileUri ->
                        // 处理文件，获取文件名、大小和MIME类型
                        fileName.value =
                            U.FileUtils.getFileName(thisActivity, sharedFileUri).toString()
                        fileSize.value = U.FileUtils.getFileSize(thisActivity, sharedFileUri)
                        mimeType.value =
                            U.FileUtils.getMimeType(thisActivity, sharedFileUri).toString()
                        fileType.value = U.FileUtils.getFileMIMEType(mimeType.value, fileName.value)
                            ?: U.FileUtils.getFileMIMEType(mimeType.value)
                    }
                }

                else -> {
                    // 处理其他类型的 intent
                    in2_intent?.data?.let {
                        fileName.value = U.FileUtils.getFileName(
                            thisActivity, it
                        ).toString()
                        fileSize.value = U.FileUtils.getFileSize(
                            thisActivity, it
                        )
                        mimeType.value = U.FileUtils.getMimeType(
                            thisActivity, it
                        ).toString()
                        fileType.value = fileName.value.let { it1 ->
                            U.FileUtils.getFileMIMEType(mimeType.value, it1)
                        }
                    } ?: {
                        mimeType.value = ""
                        fileType.value = U.FileUtils.getFileMIMEType(mimeType.value)
                    }
                }
            }

            BuglyLog.w(TAG, "mimeType: ${mimeType.value}, fileType: ${fileType.value}")
        }
        Scaffold(
            topBar = {
                CommonTopAppBar(
                    head_title,
                    TAG,
                    in2_intent?.data,
                    isMenuVisible,
                    additionalMenuItem = {
                        AddDropdownMenu(token, onDismiss = {
                            isMenuVisible.value = false
                        })
                    }) {
                    // 将Context对象安全地转换为Activity
                    thisActivity.finish() // 结束活动
                }
            }, modifier = Modifier.background(Color.Gray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
            ) {
                NetworkAware()
                if (in2_intent?.action == Intent.ACTION_SEND && in2_intent?.type?.startsWith("text/") == true) {
                    val sharedText = in2_intent?.getStringExtra(Intent.EXTRA_TEXT) ?: return@Box
                    val html: String
                    // 判断文本类型并进行合法性校验
                    when (U.DOSC.checkContentFormat(sharedText)) {
                        "HTML" -> {
                            // 处理HTML文本
                            html = U.DOSC.processHtml(sharedText)
                        }

                        "Markdown" -> {
                            // 处理Markdown文本
                            html = U.DOSC.processMarkdown(sharedText)
                        }

                        else -> {
                            // 处理普通文本
                            html = U.DOSC.processPlainText(sharedText)
                        }
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            SelectableHtmlText(
                                html,
                                Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(10.dp)
                            )
                            SendBtnPart(sharedText, token)
                        }
                    }
                } else if (isLandscape) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (fileType.value.isNotBlank() && fileName.value.isNotBlank() && fileSize.value.isNotBlank()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(.4f)
                                    .fillMaxHeight()
                                    .padding(vertical = 16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                InfoPart(
                                    fileType = fileType, fileName = fileName, fileSize = fileSize
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(.6f)
                                    .fillMaxHeight()
//                            .height(IntrinsicSize.Min)
                                    .padding(vertical = 16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                BtnPart(
                                    mimeType = mimeType, fileName = fileName
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            InfoPart(
                                fileType = fileType, fileName = fileName, fileSize = fileSize
                            )
                            BtnPart(
                                mimeType = mimeType, fileName = fileName
                            )
                        }
                    }
                }
            }


        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun AddDropdownMenu(
        token: MutableState<String?>,
        onDismiss: () -> Unit,
    ) {
        DdMenuI(
            text = { Text("汐洛绞架内核 API TOKEN") },
            icon = { Icon(Icons.TwoTone.Token, contentDescription = null) },
            cb = {
                onDismiss()
                val deToken = U.getDecryptedToken(
                    mmkv, S.KEY_TOKEN_Sillot_Gibbet, S.KEY_AES_TOKEN_Sillot_Gibbet
                )
                InputDialog("🛸 API TOKEN",
                    "可在汐洛绞架 设置 - 关于 中找到 API Token，固定以 'token ' 开头\n\n温馨提示：应用存储 Token 时进行了一定的处理，且不会传输到网络，但用户仍需注意防止 Token 泄露！建议使用前先阅读源代码",
                    "确定",
                    "取消",
                    deToken?.let { deToken } ?: run { "token " }).setCancelable(false)
                    .setOkButton { baseDialog, v, inputStr ->
                        token.value = inputStr
                        // 生成AES密钥
                        val aesKey = U.generateAesKey()
                        // 注意：这里需要将SecretKey转换为可以存储的格式，例如转换为字节数组然后进行Base64编码
                        val encodedKey = Base64.encodeToString(aesKey.encoded, Base64.DEFAULT)
                        // 加密Token
                        val encryptedToken = U.encryptAes(inputStr, aesKey)
                        // 将加密后的Token存储到MMKV中
                        mmkv.encode(S.KEY_AES_TOKEN_Sillot_Gibbet, encodedKey)
                        mmkv.encode(S.KEY_TOKEN_Sillot_Gibbet, encryptedToken)
                        U.DialogX.PopNoteShow(
                            thisActivity, "TOKEN已更新（${
                                U.displayTokenLimiter(
                                    inputStr, "token ".length + 4, 4
                                )
                            }）"
                        ).noAutoDismiss()
                        false
                    }.show()
            },
        )
    }

    // 在协程中调用sendMD2siyuan
    fun runSendMD2siyuan(markdownContent: String, token: MutableState<String?>) =
        runBlocking<Unit> { // 启动主协程
            launch { // 启动一个新协程并运行挂起函数，避免阻塞UI
                sendMD2siyuan(markdownContent, token)
            }
        }

    fun sendMD2siyuan(markdownContent: String, token: MutableState<String?>) {
        val retrofit = createRetrofit("http://0.0.0.0:58131/")
        val api = retrofit.create(SiyuanNoteAPI::class.java)
        val helpInfo =
            "请注意：（1）TOKEN是否正确；（2）当前工作空间是否存在有效笔记本；（3）笔记本是否被关闭了"
        token.value?.let { _token ->
            siyuan.Works.getNotebooks(api, _token) { notebooks, info ->
                if (notebooks.isNullOrEmpty()) {
                    // 处理笔记本列表为空的情况
                    thisActivity.runOnUiThread {
                        U.DialogX.PopNoteShow(
                            thisActivity, TAG, "No notebooks received. reason:\n$info\n$helpInfo"
                        ).noAutoDismiss()
                    }
                } else {
                    // 处理获取到的笔记本列表
                    BuglyLog.i(TAG, "Received ${notebooks.size} notebooks.")
                    val notebookIDs: Array<String> = notebooks.map { it.id }.toTypedArray()
                    val notebookInfos: Array<String> = notebooks.map {
                        "（${if (it.closed) "不可用" else "可使用"}）${it.name}"
                    }.toTypedArray()
                    var selectMenuIndex = 0
                    BottomMenu.show(notebookInfos).setMessage("仅支持当前工作空间")
                        .setTitle("选择要存入的笔记本").setSelection(selectMenuIndex) //指定已选择的位置
                        .setOnMenuItemClickListener { dialog, text, index ->
                            selectMenuIndex = index
                            dialog.refreshUI() // 在 compose 里需要强制刷新
                            true // 点击菜单后不会自动关闭
                        }.setOkButton("确定", OnBottomMenuButtonClickListener { menu, view ->
                            val notebookId = notebookIDs[selectMenuIndex]
                            BuglyLog.e(TAG, notebookId)
                            val payload = IPayload(
                                markdownContent, notebookId, "/来自汐洛受赏 ${
                                    U.dateFormat_full1.format(
                                        Date()
                                    )
                                }"
                            )

                            siyuan.Works.createNote(api, payload, token) { success, info ->
                                if (success) {
                                    // 处理创建笔记成功的情况
                                    BuglyLog.i(TAG, "Note creation succeeded. $info")
                                } else {
                                    // 处理创建笔记失败的情况
                                    thisActivity.runOnUiThread {
                                        U.DialogX.PopNoteShow(
                                            thisActivity,
                                            TAG,
                                            "Note creation failed. reason:\n$info\n$helpInfo"
                                        ).noAutoDismiss()
                                    }
                                }
                            }
                            false
                        }).setCancelButton("取消", OnBottomMenuButtonClickListener { menu, view ->
                            false
                        })
                }
            }
        }
    }

    fun sendMD2siyuanWithoutToken(md: String) {
        val helpInfo =
            "请注意：（1）内核是否存活；（2）当前工作空间是否存在有效笔记本；（3）笔记本是否被关闭了"

        val response = Mobile.getNotebooks(false)
        if (response.error.isNotEmpty()) {
            BuglyLog.e(TAG, "Error: ${response.error}")
        } else {
            val notebooksJSON = response.notebooksJSON
            val notebooks =
                kotlinx.serialization.json.Json.decodeFromString<List<INotebook>>(notebooksJSON)
            if (notebooks.isEmpty()) {
                // 处理笔记本列表为空的情况
                thisActivity.runOnUiThread {
                    U.DialogX.PopNoteShow(
                        thisActivity, TAG, "No notebooks received. \n$helpInfo"
                    ).noAutoDismiss()
                }
            } else {
                // 处理获取到的笔记本列表
                BuglyLog.i(TAG, "Received ${notebooks.size} notebooks.")
                val notebookIDs: Array<String> = notebooks.map { it.id }.toTypedArray()
                val notebookInfos: Array<String> = notebooks.map {
                    "（${if (it.closed) "不可用" else "可使用"}）${it.name}"
                }.toTypedArray()
                var selectMenuIndex = 0
                BottomMenu.show(notebookInfos).setMessage("仅支持当前工作空间")
                    .setTitle("选择要存入的笔记本").setSelection(selectMenuIndex) //指定已选择的位置
                    .setOnMenuItemClickListener { dialog, text, index ->
                        selectMenuIndex = index
                        dialog.refreshUI() // 在 compose 里需要强制刷新
                        true // 点击菜单后不会自动关闭
                    }.setOkButton("确定", OnBottomMenuButtonClickListener { menu, view ->
                        menu.dismiss()

                        val notebookId = notebookIDs[selectMenuIndex]
                        BuglyLog.i(TAG, notebookId)

                        // 使用Handler来执行耗时操作
                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            // 在子线程中执行耗时操作
                            val payload = ICreateDocWithMdRequest(
                                Notebook = notebookId,
                                Path = "/来自汐洛受赏 ${U.dateFormat_full1.format(Date())}",
                                Markdown = md
                            )
                            val paramsJSON = kotlinx.serialization.json.Json.encodeToString(payload)
                            BuglyLog.i(TAG, paramsJSON)

                            val response2 = Mobile.createDocWithMd(paramsJSON)

                            // 切换回主线程更新UI
                            thisActivity.runOnUiThread {
                                if (response2.error.isEmpty()) {
                                    BuglyLog.i(TAG, "Created document with ID: ${response2.id}")
                                    U.startMainActivityWithBlock(
                                        "siyuan://blocks/${response2.id}",
                                        App.application
                                    )
                                } else {
                                    BuglyLog.e(TAG, "Error: ${response2.error}")
                                    U.DialogX.PopNoteShow(
                                        thisActivity,
                                        TAG,
                                        "Error: ${response2.error}\n$helpInfo"
                                    ).noAutoDismiss()
                                }
                            }
                        }
                        false
                    }).setCancelButton("取消", OnBottomMenuButtonClickListener { menu, view ->
                        false
                    })
            }
        }
    }

    @Composable
    fun SendBtnPart(markdown: String?, token: MutableState<String?>) {
        val isLandscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）
        Button(modifier = Modifier
            .width(S.C.Button_Width.current.dp)
            .padding(top = if (isLandscape) S.C.btn_PaddingTopH.current else S.C.btn_PaddingTopV.current),
            colors = ButtonDefaults.buttonColors(
                containerColor = S.C.btn_bgColor3.current, contentColor = S.C.btn_Color1.current
            ),
            enabled = true,
            onClick = {
                if (token.value.isNullOrEmpty()) {
                    U.DialogX.PopNoteShow(thisActivity, "TOKEN为空，请在右上角设置 TOKEN 后重试")
                        .noAutoDismiss()
                    return@Button
                }
                if (bootService == null) {
                    U.DialogX.PopNoteShow(
                        thisActivity, R.drawable.icon, "汐洛绞架内核尚未就绪", "请稍后再试"
                    ).noAutoDismiss()
                    return@Button
                }
                if (markdown != null) {
                    val directories =
                        U.FileUtils.getDirectoriesInPath(thisActivity.workspaceParentDir())
                    val filteredDirectories = directories.filter { it != "home" }
                    if (filteredDirectories.isNotEmpty()) {
                        runSendMD2siyuan(markdown, token)
                    } else {
                        U.DialogX.PopNoteShow(
                            thisActivity,
                            R.drawable.icon,
                            "未发现任何工作空间",
                            "请检查是否初始化了，或者路径存在异常 ${thisActivity.workspaceParentDir()}/"
                        ).noAutoDismiss()
                    }
                }
            }) {
            Text(
                text = S.C.btnText5.current,
                letterSpacing = S.C.btn_lspace.current,
                fontSize = if (isLandscape) S.C.btn_TextFontsizeH.current else S.C.btn_TextFontsizeV.current
            )
        }
        Button(modifier = Modifier
            .width(S.C.Button_Width.current.dp)
            .padding(top = if (isLandscape) S.C.btn_PaddingTopH.current else S.C.btn_PaddingTopV.current),
            colors = ButtonDefaults.buttonColors(
                containerColor = S.C.btn_bgColor3.current, contentColor = S.C.btn_Color1.current
            ),
            enabled = true,
            onClick = {
                if (bootService == null) {
                    U.DialogX.PopNoteShow(
                        thisActivity, R.drawable.icon, "汐洛绞架内核尚未就绪", "请稍后再试"
                    ).noAutoDismiss()
                    return@Button
                }
                if (markdown != null) {
                    val directories =
                        U.FileUtils.getDirectoriesInPath(thisActivity.workspaceParentDir())
                    val filteredDirectories = directories.filter { it != "home" }
                    if (filteredDirectories.isNotEmpty()) {
                        lifecycleScope.launch { // 使用 lifecycleScope 在生命周期内启动协程
                            sendMD2siyuanWithoutToken(markdown)
                        }
                    } else {
                        U.DialogX.PopNoteShow(
                            thisActivity,
                            R.drawable.icon,
                            "未发现任何工作空间",
                            "请检查是否初始化了，或者路径存在异常 ${thisActivity.workspaceParentDir()}/"
                        ).noAutoDismiss()
                    }
                }
            }) {
            Text(
                text = S.C.btnText6.current,
                letterSpacing = S.C.btn_lspace.current,
                fontSize = if (isLandscape) S.C.btn_TextFontsizeH.current else S.C.btn_TextFontsizeV.current
            )
        }
    }

    @Composable
    fun InfoPart(
        fileType: MutableState<String>,
        fileName: MutableState<String>,
        fileSize: MutableState<String>
    ) {
        val TAG = "${this.TAG}/InfoPart"
        val inspectionMode = LocalInspectionMode.current // 获取当前是否处于预览模式// 获取窗口尺寸
        val Thumbnail_Height = S.C.Thumbnail_Height.current
        val Thumbnail_Height_IMG = S.C.Thumbnail_Height_IMG.current
        var bitmap: Bitmap? by rememberSaveable {
            mutableStateOf(null)
        }
        val isLandscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）

        // 显示图像缩略图或文件类型图标
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                if (fileType.value.endsWith("图像")) {
                    try {
                        // 检查是否是 ACTION_SEND 并获取额外的文件 Uri
                        val sharedFileUri = if (in2_intent?.action == Intent.ACTION_SEND) {
                            in2_intent?.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                        } else {
                            in2_intent?.data
                        }

                        // 如果有文件 Uri，尝试加载缩略图
                        bitmap = sharedFileUri?.let { uri ->
                            thisActivity.contentResolver.loadThumbnail(
                                uri, Size(
                                    if (isLandscape) Thumbnail_Height else Thumbnail_Height_IMG,
                                    if (isLandscape) Thumbnail_Height else Thumbnail_Height_IMG
                                ), null
                            )
                        }
                    } catch (e: Exception) {
                        BuglyLog.e(TAG, "Error loading thumbnail: ${e.message}")
                    }
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Thumbnail",
                            modifier = Modifier
                                .size(Thumbnail_Height_IMG.dp)
                                .fillMaxSize()
                        )
                    }

                } else {
                    val icon = U.FileUtils.getIconForFileType(fileType.value)
                    Icon(
                        imageVector = icon,
                        contentDescription = "File Type Icon",
                        modifier = Modifier.size(Thumbnail_Height.dp)
                    )
                }

                fileName.value.let { it1 ->
                    SelectableText(
                        text = it1, // 使用获取到的文件名
                        style = TextStyle(
                            fontSize = if (isLandscape) 14.sp else 16.sp
                        ), modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "${fileSize.value} (${fileType.value})",
                        fontSize = if (isLandscape) 12.sp else 14.sp,
                        color = Color.Gray,
                        modifier = if (isLandscape) Modifier.padding(top = 6.dp) else Modifier.padding(
                            bottom = 6.dp
                        )
                    )
                }
            }
        }

    }

    @SuppressLint("Range")
    @Composable
    fun BtnPart(mimeType: MutableState<String>, fileName: MutableState<String>) {
        val TAG = "${this.TAG}/BtnPart"
        val inspectionMode = LocalInspectionMode.current // 获取当前是否处于预览模式// 获取窗口尺寸
        val coroutineScope = rememberCoroutineScope()

        var showSaveButton by rememberSaveable { mutableStateOf(false) }
        var showAudioButton by rememberSaveable { mutableStateOf(false) }
        var showVideoButton by rememberSaveable { mutableStateOf(false) }
        var showApkButton by rememberSaveable { mutableStateOf(false) }
        var showMagnetButton by rememberSaveable { mutableStateOf(false) }

        val isLandscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）

        var progressValue by rememberSaveable { mutableStateOf(0) }
        var isButton3OnClickRunning by rememberSaveable { mutableStateOf(false) }
        var isButton4OnClickRunning by rememberSaveable { mutableStateOf(false) }
        var workspaceAssetsDir by rememberSaveable { mutableStateOf("${thisActivity.workspaceParentDir()}/sillot/data/assets") }
        var uri_from_file by rememberSaveable { mutableStateOf(Uri.parse("")) }
        var uri_to_dir by rememberSaveable { mutableStateOf(Uri.parse("")) }
        var selectedFolder by rememberSaveable { mutableStateOf<Uri?>(null) }


        LaunchedEffect(key1 = fileName) {
            showSaveButton = in2_intent?.data != null
            showAudioButton = mimeType.value.startsWith("audio/")
            showVideoButton = mimeType.value.startsWith("video/")
            showApkButton =
                mimeType.value == "application/vnd.android.package-archive" || mimeType.value == "application/apk_1" || (mimeType.value == "application/octet-stream" && fileName.value.endsWith(
                    ".apk.1"
                ))
            if (in2_intent?.data != null) {
                when (in2_intent?.data?.scheme) {
                    "magnet" -> showMagnetButton = true
                }
            }
        }

        fun onCopyFileToFolderByDocumentTree() {
            // 启动一个协程来执行任务
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        if (!U.isStorageSpaceAvailable(
                                thisActivity.contentResolver, uri_from_file
                            )
                        ) {
                            // 存储空间不足，处理逻辑
                            Toast.Show(thisActivity, "存储空间不足，请先清理")
                            return@withContext
                        }
                        val sourceFilePath = U.FileUtils.getPathFromUri(thisActivity, uri_from_file)
                        // 复制文件到所选文件夹
                        fileName.value.let {
                            sourceFilePath?.let { it1 ->
                                U.FileUtils.copyFileToFolderByDocumentTree(
                                    thisActivity, uri_to_dir, it, it1, mimeType.value
                                )
                            }
                        }
                        withContext(Dispatchers.Main) {
                            Toast.Show(thisActivity, "已复制到指定文件夹")
                        }
                    } catch (e: Exception) {
                        BuglyLog.e(TAG, e.toString())
                        withContext(Dispatchers.Main) {
                            U.DialogX.PopNoteShow(thisActivity, "任务失败", e.toString())
                                .noAutoDismiss()
                        }
                    }
                    // 执行任务完成后，关闭遮罩
                    isButton3OnClickRunning = false
                }
            }
        }

        fun onCopyFileToMyAppFolder() {
            // 启动一个协程来执行任务
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        if (!U.isStorageSpaceAvailable(
                                thisActivity.contentResolver, uri_from_file
                            )
                        ) {
                            // 存储空间不足，处理逻辑
                            withContext(Dispatchers.Main) {
                                U.DialogX.PopNoteShow(
                                    thisActivity, R.drawable.icon, "存储空间不足，请先清理"
                                )
                            }
                            return@withContext
                        }
                        val sourceFilePath = U.FileUtils.getPathFromUri(thisActivity, uri_from_file)
                        // 复制文件到所选文件夹
                        fileName.value.let {
                            sourceFilePath?.let { it1 ->
                                try {
                                    U.FileUtils.copyFileToMyAppFolder(
                                        workspaceAssetsDir, it, it1
                                    )
                                    withContext(Dispatchers.Main) {
                                        U.DialogX.PopNoteShow(
                                            thisActivity,
                                            R.drawable.icon,
                                            "已存入 ${workspaceAssetsDir}"
                                        ).autoDismiss(5000)
                                    }
                                } catch (e: IOException) {
                                    BuglyLog.e(TAG, e.toString())
                                    withContext(Dispatchers.Main) {
                                        U.DialogX.PopNoteShow(
                                            thisActivity, R.drawable.icon, "任务失败", e.toString()
                                        ).noAutoDismiss()
                                    }
                                }

                            }
                        }
                    } catch (e: Exception) {
                        BuglyLog.e(TAG, e.toString())
                        withContext(Dispatchers.Main) {
                            U.DialogX.PopNoteShow(
                                thisActivity, R.drawable.icon, "任务失败", e.toString()
                            ).noAutoDismiss()
                        }
                    } finally {
                        // 执行任务完成后，关闭遮罩
                        isButton4OnClickRunning = false
                    }
                }
            }
        }

        val bt3TaskLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                BuglyLog.w(TAG, "BT3: $result")
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { _uri ->
                        isButton3OnClickRunning = true  // 没有设置 LaunchedEffect 但是需要显示遮罩
                        // 通过 SAF 获取持久性权限
                        thisActivity.contentResolver.takePersistableUriPermission(
                            _uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )

                        val sharedFileUri = if (in2_intent?.action == Intent.ACTION_SEND) {
                            in2_intent?.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                        } else {
                            in2_intent?.data
                        }
                        sharedFileUri?.let {
                            uri_from_file = U.FileUtils.getFileFromUri(
                                thisActivity, it
                            )?.toUri()
                            uri_to_dir = _uri
                            onCopyFileToFolderByDocumentTree()
                        } ?: {
                            BuglyLog.e(TAG, "无法获取 sharedFileUri")
                        }
                    }
                }
            }

        LaunchedEffect(key1 = isButton4OnClickRunning) {
            if (isButton4OnClickRunning) {
                try {
                    val sharedFileUri = if (in2_intent?.action == Intent.ACTION_SEND) {
                        in2_intent?.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    } else {
                        in2_intent?.data
                    }
                    sharedFileUri?.let {
                        uri_from_file = U.FileUtils.getFileFromUri(
                            thisActivity, it
                        )?.toUri()
                        uri_to_dir = Uri.parse(workspaceAssetsDir)
                        onCopyFileToMyAppFolder()
                    } ?: {
                        BuglyLog.e(TAG, "无法获取 sharedFileUri")
                    }
                } catch (e: Exception) {
                    BuglyLog.e(TAG, "Error when isButton4OnClickRunning: ${e.message}")
                }

            }
        }
        // 遮罩组件
        if (isButton4OnClickRunning || isButton3OnClickRunning) {
            // 锁定当前屏幕方向，避免重绘，相当于 android.app.Activity.setRequestedOrientation
            LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED)
            Dialog(
                onDismissRequest = {
                    isButton3OnClickRunning = false
                    isButton4OnClickRunning = false
                    selectedFolder = null
                }, properties = DialogProperties(
                    dismissOnBackPress = false, dismissOnClickOutside = false
                )
            ) {
                // 遮罩内容
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    if (false) {
                        LinearProgressIndicator(
                            progress = { progressValue / 100f },
                            modifier = Modifier
                                .padding(2.dp)
                                .height(13.dp)
                                .fillMaxWidth(),
                        )
                    } else {
                        // 不设置progress参数，显示不确定进度
                        LinearProgressIndicator(
                            modifier = Modifier
                                .padding(bottom = 58.dp)
                                .height(13.dp)
                                .fillMaxWidth(),
                        )
                        Text(
                            text = "操作正在进行……\n请勿退出",
                            color = Color.Yellow,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .align(Alignment.BottomCenter),
                            letterSpacing = S.C.btn_lspace.current,
                            fontSize = if (isLandscape) S.C.btn_TextFontsizeH.current else S.C.btn_TextFontsizeV.current
                        )
                    }
                }
            }
        }
        if (!isButton4OnClickRunning && !isButton3OnClickRunning) {
            // 解除屏幕方向锁定
            LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        }

        //// @D 通用按键部分

        if (showSaveButton) {
            // 保存到指定文件夹
            Button(modifier = Modifier
                .width(S.C.Button_Width.current.dp)
                .padding(top = if (isLandscape) S.C.btn_PaddingTopH.current else S.C.btn_PaddingTopV.current),
                colors = ButtonDefaults.buttonColors(
                    containerColor = S.C.btn_bgColor3.current, contentColor = S.C.btn_Color1.current
                ),
                enabled = true,
                onClick = {
                    val btn3_intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    bt3TaskLauncher.launch(btn3_intent)
                    // 非阻塞
                }) {
                Text(
                    text = S.C.btnText3.current,
                    letterSpacing = S.C.btn_lspace.current,
                    fontSize = if (isLandscape) S.C.btn_TextFontsizeH.current else S.C.btn_TextFontsizeV.current
                )
            }

            // 存入工作空间级资源目录
            Button(modifier = Modifier
                .width(S.C.Button_Width.current.dp)
                .padding(top = if (isLandscape) S.C.btn_PaddingTopH.current else S.C.btn_PaddingTopV.current),
                colors = ButtonDefaults.buttonColors(
                    containerColor = S.C.btn_bgColor4.current, contentColor = S.C.btn_Color1.current
                ),
                enabled = true,
                onClick = {
                    BuglyLog.d(TAG, thisActivity.workspaceParentDir())
                    val directories =
                        U.FileUtils.getDirectoriesInPath(thisActivity.workspaceParentDir())
                    val filteredDirectories = directories.filter { it != "home" }
                    if (filteredDirectories.isNotEmpty()) {
                        var selectMenuIndex = 0
                        var selectMenuText = "sillot"
                        BottomMenu.show(filteredDirectories).setMessage("sillot 是默认工作空间")
                            .setTitle("选择要存入的工作空间")
                            .setSelection(selectMenuIndex) //指定已选择的位置
                            .setOnMenuItemClickListener { dialog, text, index ->
                                selectMenuIndex = index
                                selectMenuText = text as String
                                dialog.refreshUI() // 在 compose 里需要强制刷新
                                true // 点击菜单后不会自动关闭
                            }
                            .setOkButton("确定", OnBottomMenuButtonClickListener { menu, view ->
                                BuglyLog.e(TAG, "${selectMenuText}")

                                workspaceAssetsDir =
                                    "${thisActivity.workspaceParentDir()}/${selectMenuText}/data/assets"
                                isButton4OnClickRunning = true // 值变化时会触发重组
                                false
                            }).setCancelButton(
                                "取消",
                                OnBottomMenuButtonClickListener { menu, view ->
                                    false
                                })
                    } else {
                        U.DialogX.PopNoteShow(
                            thisActivity,
                            R.drawable.icon,
                            "未发现任何工作空间",
                            "请检查是否初始化了，或者路径存在异常 ${thisActivity.workspaceParentDir()}/"
                        ).noAutoDismiss()
                    }
                }) {
                Text(
                    text = S.C.btnText4.current,
                    letterSpacing = S.C.btn_lspace.current,
                    fontSize = if (isLandscape) S.C.btn_TextFontsizeH.current else S.C.btn_TextFontsizeV.current
                )
            }
        }


        //// 类型按键区

        fun ApkBTNonClick1() {
            in2_intent?.data?.let {
                U.installApk2(
                    thisActivity, it
                )
            } ?: run {
                U.DialogX.PopNoteShow(thisActivity, "安装失败", "无法获取安装包 uri")
            }
        }

        fun ApkBTNonClick2() {
            in2_intent?.data?.let {
                U.installApk(
                    thisActivity, it
                )
            } ?: run {
                U.DialogX.PopNoteShow(thisActivity, "安装失败", "无法获取安装包 uri")
            }
        }

        fun MagnetBTNonClick1() {
            in2_intent?.data?.let {
                U.openUrl(it.toString(), true)
            }
        }
        if (inspectionMode || showAudioButton) {
            in2_intent?.data?.let { AudioButtons(it) }
        } else if (showVideoButton) {
            in2_intent?.data?.let { VideoButtons(it) }
        } else if (showApkButton) {
            ApkButtons(S.C.btnText5Apk1.current, ::ApkBTNonClick1)
            ApkButtons(S.C.btnText5Apk2.current, ::ApkBTNonClick2)
        } else if (showMagnetButton) {
            MagnetButtons(S.C.btnTextOpenByThirdParty.current, ::MagnetBTNonClick1)
        }

    }


    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        // 在预览环境中覆盖值。provides 提供明确值，这将覆盖组件嵌套中CompositionLocalProvider的值；providesDefault 提供默认值，
        // CompositionLocalProvider的作用域和它们在代码中的顺序决定了哪个providesDefault生效。
        CompositionLocalProvider(
            S.C.Thumbnail_Height provides 250,
            S.C.Button_Width providesDefault 300,
        ) {
            MyUI("Preview")
        }
    }

}
