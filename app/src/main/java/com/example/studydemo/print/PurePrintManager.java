package com.example.studydemo.print;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description: 打印管理类
 * <p>
 * 这个类需要打印完成通过 printEnd 通知之后，外部才可以添加下一条打印的任务进来
 * <p>
 * 使用方式：
 * 1.初始化
 * PurePrintManager.getInstance().init();
 * <p>
 * 2.设置回调监听
 * setCardListener,setFragmentListener 用于消息卡片刷新内容，页面主体接收打印状态及进度
 * void printStart(); // 打印任务开始的回调
 * <p>
 * void refreshText(long taskId, String currentText); // 打印任务执行中文本内容的回调
 * <p>
 * void printEnd(long taskId); // 打印任务结束的回调
 * <p>
 * 3.触发开始打印
 * startPrint(long taskId)
 * <p>
 * 4.添加打印内容
 * PurePrintManager.getInstance().putText(String content) 多段文本可以多次调用该方法来追加内容
 * <p>
 * 5.设置结束标志
 * PurePrintManager.getInstance().putEndFlag()
 *
 * @author glp
 * @date 2023/11/9
 */
public class PurePrintManager implements IPurePrintTask {
    private static final String TAG = "PurePrintManager";

    /**
     * 打印开始的START标识 自己定义自己使用，与外界无关
     */
    private static final String START_FLAG = "##**START**##";
    /**
     * 打印结束的END标识 自己定义自己使用，与外界无关
     */
    private static final String END_FLAG = "##**END**##";
    /**
     * 字符延迟时间，单位：毫秒
     */
    private static final int CHAR_DELAY = 50;
    private static final int UPDATE_INDEX = 0x0021;
    private static final int APPEND_TEXT = 0x0022;

    /**
     * 需要打印的文本
     */
    private final StringBuilder mPrintContent = new StringBuilder();
    private final ExecutorService mTakeThread = Executors.newSingleThreadExecutor();
    private final BlockingQueue<String> mBlockingQueue = new LinkedBlockingQueue<>();

    private final AtomicInteger mIndex = new AtomicInteger(0);
    private volatile boolean isTakeAll = false;
    private volatile boolean isPrinting = false;
    private final Handler mMainHandler;
    private OnPrintListener mCardListener;
    private OnPrintListener mFragmentListener;
    private final List<Long> mTaskIds = new ArrayList<>();
    private long mCurrentTaskId;
    private static PurePrintManager ourInstance;

    public static PurePrintManager getInstance() {
        if (ourInstance == null) {
            ourInstance = new PurePrintManager();
        }
        return ourInstance;
    }

    private PurePrintManager() {
        mMainHandler = new MyHandler(this);
    }

    public List<Long> getCompleteTaskId() {
        return mTaskIds;
    }

    /**
     * 初始化
     */
    public void init() {
        reset();
        startHandle();
    }

    /**
     * 开始打印
     */
    public void startPrint(long taskId) {
        reset();
        putText(START_FLAG + taskId);
    }

    /**
     * 添加一个打印结束标识，等待自动打印结束
     */
    public void putEndFlag() {
        putText(END_FLAG);
    }

    /**
     * 直接结束打印，一次输出当前已经添加进去的待打印数据
     */
    public void finishPrint() {
        isTakeAll = true;
        mIndex.set(mPrintContent.length());
        refreshPrint(mIndex.get());
    }

    private void reset() {
        mMainHandler.removeCallbacksAndMessages(null);
        isTakeAll = false;
        isPrinting = false;
        mCurrentTaskId = 0;
        mPrintContent.setLength(0);
        mIndex.set(0);
    }

    @Override
    public void appendText(String str) {
        mPrintContent.append(str);
    }

    @Override
    public void updatePrintIndex() {
        if (mIndex.get() < mPrintContent.length()) {
            refreshPrint(mIndex.incrementAndGet());
        }
        sendPrintCharMsg();
    }

    private void sendPrintCharMsg() {
        if (isPrinting) {
            Message message = Message.obtain();
            message.what = UPDATE_INDEX;
            mMainHandler.sendMessageDelayed(message, CHAR_DELAY);
        }
    }

    private void refreshPrint(int index) {
        if (index > 0 && index <= mPrintContent.length()) {
            String currentText = mPrintContent.substring(0, index);

            // 状态回调 打印中...
            if (mCardListener != null) {
                mCardListener.refreshText(mCurrentTaskId, currentText);
            }
            if (mFragmentListener != null) {
                mFragmentListener.refreshText(mCurrentTaskId, currentText);
            }

            if (isTakeAll && mIndex.get() == mPrintContent.length()) {
                // 状态回调 打印结束
                if (mCardListener != null) {
                    mCardListener.printEnd(mCurrentTaskId);
                }
                if (mFragmentListener != null) {
                    mFragmentListener.printEnd(mCurrentTaskId);
                }
            } else if (!isTakeAll && mIndex.get() == mPrintContent.length()) {
                Log.i(TAG, "PurePrintManager Error:  Index与Length已经匹配应该结束，但是没有触发已经取完所有要打印数据的标识导致无法触发END");
                Log.i(TAG, "PurePrintManager Error MSG:  isTakeAll=" + isTakeAll + "  mIndex=" + mIndex + "  mPrintContentLength=" + mPrintContent.length() + "  mPrintContent=" + mPrintContent);
            }
        } else {
            Log.i(TAG, "PurePrintManager Error:  refreshPrint() 更新打印进度的过程中出现异常情况导致无法走进正常的Case导致打印任务异常");
            Log.i(TAG, "PurePrintManager Error MSG:  isTakeAll=" + isTakeAll + "  mIndex=" + mIndex + "  mPrintContentLength=" + mPrintContent.length() + "  mPrintContent=" + mPrintContent);
        }
    }

    /**
     * 添加文本
     */
    public void putText(String text) {
        try {
            Log.i(TAG, "---------->>> put:" + text);
            if (text != null) {
                if (text.startsWith(START_FLAG)) {
                    // 开始标识
                    if (!isPrinting) {
                        try {
                            // 不在打印中，添加数据
                            mBlockingQueue.put(START_FLAG);
                            // 从打印开始标识符中截取出打印任务ID
                            String taskId = text.substring(START_FLAG.length());
                            mCurrentTaskId = Long.parseLong(taskId);
                            if (mCurrentTaskId > 0) {
                                mTaskIds.add(mCurrentTaskId);
                            }
                        } catch (Exception e) {
                            Log.i(TAG, "PurePrintManager putText START_FLAG Exception:" + e);
                        }
                    } else {
                        // 正在打印中，阻塞
                        Log.i(TAG, "---------->>>  isPrinting  wait:" + START_FLAG);
                    }
                } else {
                    // 其他数据直接插入
                    mBlockingQueue.put(text);
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "PurePrintManager mPutThread ---------->>> Exception:" + e);
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void startHandle() {
        mTakeThread.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String appendText;
                    try {
                        appendText = mBlockingQueue.take();
                        Log.i(TAG, "---------->>> take:" + appendText);
                        if (Objects.equals(appendText, START_FLAG)) {
                            // 打印开始符
                            isPrinting = true;
                            PurePrintManager.this.sendPrintCharMsg();

                            // 状态回调 打印开始
                            mMainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mCardListener != null) {
                                        mCardListener.printStart();
                                    }
                                    if (mFragmentListener != null) {
                                        mFragmentListener.printStart();
                                    }
                                }
                            });

                        } else if (Objects.equals(appendText, END_FLAG)) {
                            // 打印结束符
                            isTakeAll = true;
                        } else {
                            Message msg = Message.obtain();
                            msg.what = APPEND_TEXT;
                            msg.obj = appendText;
                            mMainHandler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        Log.i(TAG, "PurePrintManager mTakeThread ---------->>> Exception:" + e);
                    }
                }
            }
        });
    }

    public void onDestroy() {
        reset();
        ourInstance = null;
    }

    private static class MyHandler extends Handler {

        private final WeakReference<IPurePrintTask> taskWeakReference;

        public MyHandler(IPurePrintTask printCharTask) {
            super(Looper.getMainLooper());
            taskWeakReference = new WeakReference<>(printCharTask);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == UPDATE_INDEX) {
                IPurePrintTask printCharTask = taskWeakReference.get();
                if (printCharTask != null) {
                    printCharTask.updatePrintIndex();
                }
            } else if (msg.what == APPEND_TEXT) {
                IPurePrintTask printCharTask = taskWeakReference.get();
                if (printCharTask != null) {
                    printCharTask.appendText(((String) msg.obj));
                }
            }
        }
    }

    public void setCardListener(OnPrintListener cardListener) {
        this.mCardListener = cardListener;
    }

    public void setFragmentListener(OnPrintListener fragmentListener) {
        this.mFragmentListener = fragmentListener;
    }

    public interface OnPrintListener {
        void printStart();

        void refreshText(long taskId, String currentText);

        void printEnd(long taskId);
    }

}
