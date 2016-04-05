package com.cooeeui.downloader.core.notification;

import java.io.File;

/**
 * Created by hugo.ye on 2016/4/5.
 */
public interface IDLFileListener {

    void onFileExist(String packageName, File file);

}
