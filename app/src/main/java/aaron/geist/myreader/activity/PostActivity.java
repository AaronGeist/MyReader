package aaron.geist.myreader.activity;

import android.content.Intent;
import android.os.Bundle;
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

    private Post post = null;
    private ImageButton markBtn = null;
    private TextView title = null;
    private WebView content = null;

    public static final String POST_DATA = "post_data";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_main);
        Toolbar postToolbar = findViewById(R.id.postToolbar);
        setSupportActionBar(postToolbar);
        postToolbar.setNavigationIcon(R.drawable.close);

        postToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });

        markBtn = findViewById(R.id.markBtn);
        title = findViewById(R.id.singlePostTitle);
        content = findViewById(R.id.singlePostContent);

        Intent intent = this.getIntent();
        post = (Post) intent.getSerializableExtra(POST_DATA);

        showPost();

        setBtnListener();
    }

    private void showPost() {
        title.setText(post.getTitle());
        content.loadDataWithBaseURL(this.post.getUrl(), post.getContent(), "text/html", "utf-8", null);

        if (post.isMarked()) {
            markBtn.setBackground(getDrawable(R.drawable.mark_yes));
        }
    }

    private void setBtnListener() {
        markBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                post.setMarked(!post.isMarked());
                DBManager.getInstance().updatePostStar(post.getId(), post.isMarked());
                markBtn.setBackground(post.isMarked() ? getDrawable(R.drawable.mark_yes) : getDrawable(R.drawable.mark_no));
            }
        });
    }

}
