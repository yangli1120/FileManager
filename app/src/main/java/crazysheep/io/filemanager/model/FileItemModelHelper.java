package crazysheep.io.filemanager.model;

import android.support.annotation.NonNull;

import java.io.File;

/**
 * a helper for create FileItemModel
 *
 * Created by crazysheep on 15/11/20.
 */
public class FileItemModelHelper {

    /**
     * create FileItemModel from target file
     * */
    public static FileItemModel createFileItemModelFromFile(@NonNull File file) {
        FileItemModel itemModel = new FileItemModel();
        itemModel.filename = file.getName();
        itemModel.filepath = file.getAbsolutePath();
        itemModel.filetype = file.isDirectory() ? FileItemModel.TYPE_DIR : FileItemModel.TYPE_FILE;
        itemModel.isHidden = file.isHidden();
        itemModel.subfileCount = file.isDirectory()
                ? file.listFiles().length : FileItemModel.ILLEGAL_SUBFILE_COUNT;
        itemModel.fileByteCount = file.isDirectory()
                ? FileItemModel.ILEGAL_FILE_BYTE_COUNT : file.length();
        itemModel.fileLastModified = file.lastModified();

        return itemModel;
    }
}
