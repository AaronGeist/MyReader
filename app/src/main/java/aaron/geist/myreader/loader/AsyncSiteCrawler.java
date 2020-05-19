package aaron.geist.myreader.loader;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    private static final int MIN_POST_NUM_TO_LOAD = 10;

    private DBManager mgr;
    private Website website = null;
    private boolean crawlSuccess = false;
    private boolean isReverse = false;
    private int targetNum = MAX_POST_NUM_TO_LOAD;
    private AsyncCallback callback = null;
    private final List<Post> crawledPosts = new ArrayList<>();

    private final List<String> imageSrcAttr = Arrays.asList("src", "data-original");

    private Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(1000).build();

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
        callback.onTaskCompleted(crawlSuccess, crawledPosts, isReverse, this.website.getName());
    }

    private void crawl() {
        Log.d("", "start crawling site " + website.getName());

        int pageNum = this.isReverse ? this.website.getPageNum() : 1;

        switch (website.getType().toLowerCase()) {
            case "default":
                while (!stopCrawl) {
                    crawledPosts.addAll(crawlSinglePage(pageNum));
                    pageNum++;

                    if (crawledPosts.size() > targetNum) {
                        if (isReverse) {
                            // next time, we start from next page number
                            mgr.updateWebsitePageNo(website.getId(), pageNum);
                        }
                        break;
                    }
                }
                break;
            case "api":
                crawledPosts.addAll(crawlCertainPage());
                break;
            default:
                Log.e("", "Not supported website type: " + website.getType());
                break;
        }

        Log.d("", "finish crawling site " + website.getName());
    }


    private List<Post> crawlCertainPage() {
        Log.d("", "crawling page " + website.getHomePage());

        List<Post> result = Lists.newArrayList();
        final List<String> postUrls = new ArrayList<>();
        try {
            URL url = new URL(website.getHomePage());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                InputStream inStream = conn.getInputStream();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, length);
                }
                String str = baos.toString(StandardCharsets.UTF_8.name());
                JSONObject jsonObject = new JSONObject(str);
                JSONArray jsonArray = (JSONArray) jsonObject.get("objects");

                int size = jsonArray.length();
                for (int i = 0; i < size; i++) {
                    postUrls.add((String) jsonArray.getJSONObject(i).get("post_url"));
                }
            } else {
                Log.d("", "Fail to crawl: " + url.getPath());
                stopCrawl = true;
                return result;
            }
        } catch (Exception e) {
            Log.d("", "IOException: " + e.getMessage());
        }

        crawlSuccess = true;

        return doCrawl(postUrls);
    }

    private List<Post> crawlSinglePage(int pageNum) {
        Log.d("", "crawling page " + String.format(website.getNavigationUrl(), pageNum));

        List<Post> result = Lists.newArrayList();
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

        // maybe this website find the crawler and forbidden
        if (document == null) {
            Log.d("", "Fail to crawl: " + url.getPath());
            stopCrawl = true;
            return result;
        }

        Element body = document.body();

        Elements posts = body.select(website.getPostEntryTag());

        // no longer valid pageNum, being zero or exceeding the maximum
        if (posts.size() == 0) {
            stopCrawl = true;
            return result;
        }


        List<Element> postList = posts.subList(0, posts.size());
        List<String> postUrls = Lists.newArrayList();
        postList.forEach(post -> {
            String href = post.attr(HtmlConstants.ATTR_HREF);

            // some link contains full url path, so we don't need to do anything.
            // Others is only part, so need to add root url.
            if (!href.startsWith("http")) {
                href = website.getHomePage() + href;
            }

            // replace // with /
            href = href.replaceAll("//", "/");

            postUrls.add(href);
        });

        crawlSuccess = true;

        return doCrawl(postUrls);
    }

    private List<Post> doCrawl(List<String> postUrls) {
        Log.d("", "going to crawl " + postUrls.size() + " posts.");

        List<Post> result = Lists.newArrayList();
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<Future<Post>> futureList = Lists.newArrayList();
        for (int i = 0, size = postUrls.size(); i < size; i++) {
            final String postUrl = postUrls.get(i);


            String hash = UrlParser.getMd5Digest(postUrl);
            if (cache.getIfPresent(hash) != null || mgr.isPostExists(hash)) {
                if (!isReverse) {
                    // do not stop crawling until existing post is found
                    stopCrawl = true;
                    break;
                } else {
                    continue;
                }
            }

            futureList.add(executorService.submit(() -> {
                long start = System.currentTimeMillis();

                Post post = crawlSinglePost(postUrl);
                cache.put(hash, hash);

                Log.d(postUrl, "Crawl single post cost: " + (System.currentTimeMillis() - start) + "ms");

                return post;
            }));
        }

        futureList.forEach(future -> {
            try {
                Post post = future.get(10, TimeUnit.SECONDS);
                if (post != null) {
                    result.add(post);
                    mgr.addPost(post);
                }
            } catch (TimeoutException e) {
                Log.e("TimeoutException", "crawling post");
            } catch (Exception e) {
                Log.e("Exception", "crawling post");
            }
        });

        return result;
    }

    private Post crawlSinglePost(String urlStr) {
        Log.d("", "start crawl post: " + urlStr);
        Document document = null;
        URL url = null;
        try {
//            urlStr = urlStr.replaceAll("\\/\\/", "\\/");
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
        post.setHash(UrlParser.getMd5Digest(urlStr));
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
                        String destPath = Environment.getExternalStorageDirectory() + "/myReader/images/" + site + "/" + postExternalId + "/thumb.jpg";
                        BitmapUtils.decodeSampledBitmapFromFd(filePath, 100, 100, destPath);
                        Log.d("", "Created thumbnail, cost " + (System.currentTimeMillis() - start) + "ms");
                    }

                    img.attr(attr, "file://" + filePath);
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
