package aaron.geist.myreader.domain;

import java.io.Serializable;

/**
 * Created by Aaron on 2015/7/27.
 */
public class Post implements Serializable {

    private static final long serialVersionUID = 9150231278934934185L;

    private long id;
    private String title;
    private String content;
    /**
     * post id in original website.
     */
    private int externalId;
    private String url;
    private long websiteId;
    private boolean stared;
    private boolean read;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getExternalId() {
        return externalId;
    }

    public void setExternalId(int externalId) {
        this.externalId = externalId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getWebsiteId() {
        return websiteId;
    }

    public void setWebsiteId(long websiteId) {
        this.websiteId = websiteId;
    }

    public boolean isStarted() {
        return stared;
    }

    public void setStared(boolean stared) {
        this.stared = stared;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
