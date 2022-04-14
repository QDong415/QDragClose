package com.dq.dragclosedemo.waterfall;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dq.dragclosedemo.R;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class WaterfallAdapter extends RecyclerView.Adapter<WaterfallAdapter.ViewHolder> {

    private List<GoodsBean> list;
    private Context context;

    //item上的控件的点击事件
    private OnItemClickListener onItemClickListener;

    public WaterfallAdapter(Context context, List<GoodsBean> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.griditem_goods, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null)
                    onItemClickListener.onItemClick((Integer) viewHolder.itemView.getTag(), v);
            }
        });
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)holder.coverImageView.getLayoutParams();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int width = (dm.widthPixels - 36)/2;//宽度为屏幕宽度一半
        float height = list.get(position).getPhotoOriginalHeight()*width/(float)list.get(position).getPhotoOriginalWidth();//计算View的高度
        params.height = (int)height;
        params.width =  width;

        holder.itemView.setTag(position);

        Glide.with(context)//.context是MainActivity
                .load(list.get(position).getPictures().get(0).getUrl())
                .into(holder.coverImageView);
        holder.nameTextView.setText(position % 2 == 0 ? "跳到非侧滑Ac":"跳到<可侧滑>Ac");

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView coverImageView;
        private TextView nameTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            coverImageView = itemView.findViewById(R.id.cover);
            nameTextView = itemView.findViewById(R.id.name_tv);
        }
    }

    interface OnItemClickListener {
        void onItemClick(int position, View v);
    }
}
