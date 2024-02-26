package com.mediatek.wwtv.mediaplayer.mmp.multimedia.image;

import android.util.Log;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class PhotoTaskExecutor extends Thread {
    private static final String TAG = "PhotoTaskExecutor";
    public static final int MAX_TASK_SIZE = 2;
    private BlockingDeque<Runnable> mEventQueue = new LinkedBlockingDeque<>(MAX_TASK_SIZE);
    private boolean mShouldExit = false;

    @Override
    public void run() {
        Log.i(TAG, "TaskThread run");
        startExecutor();
    }

    public void queueEvent(Runnable r) {
        Log.i(TAG, "queueEvent queue size:" + mEventQueue.size());
        try {
            mEventQueue.put(r);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void clearEvents() {
        mEventQueue.clear();
    }

    public void requestExit() {
        mShouldExit = true;
    }

    private void startExecutor() {
        Runnable event;
        while (!mShouldExit && !isInterrupted()) {
            try {
                event = mEventQueue.take();
                event.run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public int getTaskSize() {
        return mEventQueue.size();
    }
}
