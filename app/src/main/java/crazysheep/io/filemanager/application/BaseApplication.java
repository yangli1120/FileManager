package crazysheep.io.filemanager.application;

import android.app.Application;

import com.orhanobut.logger.Logger;

/**
 * Created by crazysheep on 15/11/25.
 */
public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.init(getPackageName());
    }
}
