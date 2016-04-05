package com.cooeeui.downloader.api;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;

import com.cooeeui.downloader.core.notification.DLNotification;
import com.cooeeui.downloader.core.notification.IDLFileListener;
import com.cooeeui.downloaderlib.R;

import java.io.File;
import java.util.HashMap;

/**
 * Created by hugo.ye on 2016/3/30.
 */
public class DLNotificationManager implements IDLFileListener {

    private static final String TAG = DLNotificationManager.class.getSimpleName();

    private static final String DOWN_LOAD_PATH = Environment.getExternalStorageDirectory().
        getPath() + "/Download/";

    public static final String SP_FILE_NAME = "downloader_notification_id";
    private static final String SP_KEY_LAST_ID = "downloader_notification_last_id";

    private static DLNotificationManager sInstance;
    private Context mContext;
    private HashMap<String, File> mFileMap = new HashMap<>();


    private DLNotificationManager(Context context) {
        mContext = context;
    }

    public static DLNotificationManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DLNotificationManager(context);
        }
        return sInstance;
    }

    /**
     * 此函数是判断手机上是否有指定的APP
     *
     * @param context     上下文对象
     * @param packageName app的包名
     * @return 如果手机上已经存在指定的app则返回true 否则返回false
     */
    private boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 将web端传过来的bitmap字串转换为bitmap
     **/
    public Bitmap base64ToBitmap(String base64String) {
        if (base64String.split(",").length > 1) {
            base64String = base64String.split(",")[1];
        }
        try {
            byte[] bytes = Base64.decode(base64String, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param apkurl         apk下载链接地址
     * @param apkPackageName 需要下载的apk包名
     * @param apkName        apk名称
     **/
    public void startDLNotification(String apkurl, String apkPackageName, String apkName) {
        this.startDLNotification(apkurl, apkPackageName, apkName,
                                 R.drawable.downloader_notification_icon,
                                 R.drawable.downloader_notification_warn_icon,
                                 DOWN_LOAD_PATH);
    }


    /**
     * @param apkurl            apk下载链接地址
     * @param apkPackageName    需要下载的apk包名
     * @param apkName           apk名称
     * @param notiIconBase64Str 通知栏提示图标
     **/
    public void startDLNotification(String apkurl, String apkPackageName, String apkName,
                                    String notiIconBase64Str) {
        this.startDLNotification(apkurl, apkPackageName, apkName, notiIconBase64Str,
                                 R.drawable.downloader_notification_warn_icon,
                                 DOWN_LOAD_PATH);
    }


    /**
     * @param apkurl         apk下载链接地址
     * @param apkPackageName 需要下载的apk包名
     * @param apkName        apk名称
     * @param notiIcon       通知栏提示图标
     **/
    public void startDLNotification(String apkurl, String apkPackageName, String apkName,
                                    int notiIcon) {
        this.startDLNotification(apkurl, apkPackageName, apkName, notiIcon,
                                 R.drawable.downloader_notification_warn_icon,
                                 DOWN_LOAD_PATH);
    }

    /**
     * @param apkurl            apk下载链接地址
     * @param apkPackageName    需要下载的apk包名
     * @param apkName           apk名称
     * @param notiIconBase64Str 通知栏提示图标
     * @param notiWarnIcon      状态栏提示图标
     **/
    public void startDLNotification(String apkurl, String apkPackageName, String apkName,
                                    String notiIconBase64Str, int notiWarnIcon) {
        this.startDLNotification(apkurl, apkPackageName, apkName, notiIconBase64Str, notiWarnIcon,
                                 DOWN_LOAD_PATH);
    }

    /**
     * @param apkurl         apk下载链接地址
     * @param apkPackageName 需要下载的apk包名
     * @param apkName        apk名称
     * @param notiIcon       通知栏提示图标
     * @param notiWarnIcon   状态栏提示图标
     **/
    public void startDLNotification(String apkurl, String apkPackageName, String apkName,
                                    int notiIcon, int notiWarnIcon) {
        this.startDLNotification(apkurl, apkPackageName, apkName, notiIcon, notiWarnIcon,
                                 DOWN_LOAD_PATH);
    }

    /**
     * @param apkurl            apk下载链接地址
     * @param apkPackageName    需要下载的apk包名
     * @param apkName           apk名称
     * @param notiIconBase64Str 通知栏提示图标,可以为null或者“”，使用系统默认的
     * @param notiWarnIcon      状态栏提示图标,传入“-1”则使用系统默认的
     * @param downloadPath      下载保存路径,如果为null或者“”，则使用默认保存路径
     **/
    public void startDLNotification(String apkurl, String apkPackageName, String apkName,
                                    String notiIconBase64Str, int notiWarnIcon,
                                    String downloadPath) {
        // 获取sp文件句柄
        SharedPreferences sp = mContext.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        // 确保通知栏ID在100-1000范围内
        if (sp.getInt(SP_KEY_LAST_ID, 100) >= 1000) {
            sp.edit().remove(SP_KEY_LAST_ID).commit();
        }

        // 生成新notification id
        int notificationId = 100;

        //之前有的id继续使用
        if (sp.getInt(apkPackageName, -1) != -1) {
            notificationId = sp.getInt(apkPackageName, -1);
        } else {
            notificationId = sp.getInt(SP_KEY_LAST_ID, 100) + 1;
            // 保存最近生成的notification id
            sp.edit().putInt(SP_KEY_LAST_ID, notificationId).commit();
            // 保存下载应用对应的notification id
            sp.edit().putInt(apkPackageName, notificationId).commit();
        }

        Bitmap notificationIcon = null;
        if (notiIconBase64Str != null) {
            notificationIcon = base64ToBitmap(notiIconBase64Str);
        }

        if (downloadPath == null || TextUtils.isEmpty(downloadPath)) {
            downloadPath = DOWN_LOAD_PATH;
        }

        DLNotification dlNotification = new DLNotification(mContext,
                                                           apkurl, apkPackageName, apkName,
                                                           notificationId,
                                                           notificationIcon, notiWarnIcon,
                                                           downloadPath);
        dlNotification.startDLNotification();
    }

    /**
     * @param apkurl         apk下载链接地址
     * @param apkPackageName 需要下载的apk包名
     * @param apkName        apk名称
     * @param notiIcon       通知栏提示图标,传入“-1”则使用系统默认的
     * @param notiWarnIcon   状态栏提示图标,传入“-1”则使用系统默认的
     * @param downloadPath   下载保存路径,如果为null或者“”，则使用默认保存路径
     **/
    public void startDLNotification(String apkurl, String apkPackageName, String apkName,
                                    int notiIcon, int notiWarnIcon, String downloadPath) {
        // 获取sp文件句柄
        SharedPreferences sp = mContext.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        // 确保通知栏ID在100-1000范围内
        if (sp.getInt(SP_KEY_LAST_ID, 100) >= 1000) {
            sp.edit().remove(SP_KEY_LAST_ID).commit();
        }

        // 生成新notification id
        int notificationId = 100;

        //之前有的id继续使用
        if (sp.getInt(apkPackageName, -1) != -1) {
            notificationId = sp.getInt(apkPackageName, -1);
        } else {
            notificationId = sp.getInt(SP_KEY_LAST_ID, 100) + 1;
            // 保存最近生成的notification id
            sp.edit().putInt(SP_KEY_LAST_ID, notificationId).commit();
            // 保存下载应用对应的notification id
            sp.edit().putInt(apkPackageName, notificationId).commit();
        }

        if (downloadPath == null || TextUtils.isEmpty(downloadPath)) {
            downloadPath = DOWN_LOAD_PATH;
        }

        DLNotification dlNotification = new DLNotification(mContext,
                                                           apkurl, apkPackageName, apkName,
                                                           notificationId,
                                                           notiIcon, notiWarnIcon,
                                                           downloadPath);
        dlNotification.setFileListener(this);
        dlNotification.startDLNotification();
    }

    public void appInstalled(String apkPackageName) {
        // 获取sp文件句柄
        SharedPreferences sp = mContext.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        int notificationId = sp.getInt(apkPackageName, -1);
        if (notificationId != -1) {
            NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
            sp.edit().remove(apkPackageName).commit();
        }
        if (mFileMap.containsKey(apkPackageName)) {
            File file = mFileMap.get(apkPackageName);
            if (file.exists()) {
                file.delete();
            }
            mFileMap.remove(apkPackageName);
        }
    }

    @Override
    public void onFileExist(String packageName, File file) {
        if (!mFileMap.containsKey(packageName)) {
            mFileMap.put(packageName, file);
        }
    }
}
