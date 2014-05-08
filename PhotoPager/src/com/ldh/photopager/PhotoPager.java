package com.ldh.photopager;

import java.util.ArrayList;
import java.util.List;

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
  private List<View> list;
  private boolean allowLayout = true;
  private boolean addToRight;
  private boolean addToLeft;

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

  public void addViewToList(View child) {
    addView(child);
    list.add(child);
  }

  @Override
  public void computeScroll() {
    if (mScroller.computeScrollOffset()) {
      scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
      postInvalidate();
      if (mScroller.isFinished()) {
        mOnPageChangeListener.OnPageChange(currentPage);
        update();
      }
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
   * 设置view切换监听,该监听会告诉你当前位置position
   * 
   * @param onViewChangeListener view切换监听
   */
  public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
    mOnPageChangeListener = onPageChangeListener;
  }

  @Override
  protected void onAttachedToWindow() {
    mOnPageChangeListener.OnPageChange(currentPage);
    super.onAttachedToWindow();
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    if (allowLayout) {
      System.out.println("onLayout");
      int childCount = getChildCount();
      for (int i = 0; i < childCount; i++) {
        View childView = getChildAt(i);
        int childWidth = childView.getMeasuredWidth();
        getChildAt(i)
            .layout(childWidth * i, 0, childWidth * (i + 1), childView.getMeasuredHeight());
      }
      allowLayout = false;
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    final int count = getChildCount();
    for (int i = 0; i < count; i++) {
      getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
    }
  }

  private void fling(int type) {
    int[] location = new int[2];
    int index = getIndex(currentPage, type);
    list.get(index).getLocationInWindow(location);
    currentPage = index;
    mScroller.startScroll(getScrollX(), 0, location[0], 0, FLING_DURATION);
    invalidate();
  }

  private void fling2(int type) {
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
    mScroller.startScroll(getScrollX(), 0, dx, 0, FLING_DURATION);
    invalidate();
  }

  private int getIndex(int i, int type) {
    if (i + type > list.size() - 1) {
      return 0;
    }
    if (i + type < 0) {
      return list.size() - 1;
    }
    return i + type;
  }

  private void init() {
    mScroller = new Scroller(mContext);
    list = new ArrayList<View>();
  }

  private void update() {
    if (currentPage == getChildCount() - 1) {
      addToRight = true;
    }
    if (addToRight) {
      View firstChild = getChildAt(0);
      removeView(firstChild);
      View lastChild = getChildAt(getChildCount() - 1);
      System.out.println(lastChild.getRight());
      addView(firstChild);
      firstChild.layout(lastChild.getRight(), 0, lastChild.getRight()
          + firstChild.getMeasuredWidth(), firstChild.getMeasuredHeight());
    }
  }
}