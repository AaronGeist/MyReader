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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import aaron.geist.myreader.constant.LoaderConstants;
import aaron.geist.myreader.database.DBManager;
import aaron.geist.myreader.domain.CrawlerRequest;
import aaron.geist.myreader.domain.Post;
import aaron.geist.myreader.domain.Website;
import aaron.geist.myreader.utils.FileDownloader;
import aaron.geist.myreader.utils.UrlParser;

/**
 * Created by Aaron on 2015/8/6.
 */
public class AsyncSiteCrawler extends AsyncTask<CrawlerRequest, Integer, Boolean> {

    public static final String CLASS_ENTRY = "entry";

    private static final int MAX_POST_NUM_TO_LOAD = 5;

    private DBManager mgr;
    private Website website = null;
    private Boolean crawlSuccess = false;
    private boolean isReverse = false;
    public AsyncSiteCrawlerResponse response = null;
    private final List<Post> crawledPosts = new ArrayList<>();

    /**
     * signal to stop crawling when:
     * <ul>
     * <li>reach limit of posts to load in one time</li>
     * <li>reach first page or last page</li>
     * </ul>
     */
    private volatile boolean stopCrawl = false;

    public AsyncSiteCrawler(Context ctx) {
        mgr = new DBManager(ctx);
    }

    @Override
    protected Boolean doInBackground(CrawlerRequest... requests) {
        if (requests.length > 0) {
            crawledPosts.clear();
            this.website = requests[0].getWebsite();
            crawl(requests[0].isReverse());
        }
        return crawlSuccess;
    }

    @Override
    protected void onPostExecute(Boolean crawlSuccess) {
        response.onTaskCompleted(crawlSuccess, crawledPosts, isReverse);
    }

    public void crawl(boolean isReserve) {
        Log.d("", "start crawling site " + website.getName());
        this.isReverse = isReserve;

        int pageNum = 0;
        int step = isReverse ? 1 : -1;

        long targetPostId = isReserve ? mgr.getMinPostIdByWebsite(website.getId())
                : mgr.getMaxPostIdByWebsite(Collections.singletonList(website.getId()));

        if (targetPostId > 0) {
            // find page num of targetPostId
            int lastPostId;
            do {
                // TODO pageNum might be cached, so that next search wouldn't take too long
                lastPostId = findLastPostInCurrentPage(++pageNum);
            } while (lastPostId > targetPostId);
        } else {
            // first time to crawl posts
            pageNum = 1;
        }

        while (!stopCrawl) {
            crawlSinglePage(pageNum, targetPostId, crawledPosts);
            pageNum += step;
        }

        Log.d("", "finish crawling site " + website.getName());
    }

    private void crawlSinglePage(int pageNum, long targetPostId, List<Post> postResults) {
        if (pageNum <= 0) {
            stopCrawl = true;
            return;
        }

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

        Element body = document.body();
        Elements posts = body.select(website.getPostEntryTag());

        // no longer valid pageNum, being zero or exceeding the maximum
        if (posts.size() == 0) {
            stopCrawl = true;
            return;
        }

        List<Element> postList = posts.subList(0, posts.size());
        // if we try to pull latest posts, we find max postId in DB is n,
        // then we trying to find n-1, n-2...n-m, in which m is the max post num to load
        // so the list is reversed here.
        if (!isReverse) {
            Collections.reverse(postList);
        }

        Log.d("", "find post number=" + posts.size());
        Element post;
        String postUrl;
        for (int i = 0, size = postList.size(); i < size && !stopCrawl; i++) {
            post = postList.get(i);
            postUrl = post.attr(HomePageParser.ATTR_HREF);

            // post already exists
            if (mgr.getPostByExternalId(UrlParser.getPostId(postUrl)) != null) {
                continue;
            }

            Post res = crawlSinglePost(postUrl);
            if (res != null) {
                if (!isReverse) {
                    res.setInOrder(res.getExternalId() > targetPostId);
                    postResults.add(0, res);
                } else {
                    res.setInOrder(res.getExternalId() < targetPostId);
                    postResults.add(res);
                }
                mgr.addPost(res);
            }
            if (postResults.size() >= MAX_POST_NUM_TO_LOAD) {
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

    private Post crawlSinglePost(String urlStr) {
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
        Element entry = body.select(CLASS_ENTRY).first();
        if (entry == null) {
            entry = body.select(website.getInnerPostSelect()).first();
        }

        Long timestampLong = System.currentTimeMillis();
        try {
            Element timestamp = body.select(website.getInnerTimestampSelect()).first();
            if (timestamp != null) {
                String text = timestamp.text();
                Matcher m = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2})").matcher(text);
                if (m.find()) {
                    String t = m.group(1);
                    System.out.println(t);
                    timestampLong = new SimpleDateFormat("yyyy-MM-dd hh:mm").parse(t).getTime();
                } else {
                    m = Pattern.compile("(\\d{4}/\\d{2}/\\d{2})").matcher(text);
                    if (m.find()) {
                        String t = m.group(1);
                        timestampLong = new SimpleDateFormat("yyyy/MM/dd").parse(t).getTime();
                    }
                }
            }
        } catch (Exception e) {
        }

        int currentPostId = UrlParser.getPostId(urlStr);
        Post post = new Post();
        post.setUrl(urlStr);
        post.setTitle(titleStr);
        post.setContent(localizeImages(entry, website.getName(), currentPostId));
        post.setExternalId(currentPostId);
        post.setTimestamp(timestampLong);
        post.setWebsiteId(website.getId());

        return post;
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
