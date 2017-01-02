package aaron.geist.myreader.subscriber;

import android.content.Context;

import java.util.Collection;
import java.util.List;

import aaron.geist.myreader.constant.TestConstants;
import aaron.geist.myreader.database.DBManager;
import aaron.geist.myreader.loader.HomePageParser;
import aaron.geist.myreader.storage.Post;
import aaron.geist.myreader.storage.Website;

/**
 * Created by yzhou7 on 2015/7/27.
 */
public class SubscribeManager {

    private final Context ctx;
    private final DBManager mgr;

    public SubscribeManager(Context ctx) {
        this.ctx = ctx;
        this.mgr = new DBManager(ctx);
    }

    /**
     * Parse home page and add site to DB asynchronously.
     *
     * @param url
     */
    public void subscribe(String url) {
        url = TestConstants.TestRssUrl;
        HomePageParser parser = new HomePageParser(url, ctx);
        parser.parse();
    }

    // TODO add DB method to remove site more efficiently
    public void unSubscribeAll() {
        Collection<Website> websites = mgr.getAllWebsites();
        for (Website website : websites) {
            mgr.removeWebsite(website);

            Collection<Post> posts = mgr.getAllPostsBySiteId(website.getId());
            for (Post post : posts) {
                mgr.removePost(post);
            }
        }
    }

    /**
     * Load current sites subscribed.
     *
     * @return list of website object
     */
    public List<Website> getAll() {
        return mgr.getAllWebsites();
    }

    public void destory() {
        mgr.closeDB();
    }
}