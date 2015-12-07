package crazysheep.io.filemanager.model;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * change FileItemDto to File
     * */
    public static File changeItem2File(@NonNull FileItemDto itemDto) {
        return new File(itemDto.filepath);
    }

    /**
     * change List<FileItemDto> to List<File>
     * */
    public static List<File> changeItems2Files(@NonNull List<FileItemDto> itemDtos) {
        List<File> files = new ArrayList<>(itemDtos.size());
        for(FileItemDto itemDto : itemDtos)
            files.add(changeItem2File(itemDto));

        return files;
    }

}
