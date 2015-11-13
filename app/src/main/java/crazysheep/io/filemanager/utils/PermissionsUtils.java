package crazysheep.io.filemanager.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import java.security.Permission;

import crazysheep.io.filemanager.R;

/**
 * Created by crazysheep on 15/11/12.
 */
public class PermissionsUtils {

    /**
     * check if permission granted
     * */
    public static boolean checkPermission(Context context, String permission) {
        return ActivityCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * request permission
     * */
    public static void requestPermission(final Activity activity, final String permission,
                                         final int requestCode) {
        if(ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            Snackbar.make(activity.getWindow().getDecorView(), "", Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(activity, new String[] {permission},
                            requestCode);
                }
            }).show();
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
        }
    }

}
