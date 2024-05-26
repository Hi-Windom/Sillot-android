package org.b3log.siyuan.compose

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.net.NetworkRequest.Builder


class NetworkViewModel(application: Application) : AndroidViewModel(application) {
    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isNetworkAvailable = mutableStateOf(false)

    init {
        // 初始网络状态检测
        isNetworkAvailable.value = isOnline(connectivityManager)

        // 监听网络变化
        viewModelScope.launch {
            connectivityManager.registerNetworkCallback(
                Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build(),
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        isNetworkAvailable.value = true
                    }

                    override fun onLost(network: Network) {
                        isNetworkAvailable.value = false
                    }
                }
            )
        }
    }

    private fun isOnline(connectivityManager: ConnectivityManager): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}