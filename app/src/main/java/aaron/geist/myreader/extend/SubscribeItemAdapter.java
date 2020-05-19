package aaron.geist.myreader.extend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import aaron.geist.myreader.R;
import aaron.geist.myreader.constant.SiteConfig;
import aaron.geist.myreader.subscriber.SubscribeManager;

public class SubscribeItemAdapter extends RecyclerView.Adapter<SubscribeItemAdapter.ViewHolder> {

    private List<SiteConfig> mItems;
    private Context mContext;

    private SubscribeManager subscribeManager;

    public SubscribeItemAdapter(Context context, List<SiteConfig> items) {
        super();
        mItems = items;
        mContext = context;
        subscribeManager = SubscribeManager.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.subscribe_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final SiteConfig siteConfig = mItems.get(position);
        holder.tv.setText(siteConfig.getName());

        if (subscribeManager.hasSubscribed(siteConfig.getName())) {
            // already subscribed
            holder.btn.setText(R.string.website_unsub);
        }

        holder.btn.setOnClickListener(v -> {
            if (subscribeManager.hasSubscribed(siteConfig.getName())) {
                subscribeManager.unsubscribe(siteConfig);
                ((Button) v).setText(R.string.website_sub);
            } else {
                subscribeManager.subscribe(siteConfig);
                ((Button) v).setText(R.string.website_unsub);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        Button btn;

        public ViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.sub_item_tv);
            btn = itemView.findViewById(R.id.sub_btn);
        }
    }


}
