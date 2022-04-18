package com.dq.dragclosedemo.waterfall;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.dq.dragclosedemo.R;
import com.dq.dragclosedemo.detail.FeedDetailActivity;
import com.dq.dragclosedemo.userdetail.FeedDetailRootActivity;

import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

//瀑布流，我这里只是demo，你可以随意修改
public class GoodsActivity extends AppCompatActivity {

	private RecyclerView recyclerView;
	private List<GoodsBean> list;
	private WaterfallAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recycleview);
		initData();
		initView();
	}

	protected void initView() {

		recyclerView = findViewById(R.id.recycler_view);
		recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
		adapter = new WaterfallAdapter(this, list);
		recyclerView.setAdapter(adapter);

		adapter.setOnItemClickListener(new WaterfallAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(int position , View v) {

				ImageView coverImageView = v.findViewById(R.id.cover);
				int[] location = new int[2];
				recyclerView.getLocationOnScreen(location);
				int recyclerViewY = location[1];

				v.getLocationOnScreen(location);
				int x = location[0];
				int y = location[1] - recyclerViewY; //为了获取封面ImageView的准确的Y，要减去状态栏和titleView高度
				int width = coverImageView.getWidth();
				int height = coverImageView.getHeight();

				Intent intent = new Intent(GoodsActivity.this, position % 2 == 0 ? FeedDetailActivity.class : FeedDetailRootActivity.class);

				intent.putExtra("GoodsBean", list.get(position));
				intent.putExtra("fromX", x);
				intent.putExtra("fromY", y);
				intent.putExtra("fromWidth", width);
				intent.putExtra("fromHeight", height);

				if (Build.VERSION.SDK_INT >= LOLLIPOP) {

					//用系统的Activity过场动画进行跳转
					Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(GoodsActivity.this
							, v.findViewById(R.id.cover), "sharedView").toBundle();
					startActivity(intent, bundle);

					//系统的Activity过场动画会让shareElementView.setAlpha(0)；然后再回退动画后再进行.setAlpha(1)
					//会导致一个问题：我们下拉返回的时候，由于弹回动画是我们自己做的。但是系统依然会再进行一遍.setAlpha(1)，导致回弹动画结束时候图片会闪一下
					//为了解决"闪一下"的问题，我用这种方法把他提前设为.setAlpha(1)
					v.postDelayed(new Runnable() {
						@Override
						public void run() {
							v.findViewById(R.id.cover).setAlpha(1);
						}
					}, 400);//activity过场动画的默认时长是300

				} else {
					startActivity(intent);
				}
			}
		});

		SpaceDecoration itemDecoration = new SpaceDecoration(12);
		itemDecoration.setPaddingEdgeSide(true);
		itemDecoration.setPaddingStart(true);
		itemDecoration.setPaddingHeaderFooter(true);
		recyclerView.addItemDecoration(itemDecoration);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	//假数据
	protected void initData() {
		list = new ArrayList<>();

		for (int i = 0; i < 5; i++) {
			GoodsBean bean = new GoodsBean();
			bean.setTitle("商品"+i);

			List<PhotoBean> pictures = new ArrayList<>();
			PhotoBean photoBean = new PhotoBean();
			photoBean.setUrl("https://p0.pipi.cn/friday/d1a34fd459495db95f1e78e58e3bf478.jpg?imageMogr2/thumbnail/2500x2500%3E");
			photoBean.setHeight(900);
			photoBean.setWidth(1600);
			pictures.add(photoBean);
			photoBean = new PhotoBean();
			photoBean.setUrl("https://p0.pipi.cn/friday/c8af4bea072f70418d7fffbeaa8c60f2.jpg?imageMogr2/thumbnail/2500x2500%3E");
			photoBean.setHeight(900);
			photoBean.setWidth(1600);
			pictures.add(photoBean);
			photoBean = new PhotoBean();
			photoBean.setUrl("https://p0.pipi.cn/friday/71b93ea1e708e1ea065e1c4fc57de743.jpg?imageMogr2/thumbnail/2500x2500%3E");
			photoBean.setHeight(900);
			photoBean.setWidth(1600);
			pictures.add(photoBean);
			bean.setPictures(pictures);
			list.add(bean);

			//第2条
			bean = new GoodsBean();
			bean.setTitle("商品"+i);

			pictures = new ArrayList<>();
			photoBean = new PhotoBean();
			photoBean.setUrl("https://wx4.sinaimg.cn/mw2000/6ade4348gy1h0tc49gfyaj21ww2pgk8p.jpg");
			photoBean.setHeight(2829);
			photoBean.setWidth(2000);
			pictures.add(photoBean);
			photoBean = new PhotoBean();
			photoBean.setUrl("https://wx1.sinaimg.cn/mw2000/6ce227dbly1h0t24gojbjj20u013yk9f.jpg");
			photoBean.setHeight(1438);
			photoBean.setWidth(180);
			pictures.add(photoBean);
			bean.setPictures(pictures);
			list.add(bean);

			//第3条
			bean = new GoodsBean();
			bean.setTitle("商品"+i);

			pictures = new ArrayList<>();
			photoBean = new PhotoBean();
			photoBean.setUrl("https://wx2.sinaimg.cn/mw1024/521c36c8ly1h0tcy9c9gsj22c0340b2b.jpg");
			photoBean.setHeight(1333);
			photoBean.setWidth(1000);
			pictures.add(photoBean);
			photoBean = new PhotoBean();
			photoBean.setUrl("https://wx4.sinaimg.cn/mw1024/521c36c8ly1h0tczw2cqcj22c0340npe.jpg");
			photoBean.setHeight(1333);
			photoBean.setWidth(1000);
			pictures.add(photoBean);
			bean.setPictures(pictures);
			list.add(bean);

			//第4条
			bean = new GoodsBean();
			bean.setTitle("商品"+i);

			pictures = new ArrayList<>();
			photoBean = new PhotoBean();
			photoBean.setUrl("https://wx4.sinaimg.cn/mw1024/521c36c8ly1h0tcyrydysj22c03404qr.jpg");
			photoBean.setHeight(1333);
			photoBean.setWidth(1000);
			pictures.add(photoBean);
			photoBean = new PhotoBean();
			photoBean.setUrl("https://wx2.sinaimg.cn/mw1024/521c36c8ly1h0td1wwj2pj22c0340u0y.jpg");
			photoBean.setHeight(1333);
			photoBean.setWidth(1000);
			pictures.add(photoBean);
			bean.setPictures(pictures);
			list.add(bean);
		}

	}
}
