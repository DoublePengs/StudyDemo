package com.example.studydemo.utils;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Description: 阻塞处理接收到的呼叫、取消 cmd 指令
 *
 * @author glp
 * @date 2020/11/30
 */
public class HandleCMDManager {

    private final Object mLock = new Object();
    private boolean mHandleFinished = false;
    private boolean mFirstStart = true;

    private final ExecutorService mSingleThreadExecutor = Executors.newSingleThreadExecutor();
    private final BlockingQueue<Object> mBlockingQueue = new LinkedBlockingQueue<>();
    private final ExecutorService mThreadPoll = new ThreadPoolExecutor(2, 4,
            60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(10));

    @SuppressWarnings("InfiniteLoopStatement")
    public void startHandleCmd() {
        if (mFirstStart) {
            mFirstStart = false;
            mSingleThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Log.e("HandleCMDManager", Thread.currentThread().getName() + "---------->>> 我在执行呀。。。。。。。。前 size=" + mBlockingQueue.size());
                        synchronized (mLock) {
                            try {
                                Log.e("HandleCMDManager", Thread.currentThread().getName() + "---------->>> 我在执行呀。。。。。。。。开始 take()");
                                Object bean = mBlockingQueue.take();
                                Log.e("HandleCMDManager", Thread.currentThread().getName() + "---------->>> 我在执行呀。。。。。。。。 take() 完毕");
                                handleCmd(bean);
                            } catch (InterruptedException e) {
                                Log.e("HandleCMDManager", Thread.currentThread().getName() + "---------->>> 我在执行呀。。。。。。。。takeException: " + e);
                                e.printStackTrace();
                            }
                        }
                        Log.e("HandleCMDManager", Thread.currentThread().getName() + "---------->>> 我在执行呀。。。。。。。。后后后");
                    }
                }
            });
        }

    }

    public void stopHandleCmd() {
        shutdownExecutor();
    }

    private void shutdownExecutor() {
        if (mSingleThreadExecutor != null) {
            Log.e("HandleCMDManager", Thread.currentThread().getName() + "---------->>> 我在执行呀。。。。。。。。shutdown");
            mSingleThreadExecutor.shutdown();
        }
    }

    /**
     * cmd 指令入队
     *
     * @param cmdBean cmd指令
     */
    public void enqueueCmd(final Object cmdBean) {
        mThreadPoll.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mBlockingQueue.put(cmdBean);
                    Log.i("HandleCMDManager", Thread.currentThread().getName() + " enqueueCmd: " + cmdBean + " size=" + mBlockingQueue.size());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 当前的 cmd 任务执行结束
     */
    private void handleFinished() {
        synchronized (mLock) {
            Log.i("HandleCMDManager", Thread.currentThread().getName() + "  handleFinished notifyAll");
            mHandleFinished = true;
            mLock.notifyAll();
        }
    }

    private void handleCmd(Object bean) {
        synchronized (mLock) {
            mHandleFinished = false;
            Log.e("HandleCMDManager", Thread.currentThread().getName() + "---------->>> 我在执行呀。。。。。。。。handleCmd  bean=" + bean);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i("HandleCMDManager", Thread.currentThread().getName() + "  start sleep ");
                        Thread.sleep(3000);
                        Log.i("HandleCMDManager", Thread.currentThread().getName() + "  sleep end ");
                        handleFinished();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


            while (!mHandleFinished) {
                Log.e("HandleCMDManager", Thread.currentThread().getName() + "---------->>> 我在执行呀。。。。。。。。wait handleFinished");
                try {
                    mLock.wait();
                } catch (InterruptedException unused) {
                    Log.e("HandleCMDManager", Thread.currentThread().getName() + "---------->>> 我在执行呀。。。。。。。。exception " + unused.getMessage());
                }
            }
        }
    }

}
