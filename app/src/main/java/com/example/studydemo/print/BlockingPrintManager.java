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
 * [不必要等到接收到打印任务执行结束printEnd，就可以一直添加后续的打印任务及内容]
 * <p>
 * 取到一段新的打印任务开始标识的时候增加了阻塞机制，实现理论上可以一直不停添加新的打印任务及内容
 * <p>
 * 使用的方法示例：
 * 1、BlockingPrintManager.getInstance().init()
 * ---- 调用init方法启动获取打印任务及内容的Take线程
 * 2、setFragmentListener 或者 setCardListener 添加一个回调监听
 * 3、BlockingPrintManager.getInstance().startPrint(long taskId) 添加一个新的打印任务
 * 4、BlockingPrintManager.getInstance().putText(String content) 添加打印的文本内容，多个片段可以多次调用
 * 5、BlockingPrintManager.getInstance().putEndFlag() 设置终止打印的标识即可
 *
 * @author glp
 * @date 2023/11/9
 */
public class BlockingPrintManager implements IPurePrintTask {
    private static final String TAG = "BlockingPrintManager";
    /**
     * 打印开始的START标识 自己定义自己使用，与外界无关
     */
    private static final String START_FLAG = "##**START**##";
    /**
     * 打印结束的END标识 自己定义自己使用，与外界无关
     */
    private static final String END_FLAG = "##**END**##";
    private final Object mLock = new Object();
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
    private final ExecutorService mPutThread = Executors.newSingleThreadExecutor();
    /**
     * 限制队列的大小
     */
    private final BlockingQueue<String> mBlockingQueue = new LinkedBlockingQueue<>(1000);

    private final AtomicInteger mIndex = new AtomicInteger(0);
    private volatile boolean isTakeAll = false;
    private volatile boolean isPrinting = false;
    private final Handler mMainHandler;
    private OnPrintListener mCardListener;
    private OnPrintListener mFragmentListener;
    private final List<Long> mTaskIds = new ArrayList<>();
    private long mCurrentTaskId;
    private static BlockingPrintManager ourInstance;

    public static BlockingPrintManager getInstance() {
        if (ourInstance == null) {
            ourInstance = new BlockingPrintManager();
        }
        return ourInstance;
    }

    private BlockingPrintManager() {
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

    private void onPrintEndAction() {
        reset();
        int remainCount = mBlockingQueue.size();
        if (remainCount == 0) {
            // 队列中没有待打印任务之后，不能继续否则会阻塞当前UI线程导致ANR
            return;
        }
        synchronized (mLock) {
            mLock.notifyAll();
        }
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
                onPrintEndAction();
            } else if (!isTakeAll && mIndex.get() == mPrintContent.length()) {
                Log.i(TAG, "BlockingPrintManager Error:  Index与Length已经匹配应该结束，但是没有触发已经取完所有要打印数据的标识导致无法触发END");
                Log.i(TAG, "BlockingPrintManager Error MSG:  isTakeAll=" + isTakeAll + "  mIndex=" + mIndex + "  mPrintContentLength=" + mPrintContent.length() + "  mPrintContent=" + mPrintContent);
            }
        } else {
            Log.i(TAG, "BlockingPrintManager Error:  refreshPrint() 更新打印进度的过程中出现异常情况导致无法走进正常的Case导致打印任务异常");
            Log.i(TAG, "BlockingPrintManager Error MSG:  isTakeAll=" + isTakeAll + "  mIndex=" + mIndex + "  mPrintContentLength=" + mPrintContent.length() + "  mPrintContent=" + mPrintContent);
        }
    }

    /**
     * 添加文本
     */
    public void putText(final String text) {
        mPutThread.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (text != null) {
                        mBlockingQueue.put(text);
                    }
                } catch (Exception e) {
                    Log.i(TAG, "BlockingPrintManager mPutThread ---------->>> Exception:" + e);
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void startHandle() {
        mTakeThread.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    synchronized (mLock) {
                        String appendText;
                        try {
                            appendText = mBlockingQueue.take();

                            if (appendText.contains(START_FLAG)) {
                                // 取到了打印的开始标识符
                                if (isPrinting) {
                                    // 正在上一个打印任务执行中，需要阻塞等待
                                    Log.e(TAG, Thread.currentThread() + "---------->>>  isPrinting  wait:" + START_FLAG);
                                    mLock.wait();
                                    realStartPrint(appendText);
                                } else {
                                    realStartPrint(appendText);
                                }
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
                            Log.i(TAG, "BlockingPrintManager mTakeThread ---------->>> Exception:" + e);
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
    }

    private void realStartPrint(String startCommand) {
        // 标记为正在打印任务中，发送更新打印任务的消息
        try {
            // 从打印开始标识符中截取出打印任务ID
            String taskId = startCommand.substring(START_FLAG.length());
            mCurrentTaskId = Long.parseLong(taskId);
            if (mCurrentTaskId > 0) {
                mTaskIds.add(mCurrentTaskId);
            }
        } catch (Exception e) {
            Log.i(TAG, "BlockingPrintManager realStartPrint START_FLAG Exception:" + e);
        }

        isPrinting = true;
        sendPrintCharMsg();
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

    public void onDestroy() {
        reset();
        ourInstance = null;
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
