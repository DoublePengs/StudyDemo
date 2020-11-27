package com.example.studydemo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Description: 单例 Toast 覆盖不显示问题测试
 * Author: glp
 * CreateDate: 2020-06-06
 */
public class ToastTest {

    private Context mContext;
    private static ToastTest mToast;
    private Toast mCommonToast;
    private View mCommonToastLayout;
    private static final String TAG = "ToastTest";
    private String mLastMsg = "";
    private long mLastTime = 0;
    // 主线程的Handler对象
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private ToastTest(Context ctx) {
        mContext = ctx;
    }

    public static ToastTest getInstance() {
        if (mToast == null) {
            mToast = new ToastTest(MyApplication.getContext());
        }
        return mToast;
    }

    public void showCommonToast(final String message) {
        if (mCommonToastLayout == null) {
            mCommonToastLayout = LayoutInflater.from(MyApplication.getContext()).inflate(R.layout.toast_common, null);
        }
        if (mCommonToast == null) {
            mCommonToast = new Toast(mContext);
            mCommonToast.setGravity(Gravity.CENTER, 0, 0);
            mCommonToast.setDuration(Toast.LENGTH_SHORT);

            TextView textView = mCommonToastLayout.findViewById(R.id.text);
            mCommonToast.setView(mCommonToastLayout);
            textView.setText(message);
        }

        long currentTime = System.currentTimeMillis();

        if (currentTime - mLastTime < 2000) {
            if (mLastMsg.equals(message)) {
                Log.e(TAG, "-------------->> 内容跟上次一样，且时间小于2秒 不处理");
            } else {
                Log.e(TAG, "-------------->> 内容不一样，且时间小于2秒 延时处理");
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "-------------->> 延时显示Toast message=" + message);
                        mLastTime = System.currentTimeMillis();
                        mLastMsg = message;
                        ((TextView) mCommonToast.getView().findViewById(R.id.text)).setText(message);
                        mCommonToast.show();
                    }
                }, 2500);
            }
            return;
        }

        Log.e(TAG, "-------------->> 常规显示Toast message=" + message);
        mLastTime = System.currentTimeMillis();
        mLastMsg = message;
        ((TextView) mCommonToast.getView().findViewById(R.id.text)).setText(message);
        mCommonToast.show();
    }

    public void show(String msg) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastTime < 2000 && mLastMsg.equals(msg)) {
            Log.e(TAG, "-------------->> 内容跟上次一样，且时间小于2秒 不处理");
            return;
        }

        mLastMsg = msg;
        mLastTime = currentTime;
        Toast toast = Toast.makeText(MyApplication.getContext(), msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void cancel() {
        if (mCommonToast != null) {
            mCommonToast.cancel();
        }
    }
}
