package com.example.studydemo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studydemo.R;

/**
 * Description: 验证改变 translationX  Y 是否会超出父布局边界的问题？
 * <p>
 * 并不会超出父布局显示： 直播项目中做讲解中卡片上浮时可能有其他问题
 *
 * @author glp
 * @date 2022/6/9
 */
public class TranslationTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation_test);
        final ImageView img = findViewById(R.id.img);
        img.setSelected(true);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "I'm here !", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img.setTranslationY(img.getTranslationY() - 20);
            }
        });

        findViewById(R.id.btn_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img.setTranslationY(img.getTranslationY() + 20);
            }
        });

        findViewById(R.id.btn_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img.setTranslationX(img.getTranslationX() - 20);
            }
        });

        findViewById(R.id.btn_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img.setTranslationX(img.getTranslationX() + 20);
            }
        });
    }
}
