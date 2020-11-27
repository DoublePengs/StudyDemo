package com.example.studydemo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studydemo.R;

/**
 * Description:
 *
 * @author: glp
 * @date: 2020/8/28
 */
public class ScrollViewActivity extends AppCompatActivity {

    private TextView mTvContent;

    private String mTextShort = "我是一个较短的文本内容哈。";
    private String mTextMiddle = "我是一个中等长度的文本内容哈。我是一个中等长度的文本内容哈。我是一个中等长度的文本内容哈。";
    private String mTextLong = "我是一个超级长长长的文本内容哈。我是一个超级长长长的文本内容哈。我是一个超级长长长的文本内容哈。我是一个超级长长长的文本内容哈。我是一个超级长长长的文本内容哈。我是一个超级长长长的文本内容哈。我是一个超级长长长的文本内容哈。我是一个超级长长长的文本内容哈。我是一个超级长长长的文本内容哈。我是一个超级长长长的文本内容哈。我是一个超级长长长的文本内容哈。我是一个超级长长长的文本内容哈。";
    private String[] mTexts = {mTextShort, mTextMiddle, mTextLong};
    private int mIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll_view);
        mTvContent = findViewById(R.id.tv_content);

        findViewById(R.id.btn_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int result = mIndex % 3;
                mTvContent.setText(mTexts[result]);
                mIndex++;
            }
        });
    }
}
