package aaron.geist.myreader.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aaron.geist.myreader.R;
import aaron.geist.myreader.database.DBManager;
import aaron.geist.myreader.domain.CrawlerRequest;
import aaron.geist.myreader.domain.Post;
import aaron.geist.myreader.domain.Website;
import aaron.geist.myreader.extend.PostAdapter;
import aaron.geist.myreader.extend.RefreshLayout;
import aaron.geist.myreader.loader.AsyncSiteCrawler;
import aaron.geist.myreader.loader.AsyncSiteCrawlerResponse;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AsyncSiteCrawlerResponse, RefreshLayout.OnRefreshListener, RefreshLayout.OnLoadListener {

    private ListView listView = null;
    private RefreshLayout postRefresh = null;
    private BaseAdapter adapter = null;
    private DBManager dbManager = null;
    private List<Post> adapterList = new ArrayList<>();
    private int pageNum = 1;
    private Map<Long, Integer> initPostIdMap = new HashMap<>();

    private Long siteId = -1L;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        grantPermissions();

        showPosts();
    }

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

    private void showPosts() {
        listView = (ListView) findViewById(R.id.postTitleList);
        Drawable Null = new ColorDrawable();
        Null.setAlpha(0);
        listView.setDivider(Null);
        // bug: must setDivider first, otherwise divider will be invisible
        listView.setDividerHeight(20);
        dbManager = new DBManager(this);

        List<Website> websites = dbManager.getAllWebsites();
        if (websites != null && websites.size() > 0) {
            // TODO load all posts of all sites
            siteId = websites.get(0).getId();
            loadAllPostTitle(siteId);
        }

        // pull to refresh latest posts
        postRefresh = (RefreshLayout) findViewById(R.id.postRefresh);
        postRefresh.setOnRefreshListener(this);
        postRefresh.setOnLoadListener(this);
        postRefresh.setColorSchemeColors(getResources().getColor(R.color.lightRed),
                getResources().getColor(R.color.lightBlue), getResources().getColor(R.color.lightYellow));
    }

    public void loadAllPostTitle(long siteId) {

        Integer initPostId = dbManager.getMaxPostIdByWebsite(siteId);
        initPostIdMap.put(siteId, initPostId);

        adapterList = dbManager.getPosts(pageNum, initPostId);
        Log.d("", "load post title number=" + adapterList.size());

        adapter = new PostAdapter(this, adapterList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Post selectedPost = (Post) listView.getItemAtPosition(pos);

                Intent intent = new Intent();
                intent.putExtra(PostActivity.POST_ITEM, selectedPost);
                intent.setClass(view.getContext(), PostActivity.class);
                startActivity(intent);

                // update post as read
                if (!selectedPost.isRead()) {
                    selectedPost.setRead(true);
                    dbManager.updatePostRead(selectedPost.getId(), true);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int pos, long l) {
                new AlertDialog.Builder(view.getContext()).setTitle("DELETE CURRENT POST?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Post selectPost = (Post) listView.getItemAtPosition(pos);
                                dbManager.removePost(selectPost);
                                adapterList.remove(selectPost);
                                adapter.notifyDataSetChanged();
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
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_subscribe) {
            Intent intent = new Intent();
            intent.setClass(this.getApplicationContext(), SubscribeActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onTaskCompleted(Boolean crawlSuccess, List<Post> posts, boolean isReverse) {
        postRefresh.setRefreshing(false);
        postRefresh.setLoading(false);
        if (crawlSuccess) {
            if (isReverse) {
                adapterList.addAll(posts);
            } else {
                adapterList.addAll(0, posts);
            }
            adapter.notifyDataSetChanged();
        }
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
        // TODO load all sites
        Integer initPostId = initPostIdMap.get(dbManager.getAllWebsites().get(0).getId());

        // load local DB first
        List<Post> posts = dbManager.getPosts(++pageNum, initPostId);
        if (posts.size() > 0) {
            Toast.makeText(this, "Load posts:" + posts.size(), Toast.LENGTH_SHORT).show();
            this.onTaskCompleted(true, posts, true);
            return;
        }

        // if all loaded, then load from online
        Website website = dbManager.getWebsiteById(siteId);
        AsyncSiteCrawler crawler = new AsyncSiteCrawler(getApplication().getApplicationContext());
        crawler.response = this;
        CrawlerRequest request = new CrawlerRequest();
        request.setWebsite(website);
        request.setReverse(true);
        crawler.execute(request);
    }
}
