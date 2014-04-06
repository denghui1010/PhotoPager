package com.ldh.photopager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class PhotoPager extends ViewGroup {

  public interface OnPageChangeListener {
    void OnPageChange(int position);
  }

  private Scroller mScroller;
  private final Context mContext;
  private float lastX;
  private final int FLING_TO_LEFT = 1;
  private final int FLING_TO_RIGHT = -1;
  private final int FLING_RETURN = 0;
  private int FLING_DURATION = 500;
  private int DEF_DISTANCE = 100;
  private int currentPage;
  private boolean isLoop = true;
  private OnPageChangeListener mOnPageChangeListener;

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
        if (!mScroller.isFinished()) {
          mScroller.abortAnimation();
        }
        break;
      case MotionEvent.ACTION_MOVE:
        float x = event.getRawX();
        int dx = (int) (x - lastX);
        if (!isLoop && currentPage == 0 && getScrollX() - dx < 0) {
          dx = 0;
        }
        if (!isLoop && currentPage == this.getChildCount() - 1
            && getScrollX() - dx > getWidth() * currentPage) {
          dx = 0;
        }
        scrollBy(-dx, 0);
        lastX = x;
        break;
      case MotionEvent.ACTION_UP:
        if (currentPage * getWidth() - Math.abs(getScrollX()) > DEF_DISTANCE) {
          fling(FLING_TO_RIGHT);
        } else if (currentPage * getWidth() - Math.abs(getScrollX()) < -DEF_DISTANCE) {
          fling(FLING_TO_LEFT);
        } else {
          fling(FLING_RETURN);
        }
        break;
    }
    return true;
  }

  /**
   * 设置滑动多长距离后松手可以开始fling
   * 
   * @param distance 阀值长度
   */
  public void setDefDistance(int distance) {
    DEF_DISTANCE = distance;
  }

  /**
   * 设置fling状态的时间
   * 
   * @param mills fling时间,单位毫秒
   */
  public void setFlingDuration(int mills) {
    FLING_DURATION = mills;
  }

  /**
   * 设置是否开启循环
   * 
   * @param bool true开启,false关闭,默认开启
   */
  public void setLoop(boolean bool) {
    isLoop = bool;
  }

  /**
   * 设置view切换监听,给监听会告诉你当前位置position
   * 
   * @param onViewChangeListener view切换监听
   */
  public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
    mOnPageChangeListener = onPageChangeListener;
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
  }

  private void fling(int type) {
    int dx = 0;
    if (type == FLING_TO_LEFT) {
      currentPage += 1;
      dx = currentPage * getWidth() - Math.abs(getScrollX());
    }
    if (type == FLING_RETURN) {
      dx = (currentPage) * getWidth() - Math.abs(getScrollX());
    }
    if (type == FLING_TO_RIGHT) {
      currentPage -= 1;
      dx = type * (Math.abs(getScrollX()) - currentPage * getWidth());
    }
    mOnPageChangeListener.OnPageChange(currentPage);
    mScroller.startScroll(getScrollX(), 0, dx, 0, FLING_DURATION);
    invalidate();
  }

  private void init() {
    mScroller = new Scroller(mContext);
  }

}
