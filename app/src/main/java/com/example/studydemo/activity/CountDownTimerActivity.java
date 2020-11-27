package com.example.studydemo.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studydemo.R;

/**
 * Description:
 *
 * @author: glp
 * @date: 2020/8/24
 */
public class CountDownTimerActivity extends AppCompatActivity {

    private TextView mTvContent;
    private int second = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        mTvContent = findViewById(R.id.tv_timer);


        CountDownTimer timer = new CountDownTimer(1000 * 60 * 5, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                second++;
                Log.e("CountDownTimerActivity", "--------->> onTick " + second);
                mTvContent.setText(second + "");
            }

            @Override
            public void onFinish() {
                Log.e("CountDownTimerActivity", "--------->> onFinish " + second);
                mTvContent.setText(second + "倒计时结束");
            }
        };
        timer.start();
    }
}
