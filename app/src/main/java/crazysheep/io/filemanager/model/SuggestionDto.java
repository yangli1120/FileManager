package crazysheep.io.filemanager.model;

import android.support.annotation.NonNull;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * table for search suggestions
 *
 * Created by crazysheep on 15/12/15.
 */
@Table(name = "suggestions")
public class SuggestionDto extends BaseModel {

    public static final String COLUMN_SUGGESTION = "suggestion";
    public static final String COLUMN_DATE = "date";
    public static String[] COLUMNS = new String[] {COLUMN_ID, COLUMN_SUGGESTION, COLUMN_DATE};

    @Column(name = COLUMN_SUGGESTION)
    public String suggestion;

    @Column(name = COLUMN_DATE)
    public long date;

    public SuggestionDto() {
        super();
    }

    public SuggestionDto(@NonNull String suggestion, long date) {
        super();

        this.suggestion = suggestion;
        this.date = date;
    }

    @Override
    public String toString() {
        return new StringBuilder(COLUMN_SUGGESTION).append(":").append(suggestion).toString();
    }
}
