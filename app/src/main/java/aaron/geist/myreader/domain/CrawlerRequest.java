package aaron.geist.myreader.domain;

/**
 * Created by Aaron on 2017/1/2.
 */

public class CrawlerRequest {

    private Website website;
    private boolean reverse;

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

    public boolean isReverse() {
        return reverse;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }
}
