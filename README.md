# QDragClose

## 简介：

下拉拖拽关闭Activity。仿大众点评、快手、小红书详情界面可下滑关闭界面。
Drag down to close activity

## 安装体验：
![](https://upload-images.jianshu.io/upload_images/26002059-bbd971084ea123f9.jpeg)

## 功能（优点）：
- ✅Demo包含`瀑布列表跳转到详情，带动画`+`详情可左滑进入个人主页`+`下拉拖拽关闭Activity`
- ✅到为了让Activity的xml布局层级最少，只需要把本库设置为最外层的RelativeLayout
- ✅仿大众点评：下拉过程中除了图片，别的部分随着下拉距离而半透明
- ✅仿快手：fling快速下滑也可触发关闭
- ✅详情界面可左滑进入个人主页，你可以自己实现懒加载
- ✅完美解耦，可轻松让你的任何Activity实现下拉关闭效果

## 作者说明：
- 用leakCanary有时候会报内存泄漏，泄漏内容是FrameLayout，这是Android系统的bug，不是我的问题（也可能是leakCanary误报）
- 你可以自己新建一个项目试一下，复现步骤：<br />
1、在`ActivityA`点击按钮，通过系统过场动画（ActivityOptions.makeSceneTransitionAnimation）跳转到`ActivityB`<br />
2、`ActivityB`的xml中有一个你的自定义View（自己随便写个MyView extend View）<br />
3、关闭`ActivityB`<br />
4、重复 1、2、3步骤 重复三次。就会被leakCanary爆出内存泄漏，但是这个内存泄漏貌似不会复现。<br />

我试了很多办法，比如onDestory里remove DecorView，也没用。如果你知道这个问题具体情况，请联系我，谢谢

## 效果gif图（Gif图有点卡，实际运行一点都不卡）：
![](https://upload-images.jianshu.io/upload_images/26002059-96c272f540bddb21.gif)
![](https://upload-images.jianshu.io/upload_images/26002059-da019a1de650eca8.gif)

## 导入
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

	dependencies {
	        implementation 'com.github.QDong415:QDragClose:v1.0'
	}
```

## 使用

```java
        QDragRelativeLayout contentLayout = findViewById(R.id.drag_layout);
        contentLayout.setOnDragCloseListener(this);
        //传入列表的点击项目的ImageView的坐标
        contentLayout.setupFromImageView(fromX, fromY, fromWidth, fromHeight, transition_share_view);
```

```xml
    <declare-styleable name="QDragClose">
        <!-- 是否可以手势下拉，默认true -->
        <attr name="dragEnable" format="boolean" />
        <!-- 下拉距离占总height百分之多少就触发关闭，0 - 1之间，默认0.2 -->
        <attr name="closeYRatio" format="float" />
        <!-- 手指快速下滑也可以触发关闭，默认true -->
        <attr name="flingCloseEnable" format="boolean" />
        <!-- 手势下拉过程中，其他View根据滑动距离半透明，默认false -->
        <attr name="alphaWhenDragging" format="boolean" />
        <!-- 关闭动画耗时，默认450 -->
        <attr name="closeAnimationDuration" format="integer" />
        <!-- 下拉力度不够，反弹回正常状态动画耗时，默认200 -->
        <attr name="rollToNormalAnimationDuration" format="integer" />
    </declare-styleable>
```


## Author：DQ

有问题联系QQ：285275534, 285275534@qq.com