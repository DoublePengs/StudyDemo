package com.example.studydemo;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.didichuxing.doraemonkit.DoraemonKit;

/**
 * Description:
 * Author: glp
 * CreateDate: 2020-06-06
 */
public class MyApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

//        DoraemonKit.install(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // 主要是添加下面这句代码
        MultiDex.install(this);

    }

    public static Context getContext() {
        return mContext;
    }
}
