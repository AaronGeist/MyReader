package aaron.geist.myreader.loader;

import org.jsoup.nodes.Document;

/**
 * Created by yzhou7 on 2015/8/7.
 */
public interface AsyncPageLoaderResponse {

    void onTaskCompleted(Document output);

}
