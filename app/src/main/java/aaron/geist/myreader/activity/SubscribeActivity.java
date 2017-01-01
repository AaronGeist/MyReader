package aaron.geist.myreader.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import aaron.geist.myreader.R;
import aaron.geist.myreader.constant.DBContants;
import aaron.geist.myreader.storage.Website;
import aaron.geist.myreader.subscriber.SubscribeManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yzhou7 on 2015/7/27.
 */
public class SubscribeActivity extends Activity {

    private ListView listView;
    private SubscribeManager subscribeManager;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subscribe);

        subscribeManager = new SubscribeManager(this);
        listView = (ListView) findViewById(R.id.siteList);
        setBtnListener();
        loadAllSites();
    }

    public void addSite(View view) {
        EditText newWebsiteInput = (EditText) findViewById(R.id.newSiteInput);
        String url = newWebsiteInput.getText().toString();
        subscribeManager.subscribe(url);
    }

    @SuppressWarnings("unchecked")
    public void loadAllSites() {
        List<Website> websites = subscribeManager.getAll();
        List<Map<String, String>> adapterList = new ArrayList<Map<String, String>>();
        for (Website website : websites) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(DBContants.WEBSITE_COLUMN_NAME, website.getName());
            map.put(DBContants.WEBSITE_COLUMN_HOMEPAGE, website.getHomePage());
            map.put(DBContants.COLUMN_ID, String.valueOf(website.getId()));
            adapterList.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, adapterList, R.layout.feed_list,
                new String[]{DBContants.WEBSITE_COLUMN_NAME, DBContants.WEBSITE_COLUMN_HOMEPAGE}, new int[]{R.id.feedName, R.id.feedHomePage});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Map<String, String> map = (Map<String, String>) listView.getItemAtPosition(pos);
                String siteId = map.get(DBContants.COLUMN_ID);

                Intent intent = new Intent();
                intent.putExtra(DBContants.POST_COLUMN_EXTERNAL_ID, Long.valueOf(siteId));
                intent.setClass(view.getContext(), PostTitleListActivity.class);
                startActivity(intent);
            }
        });
    }

    public void remove(View view) {
//        mgr.removeTable();
    }

    public void setBtnListener() {
        Button btn = (Button) findViewById(R.id.addSiteBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSite(view);
            }
        });

        btn = (Button) findViewById(R.id.removeAllSitesBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove(view);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscribeManager.destory();
    }
}
