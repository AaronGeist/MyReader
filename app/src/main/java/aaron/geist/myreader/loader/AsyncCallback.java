package aaron.geist.myreader.loader;

import java.util.List;

import aaron.geist.myreader.domain.Post;

/**
 * Created by yzhou7 on 2015/8/7.
 */
public interface AsyncCallback {

    void onTaskCompleted(Boolean crawlSuccess, List<Post> newPosts, boolean isReverse, String websiteName);

}
