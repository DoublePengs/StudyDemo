package com.example.studydemo.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studydemo.R;
import com.example.studydemo.utils.DialogUtil;

/**
 * Description:
 *
 * @author: glp
 * @date: 2020/11/5
 */
public class DialogActivity extends AppCompatActivity {

    AlertDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dialog);

        findViewById(R.id.btn_remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.getInstance().showDialog(DialogActivity.this);
//                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Log.i("DialogTest", "------------>> onBackPressed finish activity ");
    }
}
