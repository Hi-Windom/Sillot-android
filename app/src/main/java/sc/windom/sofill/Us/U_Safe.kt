/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/8 下午12:07
 * updated: 2024/7/8 下午12:07
 */

package sc.windom.sofill.Us

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import sc.windom.sofill.android.BiometricCallback
import sc.windom.sofill.android.newBiometricPrompt

object U_Safe {
    private val TAG = "sc.windom.sofill.Us.U_Safe"
    fun checkBiometric(
        activity: AppCompatActivity,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        onError: (CharSequence, state: Int) -> Unit
    ) {
        Log.d(TAG, "showBiometricPrompt() invoked")
        val mainHandler = Handler(Looper.getMainLooper())
        // 在主线程中执行
        mainHandler.post {
            // 在 MainActivity 中调用 showBiometricPrompt 方法
            try {
                newBiometricPrompt(
                    activity,
                    "指纹解锁",
                    "",
                    "取消",
                    object : BiometricCallback {
                        override fun onAuthenticationSuccess() {
                            // 认证成功的处理逻辑
                            TipDialog.show("Success!", WaitDialog.TYPE.SUCCESS, 200)
                            onSuccess.invoke()
                        }

                        override fun onAuthenticationFailed() {
                            // 认证失败的处理逻辑
                            TipDialog.show("指纹不匹配!", WaitDialog.TYPE.ERROR)
                            onFailure.invoke()
                        }

                        override fun onAuthenticationError(errString: CharSequence) {
                            // 认证错误的处理逻辑（一般是用户点击了取消）
                            val biometricManager =
                                BiometricManager.from(activity)
                            val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
//                            PopTip.show("用户取消了指纹解锁")
                            onError.invoke(errString, canAuthenticate)
                        }
                    })
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}