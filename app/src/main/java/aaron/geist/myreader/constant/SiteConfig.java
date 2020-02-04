package aaron.geist.myreader.constant;

/**
 * Created by Aaron on 17/1/30.
 */

public enum SiteConfig {

    guokr("果壳", "default", "https://www.guokr.com/", "scientific/all/archive/", "div.article-item > h3 > a", "div.rich_media_content", "?page=%d", "#commentAchor + p"),
    jiandan("煎蛋", "default", "http://jandan.net/", "", "#content div.post h2 > a", "div.post", "page/%d", "div.post div.time_s"),
    cnbeta("cnBeta", "default", "https://m.cnbeta.com/", "list/", "ul.info_list div.txt_area > a", "#artibody", "latest_%d.htm", "time.time"),
    luobo("萝卜", "default", "https://bh.sb/", "", "article h2 a", "article", "page/%d/", "ul.article-meta > li:nth-of-type(1)"),
    ifanr("爱范儿", "api", "https://sso.ifanr.com/api/v5/wp/web-feed/?published_at__lte=2999-01-01%2013:45:26&limit=50&offset=0", "", "", "article", "", "#ArticleContentMeta a > p")
//    jobbole("伯乐", "http://blog.jobbole.com/", "all-posts", "div.post > div.post-meta > p > a.meta-title", "div.entry", "navigation", "p.entry-meta-hide-on-mobile"),
//    infoq("infoQ", "http://www.infoq.com/", "cn/articles", "#content div.news_type2 > h2 > a", "div.text_info_article", "", ""), //
    ; //

    private String type;
    private String name;
    private String rootUrl;
    private String postsPath;
    private String outerPostSelect;
    private String navigationClassName;
    private String innerPostSelect;
    private String innerTimestampSelect;

    SiteConfig(String name, String type, String rootUrl, String postsPath, String outerPostSelect, String innerPostSelect, String navigationClassName, String innerTimestampSelect) {
        this.name = name;
        this.type = type;
        this.rootUrl = rootUrl;
        this.postsPath = postsPath;
        this.outerPostSelect = outerPostSelect;
        this.innerPostSelect = innerPostSelect;
        this.navigationClassName = navigationClassName;
        this.innerTimestampSelect = innerTimestampSelect;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
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
