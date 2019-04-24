package com.example.androidversiondemo;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class ConfirmedActivity extends AppCompatActivity {

    private KeyguardManager manager;
    private View mLayout;
    private static final String KEY_NAME = "my_key";
    private static final byte[] SECRET_BYTE_ARRAY = new byte[]{1, 2, 3, 4, 5, 6};

    private static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 1;
    private static final int AUTHENTICATION_DURATION_SECONDS = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmed);
        mLayout = findViewById(R.id.sample_layout_fragment);
        manager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (!manager.isKeyguardSecure()) {
            Snackbar.make(mLayout, "没有锁屏", Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                tryEncrypt();
            }
        });
        createKey();
    }

    private void createKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setUserAuthenticationRequired(true)
                        // Require that the user has unlocked in the last 30 seconds
                        .setUserAuthenticationValidityDurationSeconds(AUTHENTICATION_DURATION_SECONDS)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build());
            }
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | NoSuchProviderException
                | InvalidAlgorithmParameterException | KeyStoreException
                | CertificateException | IOException e) {
            throw new RuntimeException("Failed to create a symmetric key", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean tryEncrypt() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_NAME, null);
            Cipher cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            cipher.doFinal(SECRET_BYTE_ARRAY);

            Snackbar.make(mLayout, "已经验证了", Snackbar.LENGTH_SHORT)
                    .show();
            return true;
        } catch (UserNotAuthenticatedException e) {
            // 用户未经过身份验证，让我们使用设备凭据进行身份验证。
            showAuthenticationScreen();
            return false;
        } catch (KeyPermanentlyInvalidatedException e) {
            Toast.makeText(this, "密钥创建后无效"
                            + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            return false;
        } catch (KeyStoreException |
                CertificateException | UnrecoverableKeyException | IOException
                | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showAuthenticationScreen() {
        //创建确认凭据屏幕。您可以自定义标题和描述。或
        //如果您让它为空，我们将为您提供一个通用的
        Intent intent = manager.createConfirmDeviceCredentialIntent(null, null);
        if (intent != null) {
            startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            if (resultCode == RESULT_OK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tryEncrypt();
                }
            } else {
                // 用户取消或未完成锁定屏幕操作。进入错误/取消流程。
                Snackbar.make(mLayout, "身份验证失败", Snackbar.LENGTH_SHORT)
                        .show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
