package crazysheep.io.filemanager.compat;

import android.os.Build;

/**
 * check current api
 *
 * Created by crazysheep on 15/12/24.
 */
public class APIHelper {

    /**
     * check if current api later than target api
     * */
    public static boolean checkAPI(int api) {
        return Build.VERSION.SDK_INT >= api;
    }

}
