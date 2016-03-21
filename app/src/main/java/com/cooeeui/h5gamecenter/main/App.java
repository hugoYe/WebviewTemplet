package com.cooeeui.h5gamecenter.main;

import android.app.Application;

import com.cooeeui.h5gamecenter.basecore.utils.AssetsConfigUtil;
import com.umeng.analytics.AnalyticsConfig;

/**
 * Created by Hugo.ye on 2016/3/21.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AssetsConfigUtil.getsInstance().init(this);

        // umeng initial begin
        String umengAppKey = AssetsConfigUtil.getsInstance().getUmengAppKey();
        AnalyticsConfig.setAppkey(null, umengAppKey);
        String umengChannel = AssetsConfigUtil.getsInstance().getUmengChannel();
        AnalyticsConfig.setChannel(umengChannel);
        // umeng initial end
    }
}
