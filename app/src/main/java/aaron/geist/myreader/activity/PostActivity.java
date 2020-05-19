package aaron.geist.myreader.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;

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
    private Toolbar postToolbar = null;
    private WebView content = null;

    public static final String POST_DATA = "post_data";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_main);
        postToolbar = findViewById(R.id.postToolbar);
        setSupportActionBar(postToolbar);
        postToolbar.setNavigationIcon(R.drawable.close);

        postToolbar.setNavigationOnClickListener(view -> finish());

        markBtn = findViewById(R.id.markBtn);
        content = findViewById(R.id.singlePostContent);
        content.setHorizontalScrollBarEnabled(false);

        Intent intent = this.getIntent();
        post = (Post) intent.getSerializableExtra(POST_DATA);

        showPost();

        setBtnListener();
    }

    private void showPost() {
        // have image width auto scaled
        String html = post.getContent().replace("<img", "<img style=\"max-width:100%;height:auto\"");
        html = String.format("<h3>%s</h3>\n%s", post.getTitle(), html);
        content.loadDataWithBaseURL(this.post.getUrl(), html, "text/html", "utf-8", null);

        if (post.isMarked()) {
            markBtn.setBackground(getDrawable(R.drawable.mark_yes));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setBtnListener() {
        markBtn.setOnClickListener(view -> {
            post.setMarked(!post.isMarked());
            DBManager.getInstance().updatePostStar(post.getId(), post.isMarked());
            markBtn.setBackground(post.isMarked() ? getDrawable(R.drawable.mark_yes) : getDrawable(R.drawable.mark_no));
        });

        //使用SimpleOnGestureListener可以只覆盖实现自己想要的手势
        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                //DoubleTap手势的处理
                content.scrollTo(0, 0);
                return super.onDoubleTap(e);
            }
        });

        //使用GestureDetector对Toolbar进行手势监听
        postToolbar.setOnTouchListener((v, event) -> {
            return gestureDetector.onTouchEvent(event);
        });
    }
}
