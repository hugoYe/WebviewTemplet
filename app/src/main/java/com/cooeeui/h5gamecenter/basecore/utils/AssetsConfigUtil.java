package com.cooeeui.h5gamecenter.basecore.utils;

import android.content.Context;
import android.content.res.AssetManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AssetsConfigUtil {

    private static final String CONFIG_FILE_NAME = "config.ini";

    private static AssetsConfigUtil sInstance;
    private Context mContext;
    private JSONObject mConfig;

    private AssetsConfigUtil() {

    }

    public static AssetsConfigUtil getsInstance() {
        if (sInstance == null) {
            sInstance = new AssetsConfigUtil();
        }
        return sInstance;
    }


    public void init(Context context) {
        mContext = context;
        mConfig = getConfig();
    }


    private JSONObject getConfig() {
        AssetManager assetManager = mContext.getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(CONFIG_FILE_NAME);
            String config = readTextFile(inputStream);
            JSONObject jObject;
            try {
                jObject = new JSONObject(config);
                jObject = jObject.getJSONObject("config");
                return jObject;
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String readTextFile(InputStream inputStream) {
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
        }
        return outputStream.toString();
    }

    public String getGameCenterUrl() {
        if (mConfig != null) {
            try {
                String url = mConfig.getString("gameCenterUrl");
                return url;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getGameUrl() {
        if (mConfig != null) {
            try {
                String url = mConfig.getString("gameUrl");
                return url;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * orientation of the screen.May be one of 0,1
     */
    public int getScreenOrientation() {
        if (mConfig != null) {
            try {
                int oreentation = mConfig.getInt("screenOrientation");
                return oreentation;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public String getUmengAppKey() {
        if (mConfig != null) {
            try {
                String url = mConfig.getString("UMENG_APPKEY");
                return url;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getUmengChannel() {
        if (mConfig != null) {
            try {
                String url = mConfig.getString("UMENG_CHANNEL");
                return url;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getNesUrl() {
        if (mConfig != null) {
            try {
                String url = mConfig.getString("news_url");
                return url;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getNewsNotificationUrl() {
        if (mConfig != null) {
            try {
                String url = mConfig.getString("news_notification_url");
                return url;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
