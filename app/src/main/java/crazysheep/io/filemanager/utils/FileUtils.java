package crazysheep.io.filemanager.utils;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;

/**
 * file utils
 *
 * Created by crazysheep on 15/11/16.
 */
public class FileUtils {

    /**
     * get extension from file
     *
     * @param file The target file to parse extension
     * */
    public static String getExtension(@NonNull File file) {
        return MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
    }

    /**
     * get mimetype from file's extension
     *
     * @param file The target file to parse mimetype
     * */
    public static String getMimeType(@NonNull File file) {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtension(file));
    }

    /**
     * check if target is image file
     *
     * @param file The target file to check if it's a image file
     * */
    public static boolean isPicture(@NonNull File file) {
        String mimetype = getMimeType(file);
        return !TextUtils.isEmpty(mimetype) && mimetype.startsWith("image/");
    }

}
