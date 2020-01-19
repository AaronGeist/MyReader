package aaron.geist.myreader.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import aaron.geist.myreader.R;
import aaron.geist.myreader.database.DBManager;
import aaron.geist.myreader.domain.Post;

/**
 * Created by Aaron on 2015/8/7.
 */
public class PostActivity extends AppCompatActivity {

    private DBManager dbManager = null;
    private Post currentPost = null;
    private ImageButton collectBtn = null;

    public static final String POST_ITEM = "postItem";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_main);
        Toolbar postToolbar = findViewById(R.id.postToolbar);
        setSupportActionBar(postToolbar);
        postToolbar.setNavigationIcon(R.drawable.ic_menu_back);

        postToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });

        collectBtn = findViewById(R.id.collectBtn);

        Intent intent = this.getIntent();
        currentPost = (Post) intent.getSerializableExtra(POST_ITEM);
        dbManager = new DBManager(this);
        showPost(currentPost);

        setBtnListener();
    }

    private void showPost(Post post) {
        Log.d("", "Loading post id=" + post.getId());

        currentPost = dbManager.getSinglePost(currentPost.getId());
        final TextView title = findViewById(R.id.singlePostTitle);
        title.setText(currentPost.getTitle());
        final WebView content = findViewById(R.id.singlePostContent);
        content.loadDataWithBaseURL(currentPost.getUrl(), currentPost.getContent(), "text/html", "utf-8", null);

        if (currentPost.isStarted()) {
            collectBtn.setBackground(getDrawable(R.drawable.star));
        }
    }

    private void setBtnListener() {
        collectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPost.setStared(!currentPost.isStarted());
                dbManager.updatePostStar(currentPost.getId(), currentPost.isStarted());
                collectBtn.setBackground(currentPost.isStarted() ? getDrawable(R.drawable.star) : getDrawable(R.drawable.unstar));
            }
        });
    }

}
