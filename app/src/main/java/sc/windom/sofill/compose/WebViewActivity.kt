/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/8 上午9:32
 * updated: 2024/7/8 上午9:32
 */

package sc.windom.sofill.compose

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.tencent.bugly.crashreport.BuglyLog
import com.tencent.mmkv.MMKV
import sc.windom.sofill.Ss.S_Uri
import sc.windom.sofill.Us.U_DEBUG.srcPath
import sc.windom.sofill.compose.components.WaitUI
import sc.windom.sofill.compose.theme.CascadeMaterialTheme

class WebViewActivity: ComponentActivity() {
    private val TAG = "MainPro.kt"
    private val srcPath = srcPath(TAG)
    private var mmkv: MMKV = MMKV.defaultMMKV()
    private lateinit var thisActivity: Activity
    private var in2_intent: Intent? = null
    private var FullScreenWebView_url: MutableState<String?> = mutableStateOf(null)
    private var webView: WebView? = null
    private var created = mutableStateOf(false)
    override fun onSaveInstanceState(outState: Bundle) {
        BuglyLog.d(TAG, "outState: $outState")
        if (outState.isEmpty) return // avoid crash
        super.onSaveInstanceState(outState)
        // 可添加额外需要保存可序列化的数据
    }

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
    }

    private fun init(intent: Intent?) {
        thisActivity = this
        in2_intent = intent
        if (intent == null) {
            return
        }
        val uri = intent.data
        val scheme = in2_intent?.data?.scheme
        val host = in2_intent?.data?.host
        BuglyLog.d(
            TAG,
            "scheme: $scheme, host: $host, action: ${in2_intent?.action}, type: ${in2_intent?.type}"
        )
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

        // 设置沉浸式通知栏
        window.setDecorFitsSystemWindows(false)
        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            insets
        }

        created.value = true
        setContent {
            CascadeMaterialTheme {
                WebViewUI()
            }
        }
    }

    @Composable
    private fun WebViewUI() {
        val showFullScreenWebView = rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(FullScreenWebView_url.value) {
            if (!FullScreenWebView_url.value.isNullOrBlank()) {
                BuglyLog.d(TAG, "new FullScreenWebView_url -> ${FullScreenWebView_url.value}")
                showFullScreenWebView.value = true
            }
        }
        if (!FullScreenWebView_url.value.isNullOrBlank() && showFullScreenWebView.value) {
            FullScreenWebView(thisActivity, FullScreenWebView_url.value!!) {
                showFullScreenWebView.value = false
                thisActivity.finish()
            }
        }
    }
}