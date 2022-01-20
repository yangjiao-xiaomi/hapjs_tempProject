/*
 * Copyright (c) 2021, the hapjs-platform Project Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hapjs.common.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;
import androidx.annotation.RequiresApi;
import java.io.File;

public class WebViewUtils {

    private static final String TAG = "WebViewUtils";
    private static boolean sHasSetDataDirectory = false;

    /**
     * Android P之后不允许多进程同时访问同一个webview目录
     *
     * <p>https://developer.android.com/about/versions/pie/android-9.0-changes-28#web-data-dirs
     */
    public static void setDataDirectory(String pkg) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P || sHasSetDataDirectory) {
            return;
        }
        try {
            // WebView 初始化后不允许再调用
            WebView.setDataDirectorySuffix(pkg);
            sHasSetDataDirectory = true;
        } catch (Exception e) {
            Log.e(TAG, "setDataDirectory failed!", e);
        }
    }

    /**
     * 获取 WebView 数据目录
     *
     * <p>/data/data/com.xxx.hybrid/app_webview_{pkg}
     */
    @RequiresApi(Build.VERSION_CODES.P)
    public static File getWebViewData(Context context, String pkg) {
        return context.getDir("webview_" + pkg, Context.MODE_PRIVATE);
    }

    /**
     * 获取 WebView 缓存目录
     *
     * <p>/data/data/com.xxx.hybrid/cache/webview_{pkg}
     */
    @RequiresApi(Build.VERSION_CODES.P)
    public static File getWebViewCache(Context context, String pkg) {
        return new File(context.getCacheDir(), "webview_" + pkg);
    }
}
