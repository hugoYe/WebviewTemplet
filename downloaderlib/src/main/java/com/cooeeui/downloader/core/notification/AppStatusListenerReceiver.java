package com.cooeeui.downloader.core.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.cooeeui.downloader.api.DLNotificationManager;

public class AppStatusListenerReceiver extends BroadcastReceiver {

    public AppStatusListenerReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)
            || Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            if (packageName != null && !TextUtils.isEmpty(packageName)) {
                DLNotificationManager.getInstance(context).appInstalled(packageName);
            }
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
        }
    }
}
