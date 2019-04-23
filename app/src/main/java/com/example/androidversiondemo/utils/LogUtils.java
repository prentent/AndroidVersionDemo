package com.example.androidversiondemo.utils;

import android.util.Log;

/*
日志打印类
 */
public class LogUtils {
    public static boolean isDebug = true;

    public static void v(Object object, String string) {
        if (isDebug)
            Log.v("===" + object.getClass().getSimpleName(), string);
    }

    public static void d(Object object, String string) {
        if (isDebug)
            Log.d("===" + object.getClass().getSimpleName(), string);
    }

    public static void i(Object object, String string) {
        if (isDebug)
            Log.i("===" + object.getClass().getSimpleName(), string);
    }

    public static void w(Object object, String string) {
        if (isDebug)
            Log.w("===" + object.getClass().getSimpleName(), string);
    }

    public static void e(Object object, String string) {
        if (isDebug)
            Log.e("===" + object.getClass().getSimpleName(), string);
    }
}
