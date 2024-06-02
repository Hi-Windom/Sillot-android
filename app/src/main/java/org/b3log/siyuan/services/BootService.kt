package org.b3log.siyuan.services

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import android.webkit.ValueCallback
import com.koushikdutta.async.AsyncServer
import com.koushikdutta.async.http.server.AsyncHttpServer
import com.koushikdutta.async.http.server.AsyncHttpServerRequest
import com.koushikdutta.async.http.server.AsyncHttpServerResponse
import com.koushikdutta.async.util.Charsets
import mobile.Mobile
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.DirectoryFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter
import org.b3log.siyuan.App
import org.b3log.siyuan.Utils
import org.json.JSONArray
import org.json.JSONObject
import sc.windom.sofill.S
import sc.windom.sofill.U
import sc.windom.sofill.android.webview.WebViewPool
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.util.Locale
import java.util.TimeZone
import java.util.function.Consumer

class BootService : Service() {
    private val TAG = "BootService-SiYuan"
    var server: AsyncHttpServer? = null
    var serverPort = S.DefaultHTTPPort
    val webView by lazy { WebViewPool.getInstance().getWebView(this) }
    var webViewVer: String? = null
    var userAgent: String? = null
    var kernelStarted = false
    private val uploadMessage: ValueCallback<Array<Uri>>? = null
    override fun onCreate() {
        super.onCreate()
        // 初始化 UI 元素
        Log.w(TAG, "onStart() -> initUIElements() invoked")
        init_webView()

        // 拉起内核
        Log.w(TAG, "onStart() -> startKernel() invoked")
        startKernel()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Call the method to start the kernel or other necessary tasks
        startKernel()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the server and release resources if necessary...
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    inner class LocalBinder : Binder() {
        fun getService(): BootService = this@BootService
    }

    private fun init_webView() {
        webView.setBackgroundColor(Color.parseColor(S.ColorStringHex.bgColor_light))
        val ws = webView.getSettings()
        userAgent = ws.userAgentString
        webViewVer = U.checkWebViewVer(ws)
    }

    private val bootHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val cmd = msg.getData().getString("cmd")
            if ("startKernel" == cmd) {
                bootKernel(true)
            } else {
                Log.w(TAG, cmd.toString())
            }
        }
    }

    private fun startHttpServer(isServable: Boolean) {
        if (null != server) {
            server?.stop()
            Log.w(TAG, "startHttpServer() stop exist server")
        }
        try {
            // 解决乱码问题 https://github.com/koush/AndroidAsync/issues/656#issuecomment-523325452
            val charsetClass = Charsets::class.java
            val usAscii = charsetClass.getDeclaredField("US_ASCII")
            usAscii.isAccessible = true
            usAscii[Charsets::class.java] = Charsets.UTF_8
        } catch (e: Exception) {
            Utils.LogError("http", "init charset failed", e)
        }
        server = AsyncHttpServer()
        server?.post("/api/walkDir") { request: AsyncHttpServerRequest, response: AsyncHttpServerResponse ->
            try {
                val start = System.currentTimeMillis()
                val requestJSON = request.body.get() as JSONObject
                val dir = requestJSON.optString("dir")
                val data = JSONObject()
                val files = JSONArray()
                FileUtils.listFilesAndDirs(
                    File(dir),
                    TrueFileFilter.INSTANCE,
                    DirectoryFileFilter.DIRECTORY
                ).forEach(
                    Consumer { file: File ->
                        val path = file.absolutePath
                        val info = JSONObject()
                        try {
                            info.put("path", path)
                            info.put("name", file.getName())
                            info.put("size", file.length())
                            info.put("updated", file.lastModified())
                            info.put("isDir", file.isDirectory())
                        } catch (e: Exception) {
                            Utils.LogError("http", "walk dir failed", e)
                        }
                        files.put(info)
                    })
                data.put("files", files)
                val responseJSON = JSONObject().put("code", 0).put("msg", "").put("data", data)
                response.send(responseJSON)
                Utils.LogInfo(
                    "http",
                    "walk dir [" + dir + "] in [" + (System.currentTimeMillis() - start) + "] ms"
                )
            } catch (e: Exception) {
                Utils.LogError("http", "walk dir failed", e)
                try {
                    response.send(JSONObject().put("code", -1).put("msg", e.message))
                } catch (e2: Exception) {
                    Utils.LogError("http", "walk dir failed", e2)
                }
            }
        }
        serverPort = getAvailablePort()
        val s = AsyncServer.getDefault()
        if (isServable) {
            // 绑定所有网卡以便通过局域网IP访问
            s.listen(null, serverPort, server!!.listenCallback)
            Log.w(TAG, "startHttpServer() -> HTTP server is listening on all interfaces, port [$serverPort]")
        } else {
            // 绑定 ipv6 回环地址 [::1] 以防止被远程访问
            s.listen(InetAddress.getLoopbackAddress(), serverPort, server!!.listenCallback)
            Log.w(TAG, "startHttpServer() -> HTTP server is listening on loopback address, port [$serverPort]")
        }
        Log.w(TAG, "startHttpServer() -> HTTP server is listening on port [$serverPort]")
        Utils.LogInfo("http", "HTTP server is listening on port [$serverPort]")
    }

    private fun getAvailablePort(): Int {
        var ret = S.DefaultHTTPPort
        try {
            ServerSocket(0).use { socket ->
                ret = socket.localPort
            }
        } catch (e: Exception) {
            Utils.LogError("http", "get available port failed", e)
        }
        return ret
    }

    private fun startKernel() {
        if (kernelStarted) {
            return
        }
        synchronized(this) {
            if (kernelStarted) {
                return
            }

            kernelStarted = true
            val b = Bundle()
            b.putString("cmd", "startKernel")
            val msg = Message()
            msg.data = b
            bootHandler.sendMessage(msg)
        }
    }

    private fun bootKernel(isServable: Boolean) {
        Mobile.setHttpServerPort(serverPort.toLong())
        if (Mobile.isHttpServing()) {
            Utils.LogInfo("boot", "kernel HTTP server is running")
            Log.w(TAG, "showBootIndex();")
            return
        }
        startHttpServer(isServable)
        val appDir = filesDir.absolutePath + "/app"
        // As of API 24 (Nougat) and later 获取用户的设备首选语言
        val locales = resources.configuration.getLocales()
        val locale = locales[0]
        val workspaceBaseDir = getExternalFilesDir(null)!!.absolutePath
        val timezone = TimeZone.getDefault().id
        Thread {
            val localIPs = Utils.getIPAddressList()
            var lang = locale.language + "_" + locale.country
            lang = if (lang.lowercase(Locale.getDefault()).contains("cn")) {
                "zh_CN"
            } else if (lang.lowercase(Locale.getDefault()).contains("es")) {
                "es_ES"
            } else if (lang.lowercase(Locale.getDefault()).contains("fr")) {
                "fr_FR"
            } else {
                "en_US"
            }
            Mobile.startKernel(
                "android", appDir, workspaceBaseDir, timezone, localIPs, lang,
                Build.VERSION.RELEASE +
                        "/SDK " + Build.VERSION.SDK_INT +
                        "/WebView " + webViewVer +
                        "/Manufacturer " + Build.MANUFACTURER +
                        "/Brand " + Build.BRAND +
                        "/UA " + userAgent
            )
        }.start()
        val b = Bundle()
        b.putString("cmd", "bootIndex")
        val msg = Message()
        msg.data = b
        bootHandler.sendMessage(msg)
    }
}