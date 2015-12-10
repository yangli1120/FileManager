package crazysheep.io.filemanager.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import java.io.File;

import crazysheep.io.filemanager.utils.FileUtils;

/**
 * file info bean
 *
 * Created by crazysheep on 15/12/5.
 */
@ParcelablePlease
public class SingleFileInfoDto extends FileInfoDto implements Parcelable {

    public String filetype;
    public String filepath;
    public long filesize;
    public long lastmodified;

    /**
     * parse file info from target file
     * */
    public static SingleFileInfoDto parseInfoFromFile(@NonNull File file) {
        SingleFileInfoDto infoDto = new SingleFileInfoDto();
        infoDto.filepath = file.getAbsolutePath();
        infoDto.filesize = FileUtils.sizeOfFile(file);
        infoDto.filetype = FileUtils.getMimeType(file);
        infoDto.lastmodified = file.lastModified();

        return infoDto;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        SingleFileInfoDtoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<SingleFileInfoDto> CREATOR = new Creator<SingleFileInfoDto>() {
        public SingleFileInfoDto createFromParcel(Parcel source) {
            SingleFileInfoDto target = new SingleFileInfoDto();
            SingleFileInfoDtoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public SingleFileInfoDto[] newArray(int size) {
            return new SingleFileInfoDto[size];
        }
    };
}
