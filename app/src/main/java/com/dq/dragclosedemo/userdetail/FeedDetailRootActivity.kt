package com.dq.dragclosedemo.userdetail

import android.app.Activity
import android.app.ActivityOptions
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Transition
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.dq.dragclosedemo.R
import com.dq.dragclosedemo.waterfall.GoodsBean

//可以侧滑到个人主页Demo，我这里用kotlin写的，你要是用java的话自己转
class FeedDetailRootActivity : AppCompatActivity() {

    private lateinit var todayFragment: FeedDetailFragment
    private lateinit var yesterdayFragment: UserProfileFragment

    private var mainHandler = Handler(Looper.getMainLooper())

    //本Demo是用系统的transitionName做跳转Activity动画
    private var transitionShareOuterView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_detail_viewpager)
        initView(savedInstanceState)

        if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            //监听activity过场动画
            //Ac.onWindowFocusChanged -> onTransitionStart -> 0.35秒后 -> onTransitionEnd
            window.sharedElementEnterTransition.addListener(object :
                Transition.TransitionListener {
                override fun onTransitionStart(transition: Transition?) {
                }

                override fun onTransitionEnd(transition: Transition?) {
                    //移除RootActivity的共享View。DQ目前尚不确定6.0手机是不是也有这问题，手上没6.0手机
                    if (Build.VERSION.SDK_INT <= VERSION_CODES.LOLLIPOP_MR1) {
                        mainHandler.post {

                            //Q:为什么要用代码设置透明
                            //A:因为5.1系统，如果你即使在设置xml里的透明theme，Activity背景还是黑的，只能用反射去修改Activity的backgroundColor
                            translucentActivity(this@FeedDetailRootActivity )

                            //Q:为什么要放到runable里？
                            //A:因为5.1系统在这里直接removeView会崩溃，原因是ViewTree。让在runable里可以让别的流程彻底走完再remove
                            removeTransitionShareOuterView()
                        }
                    } else {
                        removeTransitionShareOuterView()
                    }
                }

                override fun onTransitionCancel(transition: Transition?) {
                    //移除RootActivity的共享View
                    mainHandler.postDelayed(Runnable { removeTransitionShareOuterView() }, 0)
                }

                override fun onTransitionPause(transition: Transition?) {
                }

                override fun onTransitionResume(transition: Transition?) {
                }
            })
        }
    }

    private fun initView(savedInstanceState: Bundle?) {

        if (savedInstanceState != null && supportFragmentManager.fragments.size > 2) {
            //mainActivity内存重启。todayFragment==null， todayFragment。而并不是new
            //但是如果new，有的tagertsdk会出现Fragment重影，有的会出现无法给fragment传参
            todayFragment = supportFragmentManager.fragments[0] as FeedDetailFragment
            yesterdayFragment = supportFragmentManager.fragments[1] as UserProfileFragment
        } else {
            todayFragment = FeedDetailFragment()
            yesterdayFragment = UserProfileFragment()
        }

        //从前一个界面传过来的东西
        val goodsBean = intent.getSerializableExtra("GoodsBean") as GoodsBean
        val dm = resources.displayMetrics
        //按比例计算View的高度
        val imageViewHeight = goodsBean.getPhotoOriginalHeight() * dm.widthPixels / goodsBean.getPhotoOriginalWidth().toFloat()

        //第一步：设置shareImageView图片
        transitionShareOuterView = findViewById<ImageView>(R.id.transition_share_outer_view)
        transitionShareOuterView?.let {
            Glide.with(this)
                .load(goodsBean.getPictures().get(0).getUrl())
                .into(transitionShareOuterView!!)
            val params = it.layoutParams as RelativeLayout.LayoutParams
            //重新设置share元素的高度
            params.height = imageViewHeight.toInt()
        }

        val myAdapter = Mydapter(supportFragmentManager)
        val viewpager = findViewById<ViewPager>(R.id.viewpager)
        viewpager.setAdapter(myAdapter)
    }

    //Q:移除RootActivity的共享View，为什么要隐藏？
    //A:因为下拉的时候，被缩放的是Fragment的contentView，即QDragRelativeLayout。所以viewPager的背景必须是透明
    //虽然viewPager的背景是透明了，但是会看到共享View。所以本RootActivity共享View在跳转完就可以丢弃了
    //用系统back关闭RootActivity的时候，QDragRelativeLayout里的共享View一样能起到动画效果
    private fun removeTransitionShareOuterView(){
        if (transitionShareOuterView != null){
            val vg = transitionShareOuterView!!.getParent() as ViewGroup;
            vg.removeView(transitionShareOuterView!!)
            Log.e("dz","移除TransitionShareOuterView")
            transitionShareOuterView = null
        }
    }


    //把Activity设置为透明。因为5.1系统，如果你即使在设置xml里的透明theme。Activity背景还是黑的
    private fun translucentActivity(activity: Activity) {
        try {
            activity.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            activity.window.decorView.background = null
            val activityOptions = Activity::class.java.getDeclaredMethod("getActivityOptions")
            activityOptions.isAccessible = true
            val options = activityOptions.invoke(activity)
            val classes = Activity::class.java.declaredClasses
            var aClass: Class<*>? = null
            for (clazz in classes) {
                if (clazz.simpleName.contains("TranslucentConversionListener")) {
                    aClass = clazz
                }
            }
            val method = Activity::class.java.getDeclaredMethod(
                "convertToTranslucent",
                aClass, ActivityOptions::class.java
            )
            method.isAccessible = true
            method.invoke(activity, null, options)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    // 会高频触发，切换fragment甚至都会触发
    // Fg.onActivityCreated -> Fg.onResume -> Root.onWindowFocusChanged
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        //如果你在这里removeView(transitionShareOuterView)，会导致无跳转动画
        Log.e("dz","Root onWindowFocusChanged")
    }

    //kotlin 内部类默认是static ,前面加上inner为非静态
    private inner class Mydapter(fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> todayFragment
                else -> yesterdayFragment
            }
        }

        override fun getCount(): Int {
            return 2
        }
    }

}
