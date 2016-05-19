package com.cooeeui.h5gamecenter.main;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cooeeui.downloader.api.DLNotificationManager;
import com.cooeeui.h5gamecenter.R;
import com.cooeeui.h5gamecenter.basecore.utils.AssetsConfigUtil;
import com.cooeeui.h5gamecenter.basecore.views.BaseActivity;
import com.cooeeui.h5gamecenter.basecore.views.NumberProgressBar;

public class H5GameCenterActivity extends BaseActivity {

    private final String TAG = H5GameCenterActivity.class.getSimpleName();

    private final int PROGRESSBAR_MIN = 10;
    private final int BACKKEY_RESPONSE_TIME = 5000;

    private LinearLayout mCenterLayout;
    private NumberProgressBar mProgressBar;
    private long mBackKeyResponseTime;

    private long mExitTime;

    @Override
    public void setUpContentViews() {
        setContentView(R.layout.game_center);
        mCenterLayout = (LinearLayout) findViewById(R.id.ll_game_center);
        mCenterLayout.addView(mWebView,
                              new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                         ViewGroup.LayoutParams.MATCH_PARENT));
        mProgressBar = (NumberProgressBar) findViewById(R.id.progressBar);

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

        mWebView.setWebViewClient(new WebViewClient());

        mWebView.addJavascriptInterface(new JavaScriptObject(), "H5GameCenter");
    }

    @Override
    public String getWebViewUrl() {
        return AssetsConfigUtil.getsInstance().getGameCenterUrl();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBackKeyResponseTime = System.currentTimeMillis();
    }

    @Override
    public void onBackPressed() {
        // 进入游戏中心5秒后才能操作返回键
        if (System.currentTimeMillis() - mBackKeyResponseTime > BACKKEY_RESPONSE_TIME) {
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

    public class JavaScriptObject {

        @JavascriptInterface
        public void downloadGame(String apkurl, String apkPackageName, String apkName,
                                 String notiIconBase64Str) {
            DLNotificationManager.getInstance(H5GameCenterActivity.this)
                .startDLNotification(apkurl, apkPackageName, apkName, notiIconBase64Str);
        }

    }
}
