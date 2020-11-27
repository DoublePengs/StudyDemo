package com.example.studydemo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

import com.example.studydemo.R;

/**
 * Description: 最大高度填充的ScrollView
 *
 * @author: glp
 * @date: 2020/8/27
 */
public class AutoFitScrollView extends NestedScrollView {

    private static final int MAX_HEIGHT = 300;
    private float mMaxHeight;
    private static final String TAG = "AutoFitScrollView";

    public AutoFitScrollView(@NonNull Context context) {
        this(context, null);
    }

    public AutoFitScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.AutoFitScrollView, defStyleAttr, 0);
        mMaxHeight = a.getDimension(R.styleable.AutoFitScrollView_max_height, MAX_HEIGHT);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        View child = getChildAt(0);
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        child.measure(widthMeasureSpec, heightMeasureSpec);
        int childWidth = child.getMeasuredWidth();
        int childHeight = child.getMeasuredHeight();
        Log.i(TAG, "------------>> childWidth=" + childWidth + "  childHeight=" + childHeight);
        Log.i(TAG, "------------>> childMargin: top=" + lp.topMargin + "  bottom=" + lp.bottomMargin + "  left=" + lp.leftMargin + "  right=" + lp.rightMargin);

        int width = child.getMeasuredWidth();

        // 理论上的高度值
        int tempHeight = childHeight + lp.topMargin + lp.bottomMargin + getPaddingTop() + getPaddingBottom();

        // 计算高度
        int height = (int) Math.min(tempHeight, mMaxHeight);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }
}
