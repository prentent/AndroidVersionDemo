package com.example.androidversiondemo;

import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.design.widget.Snackbar;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.androidversiondemo.utils.CipherHelper;
import com.example.androidversiondemo.utils.FingerprintUiHelper;
import com.example.androidversiondemo.utils.LogUtils;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class FingerprintManagerActivity extends AppCompatActivity {

    private TextView test_fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint_manager);
        View mLayout = findViewById(R.id.sample_main_fragment);
        test_fm = (TextView) findViewById(R.id.test_fm);
        Log.e("=====","dsada");
        CipherHelper cipherHelper = new CipherHelper();
        FingerprintUiHelper fingerprintUiHelper = new FingerprintUiHelper(this, new FingerprintUiHelper.Callback() {
            // 当出现错误的时候回调此函数，比如多次尝试都失败了的时候，errString是错误信息
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                test_fm.setText("onAuthenticationError--" + errString);
                LogUtils.e(this, "onAuthenticationError--" + errString);
            }

            // 当指纹验证失败的时候会回调此函数，可以回复的异常
            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                test_fm.setText("onAuthenticationHelp--" + helpString);
                LogUtils.e(this, "onAuthenticationHelp--" + helpString);
            }

            // 当验证的指纹成功时会回调此函数，然后不再监听指纹sensor
            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                test_fm.setText("认证成功");
            }

            // 当指纹验证失败的时候会回调此函数，失败之后允许多次尝试，失败次数过多会停止响应一段时间然后再停止sensor的工作
            @Override
            public void onAuthenticationFailed() {
                test_fm.setText("认证失败");
            }
        });

        findViewById(R.id.btn_qxfm).setOnClickListener(v -> fingerprintUiHelper.stopListening());
        findViewById(R.id.btn_fm).setOnClickListener(v -> {
            if (cipherHelper.initCipher()) {
                Snackbar.make(mLayout, "打开指纹", Snackbar.LENGTH_SHORT).show();
                fingerprintUiHelper.startListening(cipherHelper.getCipher());
            } else {
                Snackbar.make(mLayout, "未能打开指纹", Snackbar.LENGTH_SHORT).show();
            }
        });


        //获取锁定和解锁键盘锁的类。
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        //检测是否设置了指纹锁。(最小版本支持16)
        if (!keyguardManager.isKeyguardSecure()) {
            Snackbar.make(mLayout, "安全锁屏还没有设置指纹", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (!fingerprintUiHelper.isFingerprintAuthAvailable()) {
            Snackbar.make(mLayout, "进入“设置->安全->指纹”，并注册至少一个指纹", Snackbar.LENGTH_SHORT).show();
            return;
        }
    }
}