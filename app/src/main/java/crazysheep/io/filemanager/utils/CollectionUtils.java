package crazysheep.io.filemanager.utils;

import android.support.annotation.NonNull;

/**
 * collection utils
 *
 * Created by crazysheep on 15/12/25.
 */
public class CollectionUtils {

    /**
     * find position in array
     * */
    public static <T> int findPosition(@NonNull T[] ts, @NonNull T t) {
        for(int i = 0; i < ts.length; i++)
            if(t.equals(ts[i]))
                return i;

        return -1;
    }
}
