package com.dq.dragclosedemo.userdetail;

import android.os.Bundle;
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
import com.dq.dragclosedemo.detail.DetailRecyclerAdapter;
import com.dq.dragclosedemo.detail.BannerViewPagerAdapter;
import com.dq.dragclosedemo.waterfall.GoodsBean;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

public class FeedDetailFragment extends Fragment implements DragCloseListener {

    private RecyclerView mRecyclerView;
    private DetailRecyclerAdapter mDetailRecyclerAdapter;
    private List<String> commentList;

    private GoodsBean goodsBean;

    private int fromX , fromY, fromWidth, fromHeight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_feed_detail,container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView(savedInstanceState);
    }

    protected void initView(Bundle savedInstanceState) {

        //从前一个界面传过来的东西
        goodsBean = (GoodsBean)getActivity().getIntent().getSerializableExtra("GoodsBean");
        //点击的是界面界面的某个feed的图片的Rect
        fromX = getActivity().getIntent().getIntExtra("fromX", 0);
        fromY = getActivity().getIntent().getIntExtra("fromY", 0);
        fromWidth = getActivity().getIntent().getIntExtra("fromWidth", 0);
        fromHeight = getActivity().getIntent().getIntExtra("fromHeight", 0);

        //构造假数据
        initData();

        DisplayMetrics dm = getResources().getDisplayMetrics();
        //按比例计算View的高度
        float imageViewHeight = goodsBean.getPhotoOriginalHeight()*dm.widthPixels/(float)goodsBean.getPhotoOriginalWidth();

        //第一步：设置shareImageView图片（必须）
        ImageView transition_share_view = getView().findViewById(R.id.transition_share_view);
        Glide.with(FeedDetailFragment.this)
                .load(goodsBean.getPictures().get(0).getUrl())
                .into(transition_share_view);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)transition_share_view.getLayoutParams();
        //重新设置share元素的高度
        params.height = (int)imageViewHeight;

        //第二步：设置RecyclerView的adpater和header（改成你自己的）
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mDetailRecyclerAdapter = new DetailRecyclerAdapter(commentList);
        mRecyclerView.setAdapter(mDetailRecyclerAdapter);

        View header = LayoutInflater.from(getActivity()).inflate(R.layout.header_viewpager, null);
        ViewPager vp = header.findViewById(R.id.viewpager);
        ViewGroup.LayoutParams layoutParams = vp.getLayoutParams();
        //重设高度+成为rv的item = 会导致内存泄漏（leakcanary）原因不详
        layoutParams.height = (int)imageViewHeight;

        BannerViewPagerAdapter viewPagerAdapter = new BannerViewPagerAdapter(getActivity(), goodsBean.getPictures());
        vp.setAdapter(viewPagerAdapter);
        mDetailRecyclerAdapter.setHeaderView(header);

        //第三步：这是最重要的（必须）
        QDragRelativeLayout contentLayout = getView().findViewById(R.id.drag_layout);
        contentLayout.setOnDragCloseListener(this);
        contentLayout.setupFromImageView(fromX, fromY, fromWidth, fromHeight, transition_share_view);
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
        getActivity().finish();
        getActivity().overridePendingTransition(0, 0);
    }

    @Override
    public void onRollBackToNormalAnimationEnd() {

    }
}
