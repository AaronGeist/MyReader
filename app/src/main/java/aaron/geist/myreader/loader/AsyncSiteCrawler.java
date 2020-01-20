package aaron.geist.myreader.loader;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import aaron.geist.myreader.constant.HtmlConstants;
import aaron.geist.myreader.constant.LoaderConstants;
import aaron.geist.myreader.database.DBManager;
import aaron.geist.myreader.domain.CrawlerRequest;
import aaron.geist.myreader.domain.Post;
import aaron.geist.myreader.domain.Website;
import aaron.geist.myreader.utils.DateUtil;
import aaron.geist.myreader.utils.FileDownloader;
import aaron.geist.myreader.utils.UrlParser;

/**
 * Created by Aaron on 2015/8/6.
 */
public class AsyncSiteCrawler extends AsyncTask<CrawlerRequest, Integer, Boolean> {

    private static final int MAX_POST_NUM_TO_LOAD = 5;

    private DBManager mgr;
    private Website website = null;
    private boolean crawlSuccess = false;
    private boolean isReverse = false;
    private AsyncCallback callback = null;
    private final List<Post> crawledPosts = new ArrayList<>();

    private final List<String> imageSrcAttr = Arrays.asList("src", "data-original");

    /**
     * signal to stop crawling when:
     * <ul>
     * <li>reach limit of posts to load in one time</li>
     * <li>reach first page or last page</li>
     * </ul>
     */
    private volatile boolean stopCrawl = false;

    public AsyncSiteCrawler() {
        mgr = DBManager.getInstance();
    }

    @Override
    protected Boolean doInBackground(CrawlerRequest... requests) {
        if (requests.length > 0) {
            crawledPosts.clear();
            this.website = requests[0].getWebsite();
            this.isReverse = requests[0].isReverse();
            crawl();
        }
        return crawlSuccess;
    }

    @Override
    protected void onPostExecute(Boolean crawlSuccess) {
        callback.onTaskCompleted(crawlSuccess, crawledPosts, isReverse);
    }

    private void crawl() {
        Log.d("", "start crawling site " + website.getName());

        int pageNum = 0;
        int step = isReverse ? 1 : -1;

        long targetPostId = isReverse ? mgr.getMinPostIdByWebsite(website.getId())
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

        Log.d("", "crawling page " + String.format(website.getNavigationUrl(), pageNum));
        Document document = null;
        URL url = null;
        try {
            url = new URL(String.format(website.getNavigationUrl(), pageNum));
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

            postUrl = post.attr(HtmlConstants.ATTR_HREF);
            if (!postUrl.startsWith("http")) {
                postUrl = website.getHomePage() + postUrl;
            }

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
        Log.d("", "crawling page " + String.format(website.getNavigationUrl(), pageNum));
        Document document = null;
        URL url = null;
        try {
            url = new URL(String.format(website.getNavigationUrl(), pageNum));
        } catch (MalformedURLException e) {
            Log.d("", "MalformedURLException: " + e.getMessage());
        }

        try {
            document = Jsoup.parse(url, LoaderConstants.DEFAULT_LOAD_TIMEOUT_MILLISEC);
        } catch (IOException e) {
            Log.d("", "IOException: " + e.getMessage());
        }

        Log.d("", "finish loading page " + String.format(website.getNavigationUrl(), pageNum));
        Element body = document.body();
        Log.d("", "post entry tag=" + website.getPostEntryTag());
        Elements posts = body.select(website.getPostEntryTag());

        Log.d("", "find post number=" + posts.size());

        Element lastPost = posts.last();
        return UrlParser.getPostId(lastPost.attr(HtmlConstants.ATTR_HREF));
    }

    private Post crawlSinglePost(String urlStr) {
        Log.d("", "start crawl post: " + urlStr);
        Document document = null;
        URL url = null;
        try {
            urlStr = urlStr.replaceAll("\\/\\/", "\\/");
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

        // parse title
        Element head = document.head();
        Element titleElem = head.getElementsByTag(HtmlConstants.TAG_TITLE).first();
        String title = titleElem.ownText();

        // if we find | in title, only the first part is important
        if (title.contains("|")) {
            title = title.split("\\|")[0];
        }

        Elements contents = body.select(website.getInnerPostSelect());

        long ts = System.currentTimeMillis();
        Element tsElem = body.select(website.getInnerTimestampSelect()).first();
        if (tsElem != null) {
            ts = DateUtil.find(tsElem.text());
        }

        int currentPostId = UrlParser.getPostId(urlStr);
        Post post = new Post();
        post.setUrl(urlStr);
        post.setTitle(title);
        post.setContent(localizeImages(contents, website.getName(), currentPostId));
        post.setExternalId(currentPostId);
        post.setTimestamp(ts);
        post.setWebsiteId(website.getId());

        return post;
    }

    private String localizeImages(Elements contents, String site, int postId) {
        StringBuilder sb = new StringBuilder();
        for (Element content : contents) {
            // find images first
            Elements images = content.getElementsByTag("img");
            for (Element img : images) {
                // image link might have several attributes, try one by one
                for (String attr : imageSrcAttr) {
                    String src = img.attr(attr);
                    if (StringUtil.isBlank(src)) {
                        continue;
                    }

                    String filePath = FileDownloader.download(src, "/myReader/images/" + site + "/" + postId + "/");
                    img.attr(attr, filePath);
                }
            }
            sb.append(content.html()).append("\n");
        }

        return sb.toString();
    }

    public void setCallback(AsyncCallback callback) {
        this.callback = callback;
    }
}
