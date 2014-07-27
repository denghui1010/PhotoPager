package com.ldh.photopager;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by Administrator on 2014/7/27.
 */
public class ScrollLayout extends ViewGroup {
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int mLastMotionX;
    private final int SNAP_VELOCITY = 200;
    private int mCurScreen;

    public ScrollLayout(Context context) {
        super(context);
        init();
    }

    public ScrollLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScrollLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            int childLeft = 0;
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childView = getChildAt(i);
                if (childView.getVisibility() != View.GONE) {
                    final int childWidth = childView.getMeasuredWidth();
                    childView.layout(childLeft, 0, childLeft + childWidth, childView.getMeasuredHeight());
                    childLeft += childWidth;
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void init() {
        mScroller = new Scroller(getContext());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        final int action = event.getAction();
        final int x = (int) event.getX();
        final int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastMotionX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = mLastMotionX - x;
                mLastMotionX = x;
                scrollBy(dx, 0);
                break;
            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000);
                int velocityX = (int) mVelocityTracker.getXVelocity();
                if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {
                    snapToScreen(mCurScreen - 1);
                } else if (velocityX < -SNAP_VELOCITY && mCurScreen < getChildCount() - 1) {
                    snapToScreen(mCurScreen + 1);
                } else {
                    snapToDestination();
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }
        return true;
    }

    private void snapToDestination() {
        int screenWidth = getWidth();
        int destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
        snapToScreen(destScreen);
    }

    private void snapToScreen(int whichScreen) {
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        if (getScrollX() != whichScreen * getWidth()) {
            int dx = whichScreen * getWidth() - getScrollX();
            mScroller.startScroll(getScrollX(), 0, dx, 0, Math.abs(dx));
            mCurScreen = whichScreen;
            invalidate();
        }

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
//        super.dispatchDraw(canvas);
        int saveCount = canvas.save();
        int count = getChildCount();
        int width = getWidth();
        int scrollX = getScrollX();
        for (int i = 0; i < count; i++) {
            canvas.save();
            View view = getChildAt(i);
            int viewLeft = view.getLeft();
            int dx = Math.abs(scrollX - viewLeft);
            if (dx <= width) {
                canvas.translate(0, -(1.0f * dx / width) * getHeight());
                drawChild(canvas, view, getDrawingTime());
                canvas.restore();
            } else {
                canvas.translate(0.0f, -1.0f * getHeight());
                drawChild(canvas, view, getDrawingTime());
                canvas.restore();
            }
        }
        canvas.restoreToCount(saveCount);
    }
}
