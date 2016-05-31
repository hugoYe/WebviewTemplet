package com.cooeeui.news.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SystemReceiver extends BroadcastReceiver {

    public SystemReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            Log.i("yezhennan", "action = " + intent.getAction());

            Intent serviceIntent = new Intent();
            serviceIntent.setClass(context, NewsPushService.class);
            context.startService(serviceIntent);
        } catch (Exception e) {
            Log.e("SystemReceiver", e.toString());
        }
    }
}
