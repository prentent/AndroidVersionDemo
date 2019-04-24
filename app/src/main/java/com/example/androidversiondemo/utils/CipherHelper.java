package com.example.androidversiondemo.utils;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/*
 Cipher设置类
 */
public class CipherHelper {

    private static final String KEY_NAME_NOT_INVALIDATED = "key_not_invalidated";
    private static final String DEFAULT_KEY_NAME = "default_key";
    private static final String DEFAULT_PROVIDER = "AndroidKeyStore";

    private KeyStore keyStore;
    private Cipher cipher;


    public CipherHelper() {
        try {
            initKeyStore();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0
                cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" +
                        KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
                //创建密钥
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                    createKey(KEY_NAME_NOT_INVALIDATED, false);
                } else {
                    createKey(DEFAULT_KEY_NAME, true);
                }
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    //初始化KeyStore
    private void initKeyStore() {
        try {
            keyStore = KeyStore.getInstance(DEFAULT_PROVIDER);
            keyStore.load(null);
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
            e.printStackTrace();
        }
    }

    ///keyName 要创建的密钥的名称
    ///invalidatedByBiometricEnrollment 如果传递了false，即使注册了新的指纹，创建的密钥也不会失效。
    private void createKey(String keyName, boolean invalidatedByBiometricEnrollment) {
        try {
            keyStore.load(null);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, DEFAULT_PROVIDER);
                KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyName, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        // 要求用户使用指纹进行身份验证，以授权每次使用密钥
                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);
              /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);
                }
*/
                keyGenerator.init(builder.build());
                keyGenerator.generateKey();
            }
        } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException |
                CertificateException | IOException e) {
            e.printStackTrace();
        }
    }

    public boolean initCipher() {
        try {
            keyStore.load(null);
            SecretKey key = null;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                key = (SecretKey) keyStore.getKey(KEY_NAME_NOT_INVALIDATED, null);
            } else {
                key = (SecretKey) keyStore.getKey(DEFAULT_KEY_NAME, null);
            }
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Cipher getCipher() {
        return cipher;
    }
}
