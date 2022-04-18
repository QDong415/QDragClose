package com.dq.dragclosedemo.detail;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.dq.dragclosedemo.waterfall.PhotoBean;
import java.util.List;

import androidx.viewpager.widget.PagerAdapter;

//详情界面的HeaderView的Adapter（实际开发一般是banner）。我这里只是demo，你可以随意修改
public class BannerViewPagerAdapter extends PagerAdapter {

    private Context context;
    private List<PhotoBean> photoList;

    public BannerViewPagerAdapter(Context context, List<PhotoBean> photoList) {
        this.context = context;
        this.photoList = photoList;
    }

    @Override
    public int getCount() {
        return photoList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        Glide.with(context)
                .load(photoList.get(position).getUrl())
                .into(imageView);
        container.addView(imageView);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView)object);
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return (arg0 == arg1);
    }

}