# QDrawGift

## 简介：

仿快手直播间手绘礼物，手绘+播放+底部礼物弹框，Demo非常完整，非常贴合实际开发场景 。
A Gift Painter useful in Living Room. Support paint+play+bottomSheet

## 安装体验：
![](https://upload-images.jianshu.io/upload_images/26002059-83cf0b95754b2466.png)

## 功能（优点）：
- ✅Demo包含`手绘View`+`播放View`+`底部礼物弹框View`+转成json传给服务器+模拟服务器推送来json解析队列
- ✅为了兼容底部礼物弹框是基于Dialog或者popupView做的，本Demo的`手绘View`是基于windowManager层
- ✅`手绘View`可以单独撤销一笔
- ✅`播放View`使用LinkedList做礼物队列，可以随意插入到列头或者列尾
- ✅底部礼物弹框兼容手绘礼物和普通礼物，选择不同的礼物，手绘View层可以切换placeHolder
- ✅每个模块都相互解耦，都可以自由替换，也都可以拉出来当做独立的模块
- ✅采用SpareArray做Bitmap缓存，占用内存极低
- ✅无内存泄漏。代码清晰明确，注释量比代码都多

## 效果gif图（Gif图有点卡，实际运行一点都不卡）：
![](https://upload-images.jianshu.io/upload_images/26002059-48456ffa60a85222.gif)


## 导入
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

	dependencies {
	        implementation 'com.github.QDong415:QDrawGift:v1.1.1'
	}
```

## 使用

```java
    //底部的礼物弹框
    private BottomGiftSheetBuilder giftSheetBuilder;

    //画礼物的背景View（透明的，并不是灰底）
    private DrawGiftView drawGiftView;

    //播放礼物动画的层
    private DrawGiftPlayView playView;
```

```java
    //初始化手绘礼物View
    drawGiftView = new DrawGiftView(LiveActivity.this);
    //设置当前要画的礼物
    drawGiftView.setCurrentGift(giftid ,giftBitmap , giftPrice);
    //正式显示手绘礼物View，添加它到windowManager层
    drawGiftView.showInActivityWindow(LiveActivity.this, giftSheetBuilder.mDialog.getContentView().getHeight());
```

```java
    //初始化播放View
    playView = new DrawGiftPlayView(this);

    //添加播放View到decorView
    FrameLayout contentParent = (FrameLayout) getWindow().getDecorView().findViewById(android.R.id.content);
    contentParent.addView(playView);

    //开始播放礼物，insertToFirst = 是否插入到队列靠前位置
    playView.addDrawGifts(allDrawGiftArray, insertToFirst);
```


## Author

有问题联系QQ：285275534, 285275534@qq.com