package crazysheep.io.filemanager.utils;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * utils for SnackBar
 *
 * Created by crazysheep on 15/12/5.
 */
public class SnackBarUtils {

    /**
     * show SnackBar
     * */
    public static void show(@NonNull View attach, String msg, int time) {
        Snackbar.make(attach, msg, time).show();
    }

    /**
     * show SnackBar
     * */
    public static void show(@NonNull View attach, int res, int time) {
        Snackbar.make(attach, res, time).show();
    }

    public static void dismiss(Snackbar snackbar) {
        if(snackbar != null && snackbar.isShownOrQueued())
            snackbar.dismiss();
    }

}
