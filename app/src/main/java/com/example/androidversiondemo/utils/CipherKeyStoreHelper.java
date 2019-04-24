package com.example.androidversiondemo.utils;

import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

/*
bug
 */
public class CipherKeyStoreHelper {

    public static final String KEYSTORE_PROVIDER_ANDROID_KEYSTORE = "AndroidKeyStore";

    private static final String DEFAULT_PROVIDER = "AndroidKeyStore";
    private static final byte[] SECRET_BYTE_ARRAY = new byte[]{1, 2, 3, 4, 5, 6};

    private static final String TYPE_RSA = "RSA";

    // 您可以在密钥存储库中存储多个密钥对。在本例中，
    // 用于引用要存储或稍后拉出的键的字符串被称为“alias”，
    // 因为在使用它检索键时，调用它作为键会令人不快
    private String mAlias = "myKeys";
    private PublicKey aPublic;
    private PrivateKey aPrivate;

    /**
     * 创建一个公钥和私钥，并使用Android密钥存储库存储它，这样只有这个应用程序才能访问密钥。
     */
    public void createKeys(Context context) throws NoSuchProviderException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        // 开始包括(创建有效的日期)
        // 为将要生成的密钥对的有效性范围创建一个开始和结束时间。
        Calendar start = new GregorianCalendar();
        Calendar end = new GregorianCalendar();
        end.add(Calendar.YEAR, 1);
        //END_INCLUDE(创建有效的日期)

        // BEGIN_INCLUDE(创建密钥对)
        //使用预期的算法(在本例中是RSA和密钥存储库)初始化密钥对生成器。本例使用AndroidKeyStore。
        KeyPairGenerator kpGenerator = KeyPairGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_RSA,
                        KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
        // END_INCLUDE(创建密钥对)

        // BEGIN_INCLUDE(建立规格)
        // KeyPairGeneratorSpec对象是如何将密钥对的参数传递给KeyPairGenerator。
        AlgorithmParameterSpec spec = null;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // 在Android M下面，使用keypairgenerator . builder。
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                spec = new KeyPairGeneratorSpec.Builder(context)
                        // 稍后将使用别名检索密钥。这是一把钥匙的钥匙!
                        .setAlias(mAlias)
                        // 用于生成对的自签名证书的主题
                        .setSubject(new X500Principal("CN=" + mAlias))
                        // 用于生成对的自签名证书的序列号。
                        .setSerialNumber(BigInteger.valueOf(1337))
                        // 生成对的有效性的日期范围。
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();
            }

        } else {
            // 在Android M或更高版本上，使用KeyGenparameterSpec。生成器并指定密钥的允许属性和限制。
            spec = new KeyGenParameterSpec.Builder(mAlias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setCertificateSubject(new X500Principal("CN=" + mAlias))
                    .setDigests(KeyProperties.DIGEST_SHA256)
//                    .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
//                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .setCertificateSerialNumber(BigInteger.valueOf(1337))
                    .setCertificateNotBefore(start.getTime())
                    .setCertificateNotAfter(end.getTime())
                    .build();
        }
        kpGenerator.initialize(spec);
        KeyPair kp = kpGenerator.generateKeyPair();
        aPublic = kp.getPublic();
        aPrivate = kp.getPrivate();
        // END_INCLUDE(建立规格)
        LogUtils.e(context, "Public Key is: " + kp.getPrivate().toString());
    }

    /**
     * Signature 加密
     * 使用Android密钥存储库中存储的密钥对对数据进行签名。此签名可以与稍后的数据一起使用，以验证它是由该应用程序签名的。
     *
     * @return生成的数据签名的字符串编码
     */
    public byte[] encryptData(byte[] inputBytes) throws KeyStoreException,
            NoSuchAlgorithmException, InvalidKeyException,
            IOException, CertificateException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, UnrecoverableEntryException {

        KeyStore ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
        ks.load(null);


//
//        Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_RSA + "/" +
//                KeyProperties.BLOCK_MODE_ECB + "/" + KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1);
        Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_RSA);
        cipher.init(Cipher.ENCRYPT_MODE, aPublic);
        byte[] bytes = cipher.doFinal(inputBytes);
        return bytes;
    }

    /**
     * Signature 验证
     * 给定一些数据和签名，使用Android密钥存储库中存储的密钥对来验证数据是由该应用程序使用该密钥对签名的。
     *
     * @return 一个布尔值，告诉您签名是否有效。
     */
    public byte[] decryptData(byte[] inputBytes) throws KeyStoreException,
            CertificateException, NoSuchAlgorithmException, IOException,
            UnrecoverableEntryException, InvalidKeyException, SignatureException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {

        KeyStore ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
        ks.load(null);

        Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_RSA);
        cipher.init(Cipher.DECRYPT_MODE, aPublic);
        byte[] bytes = cipher.doFinal(inputBytes);
        return bytes;
    }
}
