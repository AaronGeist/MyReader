package aaron.geist.myreader.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import aaron.geist.myreader.activity.MainActivity;
import aaron.geist.myreader.constant.DBConstants;

/**
 * This class is a helper to create/operate database.
 * Created by yzhou7 on 2015/7/27.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "zreader.db";
    // add version to call onUpgrade
    private static final int DATABASE_VERSION = 11;

    public DBHelper() {
        super(MainActivity.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " + DBConstants.WEBSITE_TABLE_NAME
                + " (" + DBConstants.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBConstants.WEBSITE_COLUMN_NAME + " VARCHAR, "
                + DBConstants.WEBSITE_COLUMN_HOMEPAGE + " VARCHAR, "
                + DBConstants.WEBSITE_COLUMN_POST_ENTRY_TAG + " VARCHAR, "
                + DBConstants.WEBSITE_COLUMN_NAVIGATION_URL + " VARCHAR, "
                + DBConstants.WEBSITE_COLUMN_SELECT_INNER_POST + " VARCHAR, "
                + DBConstants.WEBSITE_COLUMN_TYPE + " VARCHAR, "
                + DBConstants.WEBSITE_COLUMN_SELECT_INNER_TIMESTAMP + " VARCHAR)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + DBConstants.POST_TABLE_NAME
                + " (" + DBConstants.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBConstants.POST_COLUMN_TITLE + " VARCHAR, "
                + DBConstants.POST_COLUMN_CONTENT + " TEXT, "
                + DBConstants.POST_COLUMN_EXTERNAL_ID + " NUMBER, "
                + DBConstants.POST_COLUMN_TIMESTAMP + " NUMBER, "
                + DBConstants.POST_COLUMN_URL + " VARCHAR, "
                + DBConstants.POST_COLUMN_WEBSITE_ID + " NUMBER, "
                + DBConstants.POST_COLUMN_STARED + " INT DEFAULT 0, "
                + DBConstants.POST_COLUMN_READ + " INT DEFAULT 0, "
                + DBConstants.POST_COLUMN_IN_ORDER + " INT DEFAULT 0)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
//        db.execSQL("ALTER TABLE " + DBConstants.WEBSITE_TABLE_NAME + " ADD COLUMN other STRING");
        db.execSQL("ALTER TABLE " + DBConstants.WEBSITE_TABLE_NAME + " ADD COLUMN " + DBConstants.WEBSITE_COLUMN_PAGE_NUM + " INT DEFAULT 1");
    }
}
