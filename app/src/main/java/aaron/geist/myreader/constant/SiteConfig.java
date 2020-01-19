package aaron.geist.myreader.constant;

/**
 * Created by Aaron on 17/1/30.
 */

public enum SiteConfig {

    guokr("http://www.guokr.com/", "scientific/all/archive", "div.article-item > h3 > a", "div.document", "gpages", "div.content-th-info > span"),
    importnew("http://www.importnew.com/", "all-posts", "div.post > div.post-meta > p > a.meta-title", "div.entry", "navigation", "p.entry-meta-hide-on-mobile"),
    jobbole("http://blog.jobbole.com/", "all-posts", "div.post > div.post-meta > p > a.meta-title", "div.entry", "navigation", "p.entry-meta-hide-on-mobile"),
    infoq("http://www.infoq.com/", "cn/articles", "#content div.news_type2 > h2 > a", "div.text_info_article", "", ""), //
    ; //

    private String rootUrl;
    private String postsPath;
    private String outerPostSelect;
    private String navigationClassName;
    private String innerPostSelect;
    private String innerTimestampSelect;

    SiteConfig(String rootUrl, String postsPath, String outerPostSelect, String innerPostSelect, String navigationClassName, String innerTimestampSelect) {
        this.rootUrl = rootUrl;
        this.postsPath = postsPath;
        this.outerPostSelect = outerPostSelect;
        this.innerPostSelect = innerPostSelect;
        this.navigationClassName = navigationClassName;
        this.innerTimestampSelect = innerTimestampSelect;
    }

    public String getRootUrl() {
        return rootUrl;
    }

    public String getPostsPath() {
        return postsPath;
    }

    public String getOuterPostSelect() {
        return outerPostSelect;
    }

    public String getInnerPostSelect() {
        return innerPostSelect;
    }

    public String getNavigationClassName() {
        return navigationClassName;
    }

    public String getInnerTimestampSelect() {
        return innerTimestampSelect;
    }
}
