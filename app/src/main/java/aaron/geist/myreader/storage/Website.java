package aaron.geist.myreader.storage;

/**
 * This class defines the basic element for crawling a website.
 *
 * Created by yzhou7 on 2015/8/6.
 */
public class Website {

    /**
     * id in database.
     */
    private long id = 0L;
    /**
     * name of this website.
     */
    private String name = "";
    /**
     * url of home page.
     */
    private String homePage = "";
    /**
     * css tag of post entry.
     * e.g. <span class="read-more">
     *     <a target="_blank" href="http://xxxx.com/12345/">Read all</a></span>
     */
    private String postEntryTag = "";
    /**
     * url for navigation link.
     */
    private String navigationUrl = "";

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHomePage() {
        return homePage;
    }

    public void setHomePage(String homePage) {
        this.homePage = homePage;
    }

    public String getPostEntryTag() {
        return postEntryTag;
    }

    public void setPostEntryTag(String postEntryTag) {
        this.postEntryTag = postEntryTag;
    }

    public String getNavigationUrl() {
        return navigationUrl;
    }

    public void setNavigationUrl(String navigationUrl) {
        this.navigationUrl = navigationUrl;
    }
}
