/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/18 07:32
 * updated: 2024/8/18 07:32
 */

package sc.windom.sofill.compose

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest.Builder
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Deprecated("使用 NetworkViewModelFlow 代替")
class NetworkViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "NetworkViewModel"
    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val mainScope = MainScope()
    val isNetworkAvailable = mutableStateOf(false)

    init {
        Log.i(TAG, "init")
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
                        mainScope.launch {
                            isNetworkAvailable.value = true
                        }
                    }

                    override fun onLost(network: Network) {
                        mainScope.launch {
                            isNetworkAvailable.value = false
                        }
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
    override fun onCleared() {
        super.onCleared()
        // Cancel all coroutines when the ViewModel is cleared
        mainScope.cancel()
    }
}

/**
 * 适用于 compose
 */
class NetworkViewModelFlow(application: Application) : AndroidViewModel(application) {
    private val TAG = "NetworkViewModelFlow"
    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val isNetworkAvailable = MutableStateFlow(false)

    init {
        Log.i(TAG, "init")
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