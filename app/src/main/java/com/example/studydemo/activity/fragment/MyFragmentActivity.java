package com.example.studydemo.activity.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.studydemo.R;

/**
 * Description:
 *
 * @author: glp
 * @date: 2020/11/5
 */
public class MyFragmentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.layout_content, new MyFirstFragment());
        fragmentTransaction.commitNowAllowingStateLoss();

//        fragmentTransaction.commit();
//        fragmentTransaction.commitAllowingStateLoss();
//        fragmentTransaction.commitNow();
//        fragmentTransaction.commitNowAllowingStateLoss();

        findViewById(R.id.btn_replace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.layout_content, new MyFragment());
                fragmentTransaction.commitNowAllowingStateLoss();
            }
        });

    }

    @Override
    protected void onPause() {
        Log.i("myFragment", "------->> MyFragmentActivity onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i("myFragment", "------->> MyFragmentActivity onDestroy");
        super.onDestroy();
    }
}
