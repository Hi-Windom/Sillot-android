/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/8 上午11:52
 * updated: 2024/7/8 上午11:52
 */

package sc.windom.sofill.android

import android.os.Handler
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import org.json.JSONException
import java.util.concurrent.Executor
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

@JvmStatic
fun newBiometricPrompt(
    activity: FragmentActivity,
    title: String?,
    subtitle: String?,
    negativeButtonText: String?,
    callback: BiometricCallback
) {
    val semaphore = Semaphore(0)
    val handlerBiometric = Handler()
    val executorBiometric = Executor { command ->
        handlerBiometric.post(command)
    }

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title ?: "Biometric login for my app")
        .setSubtitle(subtitle ?: "Log in using your biometric credential")
        .setNegativeButtonText(negativeButtonText ?: "Cancel")
        .build()

    val biometricPrompt = BiometricPrompt(activity, executorBiometric, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            callback.onAuthenticationError(errString)
            semaphore.release() // 出现错误时释放锁
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            // 认证成功的逻辑处理可以在这里进行
            try {
                callback.onAuthenticationSuccess()
            } catch (e: JSONException) {
                throw RuntimeException(e)
            }
            semaphore.release() // 认证成功时释放锁
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            callback.onAuthenticationFailed()
            semaphore.release() // 认证失败时释放锁
        }
    })

    biometricPrompt.authenticate(promptInfo)

    // 等待认证结果
    try {
        semaphore.tryAcquire(0, TimeUnit.SECONDS)
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
}

// 定义BiometricCallback接口
interface BiometricCallback {
    fun onAuthenticationSuccess()
    fun onAuthenticationFailed()
    fun onAuthenticationError(errString: CharSequence)
}