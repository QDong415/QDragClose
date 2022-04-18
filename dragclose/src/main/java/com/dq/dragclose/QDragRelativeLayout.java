package com.dq.dragclose;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

//我这里只提供RelativeLayout，至于如果你们需要ConstraintLayout，你们自己新建一个QDragConstraintLayout然后代码复制过去就行了
//不能用linearLayout，因为要想实现Activity过场动画 就需要有transitionName的存在。有它在，再用linearLayout就会有点麻烦
public class QDragRelativeLayout extends RelativeLayout {

    //关闭动画耗时，默认400
    private long closeAnimationDuration = 400;
    //下拉力度不够，反弹回正常状态动画耗时，默认200
    private long rollToNormalAnimationDuration = 200;
    //手指快速下滑也可以触发关闭，默认true
    private boolean flingCloseEnable = true;
    //是否可以手势下拉，默认true
    private boolean dragEnable = true;
    //手势下拉过程中，其他View根据滑动距离半透明，默认false
    private boolean alphaWhenDragging = false;

    //最小滑动距离
    private final int touchSlop;
    //下滑距离大于这个就要关闭
    private final float MAX_CLOSE_Y;

    private Context mContext;

    //作者DQ：我开始想用VelocityTracker判断手指快速滑动，发现这玩意根本不准，最后的结果差异很大，且正负不一，有懂的朋友可以教教我
//    private VelocityTracker velocityTracker;
    private long touchDownTime; //按下的时间戳

    private float downX;
    private float downY;

    //黑色背景，在本类onAttachedToWindow方法里new并addView上去的。跟随手指改变透明度
    private View shadowView;

    //为了采用android系统的跳转而自定义的shareView
    private View shareTransitionView;

    //从list列表界面Activity通过Intent传过来的
    private int fromX, fromY, fromWidth, fromHeight;

    private DragCloseListener onDragCloseListener;

    public QDragRelativeLayout(Context context) {
        this(context, null);
    }

    public QDragRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QDragRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getApplicationContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.QDragClose, defStyleAttr, 0);
        flingCloseEnable = a.getBoolean(R.styleable.QDragClose_flingCloseEnable, true);
        closeAnimationDuration = a.getInteger(R.styleable.QDragClose_closeAnimationDuration, 400);
        rollToNormalAnimationDuration = a.getInteger(R.styleable.QDragClose_rollToNormalAnimationDuration, 200);
        dragEnable = a.getBoolean(R.styleable.QDragClose_dragEnable, true);
        alphaWhenDragging = a.getBoolean(R.styleable.QDragClose_alphaWhenDragging, false);
        //下滑距离大于这个就要关闭
        MAX_CLOSE_Y = screenHeight * a.getFloat(R.styleable.QDragClose_flingCloseEnable, 0.2f);

        a.recycle();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getRawX();
                    downY = event.getRawY();

                    //为啥不能在onTouch的Down里初始化velocityTracker？因为一般不走onTouch的down
                    if (flingCloseEnable) {
                        touchDownTime = System.currentTimeMillis();
                    }

                    break;
                case MotionEvent.ACTION_MOVE:
                    //拦截事件：如果斜着滑（diffX > diffY）就不拦截了
                    float diffY = event.getRawY() - downY;

                    //如果dragEnable为false 或 横滑距离大于竖滑的一半（源自viewpager源码），那么就由Rv吃掉触摸事件。
                    if (dragEnable && diffY > touchSlop && diffY > Math.abs(event.getRawX() - downX) * 2f) {
                        //说明当前手指Y 在 downY的下面（即：向下拉）
                        if (onDragCloseListener != null) {
                            return !onDragCloseListener.contentViewNeedTouchEvent();
                        }
                        //Activity居然没监听，那么就当做activity不是列表布局，本View要处理下拉事件
                        //一旦触发了return true，后续本vg就不会再触发onInterceptTouchEvent了。是因为vg的dispatchTouchEvent方法里的mFirstTouchTarget == null
                        return true;
                    } else {
                        //当前手指Y 在 downY的上面
                        //一定进入这里，事件就被rv吃掉了，并且rv require了，就由rv主宰了。本vg就也不会再触发onInterceptTouchEvent了。（但继续走vg的dispatchTouchEvent方法且mFirstTouchTarget != null）
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    downY = 0;
                    break;
            }
        }
        return false;
    }

    public void setupFromImageView(int fromX ,int fromY, int fromWidth, int fromHeight ,View shareTransitionView){
        this.fromX = fromX;
        this.fromY = fromY;
        this.fromWidth = fromWidth;
        this.fromHeight = fromHeight;
        this.shareTransitionView = shareTransitionView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getRawX();
                downY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:

                float diffX = event.getRawX() - downX;
                float diffY = event.getRawY() - downY;
                float absDiffY = Math.abs(diffY);
                float scale = 1 - absDiffY / getHeight() * .75f;
                float scaleOffsetY = (1 - scale) * (1 - scale) * getHeight() * .5f;
                if (diffY >= 0) {

                    //根据触摸点的Y坐标和屏幕的比例来更改透明度
                    final float alphaStart = 0.6f;
                    shadowView.setAlpha(1 - alphaStart - (1 - alphaStart) * (diffY / getHeight()));

                    setTranslationX(diffX); //默认TranslationX是0，
                    setTranslationY(diffY - scaleOffsetY);//- scaleOffsetY是为了Y值跟手

                    //这里很难，因为scale只能是以中心点进行缩小，这样以来就会影响实际的Y值
                    setScaleX(scale);
                    setScaleY(scale);

                    if (alphaWhenDragging){
                        //手势下拉过程中，其他View根据滑动距离半透明
                        final float otherViewAlpha = diffY >= MAX_CLOSE_Y ? 0 : ( 1 - diffY / MAX_CLOSE_Y);

                        //隐藏除了shareImageView之外的所有View（主要是隐藏recycleView）
                        for (int i = 0; i < getChildCount(); i++) {
                            if (getChildAt(i) != shareTransitionView) {
                                //隐藏别的view
                                getChildAt(i).setAlpha(otherViewAlpha);
                            }
                        }
                    }

                } else {
//                    shadowView.setAlpha(1f);算了不处理了
                    setTranslationX(diffX);
                    setTranslationY(diffY);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                //查看手指滑动速度
                float speed = 0;
                if (touchDownTime != 0) {
                    long costTime = System.currentTimeMillis() - touchDownTime;
                    speed = (event.getRawY() - downY) / (float)costTime;
                }

                //我就暂且用速度>1 就算作手指快速滑动
                if (event.getRawY() - downY > MAX_CLOSE_Y || speed > 1) {
                    //执行关闭动画
                    performCloseAnimation();

                    //作者DQ提示：这里如果你直接finish，没有系统的transition动画效果
//                   onDragCloseListener.onCloseAnimationEnd();
                } else {
                    //不满足关闭条件，执行回滚动画
                    performRollToNormalAnimation();
                }

                downX = 0;
                downY = 0;
                break;
        }

        return super.onTouchEvent(event);
    }

    //满足了关闭条件，执行关闭动画
    private void performCloseAnimation() {

        //下拉松开手指的时候：隐藏recycleView，计算并重设shareImageView的rect，然后返回rect
        float[] willCloseRect = restShareTransitionViewWillCloseRect();

        //下拉松开手指的时候：为了做收回动画，计算最终的feed列表上的shareImageView的rect
        float[] finalCloseRect = shareTransitionViewFinalCloseRect();

        float willCloseShadowViewAlpha = shadowView.getAlpha();

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1).setDuration(closeAnimationDuration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float)animation.getAnimatedValue();
                shareTransitionView.setTranslationX(willCloseRect[0] + (finalCloseRect[0] - willCloseRect[0]) * animatedValue);
                shareTransitionView.setTranslationY(willCloseRect[1] + (finalCloseRect[1] - willCloseRect[1]) * animatedValue);
                shareTransitionView.setScaleX(willCloseRect[2] + (finalCloseRect[2] - willCloseRect[2]) * animatedValue);
                shareTransitionView.setScaleY(willCloseRect[3] + (finalCloseRect[3] - willCloseRect[3]) * animatedValue);

                shadowView.setAlpha(willCloseShadowViewAlpha * (1 - animatedValue));
            }
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onDragCloseListener != null) onDragCloseListener.onCloseAnimationEnd();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                if (onDragCloseListener != null) onDragCloseListener.onCloseAnimationStart();
            }
        });
        //执行关闭动画
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(valueAnimator);
        animatorSet.start();
    }

    //下拉松开手指的时候：隐藏recycleView，计算并重设shareImageView的rect，然后返回rect
    private float[] restShareTransitionViewWillCloseRect(){

        //第一步：隐藏除了shareImageView之外的所有View（主要是隐藏recycleView）
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) != shareTransitionView) {
                //隐藏别的view
                getChildAt(i).setVisibility(GONE);
            }
        }

        //第二步：先取出当前手势缩放的x、y和缩放比例
        //Tips: 这时候用 shareTransitionView.getLocationOnScreen 获取不到正确的坐标，只能手动硬转
        final float willCloseScaleX = getScaleX();
        final float willCloseScaleY = getScaleY();
        final float willCloseX = getTranslationX();
        final float willCloseLargeViewTranslationY = getTranslationY();

        //第三步，将全部View复原（瞬时操作）
        setScaleX(1);
        setScaleY(1);
        setTranslationX(0);
        setTranslationY(0);

        //第四步，计算并重设shareImageView的x、y和缩放比例。这里的计算挺复杂，目的是为了让松开手指的瞬间shareImageView保持在原位

        //currentTranslationY 是当前大View的Y值表面移动
        //getHeight() * (1 - currentScaleY) * 0.5f 是因为大View的scaleY缩放导致的Y值偏移
        //这两者加起来就是实际距离屏幕顶部的距离
        final float willCloseYStep1 = willCloseLargeViewTranslationY + getHeight() * (1 - willCloseScaleY) * 0.5f;

        //第2步要减去 因为shareImageView的scaleY缩放而导致的Y值偏差
        final float willCloseY = willCloseYStep1 - shareTransitionView.getHeight() * (1 - willCloseScaleY) * 0.5f;

        shareTransitionView.setScaleX(willCloseScaleX);
        shareTransitionView.setScaleY(willCloseScaleY);
        shareTransitionView.setTranslationX(willCloseX);
        shareTransitionView.setTranslationY(willCloseY);

        //装一起返回
        float[] willCloseRect = new float[4];
        willCloseRect[0] = willCloseX;
        willCloseRect[1] = willCloseY;
        willCloseRect[2] = willCloseScaleX;
        willCloseRect[3] = willCloseScaleY;
        return willCloseRect;
    }


    //下拉松开手指的时候：为了做收回动画，计算最终的feed列表上的shareImageView的rect
    private float[] shareTransitionViewFinalCloseRect(){
        //最终shareImageView的缩放系数（0 - 1）
        final float resultScaleX = fromWidth / (float)shareTransitionView.getWidth();
        final float resultScaleY = fromHeight / (float)shareTransitionView.getHeight();

        //因为scaleY缩放导致的Y值偏移
        float resultDiffXByScale = (shareTransitionView.getWidth() * (1 - resultScaleX)) * 0.5f;
        float resultDiffYByScale = (shareTransitionView.getHeight() * (1 - resultScaleY)) * 0.5f;

        final float resultTranslationX = fromX - resultDiffXByScale;
        final float resultTranslationY = fromY - resultDiffYByScale;

        //装一起返回
        float[] finalCloseRect = new float[4];
        finalCloseRect[0] = resultTranslationX;
        finalCloseRect[1] = resultTranslationY;
        finalCloseRect[2] = resultScaleX;
        finalCloseRect[3] = resultScaleY;
        return finalCloseRect;
    }

    //不满足关闭条件，执行回滚动画
    private void performRollToNormalAnimation() {

        List<Animator> animatorItems = new ArrayList<Animator>();

        if (alphaWhenDragging) {
            //如果设置了手势下拉过程中，其他View根据滑动距离半透明。那么这里要把透明度反弹回来
            float willNormalAlpha = 1f;

            for (int i = 0; i < getChildCount(); i++) {
                if (getChildAt(i) != shareTransitionView && getChildAt(i).getAlpha() < 1) {
                    willNormalAlpha = getChildAt(i).getAlpha();
                    break;
                }
            }

            ValueAnimator alphaAnimator = ValueAnimator.ofFloat(willNormalAlpha, 1).setDuration(rollToNormalAnimationDuration);
            alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    //要把透明度反弹回来
                    for (int i = 0; i < getChildCount(); i++) {
                        if (getChildAt(i) != shareTransitionView) {
                            getChildAt(i).setAlpha((float) animation.getAnimatedValue());
                        }
                    }
                }
            });

            animatorItems.add(alphaAnimator);
        }

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", getScaleX(), 1.0f).setDuration(rollToNormalAnimationDuration);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", getScaleX(), 1.0f).setDuration(rollToNormalAnimationDuration);
        ObjectAnimator transX = ObjectAnimator.ofFloat(this, "translationX", getTranslationX(), 0).setDuration(rollToNormalAnimationDuration);
        ObjectAnimator transY = ObjectAnimator.ofFloat(this, "translationY", getTranslationY(), 0).setDuration(rollToNormalAnimationDuration);

        scaleX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onDragCloseListener != null) onDragCloseListener.onRollBackToNormalAnimationEnd();
            }
        });

        animatorItems.add(scaleX);
        animatorItems.add(scaleY);
        animatorItems.add(transX);
        animatorItems.add(transY);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animatorItems);

        animatorSet.start();
    }

    //Ac.onCreate.马上SetContentView -> 本类4个构造方法() -> 本类.onFinishInflate() -> Ac.onCreate.结束SetContentView方法
    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
    }

    //-> Ac.onCreate.结束 -> 本类onAttachedToWindow()
    @Override
    public void onAttachedToWindow(){
        super.onAttachedToWindow();
        View view = new View(mContext);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);//宽度、高度
        view.setLayoutParams(params);
        view.setBackgroundColor(Color.parseColor("#000000"));

        //如果是在RootActivity的ViewPager下，那么getParent()是ViewPager； getRootView()是com.android.internal.policy.DecorView
        //如果是单纯的Activity下，getParent()是FrameLayout； getRootView()还是com.android.internal.policy.DecorView

        if (getRootView() != null){
            //添加到DetorView
            FrameLayout frameLayout = (FrameLayout) getRootView();
            frameLayout.addView(view , 0);
            shadowView = view;
        }
    }

    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        //添加到DecorView上的View建议要remove掉。但是不remove貌似也没啥大问题
        //DQ一开始以为不remove会内存泄漏，后来才发现内存泄漏是android系统的bug
//        if (shadowView != null){
//            FrameLayout frameLayout = (FrameLayout) shadowView.getParent();
//            frameLayout.removeView(shadowView);
//            shadowView = null;
//        }
    }

    public void setOnDragCloseListener(DragCloseListener onDragCloseListener) {
        this.onDragCloseListener = onDragCloseListener;
    }

    public boolean isDragEnable() {
        return dragEnable;
    }

    public void setDragEnable(boolean dragEnable) {
        this.dragEnable = dragEnable;
    }
}
