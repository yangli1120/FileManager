package crazysheep.io.filemanager.application;

import com.activeandroid.ActiveAndroid;
import com.facebook.stetho.Stetho;
import com.orhanobut.logger.Logger;

/**
 * base application
 *
 * Created by crazysheep on 15/11/25.
 */
public class BaseApplication extends com.activeandroid.app.Application {

    public static final String TAG = "filemanager";

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.init(TAG);
        ActiveAndroid.initialize(this);
        Stetho.initializeWithDefaults(this);
    }
}
