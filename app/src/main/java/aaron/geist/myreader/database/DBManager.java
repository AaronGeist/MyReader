package aaron.geist.myreader.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import aaron.geist.myreader.constant.DBContants;
import aaron.geist.myreader.domain.Post;
import aaron.geist.myreader.domain.Website;

/**
 * Created by Aaron on 2015/7/27.
 */
public class DBManager {

    private DBHelper helper;
    private SQLiteDatabase db;

    public DBManager(Context context) {
        helper = new DBHelper(context);
        db = helper.getWritableDatabase();
    }

    public long addWebsite(Website website) {
        long siteId = -1L;

        db.beginTransaction();
        Cursor c = null;
        try {
            // check if exists
            c = db.rawQuery("SELECT * FROM " + DBContants.WEBSITE_TABLE_NAME + " WHERE " +
                    DBContants.WEBSITE_COLUMN_NAME + " = ?", new String[]{website.getName()});

            ContentValues cv = new ContentValues();
            cv.put(DBContants.WEBSITE_COLUMN_NAME, website.getName());
            cv.put(DBContants.WEBSITE_COLUMN_HOMEPAGE, website.getHomePage());
            cv.put(DBContants.WEBSITE_COLUMN_POST_ENTRY_TAG, website.getPostEntryTag());
            cv.put(DBContants.WEBSITE_COLUMN_NAVIGATION_URL, website.getNavigationUrl());

            if (c.moveToNext()) {
                siteId = c.getInt(c.getColumnIndex(DBContants.COLUMN_ID));
                String[] args = {String.valueOf(siteId)};
                db.update(DBContants.WEBSITE_TABLE_NAME, cv, DBContants.COLUMN_ID + "=?", args);
                Log.d("", "update website successful, siteId=" + siteId);

            } else {
                siteId = db.insert(DBContants.WEBSITE_TABLE_NAME, null, cv);
                Log.d("", "addSite website successful, siteId=" + siteId);
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
        db.delete(DBContants.WEBSITE_TABLE_NAME, DBContants.WEBSITE_COLUMN_NAME + " = ?", new String[]{website.getName()});
    }

    public List<Website> getAllWebsites() {
        List<Website> websites = new ArrayList<Website>();
        Cursor c = db.rawQuery("SELECT * FROM " + DBContants.WEBSITE_TABLE_NAME, null);
        while (c.moveToNext()) {
            Website website = new Website();
            website.setId(c.getInt(c.getColumnIndex(DBContants.COLUMN_ID)));
            website.setName(c.getString(c.getColumnIndex(DBContants.WEBSITE_COLUMN_NAME)));
            website.setHomePage(c.getString(c.getColumnIndex(DBContants.WEBSITE_COLUMN_HOMEPAGE)));
            website.setPostEntryTag(c.getString(c.getColumnIndex(DBContants.WEBSITE_COLUMN_POST_ENTRY_TAG)));
            website.setNavigationUrl(c.getString(c.getColumnIndex(DBContants.WEBSITE_COLUMN_NAVIGATION_URL)));
            websites.add(website);
        }
        c.close();
        return websites;
    }

    public Website getWebsiteById(long websiteId) {
        Website website = new Website();
        Cursor c = db.rawQuery("SELECT * FROM " + DBContants.WEBSITE_TABLE_NAME + " WHERE " +
                DBContants.COLUMN_ID + " =?", new String[]{String.valueOf(websiteId)});
        while (c.moveToNext()) {
            website.setId(c.getInt(c.getColumnIndex(DBContants.COLUMN_ID)));
            website.setName(c.getString(c.getColumnIndex(DBContants.WEBSITE_COLUMN_NAME)));
            website.setHomePage(c.getString(c.getColumnIndex(DBContants.WEBSITE_COLUMN_HOMEPAGE)));
            website.setPostEntryTag(c.getString(c.getColumnIndex(DBContants.WEBSITE_COLUMN_POST_ENTRY_TAG)));
            website.setNavigationUrl(c.getString(c.getColumnIndex(DBContants.WEBSITE_COLUMN_NAVIGATION_URL)));
        }
        c.close();
        return website;
    }

    public long addPost(Post post) {
        long postId = -1L;
        db.beginTransaction();
        Cursor c = null;
        try {
            // check if exists
            c = db.rawQuery("SELECT * FROM " + DBContants.POST_TABLE_NAME + " WHERE " +
                    DBContants.POST_COLUMN_EXTERNAL_ID + " = ?", new String[]{String.valueOf(post.getExternalId())});
            ContentValues cv = new ContentValues();
            cv.put(DBContants.POST_COLUMN_TITLE, post.getTitle());
            cv.put(DBContants.POST_COLUMN_CONTENT, post.getContent());
            cv.put(DBContants.POST_COLUMN_EXTERNAL_ID, post.getExternalId());
            cv.put(DBContants.POST_COLUMN_URL, post.getUrl());
            cv.put(DBContants.POST_COLUMN_WEBSITE_ID, post.getWebsiteId());

            if (c.moveToNext()) {
                postId = c.getInt(c.getColumnIndex(DBContants.COLUMN_ID));
                String[] args = {String.valueOf(postId)};
                db.update(DBContants.POST_TABLE_NAME, cv, DBContants.COLUMN_ID + "=?", args);
                Log.d("", "update post successful");

            } else {
                postId = db.insert(DBContants.POST_TABLE_NAME, null, cv);
//                db.execSQL("INSERT INTO " + DBContants.POST_TABLE_NAME + " VALUES(NULL, ?, ?, ?, ?, ?)",
//                        new Object[]{post.getTitle(), post.getContent(), post.getExternalId(),
//                                post.getUrl(), post.getWebsiteId()});
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
        db.delete(DBContants.POST_TABLE_NAME, DBContants.POST_COLUMN_EXTERNAL_ID + " = ?", new String[]{String.valueOf(post.getExternalId())});
    }

    public List<Post> getAllPostsBySiteId(long siteId) {
        List<Post> posts = new ArrayList<Post>();
        Cursor c = db.query(DBContants.POST_TABLE_NAME, null,
                DBContants.POST_COLUMN_WEBSITE_ID + "=" + siteId, null, null, null, DBContants.POST_COLUMN_EXTERNAL_ID + " DESC");
        while (c.moveToNext()) {
            Post post = new Post();
            post.setId(c.getLong(c.getColumnIndex(DBContants.COLUMN_ID)));
            post.setTitle(c.getString(c.getColumnIndex(DBContants.POST_COLUMN_TITLE)));
            post.setContent(c.getString(c.getColumnIndex(DBContants.POST_COLUMN_CONTENT)));
            post.setExternalId(c.getInt(c.getColumnIndex(DBContants.POST_COLUMN_EXTERNAL_ID)));
            post.setUrl(c.getString(c.getColumnIndex(DBContants.POST_COLUMN_URL)));
            posts.add(post);
        }
        c.close();
        return posts;
    }

    public Post getSinglePost(long postId) {
        Post post = null;
        Cursor c = db.query(DBContants.POST_TABLE_NAME, null,
                DBContants.COLUMN_ID + "=" + postId, null, null, null, null);
        while (c.moveToNext()) {
            post = new Post();
            post.setTitle(c.getString(c.getColumnIndex(DBContants.POST_COLUMN_TITLE)));
            post.setContent(c.getString(c.getColumnIndex(DBContants.POST_COLUMN_CONTENT)));
            post.setExternalId(c.getInt(c.getColumnIndex(DBContants.POST_COLUMN_EXTERNAL_ID)));
            post.setUrl(c.getString(c.getColumnIndex(DBContants.POST_COLUMN_URL)));
        }
        c.close();
        return post;
    }

    public Post getPostByExternalId(long externalId) {
        Post post = null;
        Cursor c = db.query(DBContants.POST_TABLE_NAME, null,
                DBContants.POST_COLUMN_EXTERNAL_ID + "=" + externalId, null, null, null, null);
        while (c.moveToNext()) {
            post = new Post();
            post.setTitle(c.getString(c.getColumnIndex(DBContants.POST_COLUMN_TITLE)));
            post.setContent(c.getString(c.getColumnIndex(DBContants.POST_COLUMN_CONTENT)));
            post.setExternalId(c.getInt(c.getColumnIndex(DBContants.POST_COLUMN_EXTERNAL_ID)));
            post.setUrl(c.getString(c.getColumnIndex(DBContants.POST_COLUMN_URL)));
        }
        c.close();
        return post;
    }

//    public List<String> getAllPostTitleByWebsite(long siteId) {
//        List<String> titleList = new ArrayList<String>();
//        Cursor c = db.query(DBContants.POST_TABLE_NAME, new String[] {DBContants.POST_COLUMN_TITLE},
//                DBContants.POST_COLUMN_WEBSITE_ID + "=" + siteId, null, null, null, null);
//        while (c.moveToNext()) {
//            titleList.add(c.getString(0));
//        }
//        c.close();
//        return titleList;
//    }

    public int getMaxPostIdByWebsite(long websiteId) {
        int result = -1;
        Cursor c = db.query(DBContants.POST_TABLE_NAME,
                new String[]{"MAX(" + DBContants.POST_COLUMN_EXTERNAL_ID + ")"},
                DBContants.POST_COLUMN_WEBSITE_ID + "=" + websiteId, null, null, null, null);
        try {
            c.moveToNext();
            result = c.getInt(0);
        } finally {
            c.close();
        }
        Log.d("", "get max post id = " + result);
        return result;
    }

    public int getMinPostIdByWebsite(long websiteId) {
        int result = -1;
        Cursor c = db.query(DBContants.POST_TABLE_NAME,
                new String[]{"MIN(" + DBContants.POST_COLUMN_EXTERNAL_ID + ")"},
                DBContants.POST_COLUMN_WEBSITE_ID + "=" + websiteId, null, null, null, null);
        try {
            c.moveToNext();
            result = c.getInt(0);
        } finally {
            c.close();
        }
        Log.d("", "get min post id = " + result);
        return result;
    }

//    public void removeTable() {
//        db.execSQL("DROP TABLE IF EXISTS " + DBContants.WEBSITE_TABLE_NAME);
//        db.execSQL("DROP TABLE IF EXISTS " + DBContants.FEED_TABLE_NAME);
//    }

    public void closeDB() {
        db.close();
    }

}
