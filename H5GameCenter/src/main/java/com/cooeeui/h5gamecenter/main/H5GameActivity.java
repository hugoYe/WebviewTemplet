package com.cooeeui.h5gamecenter.main;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.cooeeui.h5gamecenter.R;
import com.cooeeui.h5gamecenter.basecore.utils.AssetsConfigUtil;
import com.cooeeui.h5gamecenter.basecore.views.BaseActivity;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

public class H5GameActivity extends BaseActivity {

    private final String TAG = H5GameActivity.class.getSimpleName();
    private long mExitTime;

    private HashMap<String, String> mNewUsersMap = new HashMap<>();
    private static final String MAP_KEY = "GameName";


    @Override
    public void setUpContentViews() {
        setContentView(mWebView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                            ViewGroup.LayoutParams.MATCH_PARENT));

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i("yezhennan", "url = " + url);
                if (url.startsWith("geo:") || url.startsWith(WebView.SCHEME_MAILTO) || url
                    .startsWith("market:") || url.startsWith("whatsapp:")) {
                    Intent i = new Intent("android.intent.action.VIEW");
                    i.setData(Uri.parse(url));
                    startActivitySafely(i);

                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    public String getWebViewUrl() {
        return AssetsConfigUtil.getsInstance().getGameUrl();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 横竖屏切换
                int configOrientation = AssetsConfigUtil.getsInstance().getScreenOrientation();
                if (configOrientation != -1) {
                    int currentOrientation = getRequestedOrientation();
                    if (currentOrientation != configOrientation) {
                        if (configOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        } else if (configOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        }
                    }
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            }
        }, 2000);

        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("new_user", true)) {
            mNewUsersMap.put(MAP_KEY, AssetsConfigUtil.getsInstance().getUmengChannel());
            MobclickAgent.onEvent(this, "new_user", mNewUsersMap);
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("new_user", false)
                .commit();
        }

        // 渠道里填写的其实是应用的名称
        MobclickAgent.onEvent(this, AssetsConfigUtil.getsInstance().getUmengChannel());
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
            // 如果没有配置游戏中心链接，则直接退出游戏
            if (AssetsConfigUtil.getsInstance().getGameCenterUrl() == null
                || TextUtils.isEmpty(AssetsConfigUtil.getsInstance().getGameCenterUrl())) {
                finish();
            } else {
                startActivity(new Intent(this, H5GameCenterActivity.class));
                mWebView.reload();
                finish();
            }
        }
    }

    public boolean startActivitySafely(Intent intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "App isn't installed.", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
