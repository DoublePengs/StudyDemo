package com.example.studydemo.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.example.studydemo.R;
import com.example.studydemo.print.BlockingPrintManager;

/**
 * Description:
 *
 * @author glp
 * @date 2024/10/25
 */
public class PrintTestActivity extends AppCompatActivity {

    private TextView mTvContent;
    private String mAllContent = "";
    private String mPrintContent = "";
    private NestedScrollView mScrollView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_test);
        mTvContent = findViewById(R.id.tv_content);
        mScrollView = findViewById(R.id.scroll_view);

        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 阻塞队列加同步锁实现逐字文本打印输出的管理类，可以用于实现AI回复消息拟真逐字打印的效果
                BlockingPrintManager.getInstance().onDestroy();
                BlockingPrintManager.getInstance().init();
                BlockingPrintManager.getInstance()
                        .setFragmentListener(new BlockingPrintManager.OnPrintListener() {
                            @Override
                            public void printStart() {
                                Log.w("BlockingPrintManager", "------>> printStart");
                            }

                            @Override
                            public void refreshText(long taskId, String currentText) {
                                mPrintContent = currentText;
                                mTvContent.setText(String.format("%s%s", mAllContent, mPrintContent));
                                mScrollView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mScrollView.smoothScrollTo(0, mTvContent.getHeight());
                                    }
                                });
                            }

                            @Override
                            public void printEnd(long taskId) {
                                Log.e("BlockingPrintManager", "------>> printEnd  taskId=" + taskId);
                                mAllContent += mPrintContent;
                            }
                        });

                for (int i = 0; i < 23; i++) {
                    BlockingPrintManager.getInstance().startPrint(i);
                    BlockingPrintManager.getInstance().putText(i + " 项目名称：测试项目经历 \n- 描述急急\n");
                    BlockingPrintManager.getInstance().putText(i + " 急急急急急地面\n");
                    BlockingPrintManager.getInstance().putText(i + " 666 \n\n");
                    BlockingPrintManager.getInstance().putEndFlag();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BlockingPrintManager.getInstance().onDestroy();
    }
}
