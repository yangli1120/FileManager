package crazysheep.io.filemanager.model;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

/**
 * Created by crazysheep on 15/12/22.
 */
public class FileSuggestion implements SearchSuggestion {

    public SuggestionDto mSuggestionDto;

    public FileSuggestion(@NonNull SuggestionDto suggestionDto) {
        mSuggestionDto = suggestionDto;
    }

    public FileSuggestion(@NonNull Parcel in) {
        mSuggestionDto = new SuggestionDto(in.readString(), in.readLong());
    }

    @Override
    public String getBody() {
        return mSuggestionDto.suggestion;
    }

    @Override
    public Creator getCreator() {
        return CREATOR;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSuggestionDto.suggestion);
        dest.writeLong(mSuggestionDto.date);
    }

    public static final Creator<FileSuggestion> CREATOR = new Creator<FileSuggestion>() {
        public FileSuggestion createFromParcel(Parcel source) {
            return new FileSuggestion(source);
        }

        public FileSuggestion[] newArray(int size) {
            return new FileSuggestion[size];
        }
    };
}
