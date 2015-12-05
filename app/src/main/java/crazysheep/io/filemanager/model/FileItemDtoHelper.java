package crazysheep.io.filemanager.model;

import android.support.annotation.NonNull;

import java.io.File;

/**
 * a helper for create FileItemDto
 *
 * Created by crazysheep on 15/11/20.
 */
public class FileItemDtoHelper {

    /**
     * create FileItemDto from target file
     * */
    public static FileItemDto createFileItemModelFromFile(@NonNull File file) {
        FileItemDto itemModel = new FileItemDto();
        itemModel.filename = file.getName();
        itemModel.filepath = file.getAbsolutePath();
        itemModel.filetype = file.isDirectory() ? FileItemDto.TYPE_DIR : FileItemDto.TYPE_FILE;
        itemModel.isHidden = file.isHidden();
        itemModel.subfileCount = file.isDirectory()
                ? file.listFiles().length : FileItemDto.ILLEGAL_SUBFILE_COUNT;
        itemModel.fileByteCount = file.isDirectory()
                ? FileItemDto.ILEGAL_FILE_BYTE_COUNT : file.length();
        itemModel.fileLastModified = file.lastModified();

        return itemModel;
    }
}
