package crazysheep.io.filemanager.compat;

import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;

/**
 * Compat class such like support v4 other compat class
 *
 * Created by crazysheep on 15/12/24.
 */
public class ColorCompat {

    /**
     * get color int from target resource
     *
     * @param context Context
     * @param colorRes resource ID of color wanted
     * */
    public static @ColorInt int getColorInt(@NonNull Context context, @ColorRes int colorRes) {
        int colorInt;
        if(APIHelper.checkAPI(Build.VERSION_CODES.LOLLIPOP))
            colorInt = context.getColor(colorRes);
        else
            colorInt = context.getResources().getColor(colorRes, context.getTheme());

        return colorInt;
    }
}
