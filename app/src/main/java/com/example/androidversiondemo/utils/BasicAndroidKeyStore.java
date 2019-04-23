package com.example.androidversiondemo.utils;

import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

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
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.security.auth.x500.X500Principal;

/*
KeyStore签名
 */
public class BasicAndroidKeyStore {

    public static final String KEYSTORE_PROVIDER_ANDROID_KEYSTORE = "AndroidKeyStore";

    public static final String TYPE_RSA = "RSA";
    public static final String TYPE_DSA = "DSA";
    public static final String TYPE_BKS = "BKS";

    public static final String SIGNATURE_SHA256withRSA = "SHA256withRSA";
    public static final String SIGNATURE_SHA512withRSA = "SHA512withRSA";

    public static final String TAG = "KeyStoreFragment";


    // 您可以在密钥存储库中存储多个密钥对。在本例中，
    // 用于引用要存储或稍后拉出的键的字符串被称为“alias”，
    // 因为在使用它检索键时，调用它作为键会令人不快
    private String mAlias = "myKeys";

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
                .getInstance(TYPE_RSA,
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
            spec = new KeyGenParameterSpec.Builder(mAlias, KeyProperties.PURPOSE_SIGN)
                    .setCertificateSubject(new X500Principal("CN=" + mAlias))
                    .setDigests(KeyProperties.DIGEST_SHA256)
                    .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                    .setCertificateSerialNumber(BigInteger.valueOf(1337))
                    .setCertificateNotBefore(start.getTime())
                    .setCertificateNotAfter(end.getTime())
                    .build();
        }

        kpGenerator.initialize(spec);

        KeyPair kp = kpGenerator.generateKeyPair();
        // END_INCLUDE(建立规格)
        LogUtils.e(this, "Public Key is: " + kp.getPublic().toString());
    }

    /**
     * 使用Android密钥存储库中存储的密钥对对数据进行签名。此签名可以与稍后的数据一起使用，以验证它是由该应用程序签名的。
     *
     * @return生成的数据签名的字符串编码
     */
    public String signData(String inputStr) throws KeyStoreException,
            UnrecoverableEntryException, NoSuchAlgorithmException, InvalidKeyException,
            SignatureException, IOException, CertificateException {
        byte[] data = inputStr.getBytes();

        // BEGIN_INCLUDE(信号加载密钥存储库)
        KeyStore ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID_KEYSTORE);

        // Java API的bug。如果没有要加载的InputStream，仍然需要调用“load”，否则它会崩溃。
        ks.load(null);

        // 从Android密钥存储库加载密钥对
        KeyStore.Entry entry = ks.getEntry(mAlias, null);

        /* 如果条目为空，则键从未存储在此别名下。在这种情况下的调试步骤是——
        通过遍历Keystore.aliases()检查别名列表，确保别名存在。-如果是空的，
        验证它们都存储在同一个密钥库中并从同一个密钥库“AndroidKeyStore”中提取
         */
        if (entry == null) {
            LogUtils.e(this, "别名下没有找到密钥: " + mAlias);
            LogUtils.e(this, "Exiting signData()...");
            return null;
        }

        /* 如果条目不是密钥存储库。PrivateKeyEntry可能存储在使
        用其他机制的应用程序的前viou迭代中，或者被使用相同别名的
        相同密钥存储库的其他东西覆盖。您可以使用entry.getClass()确定类型，然后从那里进行调试。
         */
        if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
            LogUtils.e(this, "不是私钥项的实例");
            LogUtils.e(this, "Exiting signData()...");
            return null;
        }
        // END_INCLUDE(信号数据)

        // BEGIN_INCLUDE(创建签名标志)
        // 这个类实际上并不表示签名，只是使用指定的算法创建/验证签名的引擎。
        Signature s = Signature.getInstance(SIGNATURE_SHA256withRSA);

        // 使用指定的私钥初始化签名
        s.initSign(((KeyStore.PrivateKeyEntry) entry).getPrivateKey());

        // 对数据签名，将结果存储为Base64编码的字符串。
        s.update(data);
        byte[] signature = s.sign();
        String result = Base64.encodeToString(signature, Base64.DEFAULT);
        // END_INCLUDE(信号数据)

        return result;
    }

    /**
     * 给定一些数据和签名，使用Android密钥存储库中存储的密钥对来验证数据是由该应用程序使用该密钥对签名的。
     *
     * @param input        需要验证的数据。
     * @param signatureStr 为数据提供的签名。
     * @return 一个布尔值，告诉您签名是否有效。
     */
    public boolean verifyData(String input, String signatureStr) throws KeyStoreException,
            CertificateException, NoSuchAlgorithmException, IOException,
            UnrecoverableEntryException, InvalidKeyException, SignatureException {
        byte[] data = input.getBytes();
        byte[] signature;
        // BEGIN_INCLUDE(decode_signature)

        // 确保签名字符串存在。如果没有，就退出，什么也做不了。

        if (signatureStr == null) {
            LogUtils.e(this, "无效的签名。");
            LogUtils.e(this, "Exiting verifyData()...");
            return false;
        }

        try {
            // 签名将被检查为一个字节数组，而不是base64编码的字符串。
            signature = Base64.decode(signatureStr, Base64.DEFAULT);
        } catch (IllegalArgumentException e) {
            // signatureStr不是null，但可能没有正确编码。它不是一个有效的Base64字符串。
            return false;
        }
        // END_INCLUDE(decode_signature)

        KeyStore ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID_KEYSTORE);

        // Java API的bug。如果没有要加载的InputStream，仍然需要调用“load”，否则它会崩溃
        ks.load(null);

        // 从Android密钥存储库加载密钥对
        KeyStore.Entry entry = ks.getEntry(mAlias, null);

        if (entry == null) {
            LogUtils.e(this, "别名下没有找到密钥: " + mAlias);
            LogUtils.e(this, "Exiting verifyData()...");
            return false;
        }

        if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
            LogUtils.e(this, "不是PrivateKeyEntry的实例");
            return false;
        }

        // 这个类实际上并不表示签名，只是使用指定的算法创建/验证签名的引擎。
        Signature s = Signature.getInstance(SIGNATURE_SHA256withRSA);

        //公钥私钥
/*        LogUtils.e(this,  ((KeyStore.PrivateKeyEntry) entry).getCertificate().getPublicKey().getAlgorithm()+"");
        LogUtils.e(this,  ((KeyStore.PrivateKeyEntry) entry).getPrivateKey().getAlgorithm()+"");*/
        // BEGIN_INCLUDE(verify_data)
        // 验证数据。
        s.initVerify(((KeyStore.PrivateKeyEntry) entry).getCertificate());
        s.update(data);
        return s.verify(signature);
        // END_INCLUDE(verify_data)
    }

    public void setAlias(String alias) {
        mAlias = alias;
    }

}
