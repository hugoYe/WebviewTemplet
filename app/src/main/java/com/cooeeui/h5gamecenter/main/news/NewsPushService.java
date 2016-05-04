package com.cooeeui.h5gamecenter.main.news;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.cooeeui.h5gamecenter.R;
import com.cooeeui.h5gamecenter.basecore.utils.ThreadUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NewsPushService extends Service {

    private static final String
        NOTIFICATION_URL =
        "http://server.visualizr.me/magazineServer/getNotificationObject?clientid=trending&baseruri=nano.visualizr.me";

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
        ThreadUtil.execute(new Runnable() {
            @Override
            public void run() {
                InputStream in = null;

                try {
                    URL url = new URL(NOTIFICATION_URL);
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

                    buildExpandedNotification(title, imgUrl, detailUrl);
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
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
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
    public void buildExpandedNotification(String title, String imgUrl, String url) {

        try {
            Bitmap remotePic = null;
            remotePic = BitmapFactory.decodeStream(
                (InputStream) new URL(imgUrl).getContent());

            NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            Intent notiIntent = new Intent(getApplicationContext(), NewsActivity.class);
            notiIntent.putExtra("detailUrl", url);
            PendingIntent intent =
                PendingIntent.getActivity(getApplicationContext(), 0, notiIntent, 0);

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
            notificationManager.notify(0, notification);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
