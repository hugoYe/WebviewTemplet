package com.cooeeui.h5gamecenter.main;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.cooeeui.downloader.api.DLManager;
import com.cooeeui.downloader.api.DLNotificationManager;
import com.cooeeui.h5gamecenter.R;

/**
 * Created by Hugo.ye on 2016/3/22.
 */
public class MyTestActivity extends Activity {

    private static final String DOWNLOAD_URL = "http://www.coolauncher.cn/nano/apk/NanoIconPKG.apk";
    final String path = Environment.getExternalStorageDirectory().getPath() + "/NanoLauncher/App/";

    private ProgressBar mProgressBar;

    private LinearLayout mContainer;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_test_layout);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_download);
        mProgressBar.setMax(100);
        mContainer = (LinearLayout) findViewById(R.id.ll_container);
        initWebView();
    }

    public void onButtonClick(View v) {
        switch (v.getId()) {
            case R.id.bt_dl_start:
                DLNotificationManager.getInstance(this)
                    .startDLNotification("http://www.coolauncher.cn/nano/apk/NanoIconPKG.apk",
                                         "com.cooeeui.iconui", "NanoIconPKG");
                break;
            case R.id.bt_dl_stop:
                DLNotificationManager.getInstance(this)
                    .startDLNotification("http://www.coolauncher.cn/nano/apk/NanoLauncher.apk",
                                         "com.cooeeui.zenlauncher", "NanoLauncher");
                break;
            case R.id.bt_dl_cancel:
                DLManager.getInstance(MyTestActivity.this).dlCancel(DOWNLOAD_URL);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        DLManager.getInstance(this).dlStop(DOWNLOAD_URL);

        super.onDestroy();
    }

    private void initWebView() {
        try {
            mWebView = new WebView(this);
        } catch (Exception e) {
            mWebView = new WebView(this);
        }
        mContainer.addView(mWebView);

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        mWebView.addJavascriptInterface(new JavaScriptObject(), "H5GameCenter");

        mWebView.loadUrl("http://nanohome.cn:3000/index/action");
    }

    public class JavaScriptObject {

        @JavascriptInterface
        public void downloadGame(String apkurl, String apkPackageName, String apkName,
                                 String notiIconBase64Str) {
            Log.i("yezhennan", "downloadGame");
            DLNotificationManager.getInstance(MyTestActivity.this)
                .startDLNotification(apkurl, apkPackageName, apkName, notiIconBase64Str);
        }

    }
}
