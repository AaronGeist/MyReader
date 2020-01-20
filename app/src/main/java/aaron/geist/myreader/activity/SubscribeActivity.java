package aaron.geist.myreader.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;

import aaron.geist.myreader.R;
import aaron.geist.myreader.constant.SiteConfig;
import aaron.geist.myreader.extend.SubscribeItemAdapter;
import aaron.geist.myreader.subscriber.SubscribeManager;

/**
 * Created by yzhou7 on 2015/7/27.
 */
public class SubscribeActivity extends Activity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subscribe);

        RecyclerView subscriptionList = findViewById(R.id.subscribe_list);
        subscriptionList.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false));
        RecyclerView.Adapter subItemAdapter = new SubscribeItemAdapter(getApplicationContext(), Arrays.asList(SiteConfig.values()));
        subscriptionList.setAdapter(subItemAdapter);

        initButton();
    }

    public void initButton() {
        Button btn = findViewById(R.id.clearSubBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubscribeManager.getInstance().unSubscribeAll();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
