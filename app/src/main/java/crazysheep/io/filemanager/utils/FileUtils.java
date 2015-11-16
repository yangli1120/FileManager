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

    /**
     * for file byte count for human readable
     * */
    public static String formatFileSize(long bytecount) {
        if (bytecount <= 0) {
            return "0kb";
        } else {
            StringBuilder sb = new StringBuilder();
            if(bytecount >= 1024 * 1024 * 1024) { // GB
                float gbSize = bytecount * 1f / (1024 * 1024 * 1024);

                return sb.append(String.format("%.2f", gbSize)).append("GB").toString();
            } else if(bytecount >= 1024 * 1024) { // MB
                float mbSize = bytecount * 1f / (1024 * 1024);

                return sb.append(String.format("%.2f", mbSize)).append("MB").toString();
            } else if(bytecount >= 1024) { // KB
                float kbSize = bytecount * 1f / 1024;

                return sb.append(String.format("%.2f", kbSize)).append("KB").toString();
            } else { // byte
                float byteSize = bytecount * 1f;

                return sb.append(String.format("%.2f", byteSize)).append("b").toString();
            }
        }
    }

}
