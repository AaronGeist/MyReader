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
import aaron.geist.myreader.utils.ToastUtil;

public class SubscribeItemAdapter extends RecyclerView.Adapter<SubscribeItemAdapter.ViewHolder> {

    private List<SiteConfig> mItems;
    private Context mContext;

    private SubscribeManager subscribeManager;

    public SubscribeItemAdapter(Context context, List<SiteConfig> items) {
        super();
        mItems = items;
        mContext = context;
        subscribeManager = new SubscribeManager();
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
        holder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.toastLong(siteConfig.getName());
                subscribeManager.subscribe(siteConfig);
            }
        });

        holder.tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String siteId = map.get(DBContants.COLUMN_ID);
//
//                Intent intent = new Intent();
//                intent.putExtra(DBContants.POST_COLUMN_EXTERNAL_ID, Long.valueOf(siteId));
//                intent.setClass(view.getContext(), PostTitleListActivity.class);
//                view.getContext().startActivity(intent);
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
