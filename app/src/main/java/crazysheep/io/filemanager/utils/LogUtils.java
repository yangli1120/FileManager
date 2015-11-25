package crazysheep.io.filemanager.utils;

import android.support.annotation.NonNull;

/**
 * Created by crazysheep on 15/11/25.
 */
public class LogUtils {

    /**
     * get class name as TAG
     * */
    public static String getTag(@NonNull Object obj) {
        return obj.getClass().getName();
    }
}
