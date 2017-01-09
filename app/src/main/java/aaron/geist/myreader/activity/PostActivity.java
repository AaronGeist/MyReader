package aaron.geist.myreader.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

import aaron.geist.myreader.R;
import aaron.geist.myreader.constant.DBContants;
import aaron.geist.myreader.database.DBManager;
import aaron.geist.myreader.domain.Post;

/**
 * Created by Aaron on 2015/8/7.
 */
public class PostActivity extends AppCompatActivity {

    private DBManager dbManager = null;
    private long postId = -1;
    private Post currentPost = null;
    private ImageButton collectBtn = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_main);
        Toolbar postToolbar = (Toolbar) findViewById(R.id.postToolbar);
        setSupportActionBar(postToolbar);
        postToolbar.setNavigationIcon(R.drawable.ic_menu_back);

        postToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });

        collectBtn = (ImageButton) findViewById(R.id.collectBtn);

        Intent intent = this.getIntent();
        postId = intent.getLongExtra(DBContants.COLUMN_ID, 0);
        dbManager = new DBManager(this);
        loadPost(postId);

        setBtnListener();
    }

    private void loadPost(long postId) {
        Log.d("", "Loading post id=" + postId);
        currentPost = dbManager.getSinglePost(postId);

        final TextView title = (TextView) findViewById(R.id.singlePostTitle);
        title.setText(currentPost.getTitle());
        final WebView content = (WebView) findViewById(R.id.singlePostContent);
        content.loadDataWithBaseURL(currentPost.getUrl(), currentPost.getContent(), "text/html", "utf-8", null);

        if (currentPost.isStarted()) {
            collectBtn.setBackground(getDrawable(R.drawable.star));
        }
    }

    private void setBtnListener() {
        collectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                currentPost.setStared(!currentPost.isStarted());
                Post post = dbManager.getSinglePost(postId);
                dbManager.updatePostStar(postId, !post.isStarted());
                collectBtn.setBackground(!post.isStarted() ? getDrawable(R.drawable.star) : getDrawable(R.drawable.unstar));
            }
        });
    }

}
