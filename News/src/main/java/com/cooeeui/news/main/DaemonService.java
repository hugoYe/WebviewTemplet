package com.cooeeui.news.main;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import com.cooeeui.news.R;

public class DaemonService extends Service {

    public DaemonService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Notification.Builder builder = new Notification.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            startForeground(410401, builder.build());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopForeground(true);
                    NotificationManager manager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    manager.cancel(410401);
                    stopSelf();
                }
            }, 1500);
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
