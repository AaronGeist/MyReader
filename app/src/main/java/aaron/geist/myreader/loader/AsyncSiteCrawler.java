package aaron.geist.myreader.loader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import aaron.geist.myreader.constant.LoaderConstants;
import aaron.geist.myreader.database.DBManager;
import aaron.geist.myreader.domain.CrawlerRequest;
import aaron.geist.myreader.domain.Post;
import aaron.geist.myreader.domain.Website;
import aaron.geist.myreader.utils.FileDownloader;
import aaron.geist.myreader.utils.UrlParser;

/**
 * Created by yzhou7 on 2015/8/6.
 */
public class AsyncSiteCrawler extends AsyncTask<CrawlerRequest, Integer, Boolean> {

    public static final String CLASS_ENTRY = "entry";

    private static final int MAX_POST_NUM_TO_LOAD = 1;

    private DBManager mgr;
    private Website website = null;
    private int maxPostId = -1;
    private Boolean crawlSuccess = false;
    public AsyncSiteCrawlerResponse response = null;

    /**
     * once we find the post id crawled is smaller than the max in DB.
     * we stop crawling.
     */
    boolean stopCrawl = false;

    public AsyncSiteCrawler(Context ctx) {
        mgr = new DBManager(ctx);
    }

    @Override
    protected Boolean doInBackground(CrawlerRequest... requests) {
        if (requests.length > 0) {
            this.website = requests[0].getWebsite();
            crawl(requests[0].isReverse());
        }
        return crawlSuccess;
    }

    @Override
    protected void onPostExecute(Boolean crawlSuccess) {
        response.onTaskCompleted(crawlSuccess);
    }

    public void crawl(boolean isReserve) {
        Log.d("", "start crawling site " + website.getName());

        if (!isReserve) {
            getMaxPostId();
            int pageNum = 1;
            while (!stopCrawl) {
                crawlSinglePage(pageNum++);
            }
        } else {
            // find position of oldest post
            long minPostId = mgr.getMinPostIdByWebsite(website.getId());
            int pageNum = 0;
            int lastPostId;
            do {
                // find the oldest page num
                lastPostId = findLastPostInCurrentPage(++pageNum);
            } while (lastPostId >= minPostId);

            while (!stopCrawl) {
                crawlSinglePage(pageNum++);
            }
        }
        Log.d("", "finish crawling site " + website.getName());
    }

    /**
     * Get max post id (parsed from post link) in database.
     * This id represents the latest post..
     */
    private void getMaxPostId() {
        maxPostId = mgr.getMaxPostIdByWebsite(website.getId());
    }

    private void crawlSinglePage(int pageNum) {
        Log.d("", "crawling page " + website.getNavigationUrl() + pageNum);
        Document document = null;
        URL url = null;
        try {
            url = new URL(website.getNavigationUrl() + pageNum);
        } catch (MalformedURLException e) {
            Log.d("", "MalformedURLException: " + e.getMessage());
        }

        try {
            document = Jsoup.parse(url, LoaderConstants.DEFAULT_LOAD_TIMEOUT_MILLISEC);
        } catch (IOException e) {
            Log.d("", "IOException: " + e.getMessage());
        }

        Log.d("", "finish loading page " + website.getNavigationUrl() + pageNum);
        Element body = document.body();
        Log.d("", "post entry tag=" + website.getPostEntryTag());
        Elements posts = body.select(website.getPostEntryTag());

        Log.d("", "find post number=" + posts.size());
        Iterator<Element> iterator = posts.iterator();
        Element post;
        String postUrl;
        int cnt = 0;
        while (!stopCrawl && iterator.hasNext()) {
            post = iterator.next();
            postUrl = post.attr(HomePageParser.ATTR_HREF);

            // post already exists
            if (mgr.getPostByExternalId(UrlParser.getPostId(postUrl)) != null) {
                continue;
            }

            crawlSinglePost(postUrl);
            cnt++;
            if (cnt > MAX_POST_NUM_TO_LOAD) {
                stopCrawl = true;
            }
        }
        crawlSuccess = true;
    }

    private int findLastPostInCurrentPage(int pageNum) {
        Log.d("", "crawling page " + website.getNavigationUrl() + pageNum);
        Document document = null;
        URL url = null;
        try {
            url = new URL(website.getNavigationUrl() + pageNum);
        } catch (MalformedURLException e) {
            Log.d("", "MalformedURLException: " + e.getMessage());
        }

        try {
            document = Jsoup.parse(url, LoaderConstants.DEFAULT_LOAD_TIMEOUT_MILLISEC);
        } catch (IOException e) {
            Log.d("", "IOException: " + e.getMessage());
        }

        Log.d("", "finish loading page " + website.getNavigationUrl() + pageNum);
        Element body = document.body();
        Log.d("", "post entry tag=" + website.getPostEntryTag());
        Elements posts = body.select(website.getPostEntryTag());

        Log.d("", "find post number=" + posts.size());

        Element lastPost = posts.last();
        return UrlParser.getPostId(lastPost.attr(HomePageParser.ATTR_HREF));
    }

    private void crawlSinglePost(String urlStr) {
        Document document = null;
        URL url = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            document = Jsoup.parse(url, LoaderConstants.DEFAULT_LOAD_TIMEOUT_MILLISEC);
        } catch (IOException e) {
            Log.d("", "IOException: " + e.getMessage());
        }

        Element body = document.body();

        // parse title, same as HomePageParser
        Element head = document.head();
        Element title = head.getElementsByTag(HomePageParser.TAG_TITLE).first();
        String titleStr = title.ownText();
        Element entry = body.getElementsByClass(CLASS_ENTRY).first();

        // assume that the post id is always increasing.
        // if reach to the previous latest post, stop crawling.
        int currentPostId = UrlParser.getPostId(urlStr);
        if (currentPostId > maxPostId) {
            Post post = new Post();
            post.setUrl(urlStr);
            post.setTitle(titleStr);
            post.setContent(localizeImages(entry, website.getName(), currentPostId));
            post.setExternalId(currentPostId);
            post.setWebsiteId(website.getId());
            mgr.addPost(post);
        } else {
            Log.d("", "reach max post id, stop crawling.");
            stopCrawl = true;
        }
    }

    private String localizeImages(Element entry, String site, int postId) {
        Log.d("", "localizeImages");

        // find images first
        Elements images = entry.getElementsByTag("img");
        Iterator<Element> it = images.listIterator();
        while (it.hasNext()) {
            Element img = it.next();
            Log.d("", img.html());
            String src = img.attr("src");

            String filePath = FileDownloader.download(src, "/myReader/images/" + site + "/" + postId + "/");
            img.attr("src", filePath);
        }

        return entry.html();
    }

}
