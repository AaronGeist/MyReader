package aaron.geist.myreader.loader;

import android.content.Context;
import android.util.Log;
import aaron.geist.myreader.database.DBManager;
import aaron.geist.myreader.domain.CrawlerRequest;
import aaron.geist.myreader.domain.Website;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yzhou7 on 2015/8/6.
 */
public class HomePageParser implements AsyncPageLoaderResponse, AsyncSiteCrawlerResponse {

    public static final String TAG_LINK = "a";
    public static final String TAG_TITLE = "title";
    public static final String CLASS_POST = "post";
    public static final String CLASS_NAVI = "navigation";
    public static final String ATTR_HREF = "href";

    private String homePageUrl;
    private String className = this.getClass().getSimpleName();
    private Element head = null;
    private Element body = null;
    private Website website = null;
    private DBManager mgr;
    private Context ctx = null;

    public HomePageParser(String homePageUrl, Context context) {
        ctx = context;
        mgr = new DBManager(context);
        website = new Website();
        website.setHomePage(homePageUrl);
        this.homePageUrl = homePageUrl;
    }

    public void parse() {
        Log.d(className, "start parse homepage");
        Document document = null;
        URL url = null;
        try {
            url = new URL(this.homePageUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        AsyncPageLoader task = new AsyncPageLoader();
        task.asyncPageLoaderResponse = this;
        task.execute(url);
    }

    private void parseTitle() {
        Element title = head.getElementsByTag(TAG_TITLE).first();
        String titleStr = title.ownText();
        Log.d(className, "parse title=" + titleStr);
        website.setName(titleStr);

    }

    private void parsePostEntryClassName() {
        Elements posts = body.getElementsByClass(CLASS_POST);
        Element post = posts.first();
        Elements links = post.getElementsByTag(TAG_LINK);
        Iterator<Element> iterator = links.iterator();
        Element link = null;
        String className = "";
        while (iterator.hasNext()) {
            link = iterator.next();
            className = link.className();
            if (!"".equals(className)) {
                break;
            }
        }

        if (className != "") {
            Log.d(this.className, "Find post Entry class name=" + className);
            website.setPostEntryTag("." + CLASS_POST + " ." + className);
        } else {
            Log.d(this.className, "Failed to find post Entry class name");
        }
    }

    /**
     * parse navigation url pattern with specified tag name and class.
     */
    private void parseNavigationUrl() {
        Elements navis = body.getElementsByClass(CLASS_NAVI);
        if (navis != null) {
            Element navi = navis.first();
            if (navi != null) {
                Elements nextPages = navi.getElementsByTag(TAG_LINK);
                if (nextPages != null) {
                    Element nextPage = nextPages.first();
                    String nextPageUrl = nextPage.attr(ATTR_HREF);
                    Log.d(className, nextPageUrl);

                    Pattern p = Pattern.compile("^(.*[=/]+)(\\d+)[/]*$");
                    Matcher m = p.matcher(nextPageUrl);
                    if (m.matches()) {
                        Log.d(className, "Find navigation url=" + m.group(1));
                        website.setNavigationUrl(m.group(1));
                    }
                }
            }
        }
    }

    @Override
    public void onTaskCompleted(Document document) {
        if (document == null) {
            Log.d(className, "failed to load page ");
            return;
        }

        head = document.head();
        body = document.body();

        parseTitle();
        parseNavigationUrl();
        parsePostEntryClassName();

        Log.d(className, "finish parse homepage");

        if (website != null) {
            long siteId = mgr.addWebsite(website);
            website.setId(siteId);
        }

        // async crawl website, get posts and store in DB.
        AsyncSiteCrawler crawler = new AsyncSiteCrawler(ctx);
        crawler.response = this;
        CrawlerRequest request = new CrawlerRequest();
        request.setWebsite(website);
        request.setReverse(false);
        crawler.execute(request);
    }

    @Override
    public void onTaskCompleted(Boolean crawlSuccess) {
        Log.d("", "finish asyncSiteCrawler, result=" + crawlSuccess);
    }
}
