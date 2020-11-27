package com.example.studydemo.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studydemo.R;
import com.example.studydemo.utils.ScreenShot;

/**
 * Description:
 *
 * @author: glp
 * @date: 2020/7/28
 */
public class ScreenShotActivity extends AppCompatActivity {

    private ScreenShot mScreenShot;
    private TextView mTvContent;
    private AlertDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_shot);
        mTvContent = (TextView) findViewById(R.id.tv_content);

        // 禁止录屏和截屏
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        mScreenShot = ScreenShot.getInstance();
        mScreenShot.register(this, new ScreenShot.CallbackListener() {
            @Override
            public void onShot(final String path) {
                // 捕获到系统截屏，path是截屏的绝对路径
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ScreenShotActivity.this, "截屏了。。。", Toast.LENGTH_LONG).show();
                        mTvContent.setText("截屏图片路径：" + path);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ScreenShotActivity.this);
                        builder.setTitle("温馨提示");
                        builder.setMessage("截屏啦！截屏啦！截屏啦！截屏啦！");
                        builder.setCancelable(false);
                        builder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDialog.dismiss();
                            }
                        });
                        builder.setNegativeButton("忽略", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDialog.dismiss();
                            }
                        });

                        mDialog = builder.create();
                        mDialog.show();
                    }
                });
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScreenShot.unregister();
    }
}
