package aaron.geist.myreader.loader;

import android.os.AsyncTask;
import android.os.Environment;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import aaron.geist.myreader.constant.HtmlConstants;
import aaron.geist.myreader.constant.LoaderConstants;
import aaron.geist.myreader.database.DBManager;
import aaron.geist.myreader.domain.CrawlerRequest;
import aaron.geist.myreader.domain.Post;
import aaron.geist.myreader.domain.Website;
import aaron.geist.myreader.utils.BitmapUtils;
import aaron.geist.myreader.utils.DateUtil;
import aaron.geist.myreader.utils.FileDownloader;
import aaron.geist.myreader.utils.UrlParser;

/**
 * Created by Aaron on 2015/8/6.
 */
public class AsyncSiteCrawler extends AsyncTask<CrawlerRequest, Integer, Boolean> {

    private static final int MAX_POST_NUM_TO_LOAD = 50;

    private DBManager mgr;
    private Website website = null;
    private boolean crawlSuccess = false;
    private boolean isReverse = false;
    private int targetNum = MAX_POST_NUM_TO_LOAD;
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
            this.targetNum = requests[0].getTargetNum();
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

        int pageNum = 1;
        long existingMaxPostExternalId = mgr.getMaxPostIdByWebsite(Collections.singletonList(website.getId()));

        while (!stopCrawl) {
            crawlSinglePage(pageNum, existingMaxPostExternalId, crawledPosts);
            pageNum += 1;
        }

        Log.d("", "finish crawling site " + website.getName());
    }

    private void crawlSinglePage(int pageNum, final long existingMaxPostExternalId, final List<Post> postResults) {
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

        final List<Element> postList = posts.subList(0, posts.size());
        // if we try to pull latest posts, we find max postId in DB is n,
        // then we trying to find n-1, n-2...n-m, in which m is the max post num to load
        // so the list is reversed here.
        if (!isReverse) {
            Collections.reverse(postList);
        }

        Log.d("", "find post number=" + posts.size());

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0, size = postList.size(); i < size && !stopCrawl; i++) {

            final Element post = postList.get(i);
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    long start = System.currentTimeMillis();
                    String postUrl = post.attr(HtmlConstants.ATTR_HREF);

                    // some link contains full url path, so we don't need to do anything.
                    // Others is only part, so need to add root url.
                    if (!postUrl.startsWith("http")) {
                        postUrl = website.getHomePage() + postUrl;
                    }

                    // post already exists
                    int externalId = UrlParser.getPostId(postUrl);
                    if (mgr.getPostByExternalId(externalId) != null) {
                        stopCrawl = true;
                        return;
                    }

                    // crawling existing post
                    if (externalId < existingMaxPostExternalId) {
                        stopCrawl = true;
                        return;
                    }

                    Post res = crawlSinglePost(postUrl);
                    if (res != null) {
                        res.setInOrder(res.getExternalId() > existingMaxPostExternalId);
                        postResults.add(0, res);

                        mgr.addPost(res);
                    }

                    Log.d("", "Crawl single post cost: " + (System.currentTimeMillis() - start) + "ms");
                }
            });
        }

        try {
            executorService.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.d("", e.getMessage());
        }

        if (postResults.size() >= targetNum) {
            stopCrawl = true;
        }

        crawlSuccess = true;
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

    private String localizeImages(Elements contents, String site, int postExternalId) {
        StringBuilder sb = new StringBuilder();
        for (Element content : contents) {
            // find images first
            Elements images = content.getElementsByTag("img");
            int cnt = 0;
            for (Element img : images) {
                // image link might have several attributes, try one by one
                for (String attr : imageSrcAttr) {
                    String src = img.attr(attr);
                    if (StringUtil.isBlank(src)) {
                        continue;
                    }

                    cnt++;
                    String filePath = FileDownloader.download(src, "/myReader/images/" + site + "/" + postExternalId + "/");
                    if (cnt == 1) {
                        long start = System.currentTimeMillis();
                        String realPath = filePath.replace("file://", "");
                        String destPath = Environment.getExternalStorageDirectory() + "/myReader/images/" + site + "/" + postExternalId + "/thumb.jpg";
                        BitmapUtils.decodeSampledBitmapFromFd(realPath, 100, 100, destPath);
                        Log.d("", "Created thumbnail, cost " + (System.currentTimeMillis() - start) + "ms");
                    }

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
