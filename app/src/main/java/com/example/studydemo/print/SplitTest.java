package com.example.studydemo.print;

import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Description:
 *
 * @author glp
 * @date 2023/11/13
 */
public class SplitTest {

    public static String START_FLAG = "##**START**##";
    public static String SPLIT_TAG = "//";
    private static final ExecutorService takeThread = Executors.newSingleThreadExecutor();
    private static final ExecutorService putThread = Executors.newSingleThreadExecutor();
    private static final BlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>();

    public static void init(final int num) {
        Log.d("SplitTest", Thread.currentThread() + "    init() " + num);
        takeThread.execute(new Runnable() {
            @Override
            public void run() {
                Log.d("SplitTest", Thread.currentThread() + "    init()  " + num);
                while (true) {
                    try {
                        String appendText = blockingQueue.take();
                        if (appendText != null) {
                            boolean startWith = appendText.startsWith(START_FLAG);
                            Log.d("SplitTest", "-------->> take:" + appendText + "  startWith:" + startWith);

                            if (startWith) {
                                try {
                                    // 把打印开始标识符和打印任务ID拆分开进行处理
                                    String[] split = appendText.split(SPLIT_TAG);
                                    Log.d("SplitTest", "-------->> split:" + Arrays.asList(split));
                                    if (split.length == 2) {
                                        String content = split[0];
                                        long taskId = Long.parseLong(split[1]);
                                        Log.d("SplitTest", "-------->> taskId:" + taskId + "   content:" + content);
                                    }
                                } catch (Exception e) {
                                    Log.i("SplitTest", "BlockingPrintManager realStartPrint START_FLAG Exception:" + e);
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    public static void putText(final String text) {
        putThread.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i("SplitTest", Thread.currentThread() + "-------->> put:" + text);
                    blockingQueue.put(text);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
