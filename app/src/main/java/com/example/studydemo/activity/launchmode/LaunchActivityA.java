package com.example.studydemo.activity.launchmode;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studydemo.R;

/**
 * Description:
 *
 * @author: glp
 * @date: 2020/10/12
 */
public class LaunchActivityA extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launch_mode_activity);

        ((TextView) findViewById(R.id.tv_content)).setText("This is activity A!");

        findViewById(R.id.btn_01).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LaunchActivityA.this, LaunchActivityA.class));
            }
        });

        findViewById(R.id.btn_02).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LaunchActivityA.this, LaunchActivityB.class));
            }
        });

        findViewById(R.id.btn_03).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LaunchActivityA.this, LaunchActivityC.class));
            }
        });

    }
}
