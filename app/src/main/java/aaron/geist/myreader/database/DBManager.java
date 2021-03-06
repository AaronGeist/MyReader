package aaron.geist.myreader.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import aaron.geist.myreader.constant.DBConstants;
import aaron.geist.myreader.domain.Post;
import aaron.geist.myreader.domain.Website;

/**
 * Created by Aaron on 2015/7/27.
 */
public class DBManager {

    private volatile static DBManager instance = null;

    private SQLiteDatabase db;

    private DBManager() {
        DBHelper helper = new DBHelper();
        db = helper.getWritableDatabase();
    }

    /**
     * Singleton mode.
     *
     * @return instance
     */
    public static DBManager getInstance() {
        if (instance == null) {
            synchronized (DBManager.class) {
                if (instance == null) {
                    instance = new DBManager();
                }
            }
        }

        return instance;
    }

    public long addWebsite(Website website) {
        long siteId = -1L;

        db.beginTransaction();
        Cursor c = null;
        try {
            // check if exists
            c = db.rawQuery("SELECT * FROM " + DBConstants.WEBSITE_TABLE_NAME + " WHERE " +
                    DBConstants.WEBSITE_COLUMN_NAME + " = ?", new String[]{website.getName()});

            ContentValues cv = new ContentValues();
            cv.put(DBConstants.WEBSITE_COLUMN_NAME, website.getName());
            cv.put(DBConstants.WEBSITE_COLUMN_TYPE, website.getType());
            cv.put(DBConstants.WEBSITE_COLUMN_HOMEPAGE, website.getHomePage());
            cv.put(DBConstants.WEBSITE_COLUMN_POST_ENTRY_TAG, website.getPostEntryTag());
            cv.put(DBConstants.WEBSITE_COLUMN_NAVIGATION_URL, website.getNavigationUrl());
            cv.put(DBConstants.WEBSITE_COLUMN_SELECT_INNER_POST, website.getInnerPostSelect());
            cv.put(DBConstants.WEBSITE_COLUMN_SELECT_INNER_TIMESTAMP, website.getInnerTimestampSelect());
            cv.put(DBConstants.WEBSITE_COLUMN_PAGE_NUM, website.getPageNum());


            if (c.moveToNext()) {
                siteId = c.getInt(c.getColumnIndex(DBConstants.COLUMN_ID));
                String[] args = {String.valueOf(siteId)};
                db.update(DBConstants.WEBSITE_TABLE_NAME, cv, DBConstants.COLUMN_ID + "=?", args);
                Log.d("", "update website successful, siteId=" + siteId);

            } else {
                siteId = db.insert(DBConstants.WEBSITE_TABLE_NAME, null, cv);
                if (siteId == -1) {
                    Log.e("", "addSite failed, name=" + website.getName());
                } else {
                    Log.d("", "addSite website successful, siteId=" + siteId);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            if (c != null) {
                c.close();
            }
            db.endTransaction();

        }

        return siteId;
    }

    public void removeWebsite(Website website) {
        db.delete(DBConstants.WEBSITE_TABLE_NAME, DBConstants.WEBSITE_COLUMN_NAME + " = ?", new String[]{website.getName()});
    }

    public List<Website> getAllWebsites() {
        List<Website> websites = new ArrayList<Website>();
        Cursor c = db.rawQuery("SELECT * FROM " + DBConstants.WEBSITE_TABLE_NAME, null);
        while (c.moveToNext()) {
            Website website = new Website();
            website.setId(c.getInt(c.getColumnIndex(DBConstants.COLUMN_ID)));
            website.setName(c.getString(c.getColumnIndex(DBConstants.WEBSITE_COLUMN_NAME)));
            website.setType(c.getString(c.getColumnIndex(DBConstants.WEBSITE_COLUMN_TYPE)));
            website.setHomePage(c.getString(c.getColumnIndex(DBConstants.WEBSITE_COLUMN_HOMEPAGE)));
            website.setPostEntryTag(c.getString(c.getColumnIndex(DBConstants.WEBSITE_COLUMN_POST_ENTRY_TAG)));
            website.setNavigationUrl(c.getString(c.getColumnIndex(DBConstants.WEBSITE_COLUMN_NAVIGATION_URL)));
            website.setInnerPostSelect(c.getString(c.getColumnIndex(DBConstants.WEBSITE_COLUMN_SELECT_INNER_POST)));
            website.setInnerTimestampSelect(c.getString(c.getColumnIndex(DBConstants.WEBSITE_COLUMN_SELECT_INNER_TIMESTAMP)));
            website.setPageNum(c.getInt(c.getColumnIndex(DBConstants.WEBSITE_COLUMN_PAGE_NUM)));

            websites.add(website);
        }
        c.close();
        return websites;
    }

    public Website getWebsiteById(long websiteId) {
        Website website = new Website();
        Cursor c = db.rawQuery("SELECT * FROM " + DBConstants.WEBSITE_TABLE_NAME + " WHERE " +
                DBConstants.COLUMN_ID + " =?", new String[]{String.valueOf(websiteId)});
        while (c.moveToNext()) {
            website.setId(c.getInt(c.getColumnIndex(DBConstants.COLUMN_ID)));
            website.setName(c.getString(c.getColumnIndex(DBConstants.WEBSITE_COLUMN_NAME)));
            website.setHomePage(c.getString(c.getColumnIndex(DBConstants.WEBSITE_COLUMN_HOMEPAGE)));
            website.setPostEntryTag(c.getString(c.getColumnIndex(DBConstants.WEBSITE_COLUMN_POST_ENTRY_TAG)));
            website.setNavigationUrl(c.getString(c.getColumnIndex(DBConstants.WEBSITE_COLUMN_NAVIGATION_URL)));
            website.setInnerPostSelect(c.getString(c.getColumnIndex(DBConstants.WEBSITE_COLUMN_SELECT_INNER_POST)));
            website.setInnerTimestampSelect(c.getString(c.getColumnIndex(DBConstants.WEBSITE_COLUMN_SELECT_INNER_TIMESTAMP)));
            website.setPageNum(c.getInt(c.getColumnIndex(DBConstants.WEBSITE_COLUMN_PAGE_NUM)));
        }
        c.close();
        return website;
    }

    public void updateWebsitePageNo(long websiteId, int pageNum) {
        db.beginTransaction();
        Cursor c = null;
        try {
            // check if exists
            c = db.rawQuery("SELECT * FROM " + DBConstants.WEBSITE_TABLE_NAME + " WHERE " +
                    DBConstants.COLUMN_ID + " = ?", new String[]{String.valueOf(websiteId)});
            ContentValues cv = new ContentValues();
            cv.put(DBConstants.WEBSITE_COLUMN_PAGE_NUM, pageNum);

            if (c.moveToNext()) {
                websiteId = c.getInt(c.getColumnIndex(DBConstants.COLUMN_ID));
                String[] args = {String.valueOf(websiteId)};
                db.update(DBConstants.WEBSITE_TABLE_NAME, cv, DBConstants.COLUMN_ID + "=?", args);
                Log.d("", "update post successful");
            }
            db.setTransactionSuccessful();
        } finally {
            if (c != null) {
                c.close();
            }
            db.endTransaction();
        }
    }

    public long addPost(Post post) {
        long postId = -1L;
        db.beginTransaction();
        Cursor c = null;
        try {
            // check if exists
            c = db.rawQuery("SELECT * FROM " + DBConstants.POST_TABLE_NAME + " WHERE " +
                    DBConstants.POST_COLUMN_EXTERNAL_ID + " = ?", new String[]{String.valueOf(post.getExternalId())});
            ContentValues cv = new ContentValues();
            cv.put(DBConstants.POST_COLUMN_TITLE, post.getTitle());
            cv.put(DBConstants.POST_COLUMN_CONTENT, post.getContent());
            cv.put(DBConstants.POST_COLUMN_EXTERNAL_ID, post.getExternalId());
            cv.put(DBConstants.POST_COLUMN_TIMESTAMP, post.getTimestamp());
            cv.put(DBConstants.POST_COLUMN_URL, post.getUrl());
            cv.put(DBConstants.POST_COLUMN_WEBSITE_ID, post.getWebsiteId());
            cv.put(DBConstants.POST_COLUMN_IN_ORDER, post.isInOrder());
            cv.put(DBConstants.POST_COLUMN_HASH, post.getHash());


            if (c.moveToNext()) {
                postId = c.getInt(c.getColumnIndex(DBConstants.COLUMN_ID));
                String[] args = {String.valueOf(postId)};
                db.update(DBConstants.POST_TABLE_NAME, cv, DBConstants.COLUMN_ID + "=?", args);
                Log.d("", "update post successful");

            } else {
                postId = db.insert(DBConstants.POST_TABLE_NAME, null, cv);
                Log.d("", "insert post successful");
            }
            db.setTransactionSuccessful();
        } finally {
            if (c != null) {
                c.close();
            }
            db.endTransaction();
        }

        return postId;
    }

    public void removePost(Post post) {
        db.delete(DBConstants.POST_TABLE_NAME, DBConstants.POST_COLUMN_EXTERNAL_ID + " = ?", new String[]{String.valueOf(post.getExternalId())});
    }

    public List<Post> getAllPostsBySiteId(long siteId) {
        List<Post> posts = new ArrayList<Post>();
        Cursor c = db.query(DBConstants.POST_TABLE_NAME, null,
                DBConstants.POST_COLUMN_WEBSITE_ID + "=" + siteId, null, null, null, DBConstants.POST_COLUMN_TIMESTAMP + ", " + DBConstants.POST_COLUMN_EXTERNAL_ID + " DESC");
        while (c.moveToNext()) {
            posts.add(convertPost(c));
        }
        c.close();
        return posts;
    }

    public List<Post> getPosts(int pageNum, Collection<Long> websiteIds) {
        List<Post> posts = new ArrayList<>();

        String inSelection = "(" +
                Joiner.on(",").join(websiteIds) +
                ")";

        Cursor c = db.query(DBConstants.POST_TABLE_NAME, null,
                DBConstants.POST_COLUMN_WEBSITE_ID + " in " + inSelection, null, null, null,
                DBConstants.POST_COLUMN_TIMESTAMP + " DESC, " + DBConstants.POST_COLUMN_EXTERNAL_ID + " DESC",
                (pageNum - 1) * DBConstants.pageSize + "," + DBConstants.pageSize);
        while (c.moveToNext()) {

            posts.add(convertPost(c));
        }
        c.close();
        return posts;
    }

    public List<Post> getUnreadPosts(int pageNum, Collection<Long> websiteIds) {
        List<Post> posts = new ArrayList<>();

        String inSelection = "(" +
                Joiner.on(",").join(websiteIds) +
                ")";

        Cursor c = db.query(DBConstants.POST_TABLE_NAME, null,
                DBConstants.POST_COLUMN_WEBSITE_ID + " in " + inSelection + " AND " + DBConstants.POST_COLUMN_READ + " = 0", null, null, null,
                DBConstants.POST_COLUMN_TIMESTAMP + " DESC, " + DBConstants.POST_COLUMN_EXTERNAL_ID + " DESC",
                (pageNum - 1) * DBConstants.pageSize + "," + DBConstants.pageSize);
        while (c.moveToNext()) {
            posts.add(convertPost(c));
        }
        c.close();
        return posts;
    }

    private Post convertPost(Cursor c) {
        Post post = new Post();
        post.setId(c.getLong(c.getColumnIndex(DBConstants.COLUMN_ID)));
        post.setTitle(c.getString(c.getColumnIndex(DBConstants.POST_COLUMN_TITLE)));
        post.setContent(c.getString(c.getColumnIndex(DBConstants.POST_COLUMN_CONTENT)));
        post.setExternalId(c.getInt(c.getColumnIndex(DBConstants.POST_COLUMN_EXTERNAL_ID)));
        post.setTimestamp(c.getLong(c.getColumnIndex(DBConstants.POST_COLUMN_TIMESTAMP)));
        post.setUrl(c.getString(c.getColumnIndex(DBConstants.POST_COLUMN_URL)));
        post.setWebsiteId(c.getLong(c.getColumnIndex(DBConstants.POST_COLUMN_WEBSITE_ID)));
        post.setMarked(c.getInt(c.getColumnIndex(DBConstants.POST_COLUMN_STARED)) != 0);
        post.setRead(c.getInt(c.getColumnIndex(DBConstants.POST_COLUMN_READ)) != 0);
        post.setInOrder(c.getInt(c.getColumnIndex(DBConstants.POST_COLUMN_IN_ORDER)) != 0);
        post.setHash(c.getString(c.getColumnIndex(DBConstants.POST_COLUMN_HASH)));

        return post;
    }

    public boolean isPostExists(String hash) {
        boolean result = false;
        Cursor c = db.query(DBConstants.POST_TABLE_NAME, null,
                DBConstants.POST_COLUMN_HASH + "=\"" + hash + "\"", null, null, null, null);
        while (c.moveToNext()) {
            result = true;
        }
        c.close();
        return result;
    }

    public void updatePostStar(long postId, boolean isStared) {
        db.beginTransaction();
        Cursor c = null;
        try {
            // check if exists
            c = db.rawQuery("SELECT * FROM " + DBConstants.POST_TABLE_NAME + " WHERE " +
                    DBConstants.COLUMN_ID + " = ?", new String[]{String.valueOf(postId)});
            ContentValues cv = new ContentValues();
            cv.put(DBConstants.POST_COLUMN_STARED, isStared ? 1 : 0);

            if (c.moveToNext()) {
                postId = c.getInt(c.getColumnIndex(DBConstants.COLUMN_ID));
                String[] args = {String.valueOf(postId)};
                db.update(DBConstants.POST_TABLE_NAME, cv, DBConstants.COLUMN_ID + "=?", args);
                Log.d("", "update post successful");
            }
            db.setTransactionSuccessful();
        } finally {
            if (c != null) {
                c.close();
            }
            db.endTransaction();
        }
    }

    public void updatePostRead(long postId, boolean isRead) {
        db.beginTransaction();
        Cursor c = null;
        try {
            // check if exists
            c = db.rawQuery("SELECT * FROM " + DBConstants.POST_TABLE_NAME + " WHERE " +
                    DBConstants.COLUMN_ID + " = ?", new String[]{String.valueOf(postId)});
            ContentValues cv = new ContentValues();
            cv.put(DBConstants.POST_COLUMN_READ, isRead ? 1 : 0);

            if (c.moveToNext()) {
                postId = c.getInt(c.getColumnIndex(DBConstants.COLUMN_ID));
                String[] args = {String.valueOf(postId)};
                db.update(DBConstants.POST_TABLE_NAME, cv, DBConstants.COLUMN_ID + "=?", args);
                Log.d("", "update post successful");
            }
            db.setTransactionSuccessful();
        } finally {
            if (c != null) {
                c.close();
            }
            db.endTransaction();
        }
    }

    public void removeTable() {
        db.execSQL("DROP TABLE IF EXISTS " + DBConstants.WEBSITE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBConstants.POST_TABLE_NAME);
    }

    public void closeDB() {
        db.close();
    }

}
