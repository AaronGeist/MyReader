package aaron.geist.myreader.extend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import aaron.geist.myreader.R;
import aaron.geist.myreader.domain.Post;

/**
 * Created by Aaron on 2017/1/9.
 */

public class PostAdapter extends BaseAdapter {

    private Context context = null;
    private List<Post> posts = null;
    private LayoutInflater inflater;


    public PostAdapter(Context context, List<Post> posts) {
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

            viewHolder.layout = (RelativeLayout) view.findViewById(R.id.content_main);
            viewHolder.textViewTile = (TextView) view.findViewById(R.id.postTitle);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Post post = posts.get(i);
        viewHolder.textViewTile.setText(post.getTitle());
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
    }
}
