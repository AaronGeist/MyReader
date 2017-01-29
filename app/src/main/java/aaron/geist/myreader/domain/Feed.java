package aaron.geist.myreader.domain;

/**
 * Created by yzhou7 on 2015/7/27.
 */
public class Feed {

    private String url;
    private String name;
    private String info;

    public Feed() {
    }

    public Feed(String name, String url, String info) {
        this.url = url;
        this.info = info;
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
