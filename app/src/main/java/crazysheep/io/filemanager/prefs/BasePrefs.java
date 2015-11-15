package crazysheep.io.filemanager.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Field;

/**
 * base prefs utils
 *
 * Created by crazysheep on 15/11/15.
 */
public class BasePrefs {

    public static final String TAG = BasePrefs.class.getSimpleName();

    public static final String PREFS_NAME = "crazysheep.io.filemanager";

    protected final SharedPreferences mSharedPrefs;
    private Context mContext;

    public BasePrefs(Context context) {
        mContext = context;
        Class clazz = getClass();
        try {
            Field prefsNameField = clazz.getField("PREFS_NAME");
            String prefsNameStr = (String) prefsNameField.get(null);

            mSharedPrefs = context.getSharedPreferences(prefsNameStr, Context.MODE_PRIVATE);
        } catch (NoSuchFieldException nsfe) {
            nsfe.printStackTrace();

            throw new RuntimeException("can not find PREFS_NAME field");
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();

            throw new RuntimeException("PREFS_NAME field is NOT string");
        }
    }

    public String getString(String key, String defaultValue) {
        return mSharedPrefs.getString(key, defaultValue);
    }

    public void setString(String key, String value) {
        mSharedPrefs.edit().putString(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return mSharedPrefs.getInt(key, defaultValue);
    }

    public void setInt(String key, int value) {
        mSharedPrefs.edit().putInt(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return mSharedPrefs.getBoolean(key, defaultValue);
    }

    public void setBoolean(String key, boolean value) {
        mSharedPrefs.edit().putBoolean(key, value).apply();
    }

}
