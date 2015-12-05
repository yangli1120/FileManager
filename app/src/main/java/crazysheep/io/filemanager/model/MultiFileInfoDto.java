package crazysheep.io.filemanager.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import java.util.ArrayList;

/**
 * file info bean for multi files
 *
 * Created by crazysheep on 15/12/5.
 */
@ParcelablePlease
public class MultiFileInfoDto extends FileInfoDto implements Parcelable {

    public int filecount;
    public int totalfilesize;
    public ArrayList<SingleFileInfoDto> fileinfoList;

    public void addSingleFileInfo(@NonNull SingleFileInfoDto fileInfoDto) {
        if(fileinfoList == null)
            fileinfoList = new ArrayList<>();

        fileinfoList.add(fileInfoDto);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        MultiFileInfoDtoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<MultiFileInfoDto> CREATOR = new Creator<MultiFileInfoDto>() {
        public MultiFileInfoDto createFromParcel(Parcel source) {
            MultiFileInfoDto target = new MultiFileInfoDto();
            MultiFileInfoDtoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public MultiFileInfoDto[] newArray(int size) {
            return new MultiFileInfoDto[size];
        }
    };
}
