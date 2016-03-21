package com.cooeeui.h5gamecenter.basecore.views;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Created by Hugo.ye on 2016/3/18.
 */
public abstract class BaseActivity extends Activity {

    private final String TAG = BaseActivity.class.getSimpleName();

    protected WebView mWebView;

    abstract public void setUpContentViews();

    abstract public String getWebViewUrl();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWebView();
        setUpContentViews();
        String url = getWebViewUrl();
        loadWebView(url);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // umeng analytics
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // umeng analytics
        MobclickAgent.onPause(this);
    }

    private void initWebView() {
        try {
            mWebView = new WebView(this);
        } catch (Exception e) {
            mWebView = new WebView(this);
        }
        initWebViewSettings();
    }

    // 初始化webview页面的设置参数
    private void initWebViewSettings() {
        mWebView.setInitialScale(0);
        mWebView.setVerticalScrollBarEnabled(false);
        // Enable JavaScript
        final WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setTextSize(WebSettings.TextSize.NORMAL);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);

        // Set the nav dump for HTC 2.x devices (disabling for ICS, deprecated
        // entirely for Jellybean 4.2)
        try {
            Method gingerbread_getMethod = WebSettings.class.getMethod(
                "setNavDump", new Class[]{boolean.class});

            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB
                && android.os.Build.MANUFACTURER.contains("HTC")) {
                gingerbread_getMethod.invoke(settings, true);
            }
        } catch (NoSuchMethodException e) {
            Log.d(TAG,
                  "We are on a modern version of Android, we will deprecate HTC 2.3 devices in 2.8");
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Doing the NavDump failed with bad arguments");
        } catch (IllegalAccessException e) {
            Log.d(TAG,
                  "This should never happen: IllegalAccessException means this isn't Android anymore");
        } catch (InvocationTargetException e) {
            Log.d(TAG,
                  "This should never happen: InvocationTargetException means this isn't Android anymore.");
        }

        // We don't save any form data in the application
        settings.setSaveFormData(false);
        settings.setSavePassword(false);

        // Jellybean rightfully tried to lock this down. Too bad they didn't
        // give us a whitelist
        // while we do this
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settings.setMediaPlaybackRequiresUserGesture(false);
        }

        // Enable database
        // We keep this disabled because we use or shim to get around
        // DOM_EXCEPTION_ERROR_16
        String databasePath = mWebView.getContext().getApplicationContext()
            .getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setDatabaseEnabled(true);
        settings.setDatabasePath(databasePath);

        // Enable DOM storage
        settings.setDomStorageEnabled(true);

        // Enable built-in geolocation
        settings.setGeolocationEnabled(true);

        // Enable AppCache
        // Fix for CB-2282
        settings.setAppCacheMaxSize(5 * 1048576);
        settings.setAppCachePath(databasePath);
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
    }

    private void loadWebView(String url) {
        mWebView.loadUrl(url);
    }
}
