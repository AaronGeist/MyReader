package aaron.geist.myreader.subscriber;

import android.os.Environment;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aaron.geist.myreader.constant.SiteConfig;
import aaron.geist.myreader.database.DBManager;
import aaron.geist.myreader.domain.Post;
import aaron.geist.myreader.domain.Website;
import aaron.geist.myreader.utils.FileUtil;

/**
 * Created by Aaron on 2015/7/27.
 */
public class SubscribeManager {

    private static volatile SubscribeManager instance;

    private final DBManager mgr;

    private final Map<String, Website> websiteCache = new HashMap<>();

    private SubscribeManager() {
        this.mgr = DBManager.getInstance();
        List<Website> websites = mgr.getAllWebsites();
        for (Website website : websites) {
            websiteCache.put(website.getName(), website);
        }
    }

    public static SubscribeManager getInstance() {
        if (instance == null) {
            synchronized (SubscribeManager.class) {
                if (instance == null) {
                    instance = new SubscribeManager();
                }
            }
        }

        return instance;
    }

    /**
     * Parse home page and add site to DB asynchronously.
     *
     * @param siteConfig
     */
    public void subscribe(SiteConfig siteConfig) {
        Website website = new Website();
        website.setName(siteConfig.getName());
        website.setType(siteConfig.getType());
        website.setHomePage(siteConfig.getRootUrl());
        website.setNavigationUrl(siteConfig.getRootUrl() + siteConfig.getPostsPath() + siteConfig.getNavigationClassName());
        website.setPostEntryTag(siteConfig.getOuterPostSelect());
        website.setInnerPostSelect(siteConfig.getInnerPostSelect());
        website.setInnerTimestampSelect(siteConfig.getInnerTimestampSelect());

        long websiteId = mgr.addWebsite(website);
        website.setId(websiteId);

        websiteCache.put(website.getName(), website);
    }

    public void unsubscribe(SiteConfig siteConfig) {
        Collection<Website> websites = mgr.getAllWebsites();
        for (Website website : websites) {
            if (website.getName().equalsIgnoreCase(siteConfig.getName())) {
                mgr.removeWebsite(website);
                websiteCache.remove(website.getName());

                Collection<Post> posts = mgr.getAllPostsBySiteId(website.getId());
                for (Post post : posts) {
                    mgr.removePost(post);
                }

                File dir = new File(Environment.getExternalStorageDirectory() + "/myReader/images/" + website.getName());
                if (dir.exists()) {
                    FileUtil.delete(dir);
                }

                return;
            }
        }
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

        websiteCache.clear();
    }

    /**
     * Load current sites subscribed.
     *
     * @return list of website object
     */
    public Map<String, Website> getAll() {
        return websiteCache;
    }

    public String queryNameById(long websiteId) {
        for (Website website : websiteCache.values()) {
            if (website.getId() == websiteId) {
                return website.getName();
            }
        }

        return "NA";
    }

    public void destroy() {
        mgr.closeDB();
    }
}
