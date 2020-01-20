package aaron.geist.myreader.extend;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import aaron.geist.myreader.R;
import aaron.geist.myreader.domain.Post;
import aaron.geist.myreader.subscriber.SubscribeManager;

/**
 * Created by Aaron on 2017/1/9.
 */

public class PostTitleListAdapter extends BaseAdapter {

    private Context context = null;
    private List<Post> posts = null;
    private LayoutInflater inflater;


    public PostTitleListAdapter(Context context, List<Post> posts) {
        this.context = context;
        if (posts != null) {
            this.posts = posts;
        }
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public Object getItem(int i) {
        return posts.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = inflater.inflate(R.layout.post_title_item, viewGroup, false);
            viewHolder = new ViewHolder();

            viewHolder.layout = view.findViewById(R.id.content_main);
            viewHolder.textViewTile = view.findViewById(R.id.postTitle);
            viewHolder.meta = view.findViewById(R.id.postMeta);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Post post = posts.get(i);
        viewHolder.textViewTile.setText(post.getTitle());

        Date date = new Date(post.getTimestamp());

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        viewHolder.meta.setText(sdf.format(date) + " " + SubscribeManager.getInstance().queryNameById(post.getWebsiteId()));

        if (post.isRead()) {
            viewHolder.textViewTile.setTextColor(context.getColor(R.color.fontRead));
        } else {
            viewHolder.textViewTile.setTextColor(context.getColor(R.color.font));

        }

        return view;
    }

    private class ViewHolder {
        private RelativeLayout layout;
        private TextView textViewTile;
        private TextView meta;
    }
}
