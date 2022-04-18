package com.dq.dragclosedemo.detail;

import android.app.Activity;
import android.app.ActivityOptions;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.dq.dragclose.DragCloseListener;
import com.dq.dragclose.QDragRelativeLayout;
import com.dq.dragclosedemo.R;
import com.dq.dragclosedemo.waterfall.GoodsBean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

//这里展示的是：不可以向左滑到个人主页。如果你需要可以左滑到个人主页。去看FeedDetailRootActivity
public class FeedDetailActivity extends Activity implements DragCloseListener {

    private RecyclerView mRecyclerView;
    private DetailRecyclerAdapter mDetailRecyclerAdapter;
    private List<String> commentList;

    private GoodsBean goodsBean;

    private int fromX , fromY, fromWidth, fromHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_detail);

        //从前一个界面传过来的东西
        goodsBean = (GoodsBean)getIntent().getSerializableExtra("GoodsBean");
        //点击的是界面界面的某个feed的图片的Rect
        fromX = getIntent().getIntExtra("fromX", 0);
        fromY = getIntent().getIntExtra("fromY", 0);
        fromWidth = getIntent().getIntExtra("fromWidth", 0);
        fromHeight = getIntent().getIntExtra("fromHeight", 0);

        //构造假数据
        initData();

        DisplayMetrics dm = getResources().getDisplayMetrics();
        //按比例计算View的高度
        float imageViewHeight = goodsBean.getPhotoOriginalHeight()*dm.widthPixels/(float)goodsBean.getPhotoOriginalWidth();

        //第一步：设置shareImageView图片
        ImageView transition_share_view = findViewById(R.id.transition_share_view);
        Glide.with(FeedDetailActivity.this)
                .load(goodsBean.getPictures().get(0).getUrl())
                .into(transition_share_view);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)transition_share_view.getLayoutParams();
        //重新设置share元素的高度
        params.height = (int)imageViewHeight;

        //第二步：设置RecyclerView的adapter和header
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mDetailRecyclerAdapter = new DetailRecyclerAdapter(commentList);
        mRecyclerView.setAdapter(mDetailRecyclerAdapter);

        // 设置recyclerView的Header（即viewpager）的高度
        View header = LayoutInflater.from(this).inflate(R.layout.header_viewpager, null);
        ViewPager vp = header.findViewById(R.id.viewpager);
        ViewGroup.LayoutParams layoutParams = vp.getLayoutParams();
        //重设高度+成为rv的item = 会导致内存泄漏（leakcanary）原因不详
        layoutParams.height = (int)imageViewHeight;

        BannerViewPagerAdapter viewPagerAdapter = new BannerViewPagerAdapter(this, goodsBean.getPictures());
        vp.setAdapter(viewPagerAdapter);
        mDetailRecyclerAdapter.setHeaderView(header);

        //第三步：这是最重要的
        QDragRelativeLayout contentLayout = findViewById(R.id.drag_layout);
        contentLayout.setOnDragCloseListener(this);
        //传入列表的点击项目的ImageView的坐标
        contentLayout.setupFromImageView(fromX, fromY, fromWidth, fromHeight, transition_share_view);

        //移除RootActivity的共享View。DQ目前尚不确定6.0手机是不是也有这问题，我手上没6.0手机
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            translucentActivity(this);
        }
    }

    //Q:为什么要用代码设置透明，只用xml里设置透明theme不行吗？
    //A:因为5.1系统，如果你即使在设置xml里的透明theme，Activity背景还是黑的。只能用反射去修改Activity的backgroundColor
    private void translucentActivity(Activity activity) {
        try {
            activity.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            activity.getWindow().getDecorView().setBackground(null);
            Method activityOptions = Activity.class.getDeclaredMethod("getActivityOptions");
            activityOptions.setAccessible(true);
            Object options = activityOptions.invoke(activity);

            Class<?>[] classes = Activity.class.getDeclaredClasses();
            Class<?> aClass = null;
            for (Class clazz : classes) {
                if (clazz.getSimpleName().contains("TranslucentConversionListener")) {
                    aClass = clazz;
                }
            }
            Method method = Activity.class.getDeclaredMethod("convertToTranslucent",
                    aClass, ActivityOptions.class);
            method.setAccessible(true);
            method.invoke(activity, null, options);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    //初始化RecyclerView中每个item的数据
    private void initData() {
        commentList = new ArrayList<String>();
        for (int i = 0; i < 20; i++) {
            commentList.add("item" + i);
        }
    }

    @Override
    public boolean contentViewNeedTouchEvent() {
        //返回false表示不能往下滑动，即代表到顶部了；
        return mRecyclerView.canScrollVertically(-1);
    }

    @Override
    public void onCloseAnimationStart() {
    }

    @Override
    public void onCloseAnimationEnd() {
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onRollBackToNormalAnimationEnd() {

    }

}
