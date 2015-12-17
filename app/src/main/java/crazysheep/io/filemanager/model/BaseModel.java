package crazysheep.io.filemanager.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;

/**
 * base model for database
 *
 * Created by crazysheep on 15/12/15.
 */
public class BaseModel extends Model {

    public static final String COLUMN_ID = "_id";

    @Column(name = COLUMN_ID)
    public String id;

    public BaseModel() {
        super();
    }
}
