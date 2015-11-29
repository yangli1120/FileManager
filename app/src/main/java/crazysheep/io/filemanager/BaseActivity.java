package crazysheep.io.filemanager;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * base activity
 *
 * Created by crazysheep on 15/11/25.
 */
public class BaseActivity extends AppCompatActivity {

    protected static String TAG = BaseActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = this.getClass().getSimpleName();
    }

    protected final Activity getActivity() {
        return this;
    }

}
