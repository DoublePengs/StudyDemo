package com.example.studydemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.studydemo.R;
import com.example.studydemo.utils.DialogUtil;
import com.example.studydemo.utils.HandleCMDManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Description:
 *
 * @author: glp
 * @date: 2020/11/5
 */
public class SingleTaskActivity extends AppCompatActivity {

    private HandleCMDManager mHandleManager = new HandleCMDManager();
    private String task = "task ";
    private int taskIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("SingleTaskActivity--", "---------->>> onCreate ");
        setContentView(R.layout.activity_single_task);

        findViewById(R.id.tv_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.getInstance().showDialog(SingleTaskActivity.this);
            }
        });

        findViewById(R.id.btn_shutdown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandleManager.stopHandleCmd();
            }
        });

        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandleManager.startHandleCmd();
            }
        });

        findViewById(R.id.btn_enqueue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandleManager.enqueueCmd(task + taskIndex++);
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.i("SingleTaskActivity--", "---------->>> onDestroy ");
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i("SingleTaskActivity--", "---------->>> onNewIntent ");
        mHandleManager.startHandleCmd();
        super.onNewIntent(intent);
    }
}
