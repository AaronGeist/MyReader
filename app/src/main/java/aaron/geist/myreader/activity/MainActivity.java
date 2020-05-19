package aaron.geist.myreader.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
    private List<Post> postListCache = new ArrayList<>();
    private long targetWebSiteId = ALL_WEBSITE;
    private Integer currentDbPageNum = 1;

    // constants
    private static final long ALL_WEBSITE = -1L;

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
    @SuppressLint("ClickableViewAccessibility")
    private void initResources() {
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Snackbar.make(view, "回到顶端", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            postTitleListView.smoothScrollToPosition(0);
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
        postTitleListView.setOnItemClickListener((adapterView, view, pos, l) -> {
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
        });

        postTitleListView.setOnItemLongClickListener((adapterView, view, pos, l) -> {
            new AlertDialog.Builder(view.getContext()).setTitle("DELETE CURRENT POST?")
                    .setPositiveButton("YES", (dialogInterface, i) -> {
                        Post selectPost = (Post) postTitleListView.getItemAtPosition(pos);
                        postListCache.remove(selectPost);
                        postList.remove(selectPost);
                        postTitleListAdapter.notifyDataSetChanged();

                        dbManager.removePost(selectPost);
                    })
                    .setNegativeButton("NO", (dialogInterface, i) -> {
                    })
                    .show();

            return true;
        });

        //使用SimpleOnGestureListener可以只覆盖实现自己想要的手势
        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) { //DoubleTap手势的处理
                postTitleListView.smoothScrollToPosition(0);
                return super.onDoubleTap(e);
            }
        });

        //使用GestureDetector对Toolbar进行手势监听
        toolbar.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
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
    @SuppressWarnings("unchecked")
    private void loadAllPosts() {
        List<Website> websites = dbManager.getAllWebsites();

        if (websites == null || websites.size() == 0) {
            toastShort("请先订阅一个网站");
            return;
        }

        Collection<Long> websiteIds = new ArrayList<>();
        websites.forEach(w -> websiteIds.add(w.getId()));

        postListCache.addAll(dbManager.getPosts(currentDbPageNum, websiteIds));

        // sort with timestamp, then website, then externalId
        Collections.sort(postListCache);

        postList.addAll(postListCache);
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

        SubMenu editMenu = menu.addSubMenu("过滤显示");

        Map<String, Website> websites = SubscribeManager.getInstance().getAll();

        //添加菜单项
        for (Website website : websites.values()) {
            editMenu.add(1, (int) website.getId(), 1, website.getName());
        }

        editMenu.add(1, (int) ALL_WEBSITE, 1, "全部");

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

        // 只显示单个网站内容，或显示所有内容
        if (item.getGroupId() == 1) {
            targetWebSiteId = item.getItemId();
            updatePostList();
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
    public void onTaskCompleted(Boolean crawlSuccess, List<Post> posts, boolean isReverse, String websiteName) {
        postRefresh.setRefreshing(false);
        postRefresh.setLoading(false);
        if (crawlSuccess) {
            toastShort(websiteName + "加载完毕，新增 " + posts.size() + " 条");

            if (isReverse) {
                postListCache.addAll(posts);
            } else {
                postListCache.addAll(0, posts);
            }

            // re-order all posts
            Collections.sort(postListCache);

            updatePostList();
        } else {
            toastShort("加载失败");
        }
    }

    @Override
    public void onRefresh() {
        List<Website> targetWebsites = getTargetWebsites();

        if (targetWebsites.isEmpty()) {
            postRefresh.setRefreshing(false);
            postRefresh.setLoading(false);
            toastShort("请先订阅一个网站");

            return;
        }

        toastShort("看看有什么新货");

        // define specified executor to enable parallel for AsyncTask
        Executor executor = Executors.newFixedThreadPool(10);
        for (Website website : targetWebsites) {
            CrawlerRequest request = new CrawlerRequest();
            request.setWebsite(website);
            request.setReverse(false);
            // TODO make it a setting
            request.setTargetNum(10);

            AsyncSiteCrawler crawler = new AsyncSiteCrawler();
            crawler.setCallback(this);
            crawler.executeOnExecutor(executor, request);
        }
    }

    @Override
    public void onLoad() {
        List<Website> websites = getTargetWebsites();

        if (websites.isEmpty()) {
            postRefresh.setRefreshing(false);
            postRefresh.setLoading(false);
            toastShort("请先订阅一个网站");

            return;
        }

        Collection<Long> websiteIds = new ArrayList<>();
        for (Website website : websites) {
            websiteIds.add(website.getId());
        }

        // load posts from local DB
        List<Post> posts = dbManager.getPosts(++currentDbPageNum, websiteIds);

        if (posts.isEmpty()) {
            toastShort("看看有什么旧货");
            // define specified executor to enable parallel for AsyncTask
            Executor executor = Executors.newFixedThreadPool(10);
            for (Website website : websites) {
                CrawlerRequest request = new CrawlerRequest();
                request.setWebsite(website);
                request.setReverse(true);
                request.setTargetNum(20);

                AsyncSiteCrawler crawler = new AsyncSiteCrawler();
                crawler.setCallback(this);
                crawler.executeOnExecutor(executor, request);
            }
        } else {
            this.onTaskCompleted(true, posts, true, "");
        }
    }

    private void updatePostList() {
        if (targetWebSiteId != ALL_WEBSITE) {
            postList.clear();
            postList.addAll(postListCache.stream().filter((post) -> post.getWebsiteId() == targetWebSiteId).collect(Collectors.toList()));
        } else if (postList.size() != postListCache.size()) {
            postList.clear();
            postList.addAll(postListCache);
        }

        postTitleListAdapter.notifyDataSetChanged();
    }

    private List<Website> getTargetWebsites() {
        List<Website> websites = Lists.newArrayList();
        if (targetWebSiteId == ALL_WEBSITE) {
            websites.addAll(SubscribeManager.getInstance().getAll().values());
        } else {
            Website website = DBManager.getInstance().getWebsiteById(targetWebSiteId);
            websites.add(website);
        }

        return websites;
    }

    private void toastShort(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
