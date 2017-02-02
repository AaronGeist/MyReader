package aaron.geist.myreader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import aaron.geist.myreader.constant.DBContants;

/**
 * This class is a helper to create/operate database.
 * Created by yzhou7 on 2015/7/27.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "zreader.db";
    // add version to call onUpgrade
    private static final int DATABASE_VERSION = 5;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " + DBContants.WEBSITE_TABLE_NAME
                + " (" + DBContants.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBContants.WEBSITE_COLUMN_NAME + " VARCHAR, "
                + DBContants.WEBSITE_COLUMN_HOMEPAGE + " VARCHAR, "
                + DBContants.WEBSITE_COLUMN_POST_ENTRY_TAG + " VARCHAR, "
                + DBContants.WEBSITE_COLUMN_NAVIGATION_URL + " VARCHAR, "
                + DBContants.WEBSITE_COLUMN_SELECT_INNER_POST + " VARCHAR)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + DBContants.POST_TABLE_NAME
                + " (" + DBContants.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBContants.POST_COLUMN_TITLE + " VARCHAR, "
                + DBContants.POST_COLUMN_CONTENT + " TEXT, "
                + DBContants.POST_COLUMN_EXTERNAL_ID + " NUMBER, "
                + DBContants.POST_COLUMN_URL + " VARCHAR, "
                + DBContants.POST_COLUMN_WEBSITE_ID + " NUMBER, "
                + DBContants.POST_COLUMN_STARED + " INT DEFAULT 0, "
                + DBContants.POST_COLUMN_READ + " INT DEFAULT 0, "
                + DBContants.POST_COLUMN_IN_ORDER + " INT DEFAULT 0)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
//        db.execSQL("ALTER TABLE " + DBContants.WEBSITE_TABLE_NAME + " ADD COLUMN other STRING");
        db.execSQL("ALTER TABLE " + DBContants.WEBSITE_TABLE_NAME + " ADD COLUMN " + DBContants.WEBSITE_COLUMN_SELECT_INNER_POST + " VARCHAR");
    }
}
