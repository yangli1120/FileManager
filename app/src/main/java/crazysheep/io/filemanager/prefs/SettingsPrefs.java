package crazysheep.io.filemanager.prefs;

import android.content.Context;

/**
 * Created by crazysheep on 15/11/15.
 */
public class SettingsPrefs extends BasePrefs {

    public static final String TAG = SettingsPrefs.class.getSimpleName();

    public static final String PREFS_NAME = "crazysheep.io.filemanager.settings";

    public static final String KEY_SHOW_HIDDEN_FILES = "key_show_hidden_files"; // boolean

    public SettingsPrefs(Context context) {
        super(context);
    }

    public boolean getShowHiddenFiles() {
        return getBoolean(KEY_SHOW_HIDDEN_FILES, false);
    }

    public void setShowHiddenFiles(boolean show) {
        setBoolean(KEY_SHOW_HIDDEN_FILES, show);
    }

}
