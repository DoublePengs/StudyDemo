package com.example.studydemo.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studydemo.CMDMsgManager;
import com.example.studydemo.R;

/**
 * Description:
 *
 * @author: glp
 * @date: 2020/7/11
 */
public class LeakTestActivity extends AppCompatActivity {

    private int mInterviewId;
    private String mUserName = "";
    private TextView mTvContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("LeakTestActivity", "---------->> onCreate");
        setContentView(R.layout.activity_leak_test);
        mInterviewId = 666;
        mUserName = "是我呀";

        mTvContent = findViewById(R.id.tv_content);
        mTvContent.setText("name=" + mUserName + " id=" + mInterviewId);

        CMDMsgManager.getInstance().setOnEnterRoomListener(new CMDMsgManager.OnRequestListener() {
            @Override
            public void onEnterRoom(int interviewId, String userName) {
                if (mInterviewId == interviewId && mUserName.equals(userName)) {
                    Toast.makeText(LeakTestActivity.this,
                            "EnterRoom name=" + userName + " id=" + interviewId, Toast.LENGTH_LONG).show();
                    mTvContent.setText("name=" + mUserName + " id=" + mInterviewId + " 进入来房间");
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("LeakTestActivity", "---------->> onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("LeakTestActivity", "---------->> onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("LeakTestActivity", "---------->> onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("LeakTestActivity", "---------->> onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("LeakTestActivity", "---------->> onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("LeakTestActivity", "---------->> onDestroy");
    }
}
