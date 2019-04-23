package com.example.androidversiondemo.presenter;

import android.app.KeyguardManager;
import android.content.Context;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.example.androidversiondemo.utils.CipherHelper;
import com.example.androidversiondemo.utils.FingerprintUiHelper;

/*
指纹识别
 */
public class FingerprintPresenter {

    private final KeyguardManager keyguardManager;
    private final FingerprintUiHelper fingerprintUiHelper;
    private final CipherHelper cipherHelper;

    public FingerprintPresenter(Context context, CallbackPressenter callback) {
        //获取锁定和解锁键盘锁的类。
        keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        cipherHelper = new CipherHelper();
        fingerprintUiHelper = new FingerprintUiHelper(context, new FingerprintUiHelper.Callback() {
            // 当出现错误的时候回调此函数，比如多次尝试都失败了的时候，errString是错误信息
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                callback.onAuthenticationError(errMsgId, errString);
            }

            // 当指纹验证失败的时候会回调此函数，可以回复的异常
            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                callback.onAuthenticationHelp(helpMsgId, helpString);
            }

            // 当验证的指纹成功时会回调此函数，然后不再监听指纹sensor
            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                callback.onAuthenticationSucceeded(result);
            }

            // 当指纹验证失败的时候会回调此函数，失败之后允许多次尝试，失败次数过多会停止响应一段时间然后再停止sensor的工作
            @Override
            public void onAuthenticationFailed() {
                callback.onAuthenticationFailed();
            }
        });
    }

    public boolean initCipher() {
        return cipherHelper.initCipher();
    }

    public boolean isFingerprintAuthAvailable() {
        return fingerprintUiHelper.isFingerprintAuthAvailable();
    }

    public boolean isKeyguardSecure() {
        if (keyguardManager == null) return false;
        return keyguardManager.isKeyguardSecure();
    }

    public void startListening() {
        fingerprintUiHelper.startListening(cipherHelper.getCipher());
    }

    public void stopListening() {
        fingerprintUiHelper.stopListening();
    }

    public interface CallbackPressenter {
        // 当出现错误的时候回调此函数，比如多次尝试都失败了的时候，errString是错误信息
        void onAuthenticationError(int errMsgId, CharSequence errString);

        // 当指纹验证失败的时候会回调此函数，可以回复的异常
        void onAuthenticationHelp(int helpMsgId, CharSequence helpString);

        // 当验证的指纹成功时会回调此函数，然后不再监听指纹sensor
        void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result);

        // 当指纹验证失败的时候会回调此函数，失败之后允许多次尝试，失败次数过多会停止响应一段时间然后再停止sensor的工作
        void onAuthenticationFailed();
    }

}
