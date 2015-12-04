package crazysheep.io.filemanager.utils;

import android.support.annotation.NonNull;

import com.orhanobut.logger.Logger;

import crazysheep.io.filemanager.BuildConfig;

/**
 * log utils
 *
 * Created by crazysheep on 15/11/25.
 */
public class L {

    public static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * get class name as TAG
     * */
    public static String getTag(@NonNull Object obj) {
        return obj.getClass().getName();
    }

    /**
     * info log
     * */
    public static void i(@NonNull String tag, @NonNull String msg) {
        if(DEBUG)
            Logger.t(tag).i(msg);
    }

    /**
     * info log with default tag
     * */
    public static void i(@NonNull String msg) {
        if(DEBUG)
            Logger.i(msg);
    }

    /**
     * debug log
     * */
    public static void d(@NonNull String tag, @NonNull String msg) {
        if(DEBUG)
            Logger.t(tag).d(msg);
    }

    /**
     * debug log with default tag
     * */
    public static void d(@NonNull String msg) {
        if(DEBUG)
            Logger.d(msg);
    }

    /**
     * error log
     * */
    public static void e(@NonNull String tag, @NonNull String msg, Exception e) {
        if(DEBUG)
            Logger.t(tag).e(e, msg);
    }

    /**
     * error log with default tag
     * */
    public static void e(@NonNull String msg, Exception e) {
        if(DEBUG)
            Logger.e(e, msg);
    }

}
