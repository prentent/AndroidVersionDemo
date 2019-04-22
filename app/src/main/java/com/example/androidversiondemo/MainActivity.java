package com.example.androidversiondemo;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.androidversiondemo.utils.LogUtils;
import com.example.androidversiondemo.utils.RuntimePermissions;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PlusOneFragment.OnFragmentInteractionListener {

    private View mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        PlusOneFragment fragment = PlusOneFragment.newInstance("", "");
        transaction.replace(R.id.sample_main_layout, fragment);
        transaction.commit();


    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
