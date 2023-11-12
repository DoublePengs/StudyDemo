package com.example.studydemo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.LinearLayoutCompat;

/**
 * Description:
 *
 * @author glp
 * @date 2022/6/23
 */
public class MyLinearLayoutLayout extends LinearLayoutCompat {

    public MyLinearLayoutLayout(Context context) {
        super(context);
    }

    public MyLinearLayoutLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLinearLayoutLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e("View绘制流程", "MyLinearLayoutLayout  ------>> onMeasure");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Log.e("View绘制流程", "MyLinearLayoutLayout  ------>> onLayout");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e("View绘制流程", "MyLinearLayoutLayout  ------>> onDraw");
    }
}