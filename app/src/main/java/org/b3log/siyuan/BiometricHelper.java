package org.b3log.siyuan;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONException;

import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class BiometricHelper {

    public static void showBiometricPrompt(FragmentActivity activity, String title, String subtitle, String negativeButtonText, BiometricCallback callback) {
        Semaphore semaphore = new Semaphore(0);
        Handler handlerBiometric = new Handler();
        Executor executorBiometric = new Executor() {
            @Override
            public void execute(Runnable command) {
                handlerBiometric.post(command);
            }
        };

        BiometricPrompt.PromptInfo promptInfo =
                new BiometricPrompt.PromptInfo.Builder()
                        .setTitle(title != null ? title : "Biometric login for my app")
                        .setSubtitle(subtitle != null ? subtitle : "Log in using your biometric credential")
                        .setNegativeButtonText(negativeButtonText != null ? negativeButtonText : "Cancel")
                        .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(activity,
                executorBiometric, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                callback.onAuthenticationError(errString);
                semaphore.release(); // 出现错误时释放锁
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // 认证成功的逻辑处理可以在这里进行
                try {
                    callback.onAuthenticationSuccess();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                semaphore.release(); // 认证成功时释放锁
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                callback.onAuthenticationFailed();
                semaphore.release(); // 认证失败时释放锁
            }
        });

        biometricPrompt.authenticate(promptInfo);

        // 等待认证结果
        try {
            semaphore.tryAcquire(0, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 定义BiometricCallback接口
    public interface BiometricCallback {
        void onAuthenticationSuccess() throws JSONException;
        void onAuthenticationFailed();
        void onAuthenticationError(CharSequence errString);
    }
}
