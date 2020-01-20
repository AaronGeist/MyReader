package aaron.geist.myreader.subscriber;

import android.os.Environment;

import java.io.File;
import java.util.Collection;
import java.util.List;

import aaron.geist.myreader.constant.SiteConfig;
import aaron.geist.myreader.database.DBManager;
import aaron.geist.myreader.domain.Post;
import aaron.geist.myreader.domain.Website;
import aaron.geist.myreader.loader.HomePageParser;
import aaron.geist.myreader.utils.FileUtil;

/**
 * Created by Aaron on 2015/7/27.
 */
public class SubscribeManager {

    private final DBManager mgr;

    public SubscribeManager() {
        this.mgr = DBManager.getInstance();
    }

    /**
     * Parse home page and add site to DB asynchronously.
     *
     * @param siteConfig
     */
    public void subscribe(SiteConfig siteConfig) {
        HomePageParser parser = new HomePageParser(siteConfig);
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

            File dir = new File(Environment.getExternalStorageDirectory() + "/myReader/images/");
            if (dir.exists()) {
                FileUtil.delete(dir);
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

    public void destroy() {
        mgr.closeDB();
    }
}
