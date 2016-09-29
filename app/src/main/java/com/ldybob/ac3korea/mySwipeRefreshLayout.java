package com.ldybob.ac3korea;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * 继承自SwipeRefreshLayout,从而实现滑动到底部时上拉加载更多的功能.
 *
 * @author mrsimple
 */
public class mySwipeRefreshLayout extends SwipeRefreshLayout implements AbsListView.OnScrollListener {

    /**
     * 滑动到最下面时的上拉操作
     */

    private int mTouchSlop;
    /**
     * listview实例
     */
    private ListView mListView;

    /**
     * 上拉监听器, 到了最底部的上拉加载操作
     */
    private OnLoadListener mOnLoadListener;

    /**
     * ListView的加载中footer
     */
//    private View mListViewFooter;

    /**
     * 按下时的y坐标
     */
    private int mYDown;
    /**
     * 抬起时的y坐标, 与mYDown一起用于滑动到底部时判断是上拉还是下拉
     */
    private int mLastY;
    /**
     * 是否在加载中 ( 上拉加载更多 )
     */
    private boolean isLoading = false;

    private Context mContext;

    private int mDeviceHeight;

    /**
     * @param context
     */
    public mySwipeRefreshLayout(Context context) {
        this(context, null);
        mContext = context;
    }

    public mySwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        mDeviceHeight = getDeviceHeight();

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

//        mListViewFooter = LayoutInflater.from(context).inflate(R.layout.listview_footer, null,
//                false);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // 初始化ListView对象
        if (mListView == null) {
            getListView();
        }
    }

    /**
     * 获取ListView对象
     */
    private void getListView() {
        int childs = getChildCount();
        if (childs > 0) {
            View childView = getChildAt(0);
            if (childView instanceof ListView) {
                mListView = (ListView) childView;
                // 设置滚动监听器给ListView, 使得滚动的情况下也可以自动加载
                mListView.setOnScrollListener(this);
                //Log.d(VIEW_LOG_TAG, "### 找到listview");
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see android.view.ViewGroup#dispatchTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isLoading) {
            return true;
        }
        final int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 按下
                mYDown = (int) event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                // 移动
                mLastY = (int) event.getRawY();
                break;

            case MotionEvent.ACTION_UP:
                // 抬起
                if (canLoad()) {
                    loadData();
                }
                break;
            default:
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    public void refreshing(final boolean b) {
        this.post(new Runnable() {
            @Override
            public void run() {
                setRefreshing(b);
            }
        });
    }

    public void ProgressViewInit() {
        this.setProgressViewEndTarget(false, 200);
    }

    /**
     * 是否可以加载更多, 条件是到了最底部, listview不在加载中, 且为上拉操作.
     *
     * @return
     */
    private boolean canLoad() {
        return isBottom() && !isLoading && isPullUp();
    }

    /**
     * 判断是否到了最底部
     */
    private boolean isBottom() {
//        Log.d("aaa", mListView.getScrollY() + ", " + mListView.getY() + ", " + mListView.getHeight() + ", " + mListView.getMeasuredHeight());
        if (mListView != null && mListView.getAdapter() != null) {
            if (mListView.getLastVisiblePosition() == (mListView.getAdapter().getCount() - 1)
                    && mListView.getChildCount() != 0) {
                this.setProgressViewEndTarget(false, mDeviceHeight - 400);
                return true;
            } else {
                this.setProgressViewEndTarget(false, 200);
            }
        }
        return false;
    }

    private int getDeviceHeight() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        return height;
    }

    /**
     * 是否是上拉操作
     *
     * @return
     */
    private boolean isPullUp() {
        return (mYDown - mLastY) >= mTouchSlop;
    }

    /**
     * 如果到了最底部,而且是上拉操作.那么执行onLoad方法
     */
    private void loadData() {
        if (mOnLoadListener != null) {
            // 设置状态
            setLoading(true);
            //
            mOnLoadListener.onLoad();
        }
    }

    /**
     * @param loading
     */
    public void setLoading(boolean loading) {
        isLoading = loading;
        if (mListView == null) {
            getListView();
        }
        if (isLoading) {
//            mListView.addFooterView(mListViewFooter);
            mListView.setClickable(false);
            this.setClickable(false);
            refreshing(true);
        } else {
            mListView.setClickable(true);
            this.setClickable(true);
            refreshing(false);
//            mListView.removeFooterView(mListViewFooter);
            mYDown = 0;
            mLastY = 0;
        }
    }

    /**
     * @param loadListener
     */
    public void setOnLoadListener(OnLoadListener loadListener) {
        mOnLoadListener = loadListener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {
        // 滚动时到了最底部也可以加载更多
        if (canLoad()) {
            loadData();
        }
    }

    /**
     * 加载更多的监听器
     *
     * @author mrsimple
     */
    public static interface OnLoadListener {
        public void onLoad();
    }
}
