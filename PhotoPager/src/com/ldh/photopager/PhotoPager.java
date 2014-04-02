package com.ldh.photopager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class PhotoPager extends ViewGroup {

  public interface OnViewChangeListener {
    void OnViewChange(int position);
  }

  private VelocityTracker mVelocityTracker;
  private Scroller mScroller;
  private final Context mContext;
  private int mCurScreen;
  private float lastX;
  private final float DEF_XVELOCITY = 500f;
  private final int FLING_TO_LEFT = 1;
  private final int FLING_TO_RIGHT = -1;
  private final int FLING_RETURN = 0;
  private int FLING_DURATION = 400;
  private int currentPage;
  private OnViewChangeListener mOnViewChangeListener;

  public PhotoPager(Context context) {
    super(context);
    mContext = context;
    init();
  }

  public PhotoPager(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
    init();
  }

  public PhotoPager(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    mContext = context;
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
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        getParent().requestDisallowInterceptTouchEvent(true);
        lastX = event.getRawX();
        if (mVelocityTracker == null) {
          mVelocityTracker = VelocityTracker.obtain();
          mVelocityTracker.addMovement(event);
        }
        if (!mScroller.isFinished()) {
          mScroller.abortAnimation();
        }
        break;
      case MotionEvent.ACTION_MOVE:
        float x = event.getRawX();
        int dx = (int) (lastX - x);
        mVelocityTracker.addMovement(event);
        mVelocityTracker.computeCurrentVelocity(500);
        scrollBy(dx, 0);
        lastX = x;
        break;
      case MotionEvent.ACTION_UP:
        float xVelocity = mVelocityTracker.getXVelocity();
        if (xVelocity > DEF_XVELOCITY) {
          fling(FLING_TO_RIGHT);
        } else {

        }
        if (xVelocity < -DEF_XVELOCITY) {
          fling(FLING_TO_LEFT);
        } else {

        }
        mVelocityTracker.recycle();
        mVelocityTracker = null;
        break;
    }
    return true;
  }

  public void setFlingDuration(int mills) {
    FLING_DURATION = mills;
  }

  public void setOnViewChangeListener(OnViewChangeListener l) {
    mOnViewChangeListener = l;
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    if (changed) {
      int childLeft = 0;
      final int childCount = getChildCount();
      for (int i = 0; i < childCount; i++) {
        final View childView = getChildAt(i);
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
    scrollTo(mCurScreen * width, 0);
  }

  private void fling(int type) {
    int dx = 0;
    if (type == FLING_TO_LEFT) {
      currentPage += 1;
      dx = currentPage * getWidth() - Math.abs(getScrollX());
    }
    if (type == FLING_RETURN) {
      dx = getWidth() - getScrollX();
    }
    if (type == FLING_TO_RIGHT) {
      currentPage -= 1;
      dx = type * (Math.abs(getScrollX()) - currentPage * getWidth());
    }
    mOnViewChangeListener.OnViewChange(currentPage);
    mScroller.startScroll(getScrollX(), 0, dx, 0, FLING_DURATION);
    invalidate();
  }

  private void init() {
    mScroller = new Scroller(mContext);
  }
}
