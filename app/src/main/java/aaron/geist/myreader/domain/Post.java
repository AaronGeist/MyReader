package aaron.geist.myreader.domain;

import java.io.Serializable;

/**
 * Created by Aaron on 2015/7/27.
 */
public class Post implements Serializable, Comparable {

    private static final long serialVersionUID = 9150231278934934185L;

    private long id;
    private String title;
    private String content;
    /**
     * post id in original website.
     */
    private int externalId;
    private String url;
    private long timestamp;
    private long websiteId;
    private boolean mark;
    private boolean read;
    private boolean inOrder;
    private String hash;

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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getWebsiteId() {
        return websiteId;
    }

    public void setWebsiteId(long websiteId) {
        this.websiteId = websiteId;
    }

    public boolean isMarked() {
        return mark;
    }

    public void setMarked(boolean mark) {
        this.mark = mark;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isInOrder() {
        return inOrder;
    }

    public void setInOrder(boolean inOrder) {
        this.inOrder = inOrder;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * Sort by timestamp, then external ID
     *
     * @param another post to be compared
     * @return 1, -1 or 0
     */
    @Override
    public int compareTo(Object another) {
        Post post = (Post) another;

        int res = Long.compare(this.getTimestamp(), post.getTimestamp());
        if (res == 0) {
            res = Long.compare(this.getWebsiteId(), post.getWebsiteId());
            if (res == 0) {
                res = Integer.compare(this.getExternalId(), post.getExternalId());
            }
        }

        // in desc order, so add minus in the front
        return -res;
    }


}
