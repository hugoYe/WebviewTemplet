package com.cooeeui.news.main;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MonitorService extends Service {

    private static boolean sIsRunning;
    private Intent mNewsPushService;

    public MonitorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNewsPushService = new Intent(this, NewsPushService.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (sIsRunning) {
            return START_STICKY;
        }

        sIsRunning = true;

        new Thread("MonitorServiceThread") {
            @Override
            public void run() {
                while (sIsRunning) {
                    if (mNewsPushService != null) {
                        startService(mNewsPushService);
                    }

                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException e) {
                        //
                    }
                }
            }
        }.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sIsRunning = false;
    }
}
