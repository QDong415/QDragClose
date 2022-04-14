package com.dq.dragclosedemo.detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dq.dragclosedemo.R;
import java.util.List;
import androidx.recyclerview.widget.RecyclerView;

public class DetailRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0; //说明是带有Header的
    private static final int TYPE_NORMAL = 1; //说明是普通Item

    //获取从Activity中传递过来每个item的数据集合
    private List<String> mDatas;
    //HeaderView, FooterView
    private View mHeaderView;

    //构造函数
    public DetailRecyclerAdapter(List<String> list) {
        this.mDatas = list;
    }

    //HeaderView和FooterView的get和set函数
    public View getHeaderView() {
        return mHeaderView;
    }

    public void setHeaderView(View headerView) {
        this.mHeaderView = headerView;
        notifyItemInserted(0);
    }

    /**
     * 重写这个方法，很重要，是加入Header和Footer的关键，我们通过判断item的类型，从而绑定不同的view *
     */
    @Override
    public int getItemViewType(int position) {
        if (mHeaderView == null) {
            return TYPE_NORMAL;
        }
        if (position == 0) {
            //第一个item应该加载Header
            return TYPE_HEADER;
        }
        return TYPE_NORMAL;
    }

    //创建View，如果是HeaderView或者是FooterView，直接在Holder中返回
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderView != null && viewType == TYPE_HEADER) {
            return new RecyclerView.ViewHolder(mHeaderView){};
        }
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_follower, parent, false);
        return new ListHolder(layout);
    }

    //绑定View，这里是根据返回的这个position的类型，从而进行绑定的， HeaderView和FooterView, 就不同绑定了
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_NORMAL) {
            if (holder instanceof ListHolder) {
                //这里加载数据的时候要注意，是从position-1开始，因为position==0已经被header占用了
                ((ListHolder) holder).tv.setText(mDatas.get(position - (mHeaderView == null ? 0 : 1)));
                return;
            }
        } else if (getItemViewType(position) == TYPE_HEADER) {
        }
    }

    //在这里面加载ListView中的每个item的布局
    public static class ListHolder extends RecyclerView.ViewHolder {
        TextView tv;

        public ListHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.name_tv);
        }
    }

    //返回View中Item的个数，这个时候，总的个数应该是ListView中Item的个数加上HeaderView和FooterView
    @Override
    public int getItemCount() {
        if (mHeaderView != null) {
            return mDatas.size() + 1;
        }
        return mDatas.size();
    }
}