package com.cooeeui.h5gamecenter.main.news;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.cooeeui.h5gamecenter.R;
import com.cooeeui.h5gamecenter.basecore.utils.AssetsConfigUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

public class NewsPushService extends Service {

    private static final String TAG = NewsPushService.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static final String SP_FILE_NAME = "news_notification";
    private static final String SP_KEY_NOTIFICATION_10_12 = "news_notification_10_12";
    private static final String SP_KEY_NOTIFICATION_17_19 = "news_notification_17_19";
    private static final String SP_KEY_NOTIFICATION_21_22 = "news_notification_21_22";
    private SharedPreferences mSp;

    private Random mRandom = new Random();

    private static boolean sThreadIsRunning;


    public NewsPushService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mSp = getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);

        sThreadIsRunning = true;

        new Thread("NewsPushServiceThread") {
            @Override
            public void run() {
                while (sThreadIsRunning) {
                    Calendar calendar = Calendar.getInstance();
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    if (hour >= 10 && hour <= 12) {
                        mSp.edit().putBoolean(SP_KEY_NOTIFICATION_17_19, false).commit();
                        mSp.edit().putBoolean(SP_KEY_NOTIFICATION_21_22, false).commit();

                        if (!mSp.getBoolean(SP_KEY_NOTIFICATION_10_12, false)) {
                            boolean rst = getNotification();
                            mSp.edit().putBoolean(SP_KEY_NOTIFICATION_10_12, rst).commit();
                            if (DEBUG) {
                                Log.i(TAG, "SP_KEY_NOTIFICATION_10_12 = " + rst);
                            }
                        }
                    } else if (hour >= 17 && hour <= 19) {
                        mSp.edit().putBoolean(SP_KEY_NOTIFICATION_10_12, false).commit();
                        mSp.edit().putBoolean(SP_KEY_NOTIFICATION_21_22, false).commit();

                        if (!mSp.getBoolean(SP_KEY_NOTIFICATION_17_19, false)) {
                            boolean rst = getNotification();
                            mSp.edit().putBoolean(SP_KEY_NOTIFICATION_17_19, rst).commit();
                            if (DEBUG) {
                                Log.i(TAG, "SP_KEY_NOTIFICATION_17_19 = " + rst);
                            }
                        }
                    } else if (hour >= 21 && hour <= 22) {
                        mSp.edit().putBoolean(SP_KEY_NOTIFICATION_10_12, false).commit();
                        mSp.edit().putBoolean(SP_KEY_NOTIFICATION_17_19, false).commit();

                        if (!mSp.getBoolean(SP_KEY_NOTIFICATION_21_22, false)) {
                            boolean rst = getNotification();
                            mSp.edit().putBoolean(SP_KEY_NOTIFICATION_21_22, rst).commit();
                            if (DEBUG) {
                                Log.i(TAG, "SP_KEY_NOTIFICATION_21_22 = " + rst);
                            }
                        }
                    }
                }
            }
        }.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        sThreadIsRunning = false;
    }

    public boolean getNotification() {

        boolean result = false;

        InputStream in = null;
        try {
            URL url = new URL(AssetsConfigUtil.getsInstance().getNewsNotificationUrl());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Charset", "UTF-8");

            urlConnection.connect();
            in = urlConnection.getInputStream();

            String res = inputStream2String(in);

            JSONArray jsonArray = new JSONArray(res);
            JSONObject jsonObject = (JSONObject) jsonArray.get(0);

            String title = jsonObject.getString("title");
            String imgUrl = jsonObject.getString("imgUrl");
            String detailUrl = jsonObject.getString("url");

            result = buildExpandedNotification(title, imgUrl, detailUrl);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    /**
     * 将流转换为String类型
     */
    public static String inputStream2String(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toString();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean buildExpandedNotification(String title, String imgUrl, String url) {
        boolean result = false;

        try {
            Bitmap remotePic = null;
            remotePic = BitmapFactory.decodeStream(
                (InputStream) new URL(imgUrl).getContent());

            NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            Intent notiIntent = new Intent(getApplicationContext(), NewsActivity.class);
            notiIntent.putExtra("detailUrl", url);
            PendingIntent intent =
                PendingIntent.getActivity(getApplicationContext(), UUID.randomUUID().hashCode(),
                                          notiIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setLargeIcon(remotePic)
                .setContentIntent(intent)
                .setContentTitle(title).build();

            RemoteViews expandedView =
                new RemoteViews(getPackageName(), R.layout.news_notification_view);
            expandedView.setImageViewBitmap(R.id.iv_news_img, remotePic);
            expandedView.setTextViewText(R.id.tv_news_title, title);
            notification.bigContentView = expandedView;
            notificationManager.notify(mRandom.nextInt(100), notification);

            result = true;

            if (DEBUG) {
                Log.i(TAG, "buildExpandedNotification = " + result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
