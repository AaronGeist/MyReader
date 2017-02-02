package aaron.geist.myreader.constant;

/**
 * Created by shakazxx on 17/1/30.
 */

public enum SiteConfig {

    guokr("http://www.guokr.com/", "http://www.guokr.com/scientific/all/archive", "div.article-item > h3 > a", "div.document", "gpages");

    private String rootUrl;
    private String url;
    private String postSelect;
    private String navigationClassName;
    private String innerPostSelect;

    SiteConfig(String rootUrl, String url, String postSelect, String innerPostSelect, String navigationClassName) {
        this.rootUrl = rootUrl;
        this.url = url;
        this.postSelect = postSelect;
        this.innerPostSelect = innerPostSelect;
        this.navigationClassName = navigationClassName;
    }

    public String getRootUrl() {
        return rootUrl;
    }

    public String getUrl() {
        return url;
    }

    public String getPostSelect() {
        return postSelect;
    }

    public String getInnerPostSelect() {
        return innerPostSelect;
    }

    public String getNavigationClassName() {
        return navigationClassName;
    }
}
