package crazysheep.io.filemanager.model;

import android.support.annotation.NonNull;

import java.io.File;

/**
 * scan file item object
 *
 * Created by crazysheep on 15/12/4.
 */
public class ScanDirBean {

    public File dir;
    public int lastTopPosition = 0;
    public int lastTopPositionOffset = 0;

    public ScanDirBean(@NonNull File dir, int topPosition, int topPositionOffset) {
        this.dir = dir;
        this.lastTopPosition = topPosition;
        this.lastTopPositionOffset = topPositionOffset;
    }
}
