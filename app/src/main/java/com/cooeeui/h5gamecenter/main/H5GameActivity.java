package com.cooeeui.h5gamecenter.main;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cooeeui.h5gamecenter.R;
import com.cooeeui.h5gamecenter.basecore.utils.AssetsConfigUtil;
import com.cooeeui.h5gamecenter.basecore.views.BaseActivity;

public class H5GameActivity extends BaseActivity {

    private final String TAG = H5GameActivity.class.getSimpleName();
    private long mExitTime;


    @Override
    public void setUpContentViews() {
        setContentView(mWebView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                            ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public String getWebViewUrl() {
        return AssetsConfigUtil.getsInstance().getGameUrl();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - mExitTime > 2000) {
            Toast.makeText(this, R.string.game_exit_warn, Toast.LENGTH_SHORT)
                .show();
            mExitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
            startActivity(new Intent(this, H5GameCenterActivity.class));
            mWebView.reload();
            finish();
        }
    }
}
