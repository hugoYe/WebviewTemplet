package com.cooeeui.downloader.core.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.cooeeui.downloader.api.DLManager;
import com.cooeeui.downloader.api.DLNotificationManager;
import com.cooeeui.downloader.core.interfaces.IDListener;
import com.cooeeui.downloaderlib.R;

import java.io.File;

/**
 * Created by hugo.ye on 2016/3/30.
 */
public class DLNotification implements IDListener {

    private static final String TAG = DLNotification.class.getSimpleName();

    private static String ACTION_BUTTON = "downloader.notifications.intent.action.ButtonClick";
    public final static String INTENT_BUTTONID_TAG = "ButtonId";
    public final static int BUTTON_CONTINUE_ID = 1; // 继续下载
    public final static int BUTTON_PAUSE_ID = 2;    // 暂停下载
    public final static int BUTTON_CANCEL_ID = 3;   // 取消下载

    private Context mContext;
    private NotificationManager mNotificationManager;
    public NotificationCompat.Builder mBuilder;
    private RemoteViews mRemoteViews;
    private Notification mNotification;
    private String mDownloadUrl;
    private String mPackageName;
    private String mDownloadPath;
    private int mNotificationId;
    private int mFileLength;
    private IDLFileListener mFileListener;
    private ButtonPendingIntentReceiver mBtnReceiver;
    private final int mButtonLeftViewId = R.id.notification_left_button;
    private final int mButtonRightViewId = R.id.notification_right_button;

    /**
     * @param context          上下文环境
     * @param apkurl           apk下载链接地址
     * @param apkPackageName   需要下载的apk包名
     * @param apkName          apk名称
     * @param notificationId   通知栏id
     * @param notificationIcon 通知栏提示图标,传入“-1”则使用系统默认的
     * @param notiWarnIcon     状态栏提示图标,传入“-1”则使用系统默认的
     * @param downloadPath     下载保存路径
     **/
    public DLNotification(Context context, String apkurl,
                          String apkPackageName, String apkName, int notificationId,
                          int notificationIcon, int notiWarnIcon, String downloadPath) {
        mContext = context;
        mDownloadUrl = apkurl;
        mPackageName = apkPackageName;
        ACTION_BUTTON = apkPackageName + "." + ACTION_BUTTON;   // 每个通知栏按钮事件都是独立的，所以需要区分事件广播
        mNotificationId = notificationId;
        mDownloadPath = downloadPath;
        mNotificationManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);

        if (notiWarnIcon == -1) {
            notiWarnIcon = R.drawable.downloader_notification_warn_icon;
        }

        mBuilder.setSmallIcon(notiWarnIcon)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(false)
            .setOngoing(true)
            .setTicker(apkName);//通知首次出现在通知栏，带上升动画效果

        mNotification = mBuilder.build();
        mRemoteViews =
            new RemoteViews(context.getPackageName(), R.layout.downloader_notification_layout);
        if (notificationIcon != -1) {
            mRemoteViews.setImageViewResource(R.id.notification_dl_icon, notificationIcon);
        }
        mNotification.contentView = mRemoteViews;
    }

    /**
     * @param context          上下文环境
     * @param apkurl           apk下载链接地址
     * @param apkPackageName   需要下载的apk包名
     * @param apkName          apk名称
     * @param notificationId   通知栏id
     * @param notificationIcon 通知栏提示图标,可以为null，使用系统默认的
     * @param notiWarnIcon     状态栏提示图标,传入“-1”则使用系统默认的
     * @param downloadPath     下载保存路径
     **/
    public DLNotification(Context context, String apkurl,
                          String apkPackageName, String apkName, int notificationId,
                          Bitmap notificationIcon, int notiWarnIcon, String downloadPath) {
        mContext = context;
        mDownloadUrl = apkurl;
        mPackageName = apkPackageName;
        ACTION_BUTTON = apkPackageName + "." + ACTION_BUTTON;   // 每个通知栏按钮事件都是独立的，所以需要区分事件广播
        mNotificationId = notificationId;
        mDownloadPath = downloadPath;
        mNotificationManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);

        if (notiWarnIcon == -1) {
            notiWarnIcon = R.drawable.downloader_notification_warn_icon;
        }

        mBuilder.setSmallIcon(notiWarnIcon)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(false)
            .setOngoing(true)
            .setTicker(apkName);//通知首次出现在通知栏，带上升动画效果

        mNotification = mBuilder.build();
        mRemoteViews =
            new RemoteViews(context.getPackageName(), R.layout.downloader_notification_layout);
        if (notificationIcon != null) {
            mRemoteViews.setImageViewBitmap(R.id.notification_dl_icon, notificationIcon);
        }
        mNotification.contentView = mRemoteViews;
    }

    public void setFileListener(IDLFileListener listener) {
        this.mFileListener = listener;
    }


    public void startDLNotification() {
        initButtonPendingIntentReceiver();
        mRemoteViews.setTextViewText(R.id.notification_dl_status, mContext.getResources()
            .getText(R.string.downloader_noti_downloading));
        registerButtonPausePendingIntent(mButtonRightViewId);
        DLManager.getInstance(mContext).dlStart(mDownloadUrl, mDownloadPath, this);
    }

    private void initButtonPendingIntentReceiver() {
        if (mBtnReceiver == null) {
            mBtnReceiver = new ButtonPendingIntentReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_BUTTON);
            mContext.registerReceiver(mBtnReceiver, intentFilter);
        }
    }

    private void registerButtonContinuePendingIntent(int buttonViewId) {
        mRemoteViews.setViewVisibility(buttonViewId, View.VISIBLE);
        mRemoteViews
            .setImageViewResource(buttonViewId, R.drawable.downloader_notification_continue);

        Intent buttonIntent = new Intent(ACTION_BUTTON);
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_CONTINUE_ID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, BUTTON_CONTINUE_ID,
                                                                 buttonIntent,
                                                                 PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(buttonViewId, pendingIntent);
    }

    private void registerButtonPausePendingIntent(int buttonViewId) {
        mRemoteViews.setViewVisibility(buttonViewId, View.VISIBLE);
        mRemoteViews.setImageViewResource(buttonViewId, R.drawable.downloader_notification_pause);

        Intent buttonIntent = new Intent(ACTION_BUTTON);
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PAUSE_ID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, BUTTON_PAUSE_ID,
                                                                 buttonIntent,
                                                                 PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(buttonViewId, pendingIntent);
    }

    private void registerButtonCancelPendingIntent(int buttonViewId) {
        mRemoteViews.setViewVisibility(buttonViewId, View.VISIBLE);
        mRemoteViews.setImageViewResource(buttonViewId, R.drawable.downloader_notification_cancel);

        Intent buttonIntent = new Intent(ACTION_BUTTON);
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_CANCEL_ID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, BUTTON_CANCEL_ID,
                                                                 buttonIntent,
                                                                 PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(buttonViewId, pendingIntent);
    }

    private Intent launchInstallAction(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        mContext.startActivity(intent);
        return intent;
    }

    @Override
    public void onPrepare() {

    }

    @Override
    public void onStart(String fileName, String realUrl, int fileLength) {
        mFileLength = fileLength;
    }

    @Override
    public void onProgress(int progress) {
        mRemoteViews.setProgressBar(R.id.notification_progressbar, mFileLength, progress, false);
        mNotificationManager.notify(mNotificationId, mNotification);
    }

    @Override
    public void onStop(int progress) {

    }

    @Override
    public void onFinish(File file) {
        registerButtonCancelPendingIntent(mButtonRightViewId);
        Intent intent = launchInstallAction(file);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        mNotification.contentIntent = pendingIntent;
        mRemoteViews.setTextViewText(R.id.notification_dl_status, mContext.getResources().
            getText(R.string.downloader_noti_done));
        mNotificationManager.notify(mNotificationId, mNotification);
        if (mFileListener != null) {
            mFileListener.onFileExist(mPackageName, file);
        }
    }

    @Override
    public void onError(int status, final String error) {

        // 广播注销
        if (mBtnReceiver != null) {
            mContext.unregisterReceiver(mBtnReceiver);
            mBtnReceiver = null;
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFileExist(File file) {
        launchInstallAction(file);
        if (mFileListener != null) {
            mFileListener.onFileExist(mPackageName, file);
        }
    }

    // 广播监听通知栏按钮点击事件
    class ButtonPendingIntentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_BUTTON)) {
                // 通过传递过来的ID判断按钮点击属性或者通过getResultCode()获得相应点击事件
                int buttonId = intent.getIntExtra(INTENT_BUTTONID_TAG, 0);
                switch (buttonId) {
                    case BUTTON_CONTINUE_ID:
                        DLManager.getInstance(mContext).dlStart(mDownloadUrl, mDownloadPath,
                                                                DLNotification.this);
                        registerButtonPausePendingIntent(mButtonRightViewId);
                        mRemoteViews.setTextViewText(R.id.notification_dl_status,
                                                     mContext.getResources().getText(
                                                         R.string.downloader_noti_downloading));
                        mRemoteViews.setViewVisibility(mButtonLeftViewId, View.GONE);
                        mNotificationManager.notify(mNotificationId, mNotification);
                        break;
                    case BUTTON_PAUSE_ID:
                        DLManager.getInstance(mContext).dlStop(mDownloadUrl);
                        registerButtonContinuePendingIntent(mButtonRightViewId);
                        registerButtonCancelPendingIntent(mButtonLeftViewId);
                        mRemoteViews.setTextViewText(R.id.notification_dl_status,
                                                     mContext.getResources().getText(
                                                         R.string.downloader_noti_pause));
                        mNotificationManager.notify(mNotificationId, mNotification);
                        break;
                    case BUTTON_CANCEL_ID:
                        DLManager.getInstance(mContext).dlCancel(mDownloadUrl);
                        mNotificationManager.cancel(mNotificationId);
                        // 获取sp文件句柄
                        SharedPreferences sp =
                            mContext.getSharedPreferences(DLNotificationManager.SP_FILE_NAME,
                                                          Context.MODE_PRIVATE);
                        sp.edit().remove(mPackageName).commit();
                        if (mBtnReceiver != null) {
                            mContext.unregisterReceiver(mBtnReceiver);
                            mBtnReceiver = null;
                        }
                        break;
                }
            }
        }
    }

}
