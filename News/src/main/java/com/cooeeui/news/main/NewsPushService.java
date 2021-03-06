package com.cooeeui.news.main;

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

import com.cooeeui.news.R;
import com.cooeeui.news.basecore.utils.AssetsConfigUtil;
import com.umeng.analytics.MobclickAgent;

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
    private static final boolean TEST = false;

    private static final String SP_FILE_NAME = "news_notification";
    private static final String SP_KEY_NOTIFICATION_10_12 = "news_notification_10_12";
    private static final String SP_KEY_NOTIFICATION_17_19 = "news_notification_17_19";
    private static final String SP_KEY_NOTIFICATION_21_22 = "news_notification_21_22";
    private static final String SP_KEY_NOTIFICATION_TIME_HOUR = "news_notification_hour";
    private static final String SP_KEY_NOTIFICATION_TIME_MINUTE = "news_notification_minute";
    private static final String SP_KEY_NOTIFICATION_10_12_RANDOM_TIME =
        "news_notification_10_12_random_time";
    private static final String SP_KEY_NOTIFICATION_17_19_RANDOM_TIME =
        "news_notification_17_19_random_time";
    private static final String SP_KEY_NOTIFICATION_21_22_RANDOM_TIME =
        "news_notification_21_22_random_time";

    private SharedPreferences mSp;

    private Random mRandom = new Random();

    private static boolean sThreadIsRunning;
    private Intent mMonitorService;


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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Notification.Builder builder = new Notification.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            startForeground(410401, builder.build());
            startService(new Intent(this, DaemonService.class));
        } else {
            startForeground(410401, new Notification());
        }

        mMonitorService = new Intent(this, MonitorService.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (sThreadIsRunning) {
            return START_STICKY;
        }

        mSp = getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);

        sThreadIsRunning = true;

        new Thread("NewsPushServiceThread") {
            @Override
            public void run() {
                while (sThreadIsRunning) {

                    // 暂时关闭，因为Monitor线程很耗电
//                    if (mMonitorService != null) {
//                        startService(mMonitorService);
//                    }

                    Calendar calendar = Calendar.getInstance();
                    int curHour = calendar.get(Calendar.HOUR_OF_DAY);
                    int curMinute = calendar.get(Calendar.MINUTE);

                    threadNeedToSleep(curHour);

                    if (curHour >= 10 && curHour < 12) {
                        if (!mSp.getBoolean(SP_KEY_NOTIFICATION_10_12, false)) {

                            randomTime_10_12(curHour, curMinute);

                            if (curHour == mSp.getInt(SP_KEY_NOTIFICATION_TIME_HOUR, -1)
                                && curMinute == mSp.getInt(SP_KEY_NOTIFICATION_TIME_MINUTE, -1)) {
                                boolean rst = getNotification();
                                mSp.edit().putBoolean(SP_KEY_NOTIFICATION_10_12, rst).commit();
                                mSp.edit().putBoolean(SP_KEY_NOTIFICATION_17_19, false).commit();
                                mSp.edit().putBoolean(SP_KEY_NOTIFICATION_21_22, false).commit();
                                if (rst) {
                                    mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_HOUR, -1).commit();
                                    mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_MINUTE, -1).commit();
                                } else {
                                    int minute = mSp.getInt(SP_KEY_NOTIFICATION_TIME_MINUTE, -1);
                                    if ((minute + 1) < 60) {
                                        mSp.edit()
                                            .putInt(SP_KEY_NOTIFICATION_TIME_MINUTE, (minute + 1))
                                            .commit();
                                    } else if ((minute + 1) == 60) {
                                        int hour = mSp.getInt(SP_KEY_NOTIFICATION_TIME_HOUR, -1);
                                        mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_HOUR, (hour + 1))
                                            .commit();
                                        mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_MINUTE, 0)
                                            .commit();
                                    } else if ((minute + 1) > 60) {
                                        int hour = mSp.getInt(SP_KEY_NOTIFICATION_TIME_HOUR, -1);
                                        mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_HOUR, (hour + 1))
                                            .commit();
                                        mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_MINUTE,
                                                          (minute + 1 - 60)).commit();
                                    }
                                }
                                if (DEBUG) {
                                    Log.i(TAG, "SP_KEY_NOTIFICATION_10_12 = " + rst);
                                }
                            }
                        }
                    } else if (curHour >= 17 && curHour < 19) {
                        if (!mSp.getBoolean(SP_KEY_NOTIFICATION_17_19, false)) {

                            randomTime_17_19(curHour, curMinute);

                            if (curHour == mSp.getInt(SP_KEY_NOTIFICATION_TIME_HOUR, -1)
                                && curMinute == mSp.getInt(SP_KEY_NOTIFICATION_TIME_MINUTE, -1)) {
                                boolean rst = getNotification();
                                mSp.edit().putBoolean(SP_KEY_NOTIFICATION_17_19, rst).commit();
                                mSp.edit().putBoolean(SP_KEY_NOTIFICATION_10_12, false).commit();
                                mSp.edit().putBoolean(SP_KEY_NOTIFICATION_21_22, false).commit();
                                if (rst) {
                                    mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_HOUR, -1).commit();
                                    mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_MINUTE, -1).commit();
                                } else {
                                    int minute = mSp.getInt(SP_KEY_NOTIFICATION_TIME_MINUTE, -1);
                                    if ((minute + 1) < 60) {
                                        mSp.edit()
                                            .putInt(SP_KEY_NOTIFICATION_TIME_MINUTE, (minute + 1))
                                            .commit();
                                    } else if ((minute + 1) == 60) {
                                        int hour = mSp.getInt(SP_KEY_NOTIFICATION_TIME_HOUR, -1);
                                        mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_HOUR, (hour + 1))
                                            .commit();
                                        mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_MINUTE, 0)
                                            .commit();
                                    } else if ((minute + 1) > 60) {
                                        int hour = mSp.getInt(SP_KEY_NOTIFICATION_TIME_HOUR, -1);
                                        mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_HOUR, (hour + 1))
                                            .commit();
                                        mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_MINUTE,
                                                          (minute + 1 - 60)).commit();
                                    }
                                }
                                if (DEBUG) {
                                    Log.i(TAG, "SP_KEY_NOTIFICATION_17_19 = " + rst);
                                }
                            }
                        }
                    } else if (curHour >= 21 && curHour < 22) {
                        if (!mSp.getBoolean(SP_KEY_NOTIFICATION_21_22, false)) {

                            randomTime_21_22(curHour, curMinute);

                            if (curHour == mSp.getInt(SP_KEY_NOTIFICATION_TIME_HOUR, -1)
                                && curMinute == mSp.getInt(SP_KEY_NOTIFICATION_TIME_MINUTE, -1)) {
                                boolean rst = getNotification();
                                mSp.edit().putBoolean(SP_KEY_NOTIFICATION_21_22, rst).commit();
                                mSp.edit().putBoolean(SP_KEY_NOTIFICATION_10_12, false).commit();
                                mSp.edit().putBoolean(SP_KEY_NOTIFICATION_17_19, false).commit();
                                if (rst) {
                                    mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_HOUR, -1).commit();
                                    mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_MINUTE, -1).commit();
                                } else {
                                    int minute = mSp.getInt(SP_KEY_NOTIFICATION_TIME_MINUTE, -1);
                                    if ((minute + 1) < 60) {
                                        mSp.edit()
                                            .putInt(SP_KEY_NOTIFICATION_TIME_MINUTE, (minute + 1))
                                            .commit();
                                    }
                                }
                                if (DEBUG) {
                                    Log.i(TAG, "SP_KEY_NOTIFICATION_21_22 = " + rst);
                                }
                            }
                        }
                    }

                    // 亮屏时每隔10秒计算一次，减轻cpu压力，从而减少电量损耗
                    try {
                        if (DEBUG) {
                            Log.i(TAG, "thread sleep = " + 15000);
                        }
                        Thread.sleep(15000);    // 15*1000  15秒
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

            //请求Nano链接开始
            MobclickAgent.onEvent(getApplicationContext(), "request_nano_url");

            urlConnection.connect();
            in = urlConnection.getInputStream();

            //请求Nano链接成功
            MobclickAgent.onEvent(getApplicationContext(), "request_nano_url_success");

            String res = inputStream2String(in);

            url = new URL(res);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Charset", "UTF-8");

            //请求第三方链接开始
            MobclickAgent.onEvent(getApplicationContext(), "request_url");

            urlConnection.connect();
            in = urlConnection.getInputStream();

            //请求第三方链接成功
            MobclickAgent.onEvent(getApplicationContext(), "request_url_success");

            res = inputStream2String(in);

            JSONArray jsonArray = new JSONArray(res);
            JSONObject jsonObject = (JSONObject) jsonArray.get(0);

            String title = jsonObject.getString("title");
            String imgUrl = jsonObject.getString("imgUrl");
            String detailUrl = jsonObject.getString("url");

            result = buildExpandedNotification(title, imgUrl, detailUrl);
        } catch (Exception e) {
            e.printStackTrace();

            // 上传自己捕获的错误，（使用自定义错误，查看时请在错误列表页面选择【自定义错误】）
            MobclickAgent.reportError(getApplicationContext(), e);
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

        //创建通知开始
        MobclickAgent.onEvent(getApplicationContext(), "notification_build_begin");

        try {
            Bitmap remotePic = null;

            URL bpUrl = new URL(imgUrl);
            HttpURLConnection conn = (HttpURLConnection) bpUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(10000);
            if (conn.getResponseCode() == 200) {
                InputStream fis = conn.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] bytes = new byte[1024];
                int length = -1;
                while ((length = fis.read(bytes)) != -1) {
                    bos.write(bytes, 0, length);
                }
                byte[] picByte = bos.toByteArray();
                bos.close();
                fis.close();
                remotePic = BitmapFactory.decodeByteArray(picByte, 0, picByte.length);
            }

//            remotePic = BitmapFactory.decodeStream(
//                (InputStream) new URL(imgUrl).getContent());

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

            //显示通知
            MobclickAgent.onEvent(getApplicationContext(), "notification_show");

            if (DEBUG) {
                Log.i(TAG, "buildExpandedNotification = " + result);
            }
        } catch (Exception e) {
            e.printStackTrace();

            // 上传自己捕获的错误，（使用自定义错误，查看时请在错误列表页面选择【自定义错误】）
            MobclickAgent.reportError(getApplicationContext(), e);
        }

        return result;
    }

    private void threadNeedToSleep(int curHour) {
        // 不在要求时间段内，挂起线程，减少cpu压力，从而减少电量损耗
        if (curHour >= 1 && curHour < 10) {
            try {
                if (DEBUG) {
                    Log.i(TAG, "thread sleep " + (9 - curHour) + " hour between 1 and 10 !");
                }
                if (!TEST) {
                    Thread.sleep((9 - curHour) * 60 * 60 * 1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (curHour >= 12 && curHour < 17) {
            try {
                if (DEBUG) {
                    Log.i(TAG, "thread sleep " + (16 - curHour) + " hour between 12 and 17 !");
                }
                if (!TEST) {
                    Thread.sleep((16 - curHour) * 60 * 60 * 1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (curHour >= 19 && curHour < 21) {
            try {
                if (DEBUG) {
                    Log.i(TAG, "thread sleep " + (20 - curHour) + " hour between 19 and 21 !");
                }
                if (!TEST) {
                    Thread.sleep((20 - curHour) * 60 * 60 * 1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (curHour >= 22 && curHour < 24) {
            try {
                if (DEBUG) {
                    Log.i(TAG, "thread sleep " + (23 - curHour) + " hour between 22 and 24 !");
                }
                if (!TEST) {
                    Thread.sleep((23 - curHour) * 60 * 60 * 1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void randomTime_10_12(int curHour, int curMinute) {
        if (!mSp.getBoolean(SP_KEY_NOTIFICATION_10_12_RANDOM_TIME, false)) {
            int radomMinute = mRandom.nextInt(60);
            if (TEST) {
                radomMinute = mRandom.nextInt(3);
            }
            if (DEBUG) {
                Log.i(TAG,
                      "SP_KEY_NOTIFICATION_10_12_RANDOM_TIME radomMinute = " + radomMinute);
            }
            if ((radomMinute + curMinute) < 60) {
                mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_HOUR, curHour)
                    .commit();
                mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_MINUTE,
                                  (radomMinute + curMinute)).commit();
            } else if ((radomMinute + curMinute) > 60) {
                if (curHour == 11) {
                    mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_HOUR, curHour)
                        .commit();
                    mSp.edit()
                        .putInt(SP_KEY_NOTIFICATION_TIME_MINUTE, curMinute)
                        .commit();
                } else {
                    mSp.edit()
                        .putInt(SP_KEY_NOTIFICATION_TIME_HOUR, curHour + 1)
                        .commit();
                    mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_MINUTE,
                                      (radomMinute + curMinute - 60)).commit();
                }
            } else {
                mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_HOUR, curHour)
                    .commit();
                mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_MINUTE, curMinute)
                    .commit();
            }

            if (DEBUG) {
                Log.i(TAG, "SP_KEY_NOTIFICATION_TIME_HOUR = " + mSp
                    .getInt(SP_KEY_NOTIFICATION_TIME_HOUR, -1));
                Log.i(TAG, "SP_KEY_NOTIFICATION_TIME_MINUTE = " + mSp
                    .getInt(SP_KEY_NOTIFICATION_TIME_MINUTE, -1));
            }

            mSp.edit().putBoolean(SP_KEY_NOTIFICATION_10_12_RANDOM_TIME, true).commit();
            mSp.edit().putBoolean(SP_KEY_NOTIFICATION_17_19_RANDOM_TIME, false).commit();
            mSp.edit().putBoolean(SP_KEY_NOTIFICATION_21_22_RANDOM_TIME, false).commit();
        }
    }

    private void randomTime_17_19(int curHour, int curMinute) {
        if (!mSp.getBoolean(SP_KEY_NOTIFICATION_17_19_RANDOM_TIME, false)) {
            int radomMinute = mRandom.nextInt(60);
            if (TEST) {
                radomMinute = mRandom.nextInt(3);
            }
            if (DEBUG) {
                Log.i(TAG, "SP_KEY_NOTIFICATION_17_19_RANDOM_TIME radomMinute = " + radomMinute);
            }
            if ((radomMinute + curMinute) < 60) {
                mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_HOUR, curHour)
                    .commit();
                mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_MINUTE,
                                  (radomMinute + curMinute)).commit();
            } else if ((radomMinute + curMinute) > 60) {
                if (curHour == 18) {
                    mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_HOUR, curHour)
                        .commit();
                    mSp.edit()
                        .putInt(SP_KEY_NOTIFICATION_TIME_MINUTE, curMinute)
                        .commit();
                } else {
                    mSp.edit()
                        .putInt(SP_KEY_NOTIFICATION_TIME_HOUR, curHour + 1)
                        .commit();
                    mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_MINUTE,
                                      (radomMinute + curMinute - 60)).commit();
                }
            } else {
                mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_HOUR, curHour)
                    .commit();
                mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_MINUTE, curMinute)
                    .commit();
            }

            if (DEBUG) {
                Log.i(TAG, "SP_KEY_NOTIFICATION_TIME_HOUR = " + mSp
                    .getInt(SP_KEY_NOTIFICATION_TIME_HOUR, -1));
                Log.i(TAG, "SP_KEY_NOTIFICATION_TIME_MINUTE = " + mSp
                    .getInt(SP_KEY_NOTIFICATION_TIME_MINUTE, -1));
            }

            mSp.edit().putBoolean(SP_KEY_NOTIFICATION_17_19_RANDOM_TIME, true).commit();
            mSp.edit().putBoolean(SP_KEY_NOTIFICATION_10_12_RANDOM_TIME, false).commit();
            mSp.edit().putBoolean(SP_KEY_NOTIFICATION_21_22_RANDOM_TIME, false).commit();
        }
    }


    private void randomTime_21_22(int curHour, int curMinute) {
        if (!mSp.getBoolean(SP_KEY_NOTIFICATION_21_22_RANDOM_TIME, false)) {
            int radomMinute = mRandom.nextInt(60);
            if (TEST) {
                radomMinute = mRandom.nextInt(3);
            }
            if (DEBUG) {
                Log.i(TAG, "SP_KEY_NOTIFICATION_21_22_RANDOM_TIME radomMinute = " + radomMinute);
            }
            if ((radomMinute + curMinute) < 60) {
                mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_HOUR, curHour)
                    .commit();
                mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_MINUTE,
                                  (radomMinute + curMinute)).commit();
            } else {
                mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_HOUR, curHour)
                    .commit();
                mSp.edit().putInt(SP_KEY_NOTIFICATION_TIME_MINUTE, curMinute)
                    .commit();
            }

            if (DEBUG) {
                Log.i(TAG, "SP_KEY_NOTIFICATION_TIME_HOUR = " + mSp
                    .getInt(SP_KEY_NOTIFICATION_TIME_HOUR, -1));
                Log.i(TAG, "SP_KEY_NOTIFICATION_TIME_MINUTE = " + mSp
                    .getInt(SP_KEY_NOTIFICATION_TIME_MINUTE, -1));
            }

            mSp.edit().putBoolean(SP_KEY_NOTIFICATION_21_22_RANDOM_TIME, true).commit();
            mSp.edit().putBoolean(SP_KEY_NOTIFICATION_10_12_RANDOM_TIME, false).commit();
            mSp.edit().putBoolean(SP_KEY_NOTIFICATION_17_19_RANDOM_TIME, false).commit();
        }
    }

}
