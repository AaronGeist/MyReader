package aaron.geist.myreader.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import aaron.geist.myreader.R;
import aaron.geist.myreader.database.DBManager;
import aaron.geist.myreader.domain.CrawlerRequest;
import aaron.geist.myreader.domain.Post;
import aaron.geist.myreader.domain.Website;
import aaron.geist.myreader.extend.PostTitleListAdapter;
import aaron.geist.myreader.extend.RefreshLayout;
import aaron.geist.myreader.loader.AsyncCallback;
import aaron.geist.myreader.loader.AsyncSiteCrawler;
import aaron.geist.myreader.subscriber.SubscribeManager;
import aaron.geist.myreader.utils.ToastUtil;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AsyncCallback, RefreshLayout.OnRefreshListener, RefreshLayout.OnLoadListener {

    // tag for log indicator
    private static final String TAG = MainActivity.class.getSimpleName();

    private static Context context;

    // android components
    private Toolbar toolbar = null;
    private ListView postTitleListView = null;
    private RefreshLayout postRefresh = null;
    private BaseAdapter postTitleListAdapter = null;

    // services
    private DBManager dbManager = null;

    // local variables
    private List<Post> postList = new ArrayList<>();
    private Integer startPostId = -1;
    private Integer currentDbPageNum = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        grantPermissions();

        initResources();

        initServices();

        loadAllPosts();
    }

    public static Context getContext() {
        return context;
    }

    /**
     * Grant all essential permissions, including:
     * <ul>
     * <li>Read storage</li>
     * <li>Write storage</li>
     * </ul>
     */
    private void grantPermissions() {
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    /**
     * Initialize all components in activity.
     */
    private void initResources() {
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // set list view style
        postTitleListView = findViewById(R.id.postTitleList);
        postTitleListView.setVerticalScrollBarEnabled(false);
        postTitleListView.setHorizontalScrollBarEnabled(false);
        Drawable Null = new ColorDrawable();
        Null.setAlpha(0);
        postTitleListView.setDivider(Null);
        // bug: must setDivider first, otherwise divider will be invisible
        postTitleListView.setDividerHeight(20);

        // pull to refresh latest posts
        postRefresh = findViewById(R.id.postRefresh);
        postRefresh.setOnRefreshListener(this);
        postRefresh.setOnLoadListener(this);
        postRefresh.setColorSchemeColors(getResources().getColor(R.color.lightRed),
                getResources().getColor(R.color.lightBlue), getResources().getColor(R.color.lightYellow));

        postTitleListAdapter = new PostTitleListAdapter(this, postList);
        postTitleListView.setAdapter(postTitleListAdapter);
        postTitleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Post selectedPost = (Post) postTitleListView.getItemAtPosition(pos);

                Intent intent = new Intent();
                intent.putExtra(PostActivity.POST_DATA, selectedPost);
                intent.setClass(view.getContext(), PostActivity.class);
                startActivity(intent);

                // update post as read
                if (!selectedPost.isRead()) {
                    selectedPost.setRead(true);
                    postTitleListAdapter.notifyDataSetChanged();

                    dbManager.updatePostRead(selectedPost.getId(), true);
                }
            }
        });

        postTitleListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int pos, long l) {
                new AlertDialog.Builder(view.getContext()).setTitle("DELETE CURRENT POST?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Post selectPost = (Post) postTitleListView.getItemAtPosition(pos);
                                postList.remove(selectPost);
                                postTitleListAdapter.notifyDataSetChanged();

                                dbManager.removePost(selectPost);
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();

                return true;
            }
        });

        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() { //使用SimpleOnGestureListener可以只覆盖实现自己想要的手势
            @Override
            public boolean onDoubleTap(MotionEvent e) { //DoubleTap手势的处理
                postTitleListView.smoothScrollToPosition(0);
                return super.onDoubleTap(e);
            }
        });

        toolbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) { //使用GestureDetector对Toolbar进行手势监听
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    /**
     * Initialize all services in background.
     */
    private void initServices() {
        // create database manager
        dbManager = DBManager.getInstance();
    }

    /**
     * Load all existing posts in DB
     */
    private void loadAllPosts() {
        List<Website> websites = dbManager.getAllWebsites();

        if (websites == null || websites.size() == 0) {
            ToastUtil.toastLong("请先配置至少一个订阅网站");
            return;
        }

        Collection<Long> websiteIds = new ArrayList<>();
        for (Website website : websites) {
            websiteIds.add(website.getId());
        }
        startPostId = dbManager.getMaxPostIdByWebsite(websiteIds);

        postList.addAll(dbManager.getPosts(currentDbPageNum, startPostId, websiteIds));

        // sort with timestamp, then website, then externalId
        Collections.sort(postList);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_subscribe) {
            Intent intent = new Intent();
            intent.setClass(this.getApplicationContext(), SubscribeActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
        } else if (id == R.id.nav_send) {
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onTaskCompleted(Boolean crawlSuccess, List<Post> posts, boolean isReverse) {
        postRefresh.setRefreshing(false);
        postRefresh.setLoading(false);
        if (crawlSuccess) {
            ToastUtil.toastLong("加载完毕，新增 " + posts.size() + " 条");

            if (isReverse) {
                postList.addAll(posts);
            } else {
                postList.addAll(0, posts);
            }

            // re-order all posts
            Collections.sort(postList);

            postTitleListAdapter.notifyDataSetChanged();
        } else {
            ToastUtil.toastLong("加载失败");
        }
    }

    @Override
    public void onRefresh() {
        ToastUtil.toastShort("看看有什么新货");

        // define specified executor to enable parallel for AsyncTask
        Executor executor = Executors.newFixedThreadPool(10);
        List<Website> websites = dbManager.getAllWebsites();
        for (Website website : websites) {
            CrawlerRequest request = new CrawlerRequest();
            request.setWebsite(website);
            request.setReverse(false);
            // TODO make it a setting
            request.setTargetNum(20);

            AsyncSiteCrawler crawler = new AsyncSiteCrawler();
            crawler.setCallback(this);
            crawler.executeOnExecutor(executor, request);
        }
    }

    @Override
    public void onLoad() {
        Map<String, Website> websites = SubscribeManager.getInstance().getAll();
        Collection<Long> websiteIds = new ArrayList<>();
        for (Website website : websites.values()) {
            websiteIds.add(website.getId());
        }

        // load posts from local DB
        List<Post> posts = dbManager.getPosts(++currentDbPageNum, startPostId, websiteIds);
        this.onTaskCompleted(true, posts, true);
    }
}
