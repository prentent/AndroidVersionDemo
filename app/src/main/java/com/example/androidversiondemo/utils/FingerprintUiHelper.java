package com.example.androidversiondemo.utils;

import android.content.Context;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;

import javax.crypto.Cipher;

public class FingerprintUiHelper {
    private FingerprintManagerCompat fingerprintManager;
    private Callback callback;
    private CancellationSignal mCancellationSignal;

    public FingerprintUiHelper(Context context, Callback callback) {
        fingerprintManager = FingerprintManagerCompat.from(context);
        this.callback = callback;
    }

    public void stopListening() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    public void startListening(Cipher cipher) {
        if (mCancellationSignal == null) {
            mCancellationSignal = new CancellationSignal();
        }
        //crypto 这是一个加密类的对象，指纹扫描器会使用这个对象来判断认证结果的合法性。为空app无条件信任认证的结果
        //flags 0
        //cancel用来在指纹识别器扫描用户指纹的是时候取消当前的扫描操作
        //认证回调
        fingerprintManager.authenticate(new FingerprintManagerCompat.CryptoObject(cipher), 0,
                mCancellationSignal, new FingerprintManagerCompat.AuthenticationCallback() {
                    // 当出现错误的时候回调此函数，比如多次尝试都失败了的时候，errString是错误信息
                    @Override
                    public void onAuthenticationError(int errMsgId, CharSequence errString) {
                        super.onAuthenticationError(errMsgId, errString);
                        callback.onAuthenticationError(errMsgId, errString);
                    }

                    // 当指纹验证失败的时候会回调此函数，可以回复的异常
                    @Override
                    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                        super.onAuthenticationHelp(helpMsgId, helpString);
                        callback.onAuthenticationHelp(helpMsgId, helpString);
                    }

                    // 当验证的指纹成功时会回调此函数，然后不再监听指纹sensor
                    @Override
                    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        callback.onAuthenticationSucceeded(result);
                    }

                    // 当指纹验证失败的时候会回调此函数，失败之后允许多次尝试，失败次数过多会停止响应一段时间然后再停止sensor的工作
                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        callback.onAuthenticationFailed();
                    }
                }, null);

    }

    //isHardwareDetected确定指纹硬件是否存在和功能。hasEnrolledFingerprints确定是否至少有一个指纹登记。
    public boolean isFingerprintAuthAvailable() {
        return fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints();
    }

    public interface Callback {
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
