package com.dq.dragclose;

public interface DragCloseListener {

        //开始下拉，activity要告知我 当前是否可以出发下拉缩小
        public boolean contentViewNeedTouchEvent();

        //满足拖拽返回条件，开始执行关闭动画。
        public void onCloseAnimationStart();

        //满足拖拽返回条件，且关闭动画执行完毕。需要finishActivity()
        public void onCloseAnimationEnd();

        //不满足拖拽返回条件，执行rollBack动画完毕
        public void onRollBackToNormalAnimationEnd();
}