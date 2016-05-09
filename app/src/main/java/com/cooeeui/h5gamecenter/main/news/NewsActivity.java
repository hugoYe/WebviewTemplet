package com.cooeeui.h5gamecenter.main.news;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.cooeeui.h5gamecenter.R;
import com.cooeeui.h5gamecenter.basecore.utils.AssetsConfigUtil;
import com.cooeeui.h5gamecenter.basecore.views.NumberProgressBar;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NewsActivity extends Activity {

    private static final String TAG = NewsActivity.class.getSimpleName();
    private static final boolean DEBUG = false;

    private final int PROGRESSBAR_MIN = 10;

    private FrameLayout mCenterLayout;
    private NumberProgressBar mProgressBar;
    private WebView mWebView;

    private long mExitTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.news_main);
        initViews();

        mWebView.loadUrl(AssetsConfigUtil.getsInstance().getNesUrl());

        Intent serviceIntent = new Intent();
        serviceIntent.setClass(getApplicationContext(), NewsPushService.class);
        startService(serviceIntent);

        UmengUpdateAgent.update(this);
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (DEBUG) {
            Log.i(TAG, "onResume");
        }
        final Intent intent = getIntent();
        if (intent != null && intent.hasExtra("detailUrl")) {
            if (DEBUG) {
                Log.i(TAG, "intent.hasExtra = " + intent.getStringExtra("detailUrl"));
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(intent.getStringExtra("detailUrl"));
                }
            }, 1500);
        }

        // umeng analytics
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // umeng analytics
        MobclickAgent.onPause(this);
    }

    protected void onNewIntent(Intent intent) {
        if (DEBUG) {
            Log.i(TAG, "onNewIntent");
        }
        if (intent != null && intent.hasExtra("detailUrl")) {
            if (DEBUG) {
                Log.i(TAG, "intent.hasExtra = " + intent.getStringExtra("detailUrl"));
            }
            mWebView.loadUrl(intent.getStringExtra("detailUrl"));
        }
//        super.onNewIntent(intent);
//        setIntent(intent);  // must store the new intent unless getIntent() will return the old one
    }


    private void initViews() {

        try {
            mWebView = new WebView(this);
        } catch (Exception e) {
            mWebView = new WebView(this);
        }
        initWebViewSettings();

        WebChromeClient webChromeClient = new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 90) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    if (mProgressBar.getVisibility() == View.GONE) {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                    if (newProgress < PROGRESSBAR_MIN) {
                        mProgressBar.setProgress(PROGRESSBAR_MIN);
                    } else {
                        mProgressBar.setProgress(newProgress);
                    }
                }

                super.onProgressChanged(view, newProgress);
            }
        };
        mWebView.setWebChromeClient(webChromeClient);

        mCenterLayout = (FrameLayout) findViewById(R.id.ll_news_center);
        mCenterLayout.addView(mWebView, 0,
                              new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                         ViewGroup.LayoutParams.MATCH_PARENT));
        mProgressBar = (NumberProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setUnreachedBarDrawStatus(false);
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

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }

        if (System.currentTimeMillis() - mExitTime > 2000) {
            Toast.makeText(this, R.string.exit_warn, Toast.LENGTH_SHORT)
                .show();
            mExitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
            finish();
        }
    }
}
