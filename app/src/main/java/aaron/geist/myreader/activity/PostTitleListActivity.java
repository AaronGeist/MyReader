package aaron.geist.myreader.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aaron.geist.myreader.R;
import aaron.geist.myreader.constant.DBContants;
import aaron.geist.myreader.database.DBManager;
import aaron.geist.myreader.domain.CrawlerRequest;
import aaron.geist.myreader.domain.Post;
import aaron.geist.myreader.domain.Website;
import aaron.geist.myreader.extend.RefreshLayout;
import aaron.geist.myreader.loader.AsyncSiteCrawler;
import aaron.geist.myreader.loader.AsyncSiteCrawlerResponse;

/**
 * Created by Aaron on 2015/8/7.
 */
public class PostTitleListActivity extends Activity implements AsyncSiteCrawlerResponse, RefreshLayout.OnRefreshListener, RefreshLayout.OnLoadListener {

    ListView listView = null;
    RefreshLayout postRefresh = null;
    SimpleAdapter adapter = null;
    DBManager dbManager = null;
    long siteId;

    List<Map<String, String>> adapterList = new ArrayList<Map<String, String>>();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_title);

        Intent intent = this.getIntent();
        siteId = intent.getLongExtra(DBContants.POST_COLUMN_EXTERNAL_ID, 0);

        listView = (ListView) findViewById(R.id.postTitleList);
        dbManager = new DBManager(this);

        loadAllPostTitle(siteId);

        // pull to refresh latest posts
        postRefresh = (RefreshLayout) findViewById(R.id.postRefresh);
        postRefresh.setOnRefreshListener(this);
        postRefresh.setOnLoadListener(this);
        postRefresh.setColorSchemeColors(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
    }

    public void loadAllPostTitle(long siteId) {

        List<Post> postList = dbManager.getAllPostsBySiteId(siteId);
        Log.d("", "load post title number=" + postList.size());

        for (Post post : postList) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(DBContants.POST_COLUMN_TITLE, post.getTitle());
            map.put(DBContants.COLUMN_ID, String.valueOf(post.getId()));
            adapterList.add(map);
        }
        adapter = new SimpleAdapter(this, adapterList, R.layout.post_title_item,
                new String[]{DBContants.POST_COLUMN_TITLE}, new int[]{R.id.postTitle});

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Map<String, String> map = (Map<String, String>) listView.getItemAtPosition(pos);
                long postId = Long.valueOf(map.get(DBContants.COLUMN_ID));

                Log.d("", "select post id=" + postId);

                Intent intent = new Intent();
                intent.putExtra(DBContants.COLUMN_ID, postId);
                intent.setClass(view.getContext(), PostActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onTaskCompleted(Boolean crawlSuccess, List<Post> posts, boolean isReverse) {
        postRefresh.setRefreshing(false);
        adapterList.clear();

        List<Post> postList = dbManager.getAllPostsBySiteId(siteId);
        Log.d("", "load post title number=" + postList.size());

        for (Post post : postList) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(DBContants.POST_COLUMN_TITLE, post.getTitle());
            map.put(DBContants.COLUMN_ID, String.valueOf(post.getId()));
            adapterList.add(map);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        Website website = dbManager.getWebsiteById(siteId);
        AsyncSiteCrawler crawler = new AsyncSiteCrawler(getApplication().getApplicationContext());
        crawler.response = this;
        CrawlerRequest request = new CrawlerRequest();
        request.setWebsite(website);
        request.setReverse(false);
        crawler.execute(request);
    }

    @Override
    public void onLoad() {
        Website website = dbManager.getWebsiteById(siteId);
        AsyncSiteCrawler crawler = new AsyncSiteCrawler(getApplication().getApplicationContext());
        crawler.response = this;
        CrawlerRequest request = new CrawlerRequest();
        request.setWebsite(website);
        request.setReverse(true);
        crawler.execute(request);
    }
}
