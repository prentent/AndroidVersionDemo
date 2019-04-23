package com.example.androidversiondemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.androidversiondemo.utils.BasicAndroidKeyStore;
import com.example.androidversiondemo.utils.LogUtils;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    private View mLayout;
    private BasicAndroidKeyStore keyStore;
    public static final String SAMPLE_INPUT = "Hello, lh";
    private String hello_word;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        keyStore = new BasicAndroidKeyStore();
        keyStore.setAlias("mykey");

        try {
            keyStore.createKeys(this);
        } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fm:
                startActivity(new Intent(this, FingerprintManagerActivity.class));
                break;
            case R.id.sign:
                try {
                    hello_word = keyStore.signData(SAMPLE_INPUT);
                    LogUtils.e(this, hello_word);
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (UnrecoverableEntryException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (SignatureException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (CertificateException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.verify:
                try {
                    boolean verifyData = keyStore.verifyData(SAMPLE_INPUT, hello_word);
                    LogUtils.e(this, verifyData + "");
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (CertificateException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UnrecoverableEntryException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (SignatureException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.all:
                try {
                    KeyStore androidKeyStore = KeyStore.getInstance("AndroidKeyStore");
                    androidKeyStore.load(null);
                    Enumeration<String> aliases = androidKeyStore.aliases();
                    LogUtils.e(this,aliases+"");
                    while (aliases.hasMoreElements()) {
                        LogUtils.e(this, aliases.nextElement());
                    }
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (CertificateException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
