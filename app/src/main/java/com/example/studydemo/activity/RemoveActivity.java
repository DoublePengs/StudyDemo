package com.example.studydemo.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Dimension;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studydemo.R;

/**
 * Description:
 *
 * @author: glp
 * @date: 2020/11/5
 */
public class RemoveActivity extends AppCompatActivity {

    private boolean isShowContent = false;
    private FrameLayout mLayoutContent;
    private ImageView mImg;

    private ViewGroup.LayoutParams mLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.remove_activity);
        mLayoutContent = findViewById(R.id.layout_content);
        mImg = findViewById(R.id.img);

        final TextView textView = new TextView(this);
        textView.setText("我是一个文本呀");
        textView.setLayoutParams(mLayoutParams);
        textView.setBackgroundColor(Color.parseColor("#00FF00"));
        textView.setTextSize(Dimension.SP, 20);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.WHITE);

        mImg.setVisibility(View.INVISIBLE);
        mLayoutContent.setVisibility(View.INVISIBLE);
        findViewById(R.id.btn_remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowContent = !isShowContent;
                if (isShowContent) {
                    mLayoutContent.removeAllViews();
                    mLayoutContent.addView(textView, mLayoutParams);
//                    mImg.setVisibility(View.INVISIBLE);
                    mLayoutContent.setVisibility(View.VISIBLE);
                } else {
                    mImg.setVisibility(View.VISIBLE);
//                    mLayoutContent.setVisibility(View.INVISIBLE);
                }
            }
        });
        // if (user.isMuteVideo) {
        //                tv_mute_video_tip.setVisibility(VISIBLE);
        //                mImgBg.setVisibility(VISIBLE);
        //                fl_video_container.setVisibility(INVISIBLE);
        //            } else {
        //                ViewUtils.removeSelfFromParent(user.surfaceView);
        //                fl_video_container.removeAllViews();
        //                if (user.surfaceView != null) {
        //                    user.surfaceView.setZOrderMediaOverlay(isMediaOverlay);
        //                    fl_video_container.addView(user.surfaceView, lp);
        //                }
        //                tv_mute_video_tip.setVisibility(INVISIBLE);
        //                fl_video_container.setVisibility(VISIBLE);
        //                mImgBg.setVisibility(INVISIBLE);
        //            }
    }
}
