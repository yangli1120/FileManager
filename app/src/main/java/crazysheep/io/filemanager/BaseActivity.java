package crazysheep.io.filemanager;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by crazysheep on 15/11/25.
 */
public class BaseActivity extends AppCompatActivity {

    protected static String TAG = BaseActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = this.getClass().getName();
    }

    protected final Activity getActivity() {
        return this;
    }

}
