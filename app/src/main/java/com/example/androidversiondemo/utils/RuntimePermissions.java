package com.example.androidversiondemo.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

/*
权限管理
 */

public class RuntimePermissions {

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    public static <T> boolean hasPermission(T context, String[] str2) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context instanceof Activity) {
                if (ActivityCompat.checkSelfPermission((Activity) context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                } else {
                    return true;
                }
            } else if (context instanceof Fragment) {
                if (ContextCompat.checkSelfPermission(((Fragment) context).getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                } else {
                    return true;
                }
            }

        }
        return true;
    }

    public static <T> void requestPermissions(T context, String[] str2) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //如果未授予权限，提示
            if (context instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) context,
                        str2, REQUEST_WRITE_EXTERNAL_STORAGE);
            } else if (context instanceof Fragment) {
                ((Fragment) context).requestPermissions(
                        str2, REQUEST_WRITE_EXTERNAL_STORAGE);
            }
//                } else {
            //权限许可还没有被批准。直接请求它。
           /* ActivityCompat.requestPermissions(activity,
                    str2, REQUEST_CAMERA);*/
            /* }*/
        }
    }

    public static boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length < 1) {
                return false;
            }
            // Verify that each required permission has been granted, otherwise return false.
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
