package com.example.androidversiondemo.utils;

import android.util.Log;

public class LogUtils {
    public static void e(Object object, String string) {
        Log.e("===" + object.getClass().getSimpleName(), string);
    }
}
