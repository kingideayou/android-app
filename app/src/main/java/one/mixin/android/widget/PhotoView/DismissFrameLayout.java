package one.mixin.android.widget.PhotoView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class DismissFrameLayout extends FrameLayout {
    private SwipeGestureDetector swipeGestureDetector;
    private OnDismissListener dismissListener;
    private int initHeight;
    private int initWidth;
    private int initLeft = 0;
    private int initTop = 0;
    private float deltaY = 0f;

    public DismissFrameLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public DismissFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DismissFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DismissFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        swipeGestureDetector = new SwipeGestureDetector(getContext(),
                new SwipeGestureDetector.OnSwipeGestureListener() {
                    @Override
                    public void onSwipeTopBottom(float deltaX, float deltaY) {
                        DismissFrameLayout.this.deltaY = deltaY;
                        dragChildView(deltaY);
                    }

                    @Override
                    public void onSwipeLeftRight(float deltaX, float deltaY) {
                    }

                    @Override
                    public void onFinish(int direction, float distanceX, float distanceY) {
                        if (dismissListener != null
                                && direction == SwipeGestureDetector.DIRECTION_TOP_BOTTOM) {
                            if (Math.abs(distanceY) > initHeight / 10) {
                                dismissListener.onDismiss();
                            } else {
                                dismissListener.onCancel();
                                reset();
                            }
                        }
                    }
                });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return swipeGestureDetector.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int count = getChildCount();
        if (count > 0) {
            View view = getChildAt(0);
            if (view instanceof PhotoView) {
                if (((PhotoView) view).getScale() != 1) {
                    if (view.onTouchEvent(event)) {
                        return true;
                    }
                }
            }
        }
        if (deltaY != 0) {
            swipeGestureDetector.onTouchEvent(event);
            return true;
        }
        return swipeGestureDetector.onTouchEvent(event);
    }

    private void dragChildView(float deltaY) {
        int count = getChildCount();
        if (count > 0) {
            View view = getChildAt(0);
            moveChildView(view, deltaY);
        }
    }

    private void moveChildView(View view, float deltaY) {
        MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
        if (params == null) {
            params = new MarginLayoutParams(view.getWidth(), view.getHeight());
        }
        if (params.width <= 0 && params.height <= 0) {
            params.width = view.getWidth();
            params.height = view.getHeight();
        }
        if (initHeight <= 0) {
            initHeight = view.getHeight();
            initWidth = view.getWidth();
            initLeft = params.leftMargin;
            initTop = params.topMargin;
        }

        percent += (deltaY / getHeight());
        params.topMargin += (calYOffset(deltaY));
        view.setLayoutParams(params);
        if (dismissListener != null) {
            dismissListener.onDismissProgress(Math.abs(percent));
        }
    }

    private float percent = 0f;

    private int calYOffset(float deltaY) {
        return (int) deltaY;
    }

    private void reset() {
        int count = getChildCount();
        if (count > 0) {
            View view = getChildAt(0);
            MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
            params.width = initWidth;
            params.height = initHeight;
            params.leftMargin = initLeft;
            params.topMargin = initTop;
            percent = 0;
            view.setLayoutParams(params);
        }
    }

    public void setDismissListener(OnDismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface OnDismissListener {
        void onDismissProgress(float scale);

        void onDismiss();

        void onCancel();
    }
}
