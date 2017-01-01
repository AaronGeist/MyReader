package aaron.geist.myreader.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import aaron.geist.myreader.R;
import aaron.geist.myreader.constant.DBContants;
import aaron.geist.myreader.database.DBManager;
import aaron.geist.myreader.storage.Post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yzhou7 on 2015/8/7.
 */
public class PostTitleListActivity extends Activity {

    ListView listView = null;
    DBManager dbManager = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_title);

        Intent intent = this.getIntent();
        long siteId = intent.getLongExtra(DBContants.POST_COLUMN_EXTERNAL_ID, 0);

        listView = (ListView) findViewById(R.id.postTitleList);
        dbManager = new DBManager(this);

        loadAllPostTitle(siteId);
    }

    public void loadAllPostTitle(long siteId) {

        List<Post> postList = dbManager.getAllPostsBySiteId(siteId);
        Log.d("", "load post title number=" + postList.size());
        List<Map<String, String>> adapterList = new ArrayList<Map<String, String>>();
        for (Post post : postList) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(DBContants.POST_COLUMN_TITLE, post.getTitle());
            map.put(DBContants.COLUMN_ID, String.valueOf(post.getId()));
            adapterList.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, adapterList, R.layout.post_title_list,
                new String[]{DBContants.POST_COLUMN_TITLE, DBContants.COLUMN_ID}, new int[]{R.id.postTitle, R.id.postId});
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
}
